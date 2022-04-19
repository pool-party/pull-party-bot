import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.6.20"
    kotlin("plugin.serialization") version "1.6.20"

    id("org.flywaydb.flyway") version "8.5.8"
}

group = "org.pool-party"
version = "1.2.4"

repositories {
    maven("https://jitpack.io")
    mavenCentral()
}

val exposedVersion = "0.38.1"
val testContainersVersion = "1.17.1"
val jupyterVersion = "5.8.2"
val kotlinVersion = "1.6.20"

dependencies {
    implementation("org.jetbrains.kotlin", "kotlin-stdlib-jdk8", kotlinVersion)
    implementation("org.jetbrains.kotlin", "kotlin-reflect", kotlinVersion)
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.3.2")
    implementation("com.github.elbekD", "kt-telegram-bot", "1.4.1")

    implementation("org.jetbrains.exposed", "exposed-core", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-dao", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-jdbc", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-jodatime", exposedVersion)

    implementation("org.flywaydb", "flyway-core", "8.5.8")

    implementation("com.natpryce", "konfig", "1.6.10.0")

    implementation("org.slf4j", "slf4j-simple", "2.0.0-alpha7")
    implementation("io.github.microutils", "kotlin-logging", "2.1.21")

    implementation("info.debatty", "java-string-similarity", "2.0.0")

    runtimeOnly("org.postgresql", "postgresql", "42.3.4")

    testImplementation("org.jetbrains.kotlin", "kotlin-test-junit5", kotlinVersion)
    testImplementation("org.junit.jupiter", "junit-jupiter-api", jupyterVersion)
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", jupyterVersion)

    testImplementation("io.mockk", "mockk", "1.12.0")
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

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlinx.coroutines.DelicateCoroutinesApi"
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
    kotlinOptions.freeCompilerArgs += "-Xcontext-receivers"
}
