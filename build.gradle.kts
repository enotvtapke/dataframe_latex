plugins {
    kotlin("jvm") version "2.0.20"
    id("org.jetbrains.kotlinx.dataframe") version "0.15.0"
}

group = "com.github.enovtapke"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:dataframe:0.15.0")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
