plugins {
    `maven-publish`
    signing
}

publishing {
    repositories {
        maven {
            name = "OSSRH"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = "io.github.petitcl"
            artifactId = project.getName()
            version = System.getenv("NEW_VERSION")
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)

    println("publishing.publications: ${publishing.publications.asMap}")
    sign(publishing.publications["maven"])
}
