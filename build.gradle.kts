plugins {
    kotlin("jvm") version "1.6.21" apply false
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0-rc-2"
    id("maven-publish")
}

group = "io.camassia"
version = System.getenv("VERSION")

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
    }

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

                // Users of this project need the below Java Version as a minimum
                jvmTarget = "11"
            }
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            val ossrhUsername = providers.environmentVariable("OSSRH_USERNAME")
            val ossrhPassword = providers.environmentVariable("OSSRH_PASSWORD")
            if (ossrhUsername.isPresent && ossrhPassword.isPresent) {
                project.logger.info("OSSRH credentials found")
                username.set(ossrhUsername.get())
                password.set(ossrhPassword.get())
            } else {
                project.logger.warn("OSSRH credentials not found. These are required to publish to Sonatype.")
            }
        }
    }
}

tasks {
    wrapper {
        gradleVersion = "8.6"
    }
}
