pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        // ✅ Mappls plugin repo for build plugins (if needed in future)
        maven {
            url = uri("https://maven.mappls.com/repository/mappls/")
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // ✅ Mappls dependency repository
        maven {
            url = uri("https://maven.mappls.com/repository/mappls/")
        }
        // ✅ JitPack for uCrop or other libraries
        maven {
            url = uri("https://jitpack.io")
        }
    }
}


rootProject.name = "seekshak.com"
include(":app")
