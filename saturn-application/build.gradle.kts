plugins {
    id("starry.saturn.kotlin-jvm")
    id("starry.saturn.maven-publish")
}

dependencies {
    api(project(":saturn-context"))
}
