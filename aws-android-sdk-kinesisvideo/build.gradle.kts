plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.kapt)
}

android {
  namespace = "com.amazonaws.kinesisvideo"
  compileSdk = 34

  defaultConfig {
    minSdk = 30
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }

  kotlinOptions {
    jvmTarget = "17"
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
  implementation("com.amazonaws:aws-android-sdk-core:2.71.0@aar")
  implementation("androidx.annotation:annotation:1.9.1")
  implementation("org.apache.httpcomponents:httpclient:4.5.14")
}

