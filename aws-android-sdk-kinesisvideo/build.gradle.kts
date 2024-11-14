plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
}

android {
  namespace = "com.amazonaws.kinesisvideo"
  compileSdk = 34

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }

  kotlinOptions {
    jvmTarget = "1.8"
  }

  defaultConfig {
    minSdk = 30
    targetSdk = 34
  }

  packaging {
    resources {
      excludes += "/META-INF/DEPENDENCIES"
    }
  }
}

dependencies {
  implementation("com.amazonaws:aws-android-sdk-core:2.71.0@aar")
  implementation("androidx.annotation:annotation:1.9.1")
  implementation("org.apache.httpcomponents:httpclient:4.5.14")
}

