plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.4.30"
    kotlin("plugin.serialization") version "1.4.30"
}

group = "org.pool-party"
version = "1.1.0"

repositories {
    maven(url = "https://jitpack.io")
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.30")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")
    implementation("com.github.elbekD:kt-telegram-bot:1.3.8")

    implementation("org.jetbrains.exposed:exposed-core:0.29.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.29.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.29.1")
    implementation("org.jetbrains.exposed:exposed-jodatime:0.29.1")

    implementation("org.postgresql:postgresql:42.2.19")

    implementation("com.natpryce:konfig:1.6.10.0")

    implementation("org.slf4j:slf4j-simple:2.0.0-alpha1")
    implementation("io.github.microutils:kotlin-logging:2.0.4")

    implementation("info.debatty:java-string-similarity:2.0.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.4.30")
    testImplementation("io.mockk:mockk:1.10.6")
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
    useJUnit()
}

tasks.compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
}
tasks.compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
}
