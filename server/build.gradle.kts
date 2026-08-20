import java.util.Properties

plugins {
  alias(libs.plugins.android.library)
}

val localProps = Properties().also { props ->
  rootProject.file("server/credentials.properties").takeIf { it.exists() }?.inputStream()?.use { props.load(it) }
}

android {
  namespace = "com.mozzarelly.rodeoserver.server"
  compileSdk {
    version = release(37)
  }

  defaultConfig {
    minSdk = 33

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")

    buildConfigField("String", "ETEK_LOGIN", "\"${localProps.getProperty("etek_login") ?: ""}\"")
    buildConfigField("String", "ETEK_PASSWORD", "\"${localProps.getProperty("etek_password") ?: ""}\"")
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  buildFeatures {
    buildConfig = true
  }
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))

  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
  implementation(libs.ktor.server.cio)
  implementation(libs.ktor.server.core)
  implementation(libs.ktor.server.content.negotiation)
  implementation(libs.ktor.serialization.kotlinx.json)
  implementation(libs.hilt.android)
  implementation(libs.hilt.navigation.compose)
  implementation(libs.room.runtime)
  implementation(libs.room.ktx)
  implementation(libs.retrofit)
  implementation(libs.retrofit.converter.kotlinx.serialization)
  implementation(libs.material)

  testImplementation(libs.junit)

  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.tooling)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
}