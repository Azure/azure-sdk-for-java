plugins {
    id("com.android.library") version "8.7.0" apply true
}

android {
    namespace = "com.azure.azure.xml"

    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // Results for linting will be in /build/reports/lint-results.debug.txt
    lint {
        baseline = file("lint-baseline.xml")
        checkAllWarnings = true
        warningsAsErrors = true
        targetSdk = 35
        lintConfig = file("lint.xml")
    }

    sourceSets.getByName("main") {
        java.srcDir(file("../src/main/java"))
        java.exclude("module-info.java")
        manifest.srcFile("AndroidManifest.xml")
    }
    sourceSets.getByName("androidTest") {
        setRoot("../src/androidTest")
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.11.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.11.2")

    // This dependency is necessary for azure-xml to work with Android.
    // Android doesn't have the javax.xml.stream* APIs required by azure-xml and this package provides them.
    implementation("javax.xml.stream:stax-api:1.0")
}
