plugins {
    kotlin("jvm") version "1.9.22"
    application
}

group = "com.kuollutkissa"
version = "1.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

application {
    mainClass = "com.kuollutkissa.klox.MainKt"
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.kuollutkissa.klox.MainKt"
    }
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}