plugins {
    id("org.springframework.boot") version "2.5.0"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("plugin.spring") version "1.4.30"
}

java {
    sourceCompatibility = JavaVersion.VERSION_13
    targetCompatibility = JavaVersion.VERSION_13
    withSourcesJar()
}

dependencies {
    api("org.dbunit:dbunit:2.7.2")
    api("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "com.vaadin.external.google", module = "android-json")
    }
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.6.2")
    testImplementation("org.assertj:assertj-core:3.19.0")
    testImplementation("io.mockk:mockk:1.10.6")
    testImplementation("com.ninja-squad:springmockk:3.0.1")

}

tasks {
    bootJar {
        enabled = false
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "spring-boot-test-dbunit"
            version = System.getenv("GITHUB_VERSION")
            from(components["kotlin"])
            artifact(tasks.kotlinSourcesJar)
            pom {
                name.set("spring-boot-test-dbunit")
                description.set("An Open Source Spring Boot DB Unit Integration, based on the popular but no longer maintained Spring Test DB Unit")
                url.set("https://github.com/camassia-io/spring-boot-test-dbunit")
                version = System.getenv("GITHUB_VERSION")
                groupId = "io.camassia"
                artifactId = "spring-boot-test-dbunit"
                packaging = "jar"
                scm {
                    connection.set("scm:https://github.com/camassia-io/spring-boot-test-dbunit.git")
                    developerConnection.set("scm:git://github.com/camassia-io/spring-boot-test-dbunit.git")
                }
                licenses {
                    license {
                        name.set("Apache License, Version 2.0")
                        url.set("https://github.com/camassia-io/spring-boot-test-dbunit/blob/main/LICENSE")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("ed0906")
                        name.set("ed0906")
                        email.set("ed0906@camassia.io")
                    }
                }
                organization {
                    name.set("Camassia")
                    url.set("https://camassia.io")
                }
            }
        }
    }
}
