package com.mozzarelly.rodeoserver.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.mozzarelly.rodeoserver.R
import com.mozzarelly.rodeoserver.devices.DeviceRepository
import com.mozzarelly.rodeoserver.server.Server
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject

private const val ServiceId = 7233
private const val ChannelId = "OngoingService"

@AndroidEntryPoint
class OngoingService : Service() {

  @Inject
  private lateinit var deviceRepository: DeviceRepository

  companion object {
    var runningService: OngoingService? = null
    var server: Server? = null

    fun startNotification(context: Context) {
      if (runningService == null) {
        context.startForegroundService(Intent(context, OngoingService::class.java))
      }
    }
  }

  private val notificationManager: NotificationManager by lazy {
    (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
  }

  private fun NotificationManager.createChannels() {
    createNotificationChannel(NotificationChannel(ChannelId, "Ongoing Weather", NotificationManager.IMPORTANCE_DEFAULT))
  }

  private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

  private val remoteViews: RemoteViews
    get() = RemoteViews(packageName, R.layout.ongoing_notification)

  private val notificationBuilder by lazy {
    NotificationCompat.Builder(this, ChannelId)
      .setStyle(NotificationCompat.DecoratedCustomViewStyle())
      .setCustomContentView(remoteViews)
      .setContentIntent(PendingIntent.getActivity(this, 333,
        Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE))
      .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    if (runningService != null) {
      return START_STICKY
    }

    notificationManager.createChannels()
    runningService = this

    server = Server(deviceRepository).also { it.start() }

    startForeground(ServiceId, notificationBuilder.build())
    return START_STICKY
  }

  fun stop() {
    server?.stop()
    stopSelf()
    runningService = null
  }

  override fun onDestroy() {
    super.onDestroy()
  }

  override fun onBind(p0: Intent?): IBinder? {
    return null
  }
}
