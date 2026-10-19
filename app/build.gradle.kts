plugins	{
	// alias(libs.plugins.kotlin.android)
	alias(libs.plugins.android.application)
	alias(libs.plugins.compose.android)
}

android	{
	namespace = "com.slambyte.tester.filesystem"
	compileSdk = 37
	defaultConfig	{
		applicationId = "com.slambyte.tester.filesystem"
		minSdk = 24
		targetSdk = 37
		versionCode = 1
		versionName = "1.0"
	}

	buildTypes	{
		release	{
			isMinifyEnabled = false
			isShrinkResources = false
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
		}
		
		debug {
		  applicationIdSuffix = ".debug"
		  isDebuggable = true
		}
	}
	
	buildFeatures {
		compose = true
	}
}

java	{
	toolchain	{
		languageVersion = JavaLanguageVersion.of(17)
	}
}

dependencies	{
  implementation(platform(libs.compose.bom))
  
  implementation(libs.core.ktx)
  implementation(libs.compose.material.icons.extended)
  implementation(libs.startup.runtime)
  implementation(libs.activity.compose)
  implementation(libs.ui.tooling.preview)
  implementation(libs.material3)
	implementation(libs.material)
	implementation(libs.filesystem)
	implementation(libs.navigation.compose)
  debugImplementation(libs.ui.tooling)
  debugImplementation(libs.ui.test.manifest)
}
