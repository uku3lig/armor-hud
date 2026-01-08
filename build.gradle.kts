plugins {
    id("net.fabricmc.fabric-loom") version "1.14-SNAPSHOT"
    id("io.freefair.lombok") version "9.1.0"
}

version = "${project.property("mod_version")}+mc${project.property("minecraft_version")}"
group = project.property("maven_group") as String

repositories {
    maven {
        url = uri("https://maven.uku3lig.net/releases")
    }
    maven {
        url = uri("https://maven.uku3lig.net/snapshots") // TODO remove
    }
    maven {
        url = uri("https://api.modrinth.com/maven")
        content {
            includeGroup("maven.modrinth")
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    implementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")

    api("net.uku3lig:ukulib-fabric:${project.property("ukulib_version")}")

    // compileOnly("maven.modrinth:bedrockify:${project.property("bedrockify_version")}")
}

base {
    archivesName = project.property("archives_base_name") as String
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.processResources {
    inputs.property("version", project.version)
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName.get()}" }
    }
}