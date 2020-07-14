package com.andrea.groupup

import android.animation.ValueAnimator
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.andrea.groupup.Http.UserHttp
import com.andrea.groupup.Http.VolleyCallback
import com.andrea.groupup.Http.VolleyCallbackArray
import com.andrea.groupup.Models.User
import com.android.volley.VolleyError
import com.facebook.*
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


class MainActivity : AppCompatActivity() {
    lateinit var callbackManager: CallbackManager
    private val AUTH_TYPE = "rerequest"

    private var firebaseToken: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FacebookSdk.sdkInitialize(this.applicationContext)
        callbackManager = CallbackManager.Factory.create()

        if (AccessToken.getCurrentAccessToken() != null) {
            val request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken()
            ) { `object`, response -> isAlreadyActive(`object`) }
            val parameters = Bundle()
            parameters.putString("fields", "id,email,first_name,last_name")
            request.parameters = parameters
            request.executeAsync()
        }

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
//                if (!task.isSuccessful) {
//                    //To do//
//                    return@OnCompleteListener
//                }

                // Get the Instance ID token//
                firebaseToken = task.result!!.token
                Log.d("FIREBASE TOKEN", firebaseToken)
            })
        
        val loginButton = findViewById<LoginButton>(R.id.login_button)
        loginButton.setReadPermissions(listOf("public_profile", "email"))
        loginButton.authType = AUTH_TYPE

        // Callback registration
        loginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult?> {
            override fun onSuccess(loginResult: LoginResult?) {
                var mDialog = ProgressDialog(this@MainActivity)
                mDialog.setMessage("Retrieving data...")
                mDialog.show()

                val request = GraphRequest.newMeRequest(loginResult!!.accessToken) { obj, _ ->
                    mDialog.dismiss()
                    isAlreadyActive(obj)
                }

                val parameters = Bundle()
                parameters.putString("fields", "id,email,first_name,last_name")
                request.parameters = parameters
                request.executeAsync()
            }

            override fun onCancel() {
                Toast.makeText(this@MainActivity, "Login Cancelled", Toast.LENGTH_LONG).show()
            }

            override fun onError(exception: FacebookException) {
                Toast.makeText(this@MainActivity, exception.message, Toast.LENGTH_LONG).show()
            }
        })

        findViewById<Button>(R.id.loginShow).setOnClickListener {
            showLogin()
        }

        findViewById<Button>(R.id.login).setOnClickListener {
            val username = findViewById<EditText>(R.id.usernameInput).text.toString()
            val password = findViewById<EditText>(R.id.passwordInput).text.toString()
            loginAction(username, password)
        }

        findViewById<Button>(R.id.createShow).setOnClickListener {
            createShow()
        }

        findViewById<Button>(R.id.create).setOnClickListener {
            createAction()
        }

        findViewById<ImageView>(R.id.returnButton).setOnClickListener {
            returnMain()
        }

        //printHashKey(this.baseContext)
    }

    private fun isAlreadyActive(obj: JSONObject) {

        UserHttp(this).getByName(obj.getString("email"), object: VolleyCallbackArray {
            override fun onResponse(array: JSONArray) {
                Log.d("USER", "getUser - onResponse")
                Log.d("OBJECT", obj.toString())
                if(array.toString() == "[]"){
                    createFacebook(obj)
                }else {
                    loginFacebook(obj)
                }

            }

            override fun onError(error: VolleyError) {
                Log.e("USER", "login - onError")
                Log.e("USER", error.javaClass.toString())
            }
        })
    }

    private fun createFacebook(obj: JSONObject){
        val password = randomPassword()
        UserHttp(this).createUserFacebook(obj.getString("email"), obj.getString("first_name" ) + obj.getString("last_name"), password, password, obj.getString("id"), object: VolleyCallback {
            override fun onResponse(jsonObject: JSONObject) {
                Log.d("USER", "getUser - onResponse")
                loginAction(obj.getString("first_name" ) + obj.getString("last_name"), password)
            }

            override fun onError(error: VolleyError) {
                Log.e("USER", "login - onError")
                Log.e("USER", error.javaClass.toString())
            }
        })
    }

    private fun loginFacebook(obj: JSONObject){
        val intent = Intent(this, GroupActivity::class.java)
        UserHttp(this).facebookLogin(obj.getString("id"), obj.getString("email"), firebaseToken, object: VolleyCallback {
            override fun onResponse(jsonObject: JSONObject) {
                Log.d("USER", "loginfacebook done - onResponse")
                val gson = Gson()
                val user: User = gson.fromJson(jsonObject.toString(), User::class.java)
                intent.putExtra("User", user)
                intent.putExtra("FacebookLogin", true)
                var token = jsonObject.get("token").toString()
                token = token.substring(token.indexOf(" ") + 1, token.length)
                intent.putExtra("Token", token)
                startActivity(intent)
            }

            override fun onError(error: VolleyError) {
                Log.e("USER", "login - onError")
                Log.e("USER", error.javaClass.toString())
            }
        })
    }

    private fun randomPassword(): String {
        val generator = Random()
        val randomStringBuilder = StringBuilder()
        val randomLength = 20
        var tempChar: Char
        for (i in 0 until randomLength) {
            tempChar = (generator.nextInt(96) + 32).toChar()
            randomStringBuilder.append(tempChar)
        }

        return randomStringBuilder.toString().replace("\"", "")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun createAction(){
        val emailInput = findViewById<EditText>(R.id.emailInput).text.toString()
        val passwordInput = findViewById<EditText>(R.id.passwordInput).text.toString()
        val passwordConfirmInput = findViewById<EditText>(R.id.passwordConfirmInput).text.toString()
        val usernameInput = findViewById<EditText>(R.id.usernameInput).text.toString()

        if(emailInput.isNotEmpty() && emailInput.isNotBlank() && passwordInput.isNotEmpty() && passwordInput.isNotBlank() && passwordConfirmInput.isNotEmpty() && passwordConfirmInput.isNotBlank() && usernameInput.isNotEmpty() && usernameInput.isNotBlank()){
            if(passwordInput == passwordConfirmInput){
                UserHttp(this).createUser(emailInput, usernameInput, passwordInput, passwordConfirmInput, object: VolleyCallback {
                    override fun onResponse(jsonObject: JSONObject) {
                        Log.d("USER", "getUser - onResponse")
                        loginAction(usernameInput, passwordInput)
                    }

                    override fun onError(error: VolleyError) {
                        Log.e("USER", "register - onError")
                        Log.e("USER", error.toString())
                        Log.e("USER", error.message.toString())
                        findViewById<TextView>(R.id.error).text = getString(R.string.errorUsername)
                        findViewById<TextView>(R.id.error).visibility = View.VISIBLE
                    }
                })
            }else{
                findViewById<TextView>(R.id.error).text = getString(R.string.errorSamePassword)
                findViewById<TextView>(R.id.error).visibility = View.VISIBLE
            }
        }else{
            findViewById<TextView>(R.id.error).text = getString(R.string.errorNotComplete)
            findViewById<TextView>(R.id.error).visibility = View.VISIBLE
        }
    }

    private fun createShow(){
        findViewById<EditText>(R.id.emailInput).visibility = View.VISIBLE
        findViewById<EditText>(R.id.passwordInput).visibility = View.VISIBLE
        findViewById<EditText>(R.id.passwordConfirmInput).visibility = View.VISIBLE
        findViewById<EditText>(R.id.usernameInput).visibility = View.VISIBLE
        findViewById<ImageView>(R.id.returnButton).visibility = View.VISIBLE
        findViewById<Button>(R.id.create).visibility = View.VISIBLE
        findViewById<TextView>(R.id.error).visibility = View.GONE

        val fadeOut = ValueAnimator.ofFloat(1f, 0f)
        fadeOut.duration = 500
        fadeOut.addUpdateListener { animation ->
            val alpha = animation.animatedValue as Float
            findViewById<Button>(R.id.loginShow).alpha = alpha
            findViewById<Button>(R.id.createShow).alpha = alpha
            findViewById<LoginButton>(R.id.login_button).alpha = alpha
        }

        val fadeIn = ValueAnimator.ofFloat(0f, 1f)
        fadeIn.duration = 500
        fadeIn.addUpdateListener { animation ->
            val alpha = animation.animatedValue as Float
            findViewById<EditText>(R.id.emailInput).alpha = alpha
            findViewById<EditText>(R.id.passwordInput).alpha = alpha
            findViewById<EditText>(R.id.passwordConfirmInput).alpha = alpha
            findViewById<EditText>(R.id.usernameInput).alpha = alpha
            findViewById<ImageView>(R.id.returnButton).alpha = alpha
            findViewById<Button>(R.id.create).alpha = alpha
        }

        fadeOut.start()
        findViewById<Button>(R.id.loginShow).visibility = View.GONE
        findViewById<Button>(R.id.createShow).visibility = View.GONE
        findViewById<LoginButton>(R.id.login_button).visibility = View.GONE


        fadeIn.start()
    }

    private fun returnMain(){
        findViewById<Button>(R.id.loginShow).visibility = View.VISIBLE
        findViewById<Button>(R.id.createShow).visibility = View.VISIBLE
        findViewById<LoginButton>(R.id.login_button).visibility = View.VISIBLE

        val fadeOut = ValueAnimator.ofFloat(1f, 0f)
        fadeOut.duration = 500
        fadeOut.addUpdateListener { animation ->
            val alpha = animation.animatedValue as Float
            findViewById<EditText>(R.id.emailInput).alpha = alpha
            findViewById<EditText>(R.id.passwordInput).alpha = alpha
            findViewById<EditText>(R.id.passwordConfirmInput).alpha = alpha
            findViewById<EditText>(R.id.usernameInput).alpha = alpha
            findViewById<Button>(R.id.login).alpha = alpha
            findViewById<Button>(R.id.create).alpha = alpha
            findViewById<ImageView>(R.id.returnButton).alpha = alpha
        }

        val fadeIn = ValueAnimator.ofFloat(0f, 1f)
        fadeIn.duration = 500
        fadeIn.addUpdateListener { animation ->
            val alpha = animation.animatedValue as Float
            findViewById<Button>(R.id.loginShow).alpha = alpha
            findViewById<Button>(R.id.createShow).alpha = alpha
            findViewById<LoginButton>(R.id.login_button).alpha = alpha
        }

        fadeOut.start()
        findViewById<EditText>(R.id.emailInput).visibility = View.GONE
        findViewById<EditText>(R.id.passwordInput).visibility = View.GONE
        findViewById<EditText>(R.id.passwordConfirmInput).visibility = View.GONE
        findViewById<EditText>(R.id.usernameInput).visibility = View.GONE
        findViewById<Button>(R.id.login).visibility = View.GONE
        findViewById<Button>(R.id.create).visibility = View.GONE
        findViewById<ImageView>(R.id.returnButton).visibility = View.GONE
        findViewById<TextView>(R.id.error).visibility = View.GONE


        fadeIn.start()
    }

    private fun loginAction(username: String, password: String){
        val intent = Intent(this, GroupActivity::class.java)
        UserHttp(this).login(username, password, firebaseToken!!, object: VolleyCallback {
            override fun onResponse(jsonObject: JSONObject) {
                Log.d("USER", "getUser - onResponse")

                val gson = Gson()
                val user: User = gson.fromJson(jsonObject.toString(), User::class.java)
                intent.putExtra("User", user)
                intent.putExtra("FacebookLogin", false)
                var token = jsonObject.get("token").toString()
                token = token.substring(token.indexOf(" ") + 1, token.length)
                intent.putExtra("Token", token)
                startActivity(intent)
            }

            override fun onError(error: VolleyError) {
                Log.e("USER", "login - onError")
                Log.e("USER", error.toString())

                findViewById<TextView>(R.id.error).text = getString(R.string.error_login)
                findViewById<TextView>(R.id.error).visibility = View.VISIBLE
            }
        })
    }

    private fun showLogin(){
        findViewById<EditText>(R.id.usernameInput).visibility = View.VISIBLE
        findViewById<EditText>(R.id.passwordInput).visibility = View.VISIBLE
        findViewById<ImageView>(R.id.returnButton).visibility = View.VISIBLE
        findViewById<Button>(R.id.login).visibility = View.VISIBLE
        findViewById<TextView>(R.id.error).visibility = View.GONE

        val fadeOut = ValueAnimator.ofFloat(1f, 0f)
        fadeOut.duration = 500
        fadeOut.addUpdateListener { animation ->
            val alpha = animation.animatedValue as Float
            findViewById<Button>(R.id.loginShow).alpha = alpha
            findViewById<Button>(R.id.createShow).alpha = alpha
            findViewById<LoginButton>(R.id.login_button).alpha = alpha
        }

        val fadeIn = ValueAnimator.ofFloat(0f, 1f)
        fadeIn.duration = 500
        fadeIn.addUpdateListener { animation ->
            val alpha = animation.animatedValue as Float
            findViewById<EditText>(R.id.usernameInput).alpha = alpha
            findViewById<EditText>(R.id.passwordInput).alpha = alpha
            findViewById<Button>(R.id.login).alpha = alpha
            findViewById<ImageView>(R.id.returnButton).alpha = alpha
        }

        fadeOut.start()
        findViewById<Button>(R.id.loginShow).visibility = View.GONE
        findViewById<Button>(R.id.createShow).visibility = View.GONE
        findViewById<LoginButton>(R.id.login_button).visibility = View.GONE
        findViewById<EditText>(R.id.passwordConfirmInput).visibility = View.GONE

        fadeIn.start()
    }

    /*private fun printHashKey(pContext: Context) {
        try {
            val info: PackageInfo = pContext.getPackageManager()
                .getPackageInfo(pContext.getPackageName(), PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(Base64.encode(md.digest(), 0))
                Log.i("HASH", "printHashKey() Hash Key: $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e("HASH", "printHashKey()", e)
        } catch (e: Exception) {
            Log.e("HASH", "printHashKey()", e)
        }
    }*/
}
