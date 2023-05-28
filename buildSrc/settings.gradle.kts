enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    versionCatalogs {
        create("conventionLibs") {
            from(files("../gradle/plugin-deps.versions.toml"))
        }
    }
}
