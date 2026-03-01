plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "saturn"

include(
    ":saturn-context",
    ":saturn-application"
)
