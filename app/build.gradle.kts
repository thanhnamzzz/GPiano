plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.ksp)
}

android {
	namespace = "com.gambisoft.gpiano"
	compileSdk = 35

	defaultConfig {
		applicationId = "com.gambisoft.gpiano"
		minSdk = 21
		targetSdk = 34
		versionCode = 1
		versionName = "1.0"

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
	kotlinOptions {
		jvmTarget = "11"
	}
	buildFeatures {
		viewBinding = true
		buildConfig = true
	}
}

dependencies {

	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.appcompat)
	implementation(libs.material)
	implementation(libs.androidx.activity)
	implementation(libs.androidx.constraintlayout)
//	testImplementation(libs.junit)
//	androidTestImplementation(libs.androidx.junit)
//	androidTestImplementation(libs.androidx.espresso.core)
	implementation(libs.commonlib)
	implementation(libs.androidx.core.splashscreen)
	implementation(project(":pianoLibrary"))

	//Gson
	implementation("com.google.code.gson:gson:2.11.0")
	//Lottie json
	implementation("com.airbnb.android:lottie:6.3.0")
	//Indicator
	implementation("com.tbuonomo:dotsindicator:5.0")
	//Animation
	implementation("com.github.gayanvoice:android-animations-kotlin:1.0.1")

	//RoomDatabase
	implementation(libs.androidx.room.runtime)
	implementation(libs.androidx.room.ktx)
	ksp(libs.androidx.room.compiler)
}