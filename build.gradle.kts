plugins {
    id ("java")
    id ("org.springframework.boot") version "3.1.12"
    id ("io.spring.dependency-management") version "1.1.7"
    id ("application")
}

group = "ru.startup.skinscan"
version = "0.0.1-SNAPSHOT"
description = "SkinScan"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "ru.startup.skinscan.Main"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
    implementation("org.projectlombok:lombok:1.18.42")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")
    implementation("org.postgresql:postgresql:42.7.11")
    testRuntimeOnly ("org.junit.platform:junit-platform-launcher:6.0.3")
    implementation("org.flywaydb:flyway-core:10.15.0")
    implementation("org.flywaydb:flyway-database-postgresql:10.15.0")
    implementation ("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.security:spring-security-crypto:7.1.0")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.minio:minio:9.0.3")
//    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
//    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
//    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")
//    implementation("io.awspring.cloud:spring-cloud-aws-starter")
}

tasks.test {
    useJUnitPlatform()
}