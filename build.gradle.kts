plugins {
    java
    id("org.springframework.boot") version "4.0.0"
}

group = "io.mycelar.example"
version = "0.1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:4.0.0"))
    implementation(platform("org.springframework.ai:spring-ai-bom:2.0.0"))

    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.ai:spring-ai-starter-model-openai")
    implementation("io.mycelar:mycelar-spring-ai")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-webmvc-test")
    testImplementation("org.testcontainers:junit-jupiter:1.21.4")
    testImplementation("org.testcontainers:testcontainers:1.21.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

configurations.configureEach {
    resolutionStrategy.force(
        "org.testcontainers:testcontainers:1.21.4",
        "org.testcontainers:junit-jupiter:1.21.4",
    )
}

tasks.test {
    useJUnitPlatform()
    systemProperty("api.version", "1.44")
}
