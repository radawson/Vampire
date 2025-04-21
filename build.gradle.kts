plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.16"
}

group = "org.clockworx"
version = "3.1.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    paperweight.paperDevBundle("1.21.5-R0.1-SNAPSHOT")
    
    // Database - Core
    implementation("org.hibernate:hibernate-core:6.4.1.Final")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("org.xerial:sqlite-jdbc:3.42.0.0")
    
    // Database - Connection Pools
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.hibernate:hibernate-c3p0:6.4.1.Final")
    implementation("org.hibernate:hibernate-hikaricp:6.4.1.Final")
    
    // SQLite Dialect
    implementation("com.github.gwenn:sqlite-dialect:0.1.2")
    
    // Logging
    implementation("org.jboss.logging:jboss-logging:3.4.3.Final")
    implementation("org.jboss.logging:jboss-logging-annotations:2.2.1.Final")
    
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
        archiveClassifier.set("")
        minimize()
    }

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
        filesMatching("plugin.yml") {
            expand(
                "version" to project.version
            )
        }
    }
} 