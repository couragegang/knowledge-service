plugins {
    id("io.micronaut.application") version "4.5.4"
    id("com.gradleup.shadow") version "8.3.7"
    jacoco
}

extra["jacocoCoverageExcludes"] = listOf(
    "**/api/dto/**",
    "**/repo/**",
    "**/Application.class",
    "**/config/JacksonFactory.class",
    "**/api/KnowledgeController.class",
)
apply(from = rootDir.resolve("gradle/jacoco-coverage.gradle.kts"))

version = "0.1.0-SNAPSHOT"
group = "com.couragegang.knowledge"

repositories {
    mavenCentral()
    maven { url = uri("https://maven.aliyun.com/repository/public") }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

micronaut {
    version("4.7.6")
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        module("com.couragegang.knowledge")
    }
}

dependencies {
    annotationProcessor("io.micronaut.serde:micronaut-serde-processor")
    annotationProcessor("io.micronaut.validation:micronaut-validation-processor")

    implementation("io.micronaut:micronaut-http-server-netty")
    implementation("io.projectreactor:reactor-core")
    implementation("io.micronaut.validation:micronaut-validation")
    implementation("io.micronaut.sql:micronaut-jdbc-hikari")
    implementation("io.micronaut.flyway:micronaut-flyway")
    implementation("io.micronaut:micronaut-management")
    implementation("io.micronaut.micrometer:micronaut-micrometer-registry-prometheus")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation("io.micronaut.sql:micronaut-jdbc")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("jakarta.annotation:jakarta.annotation-api")

    runtimeOnly("org.yaml:snakeyaml")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")

    testImplementation("io.micronaut.test:micronaut-test-junit5")
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.mockito:mockito-junit-jupiter:5.14.2")
    testImplementation("org.assertj:assertj-core:3.26.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

application {
    mainClass.set("com.couragegang.knowledge.Application")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
