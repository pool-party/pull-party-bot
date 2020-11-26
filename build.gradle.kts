plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.4.0"
}

group = "org.pool-party"
version = "1.0.2.1"

repositories {
    maven(url = "https://jitpack.io")
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.0")
    implementation("com.github.elbekD:kt-telegram-bot:1.3.4")

    implementation("org.jetbrains.exposed:exposed-core:0.24.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.24.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.24.1")

    implementation("org.postgresql:postgresql:42.2.2")

    implementation("com.natpryce:konfig:1.6.10.0")

    implementation("org.slf4j:slf4j-simple:2.0.0-alpha1")
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

tasks.compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
}
tasks.compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
}
