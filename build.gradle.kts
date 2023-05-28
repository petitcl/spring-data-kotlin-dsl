import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    api(libs.springData.jpa)
    api(libs.bundles.kotlin)
    compileOnly(libs.jakarta.persistenceApi)

    testImplementation(libs.bundles.testBase)
    testImplementation(libs.springBoot.test)
    testImplementation(libs.springBoot.dataJpa)
    testImplementation(libs.h2)
}
