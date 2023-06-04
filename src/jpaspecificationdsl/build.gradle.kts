plugins {
    id("kotlin-lib-conventions")
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
