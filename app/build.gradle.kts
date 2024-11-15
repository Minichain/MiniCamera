plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.kapt)
}

android {
  namespace = "com.minichain.minicamera"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.minichain.minicamera"
    minSdk = 30
    targetSdk = 34
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables {
      useSupportLibrary = true
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlinOptions {
    jvmTarget = "17"
  }

  buildFeatures {
    compose = true
  }

  composeOptions {
    kotlinCompilerExtensionVersion = "1.5.1"
  }

  packaging {
    resources.excludes.add("META-INF/DEPENDENCIES")
    resources.excludes.add("META-INF/LICENSE")
    resources.excludes.add("META-INF/LICENSE.txt")
    resources.excludes.add("META-INF/license.txt")
    resources.excludes.add("META-INF/NOTICE")
    resources.excludes.add("META-INF/NOTICE.txt")
    resources.excludes.add("META-INF/notice.txt")
    resources.excludes.add("META-INF/ASL2.0")
    resources.excludes.add("META-INF/*.kotlin_module")
  }
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.ui.tooling.preview)
  implementation(libs.androidx.material3)

  implementation("androidx.camera:camera-camera2:1.4.0")
  implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
  implementation("com.amplifyframework:aws-auth-cognito:1.6.4")
//  implementation("com.amplifyframework:aws-api:1.16.13")
  implementation(project(":aws-android-sdk-kinesisvideo"))

  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)
  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)
}