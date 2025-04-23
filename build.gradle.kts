plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0-beta12"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.16"
}

group = "org.clockworx"
version = "3.1.10"

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
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("org.xerial:sqlite-jdbc:3.49.1.0")
    
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

tasks {
    // Configure reobfuscation to use Mojang mappings for production
    paperweight {
        paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
    }

    // Configure shadowJar - critical for proper relocation
    shadowJar {
        enableRelocation = false
        archiveClassifier.set("all")

        // Relocate packages - include all required dependencies
        relocate("com.zaxxer.hikari", "org.clockworx.vampire.lib.hikari")
        relocate("org.hibernate", "org.clockworx.vampire.lib.hibernate")
        relocate("org.jboss.logging", "org.clockworx.vampire.lib.jboss.logging")
        relocate("jakarta.persistence", "org.clockworx.vampire.lib.jakarta.persistence")
        relocate("org.slf4j", "org.clockworx.vampire.lib.slf4j")
        
        // Exclude SQLite JDBC properly
        exclude("org/sqlite/**")

        // Merge service files - critical for service provider loading
        mergeServiceFiles()
    }

    // Configure jar task
    jar {
        manifest {
            attributes(
                "Name" to project.name,
                "Version" to projectVersion,
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
                "version" to projectVersion
            )
        }
    }
} 