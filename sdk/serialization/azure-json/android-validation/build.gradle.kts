plugins {
    id("com.android.library") version "8.7.0" apply true
}

android {
    namespace = "com.azure.azure.json"

    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    buildTypes.getByName("release") {
        isMinifyEnabled = false
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
        lintConfig = file("../../../../android-validation/lint.xml")
    }

    sourceSets.getByName("main") {
        java.srcDir(file("../src/main/java"))
        java.exclude("module-info.java")
        manifest.srcFile("../../../../android-validation/AndroidManifest.xml")
    }
    sourceSets.getByName("test") {
        setRoot("../src/test")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging {
            events("skipped", "failed")
        }
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.11.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.11.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
