package tech.kaustubhdeshpande.sos.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import tech.kaustubhdeshpande.sos.R
import tech.kaustubhdeshpande.sos.SOSActivity

class SosService : Service() {
    private var timer: CountDownTimer? = null
    private val channelId = "sos_channel"
    private val notificationId = 1

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        startForeground(notificationId, buildNotification("Sending location every 10 seconds"))
        startCountdown()
        return START_NOT_STICKY
    }

    private fun startCountdown() {
        timer = object : CountDownTimer(30 * 60 * 1000, 10 * 1000) {
            @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
            override fun onTick(millisUntilFinished: Long) {
                // for getting location
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val lat = location.latitude
                        val lon = location.longitude
                        val message = "SOS! My location: https://maps.google.com/?q=$lat,$lon"

                        // 🔹 Read contacts from SharedPreferences
                        val prefs = getSharedPreferences("sos_prefs", MODE_PRIVATE)
                        val contacts = prefs.getStringSet("contacts", emptySet()) ?: emptySet()

                        // 🔹 Send SMS to each contact
                        val smsManager = android.telephony.SmsManager.getDefault()
                        for (number in contacts) {
                            smsManager.sendTextMessage(number, null, message, null, null)
                        }
                    }
                }

            }

            override fun onFinish() {
                stopSelf()
            }
        }.start()
    }

    private fun buildNotification(contentText: String): Notification {
        val intent = Intent(this, SOSActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("SOS Active")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            "SOS Alerts",
            NotificationManager.IMPORTANCE_HIGH
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
