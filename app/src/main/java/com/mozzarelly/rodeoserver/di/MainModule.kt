package com.mozzarelly.rodeoserver.di

import android.content.Context
import android.net.ConnectivityManager
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mozzarelly.rodeoserver.AppDatabase
import com.mozzarelly.rodeoserver.server.BuildConfig
import com.mozzarelly.rodeoserver.app.AndroidConnectivity
import com.mozzarelly.rodeoserver.app.Connectivity
import com.mozzarelly.rodeoserver.AppDatabaseRoom
import com.mozzarelly.rodeoserver.RoomAppDatabase
import com.mozzarelly.rodeoserver.app.RodeoBleDeviceManager
import com.mozzarelly.rodeoserver.devices.Device
import com.mozzarelly.rodeoserver.devices.DeviceDao
import com.mozzarelly.rodeoserver.devices.DeviceRepository
import com.mozzarelly.rodeoserver.devices.DeviceRepositoryImpl
import com.mozzarelly.rodeoserver.devices.EtekApi
import com.mozzarelly.rodeoserver.devices.Subsystem
import com.mozzarelly.rodeoserver.work.WorkDao
import com.mozzarelly.rodeoserver.devices.fermenter.FermenterDevice
import com.mozzarelly.rodeoserver.server.CredentialsMap
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.serialization.json.Json

private val json = Json {
  ignoreUnknownKeys = true
  isLenient = true
}

@Module
@InstallIn(SingletonComponent::class)
abstract class MainModule {

  @Binds
  @Singleton
  abstract fun bindDeviceRepository(impl: DeviceRepositoryImpl): DeviceRepository

  companion object {
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
    ): AppDatabase = RoomAppDatabase(
      Room
        .databaseBuilder(context, AppDatabaseRoom::class.java, "db")
        .addCallback(object : RoomDatabase.Callback() {
          override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            db.execSQL("INSERT INTO Device (name, subsystem, isOn, locked, synced, address, version) VALUES ('aquarium', 'Etek', 0, 0, 0, null, 0)")
            db.execSQL("INSERT INTO Device (name, subsystem, isOn, locked, synced, address, version) VALUES ('bedheat', 'Etek', 0, 0, 0, null, 0)")
            db.execSQL("INSERT INTO Device (name, subsystem, isOn, locked, synced, address, version) VALUES ('fishfilter', 'Etek', 0, 0, 0, null, 0)")
            db.execSQL("INSERT INTO Device (name, subsystem, isOn, locked, synced, address, version) VALUES ('office', 'Tasmota', 0, 0, 0, '192.168.1.209', 0)")
          }
        })
        .build()
    )

    @Provides
    @Singleton
    fun provideDeviceDao(db: AppDatabase): DeviceDao = db.deviceDao()

    @Provides
    @Singleton
    fun provideWorkDao(db: AppDatabase): WorkDao = db.workDao()

    @Provides
    @Singleton
    fun provideEtekApi(
      okHttpClient: OkHttpClient,
    ): EtekApi {
      val retro = Retrofit.Builder()
        .baseUrl("https://smartapi.vesync.com/")
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .client(okHttpClient)
        .build()

      return retro.create(EtekApi::class.java)
    }

   /* @Provides
    @Singleton
    fun provideTasmotaApi(
      okHttpClient: OkHttpClient,
    ): TasmotaApi {
      val retro = Retrofit.Builder()
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .client(okHttpClient)
        .build()

      return retro.create(TasmotaApi::class.java)
    }*/

    @Provides
    @Singleton
    fun provideConnectivity(
      @ApplicationContext context: Context,
    ): Connectivity = AndroidConnectivity(context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)

    @Provides
    @Singleton
    fun provideFermenter(
      @ApplicationContext context: Context,
    ): FermenterDevice {
      val manager = object : RodeoBleDeviceManager(context) {
        override fun onDataReceived(data: ByteArray) {}
      }
      return FermenterDevice(manager)
    }
  }
}
