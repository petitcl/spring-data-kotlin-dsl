plugins {
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
}

group = "io.github.petitcl"

nexusPublishing {
    packageGroup.set("io.github.petitcl")
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}

//subprojects {
////    plugins {
////        `maven-publish`
////        signing
////        id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
////    }
//
////    publishing {
////        publications {
////            create<MavenPublication>("maven") {
////                from(components["java"])
////                groupId = "io.github.petitcl"
////                artifactId = project.name
////                version = System.getenv("NEW_VERSION")
////            }
////        }
////    }
//}

//
//signing {
//    val signingKey: String? by project
//    val signingPassword: String? by project
//    useInMemoryPgpKeys(signingKey, signingPassword)
//
//    sign(publishing.publications["maven"])
//}
