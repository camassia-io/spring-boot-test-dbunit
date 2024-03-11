plugins {
    kotlin("jvm") version "1.6.21" apply false
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0-rc-2"
    id("maven-publish")
    signing
}

group = "io.camassia"
version = System.getenv("GITHUB_VERSION")

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
                jvmTarget = "13"
            }
        }
    }
}

signing {
    val signingKey = providers.environmentVariable("GPG_SIGNING_KEY")
    val signingPassphrase = providers.environmentVariable("GPG_SIGNING_PASSPHRASE")

    if (signingKey.isPresent && signingPassphrase.isPresent) {
        project.logger.info("Signing Key + Passphrase found")
        useInMemoryPgpKeys(signingKey.get(), signingPassphrase.get())
        val extension = extensions.getByName("publishing") as PublishingExtension
        sign(extension.publications)
    } else {
        project.logger.warn("Signing Key or Passphrase missing. Artifacts will not be signed.")
    }
}

nexusPublishing {
    repositories {
        sonatype {
            //nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            //nexusUrl.set(uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/"))
            //snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            //snapshotRepositoryUrl.set(uri("https://oss.sonatype.org/content/repositories/snapshots/"))
            val ossrhUsername = providers.environmentVariable("OSSRH_USERNAME")
            val ossrhPassword = providers.environmentVariable("OSSRH_PASSWORD")
            if (ossrhUsername.isPresent && ossrhPassword.isPresent) {
                project.logger.info("OSSRH credentials found")
                username.set(ossrhUsername.get())
                password.set(ossrhPassword.get())
            } else {
                project.logger.warn("OSSRH credentials not found.These are required to publish to Sonatype.")
            }
        }
    }
}

tasks {
    wrapper {
        gradleVersion = "8.6"
    }
}
