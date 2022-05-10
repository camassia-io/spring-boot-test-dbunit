plugins {
    id("org.springframework.boot") version "2.5.0"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("plugin.spring") version "1.6.21"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
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

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "spring-boot-test-dbunit"
            version = System.getenv("GITHUB_VERSION")
            from(components["kotlin"])
            artifact(tasks.kotlinSourcesJar)
        }
    }
}
