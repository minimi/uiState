plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm")
}

group = "com.github.minimi"
version = "2.0"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
}
