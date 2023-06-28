plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
}


group = "io.github.petitcl"
version = if (System.getenv("NEW_VERSION") != null) { System.getenv("NEW_VERSION") } else { "0.0.1-SNAPSHOT" }

repositories {
    mavenCentral()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("Spring Data Kotlin DSL")
                description.set("Kolint DSL for Spring Data Common and JPA")
                url.set("https://github.com/gradle-nexus-e2e/nexus-publish-e2e-minimal")
                inceptionYear.set("2023")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("petitcl")
                        name.set("Clement Petit")
                    }
                }
                scm {
                    connection.set("scm:https://github.com/petitcl/spring-data-kotlin-dsl.git")
                    developerConnection.set("scm:git@github.com/petitcl/spring-data-kotlin-dsl.git")
                    url.set("https://github.com/petitcl/spring-data-kotlin-dsl")
                }
            }
        }
    }
}

signing {
    setRequired { !project.version.toString().endsWith("-SNAPSHOT") && !project.hasProperty("skipSigning") }
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["maven"])
}
