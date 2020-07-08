package com.andrea.groupup

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


class StopSharingPositionReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("SharingPositionReceiver", "onReceive")

        val preferences = context!!.getSharedPreferences("groupup", Context.MODE_PRIVATE)
        val edit = preferences.edit()
        edit.putBoolean("isSharing", false)
        edit.apply()

        context.stopService(Intent(context, SharePositionService::class.java))
//        val notificationManager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.cancel(123456789)
    }

}
