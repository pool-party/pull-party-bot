plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.5.0"
    kotlin("plugin.serialization") version "1.5.0"

    id("org.flywaydb.flyway") version "7.8.1"
}

group = "org.pool-party"
version = "1.2.0"

repositories {
    maven("https://jitpack.io")
    mavenCentral()
    jcenter()
}

val exposedVersion = "0.29.1"
val testContainersVersion = "1.15.1"
val jupyterVersion = "5.6.0"

dependencies {
    implementation("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin", "kotlin-reflect", "1.5.0")
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.1.0")
    implementation("com.github.elbekD", "kt-telegram-bot", "1.3.8")

    implementation("org.jetbrains.exposed", "exposed-core", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-dao", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-jdbc", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-jodatime", exposedVersion)

    implementation("org.flywaydb", "flyway-core", "7.8.1")

    implementation("com.natpryce", "konfig", "1.6.10.0")

    implementation("org.slf4j", "slf4j-simple", "2.0.0-alpha1")
    implementation("io.github.microutils", "kotlin-logging", "2.0.4")

    implementation("info.debatty", "java-string-similarity", "2.0.0")

    runtimeOnly("org.postgresql", "postgresql", "42.2.19")

    testImplementation("org.jetbrains.kotlin", "kotlin-test-junit5", "1.5.0")
    testImplementation("org.junit.jupiter", "junit-jupiter-api", jupyterVersion)
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", jupyterVersion)

    testImplementation("io.mockk", "mockk", "1.10.6")
    testImplementation("org.testcontainers", "postgresql", testContainersVersion)
    testImplementation("org.testcontainers", "junit-jupiter", testContainersVersion)
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    configurations["compileClasspath"].forEach { from(zipTree(it.absoluteFile)) }
    configurations["runtimeClasspath"].forEach { from(zipTree(it.absoluteFile)) }

    manifest {
        attributes(
            mapOf(
                "Main-Class" to "com.github.pool_party.pull_party_bot.MainKt"
            )
        )
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
}
tasks.compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
}
