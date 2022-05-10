plugins {
    kotlin("jvm") version "1.6.21" apply false
    id("maven-publish")
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("maven-publish")
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
        }
    }
}

tasks {
    wrapper {
        gradleVersion = "7.4.2"
    }
}