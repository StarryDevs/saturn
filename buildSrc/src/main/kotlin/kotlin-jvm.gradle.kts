package starry.saturn

allprojects {
    version = "1.0.0"
}

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(25)
    explicitApi()

    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}
