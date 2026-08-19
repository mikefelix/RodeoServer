package com.example.rodeoserver.di

import android.content.Context
import android.net.ConnectivityManager
import androidx.room.Room
import androidx.room.migration.Migration
import com.example.rodeoserver.AppDatabase
import com.example.rodeoserver.BuildConfig
import com.example.rodeoserver.ConnectivityImpl
import com.example.rodeoserver.devices.DeviceDao
import com.example.rodeoserver.devices.Subsystem
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MainModule {

  @Provides
  @Singleton
  fun provideOkHttpClient(
    @ApplicationContext context: Context,
  ): OkHttpClient {
    return OkHttpClient.Builder().build()
  }

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
  ) = ConnectivityImpl(context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
}

typealias CredentialsMap = Map<Subsystem, Map<String, String>>