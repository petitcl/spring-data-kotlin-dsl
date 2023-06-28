plugins {
    id("kotlin-lib-conventions")
    id("publishing-conventions")
}

dependencies {
    api(libs.springData.commons)
    api(libs.bundles.kotlin)

    testImplementation(libs.bundles.testBase)
    testImplementation(libs.springBoot.test)
    testImplementation(libs.springBoot.dataJpa)
    testImplementation(libs.h2)
}
