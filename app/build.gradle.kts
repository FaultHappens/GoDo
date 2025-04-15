plugins {
	alias(libs.plugins.androidApplication)
	alias(libs.plugins.jetbrainsKotlinAndroid)
	id("kotlin-kapt")
	id("com.google.dagger.hilt.android")
	id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1"
}

secrets {
	// To add your Maps API key to this project:
	// 1. If the secrets.properties file does not exist, create it in the same folder as the local.properties file.
	// 2. Add this line, where YOUR_API_KEY is your API key:
	//        MAPS_API_KEY=YOUR_API_KEY
	propertiesFileName = "secrets.properties"
	
	// A properties file containing default secret values. This file can be
	// checked in version control.
	defaultPropertiesFileName = "local.defaults.properties"
}

android {
	namespace = "com.dmtsk.godo"
	compileSdk = 35
	
	defaultConfig {
		applicationId = "com.dmtsk.godo"
		minSdk = 24
		targetSdk = 35
		versionCode = 1
		versionName = "1.0"
		
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		vectorDrawables {
			useSupportLibrary = true
		}
		
		val mapsApiKey: String = providers.gradleProperty("MAPS_API_KEY").orNull ?: "MISSING_API_KEY"
		buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")
	}
	
	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_1_8
		targetCompatibility = JavaVersion.VERSION_1_8
	}
	kotlinOptions {
		jvmTarget = "1.8"
	}
	buildFeatures {
		compose = true
		viewBinding = true
		buildConfig = true
	}
	composeOptions {
		kotlinCompilerExtensionVersion = "1.5.1"
	}
	packaging {
		resources {
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
		}
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
	
	//hilt
	implementation(libs.hilt.android)
	implementation(libs.androidx.appcompat)
	implementation(libs.androidx.constraintlayout)
	kapt(libs.hilt.android.compiler)
	
	//google maps
	implementation(libs.secrets.gradle.plugin)
	implementation(libs.maps.compose)
	
	//location
	implementation(libs.play.services.location)
	
	//location and compass
	implementation(libs.accompanist.permissions) // for permissions
	implementation(libs.play.services.location) // for location
	
	//okHTTP
	implementation(libs.okhttp)
	implementation(libs.logging.interceptor)
	
	//Retrofit 2
	implementation (libs.retrofit)
	implementation (libs.converter.gson)
	
	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
	androidTestImplementation(platform(libs.androidx.compose.bom))
	androidTestImplementation(libs.androidx.ui.test.junit4)
	debugImplementation(libs.androidx.ui.tooling)
	debugImplementation(libs.androidx.ui.test.manifest)
}

kapt {
	correctErrorTypes = true
}