plugins {
    kotlin("jvm") version "1.4.30" apply false
    id("maven-publish")
    id("signing")
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("maven-publish")
        plugin("signing")
    }

    group = "io.camassia"

    repositories {
        mavenCentral()
    }

    tasks {
        withType<Test> {
            useJUnitPlatform()
        }

        withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xjsr305=strict")
                jvmTarget = "13"
            }
        }
    }

    signing {
//        setRequired {
//            val release = !(project.version as String).contains("SNAPSHOT")
//            val uploading = project.gradle.taskGraph.hasTask("uploadArchives")
//            release && uploading
//        }
        val key = System.getenv("GPG_SIGNING_KEY")
        val password = System.getenv("GPG_SIGNING_PASSWORD")
        useInMemoryPgpKeys(key, password)
        sign(configurations["archives"])
    }

    publishing {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/camassia-io/spring-boot-test-dbunit")
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
            maven {
                name = "nexus"
                url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = System.getenv("NEXUS_USERNAME")
                    password = System.getenv("NEXUS_PASSWORD")
                }
            }
            maven {
                name = "nexus-snapshots"
                url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
                credentials {
                    username = System.getenv("NEXUS_USERNAME")
                    password = System.getenv("NEXUS_PASSWORD")
                }
            }
        }
    }
}

tasks {
    wrapper {
        gradleVersion = "7.0.2"
    }
}