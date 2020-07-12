
package com.andrea.groupup

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.*
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.andrea.groupup.Http.GroupHttp
import com.andrea.groupup.Http.Http
import com.andrea.groupup.Http.VolleyCallback
import com.andrea.groupup.Models.User
import com.android.volley.VolleyError
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject


private const val TAG = "SHARE_POSITION_SERVICE"

class SharePositionService : Service() {

    private lateinit var groupHttp: GroupHttp
    private val sharePositionRunnable =  object: Runnable {
        override fun run() {
            sharePositionHandlerFunction()
            sharePositionHandler.postDelayed(this, 5000)
        }
    }
    private var hasStarted = false
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var builder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationChannel: NotificationChannel
    private val channelId = "com.andrea.groupup"

    private var groupId: Int = 0
    private var groupName: String = ""
    private lateinit var user: User
    private lateinit var token: String

    private lateinit var preferences: SharedPreferences
    private lateinit var edit: SharedPreferences.Editor

    private lateinit var sharePositionHandler: Handler

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "ServiceonCreate")
    }
    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "ServiceOnBind")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
//        super.onStartCommand(intent, flags, startId)
        groupHttp = GroupHttp(this)

        preferences = this.getSharedPreferences("groupup", Context.MODE_PRIVATE)
        edit = preferences.edit()

        groupId = intent!!.getIntExtra("groupId", 0)
        groupName = intent.getStringExtra("groupName")
        user = intent.getSerializableExtra("user") as User
        token = intent.getStringExtra("token")

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        sharePositionHandler = Handler(Looper.getMainLooper())

        init()

        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "ServiceOnDestroy")
        super.onDestroy()
        shareUserPositionStop()
    }
    private fun init() {
        sharePositionNotification()
        shareUserPosition()
    }

    private fun sharePositionHandlerFunction() {
//        Log.d(TAG, "sharePositionHandlerFunction")
        var time = preferences.getInt("timeLeft", 3509000)
        var min = time / 1000 / 60
        var sec = time / 1000 % 60
        time -= 1000
        edit.putInt("timeLeft", time)
        edit.apply()

        if (!preferences.getBoolean("isSharing", false) || (min == 0 && sec == 0)) {
//            Log.d(TAG, "sharePositionHandlerFunction - isSharing = false")
            shareUserPositionStop()
        } else {
//            Log.d(TAG, "sharePositionHandlerFunction - isSharing = true")
            updateSharePositionNotification("$min:$sec")
            shareUserPosition()
        }
    }

    private fun sharePositionNotification() {
        val stopIntent = Intent(this, StopSharingPositionReceiver::class.java)
        val pIntentStop = PendingIntent.getBroadcast(this, 3, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val contentView = RemoteViews(this.packageName, R.layout.notification_layout)
        contentView.setTextViewText(R.id.notif_title, "Location update")
        contentView.setTextViewText(R.id.notif_content, "Now sharing position with " + groupName)
        contentView.setTextViewText(R.id.notif_time, "01:00:00")
        contentView.setOnClickPendingIntent(R.id.stopShareLocation, pIntentStop)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(channelId, "test", NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.DKGRAY
            notificationManager.createNotificationChannel(notificationChannel)

            builder = NotificationCompat.Builder(this, channelId)
                .setCustomContentView(contentView)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
//                .addAction(R.mipmap.ic_launcher, "Stop", pIntentStop)
        } else {
            builder = NotificationCompat.Builder(this)
                .setCustomContentView(contentView)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .addAction(R.mipmap.ic_launcher, "Stop", pIntentStop)
        }

        startForeground(123456789, builder.build())
//        notificationManager.notify(123456789, builder.build())
    }

    private fun shareUserPosition() {
//        Log.d(TAG, "shareUserPosition")
        mFusedLocationProviderClient.lastLocation.addOnSuccessListener {
            if (it !== null) {
                groupHttp
                    .shareUserPositionStart(groupId, it.latitude, it.longitude, token, object:
                        VolleyCallback {
                        override fun onResponse(jsonObject: JSONObject) {
                            Log.d(TAG, "shareUserPosition - onResponse")

                            if(!hasStarted)
                                showUserPositionShare()
                        }

                        override fun onError(error: VolleyError) {
                            Log.d(TAG, "shareUserPosition - onError")
                            Log.e(TAG, error.toString())
                        }
                    })
            }
        }
    }

    private fun showUserPositionShare() {
        Log.d(TAG, "showUserPositionShare")
//        DrawableCompat.setTint(DrawableCompat.wrap(shareLocationButton.background), context?.resources!!.getColor(R.color.sharePositionButtonStart))
//        getDeviceLocation()
        hasStarted = true
        edit.putInt("notificationId", 123456789)
        edit.putInt("timeLeft", 3600000)
        edit.putBoolean("isSharing", true)
        edit.apply()

        sharePositionHandler.post(sharePositionRunnable)
    }

    private fun updateSharePositionNotification(time: String) {
        builder.contentView.setTextViewText(R.id.notif_time, "$time")
//        notificationManager.notify(123456789, builder.build())
        startForeground(123456789, builder.build())
    }

    private fun hideUserPositionShare() {
//        Log.d(TAG, "hideUserPositionShare")
        edit.putBoolean("isSharing", false)
        edit.apply()
        sharePositionHandler.removeCallbacks(sharePositionRunnable)
//        DrawableCompat.setTint(DrawableCompat.wrap(shareLocationButton.background), Color.parseColor("#FFFFFF"))
        notificationManager.cancel(123456789)
        stopSelf()
    }

    private fun shareUserPositionStop() {
//        Log.d(TAG, "shareUserPositionStop")
        groupHttp
            .shareUserPositionStop(groupId, token, object: VolleyCallback {
                override fun onResponse(jsonObject: JSONObject) {
//                    Log.d(TAG, "shareUserPositionStop - onResponse")
                    hideUserPositionShare()
                }

                override fun onError(error: VolleyError) {
//                    Log.d(TAG, "shareUserPositionStop - onError")
                    Log.e(TAG, error.toString())
                }
            })
    }
}