import org.gradle.api.plugins.JavaApplication

plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"
    application
}

group = "com.getticket"
version = "1.0.0"

configure<JavaApplication> {
    mainClass.set("com.getticket.MainKt")
}

dependencies {

    implementation("io.ktor:ktor-server-core:2.3.7")
    implementation("io.ktor:ktor-server-netty:2.3.7")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    implementation("com.auth0:auth0:2.12.0")
    implementation("com.mysql:mysql-connector-j:8.3.0")
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("com.zaxxer:HikariCP:6.0.0")
    implementation("io.ktor:ktor-server-auth:2.3.7")
    implementation("org.jetbrains.exposed:exposed-core:0.45.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.45.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.45.0")
    implementation("at.favre.lib:bcrypt:0.10.2")
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("io.ktor:ktor-server-cors:2.3.7")
    implementation("io.ktor:ktor-server-status-pages:2.3.7")
    implementation("io.ktor:ktor-server-auth-jwt:2.3.7")
}