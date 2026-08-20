package com.mozzarelly.rodeoserver.di

import android.content.Context
import android.net.ConnectivityManager
import androidx.room.Room
import androidx.room.migration.Migration
import com.mozzarelly.rodeoserver.server.BuildConfig
import com.mozzarelly.rodeoserver.app.AndroidConnectivity
import com.mozzarelly.rodeoserver.AppDatabase
import com.mozzarelly.rodeoserver.devices.DeviceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Named
import javax.inject.Singleton
import kotlin.jvm.java
import com.mozzarelly.rodeoserver.devices.Subsystem
import com.mozzarelly.rodeoserver.server.CredentialsMap

@Module
@InstallIn(SingletonComponent::class)
class MainModule {

  @Provides
  @Singleton
  fun provideOkHttpClient(
    @ApplicationContext context: Context,
  ): OkHttpClient = OkHttpClient.Builder().build()

  @Provides
  @Singleton
  @Named("credentials")
  fun provideCredentials(): CredentialsMap = mapOf(
    Subsystem.Etek to mapOf(
      "etekLogin" to BuildConfig.ETEK_LOGIN,
      "etekPassword" to BuildConfig.ETEK_PASSWORD,
    )
  )

  @Provides
  @Singleton
  fun provideDatabase(
    @ApplicationContext context: Context,
  ): AppDatabase = Room
    .databaseBuilder(context, AppDatabase::class.java, "db")
    .addMigrations(
      Migration(1, 2) {
        it.execSQL("INSERT INTO device (name, subsystem, isOn) VALUES ('aquarium', 'Etek', 0)")
        it.execSQL("INSERT INTO device (name, subsystem, isOn) VALUES ('bedheat', 'Etek', 0)")
        it.execSQL("INSERT INTO device (name, subsystem, isOn) VALUES ('fishfilter', 'Etek', 0)")
      }
    )
    .build()

  @Provides
  @Singleton
  fun provideDevicesDao(db: AppDatabase): DeviceDao = db.deviceDao()

  @Provides
  @Singleton
  fun provideConnectivity(
    @ApplicationContext context: Context,
  ) = AndroidConnectivity(context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
}
