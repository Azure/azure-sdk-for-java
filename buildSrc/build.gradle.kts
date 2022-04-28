
plugins {
    `kotlin-dsl`
}

dependencies {
    implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:5.0.6")
    implementation("org.revapi:revapi:0.14.4")
    implementation("org.revapi:revapi-java:0.26.1")
}

repositories {
    // Use the plugin portal to apply community plugins in convention plugins.
    gradlePluginPortal()
}
