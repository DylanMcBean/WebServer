plugins {
    kotlin("jvm") version "1.8.0"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("io.ktor:ktor-server-core:2.3.0")
    implementation("io.ktor:ktor-server-netty:2.3.0")
    implementation("io.ktor:ktor-server-sessions:2.3.0")
    implementation("io.ktor:ktor-server-sessions:2.3.0")
    implementation("ch.qos.logback:logback-classic:1.4.7")
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("com.h2database:h2:2.1.214")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("ApplicationKt")
}