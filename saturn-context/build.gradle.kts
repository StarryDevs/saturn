plugins {
    id("starry.saturn.kotlin-jvm")
    id("starry.saturn.maven-publish")
}

dependencies {
    api(libs.arrow.core)
    api(libs.bytebuddy)
    api(libs.kotlinx.coroutines.core)
    api(libs.logback.classic)
    api(libs.logback.core)
    api(kotlin("reflect"))
}

tasks.processResources {
    filesMatching("META-INF/saturn.properties") {
        expand(
            mapOf(
                "version" to project.version
            )
        )
    }
}
