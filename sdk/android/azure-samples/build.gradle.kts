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
            pickFirsts += "/META-INF/*"
        }
    }
}
configurations{
    implementation{
        exclude(group = "com.azure", module = "azure-core-http-netty")
    }
}

dependencies {
    implementation(project.dependencies.platform("com.azure:azure-sdk-bom:1.2.17"))

    // azure core -- specifically declare 1.44.0 to get updated reflectionutils
    implementation("com.azure:azure-core:1.44.0")

    implementation("com.azure:azure-core-http-okhttp")

    // JSON
    implementation("com.azure:azure-json")

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

    // azure_messaging_eventhubs
    implementation("com.azure:azure-messaging-eventhubs")

    // android
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")

    // xml
    implementation("com.azure:azure-xml:1.0.0-beta.2")
    implementation("stax:stax:1.2.0")

    //For testing issue 35756, https://github.com/Azure/azure-sdk-for-java/issues/35756
    implementation("com.azure:azure-ai-translation-text:1.0.0-beta.1")

    //For testing issue 35719, https://github.com/Azure/azure-sdk-for-java/issues/35719
    implementation("com.azure:azure-ai-openai:1.0.0-beta.2")

    // SL4J provides more verbose logging
    /*
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.slf4j:slf4j-simple:2.0.9")
    */

    // testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    constraints {
        implementation("com.fasterxml.jackson:jackson-bom") {
            version {
                require("2.15.2")
                reject("2.13.5")
            }
            because("earlier versions declared as transitive dependencies use android non-compatible factory method")
        }
        implementation("com.fasterxml.jackson:jackson-core") {
            version {
                require("2.15.2")
                reject("2.13.5")
            }
            because("earlier versions declared as transitive dependencies for azure are not android compatible")
        }
        implementation("com.fasterxml.jackson:jackson-databind") {
            version {
                require("2.15.2")
                reject("2.13.5")
            }
            because("earlier versions declared as transitive dependencies for azure are not android compatible")
        }
        implementation("com.fasterxml.jackson:jackson-dataformat-xml") {
            version {
                require("2.15.2")
                reject("2.13.5")
            }
            because("earlier versions declared as transitive dependencies for azure are not android compatible")
        }
        implementation("com.fasterxml.jackson:jackson-datatype-jsr310") {
            version {
                require("2.15.2")
                reject("2.13.5")
            }
            because("earlier versions declared as transitive dependencies for azure are not android compatible")
        }
    }
}