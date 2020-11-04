import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    application
}
group = "lu.braungilles"
version = "1.1"

repositories {
    mavenCentral()
}
dependencies {
    testImplementation(kotlin("test-junit"))
    implementation("com.github.theholywaffle:teamspeak3-api:1.2.0")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("org.telegram:telegrambots:5.0.0")

}
tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}
application {
    mainClassName = "MainKt"
}