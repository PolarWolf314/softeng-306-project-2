plugins {
    id("java")
    id("application")
    id("io.freefair.lombok") version ("8.3")
    id("com.github.johnrengelman.shadow") version ("8.1.1")
}

group = "nz.ac.auckland.se306.group12"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

application {
    mainClass = "${group}.Main"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.sourceforge.argparse4j:argparse4j:0.9.0")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.jar {
    manifest {
        // Include the main class in the JAR file so that we can run it
        attributes["Main-Class"] = application.mainClass
    }
}

tasks.shadowJar {
    // The output JAR file must be named `scheduler.jar`
    archiveFileName = "scheduler.jar"
}

tasks.test {
    useJUnitPlatform()
}
