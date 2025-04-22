plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0-beta12"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.16"
}

group = "org.clockworx"
version = "3.1.4"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    paperweight.paperDevBundle("1.21.5-R0.1-SNAPSHOT")
    
    // Database - Core
    implementation("org.hibernate:hibernate-core:6.6.13.Final")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("org.xerial:sqlite-jdbc:3.42.0.0")
    
    // Database - Connection Pools (Shade this)
    implementation("com.zaxxer:HikariCP:5.1.0") // Keep HikariCP 
    implementation("org.hibernate:hibernate-hikaricp:6.4.1.Final") 
    
    // SQLite Dialect (Shade this)
    implementation("com.github.gwenn:sqlite-dialect:0.1.2")
    
    // Logging
    implementation("org.jboss.logging:jboss-logging:3.4.3.Final")
    implementation("org.jboss.logging:jboss-logging-annotations:2.2.1.Final")
    implementation("org.slf4j:slf4j-jdk14:2.0.13") // Use a recent 2.x version
    
    // Add any additional dependencies here
    // testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    // Configure reobfuscation to use Mojang mappings for production
    paperweight {
        paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
    }

    // Configure shadowJar
    shadowJar {
        enableRelocation = true
        archiveClassifier.set("")


    // Configure jar task
    jar {
        manifest {
            attributes(
                "Name" to project.name,
                "Version" to project.version,
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
        filesMatching(listOf("plugin.yml", "config.yml",)) {
            expand(
                "version" to project.version
            )
        }
    }
} 