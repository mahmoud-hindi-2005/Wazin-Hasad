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
        // أضف هذا السطر هنا ليتمكن البرنامج من تحميل مكتبة الرسوم البيانية
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Wazin Hasad"
include(":app")