import java.util.Properties
import java.io.FileInputStream
import java.io.FileOutputStream 

plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0-beta12"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.16"
}

group = "org.clockworx"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    paperweight.paperDevBundle("1.21.5-R0.1-SNAPSHOT")
    
    // Database - Core
    implementation("org.hibernate:hibernate-core:6.6.13.Final") 
    implementation("org.hibernate:hibernate-community-dialects:6.6.13.Final")
    implementation("org.flywaydb:flyway-core:11.7.2")
    implementation("org.flywaydb:flyway-mysql:11.7.2")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("org.xerial:sqlite-jdbc:3.49.1.0")
    implementation("org.postgresql:postgresql:42.7.5")
    
    // Database - Connection Pools (Shade this)
    implementation("com.zaxxer:HikariCP:6.3.0")
    implementation("org.hibernate.orm:hibernate-hikaricp:6.6.13.Final")

    // Jakarta Persistence API
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    
    // Logging - Make sure we use compatible versions
    implementation("org.jboss.logging:jboss-logging:3.5.3.Final")
    implementation("org.jboss.logging:jboss-logging-annotations:2.2.1.Final")
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.slf4j:slf4j-jdk14:2.0.9")
    
    // Add any additional dependencies here
    // testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

// Store version at configuration time
val projectVersion = version.toString()

// Version is automatically loaded from gradle.properties by default in recent Gradle versions
// You can access it via project.version or just 'version'
println("Initial project version from properties: $version")

// Set the project version explicitly if needed elsewhere
project.version = version.toString() 

// Task to increment the patch version in gradle.properties
val incrementPatchVersion by tasks.register("incrementPatchVersion") {
    doLast { // Use doLast to ensure file access happens at execution time
        val propsFile = project.file("gradle.properties")
        if (!propsFile.exists()) {
            throw GradleException("gradle.properties file not found!")
        }

        val props = Properties()
        // Use try-with-resources equivalent for safe file handling
        propsFile.reader(Charsets.UTF_8).use { reader ->
            props.load(reader)
        }

        val currentVersion = props.getProperty("version")
        if (currentVersion == null) {
            throw GradleException("Could not find 'version' property in gradle.properties")
        }
        println("Current version from file: $currentVersion")

        // Regex to parse Major.Minor.Patch[-Suffix]
        val versionRegex = """^(\d+)\.(\d+)\.(\d+)(.*)$""".toRegex()
        val matchResult = versionRegex.find(currentVersion)
            ?: throw GradleException("Version '$currentVersion' does not match expected Major.Minor.Patch format.")

        // Destructure the groups
        val (majorStr, minorStr, patchStr, suffix) = matchResult.destructured

        var patch = patchStr.toInt()
        patch++ // Increment the patch number

        val newVersion = "${majorStr}.${minorStr}.$patch$suffix"
        println("Incremented version to: $newVersion")

        props.setProperty("version", newVersion)
        // Write back to the properties file safely
        propsFile.writer(Charsets.UTF_8).use { writer ->
             // Pass null for comments to avoid the timestamp comment
            props.store(writer, null)
        }
    }
}

tasks {
    // Configure reobfuscation to use Mojang mappings for production
    paperweight {
        paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
    }

    // Configure shadowJar - critical for proper relocation
    shadowJar {
        enableRelocation = true
        archiveClassifier.set("all")

        // Relocate packages - include all required dependencies
        relocate("com.zaxxer.hikari", "org.clockworx.vampire.lib.hikari")
        relocate("org.hibernate", "org.clockworx.vampire.lib.hibernate")
        relocate("org.jboss.logging", "org.clockworx.vampire.lib.jboss.logging")
        relocate("jakarta.persistence", "org.clockworx.vampire.lib.jakarta.persistence")
        relocate("org.slf4j", "org.clockworx.vampire.lib.slf4j")
        relocate("org.flywaydb", "org.clockworx.vampire.lib.flywaydb")
        
        // Exclude drivers from being embedded if they are provided by server/environment
        exclude("org/sqlite/**")
        exclude("org/postgresql/**")

        // Merge service files - critical for service provider loading
        mergeServiceFiles()
    }

    

    // Configure jar task
    jar {
        manifest {
            attributes(
                "Name" to project.name,
                "Version" to provider { project.version.toString() },
                "Description" to "A modern vampire plugin for Minecraft",
                "Authors" to "MassiveCraft, ClockWorX",
                "Main" to "org.clockworx.vampire.VampirePlugin"
            )
        }
        from("LICENSE") {
            rename { "${it}_${project.name}" }
        }
        dependsOn("shadowJar")
    }

    clean {
        delete(layout.buildDirectory)
    }
    
    // Configure test task
    test {
        useJUnitPlatform()
    }
    
    // Process resources
    processResources {
        filesMatching(listOf("plugin.yml", "config.yml", "languages/**")) {
            expand(
                "version" to project.version
            )
        }
    }
}

// Ensure the 'build' task runs the increment task AFTER finishing
tasks.build {
    finalizedBy(incrementPatchVersion)
}
