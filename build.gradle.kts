// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    id("com.google.devtools.ksp") version "1.9.21-1.0.15" apply false
}
buildscript {
    repositories {
        google()
        mavenCentral()
        dependencies {
            classpath("com.android.tools.build:gradle:8.5.1")
            classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.21")
            classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.0")
        }
    }
}