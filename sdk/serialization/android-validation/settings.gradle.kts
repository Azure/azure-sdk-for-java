pluginManagement {
    repositories {
        google()
        mavenCentral()
         gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

include(":azure-json", ":azure-xml")
project(":azure-json").projectDir = file("../azure-json/android-validation")
project(":azure-xml").projectDir = file("../azure-xml/android-validation")

rootProject.name = "AndroidCompat"
