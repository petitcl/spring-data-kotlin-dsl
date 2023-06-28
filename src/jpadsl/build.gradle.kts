plugins {
    id("kotlin-lib-conventions")
    id("publishing-conventions")
}

group = "io.github.petitcl"

dependencies {
    api(projects.src.springDataCommonKotlinDsl)
    api(libs.springData.jpa)
    api(libs.bundles.kotlin)
    compileOnly(libs.jakarta.persistenceApi)

    testImplementation(libs.bundles.testBase)
    testImplementation(libs.springBoot.test)
    testImplementation(libs.springBoot.dataJpa)
    testImplementation(libs.h2)
}
