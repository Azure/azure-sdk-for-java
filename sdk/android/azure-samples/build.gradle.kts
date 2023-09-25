import com.android.build.api.dsl.Packaging

plugins {
    id("com.android.application")
}

android {
    namespace = "com.azure.android"
    compileSdk = 33

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.azure.android"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "AZURE_CLIENT_ID", "\"${System.getenv("AZURE_CLIENT_ID")}\"")
        buildConfigField("String", "AZURE_CLIENT_SECRET", "\"${System.getenv("AZURE_CLIENT_SECRET")}\"")
        buildConfigField("String", "AZURE_TENANT_ID", "\"${System.getenv("AZURE_TENANT_ID")}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    packaging {
        resources {
            excludes += "/META-INF/*"
        }
    }
    println(System.getenv("AZURE_CLIENT_SECRET"))
    println(System.getenv("AZURE_CLIENT_ID"))
    println(System.getenv("AZUE_TENANT_ID"))

}


dependencies {
    implementation(project.dependencies.platform("com.azure:azure-sdk-bom:1.2.16"))
    // azure core
    implementation("com.azure:azure-core")
    implementation("com.azure:azure-json")
    implementation("com.azure:azure-core-http-okhttp")
    // azure_appconfig
    implementation("com.azure:azure-data-appconfiguration")

    // azure_keyvault
    implementation("com.azure:azure-security-keyvault-secrets")
    implementation("com.azure:azure-security-keyvault-keys")
    implementation("com.azure:azure-security-keyvault-certificates")

    // azure_storage
    implementation("com.azure:azure-storage-blob")

    // azure_identity
    implementation("com.azure:azure-identity")

    // android
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")

    // xml
    implementation("com.azure:azure-xml:1.0.0-beta.2")
    implementation("stax:stax:1.2.0")

    // For testing issue 1540, https://github.com/microsoftgraph/msgraph-sdk-java/issues/1540
    implementation("com.azure:azure-identity:1.10.0")
    implementation("com.azure:azure-core:1.43.0")

    //For testing issue 35756, https://github.com/Azure/azure-sdk-for-java/issues/35756
    implementation("com.azure:azure-ai-translation-text:1.0.0-beta.1")

    //For testing issue 35719, https://github.com/Azure/azure-sdk-for-java/issues/35719
    implementation("com.azure:azure-ai-openai:1.0.0-beta.2")

    // testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}