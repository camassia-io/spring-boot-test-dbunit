plugins {
    id("org.springframework.boot") version "2.5.0"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("plugin.spring") version "1.6.21"

    id("maven-publish")
    signing
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    api("org.dbunit:dbunit:2.7.3")
    api("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "com.vaadin.external.google", module = "android-json")
    }
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.6.2")
    testImplementation("org.assertj:assertj-core:3.22.0")
    testImplementation("io.mockk:mockk:1.12.3")
}

tasks {
    bootJar {
        enabled = false
    }
}

signing {
    val signingKey = providers.environmentVariable("GPG_SIGNING_KEY")
    val signingPassphrase = providers.environmentVariable("GPG_SIGNING_PASSPHRASE")

    if (signingKey.isPresent && signingPassphrase.isPresent) {
        project.logger.info("Signing Key + Passphrase found")
        project.logger.info(signingKey.get())
        useInMemoryPgpKeys(signingKey.get(), signingPassphrase.get())
        sign(publishing.publications)
    } else {
        project.logger.warn("Signing Key or Passphrase missing. Artifacts will not be signed.")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.camassia"
            artifactId = "spring-boot-test-dbunit"
            version = System.getenv("GITHUB_VERSION")
            from(components["kotlin"])
            artifact(tasks.kotlinSourcesJar)
            artifact(tasks.getByName("javadocJar"))

            pom {
                name.set("spring-boot-test-dbunit")
                description.set("DB Unit Integration for Spring Boot")
                url.set("https://github.com/camassia-io/spring-boot-test-dbunit")
                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://opensource.org/licenses/Apache-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("ed0906")
                        name.set("ed0906")
                        email.set("ed0906@camassia.io")
                        organization.set("Camassia")
                        organizationUrl.set("https://camassia.io")
                    }
                }
                scm {
                    url.set("https://github.com/camassia-io/spring-boot-test-dbunit")
                    connection.set("scm:git:git://github.com/camassia-io/spring-boot-test-dbunit.git")
                    developerConnection.set("scm:git:git://github.com/camassia-io/spring-boot-test-dbunit.git")
                }
                issueManagement {
                    url.set("https://github.com/camassia-io/spring-boot-test-dbunit/issues")
                }
            }
        }
    }
}
