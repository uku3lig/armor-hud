plugins {
    id("multiloader-platform")
    id("net.fabricmc.fabric-loom")
}

repositories {
    maven {
        url = uri("https://api.modrinth.com/maven")
        content {
            includeGroup("maven.modrinth")
        }
    }
}

loom {
    runs.named("client") {
        client()

        configName = "fabric - Client"
        runDir = "../run"
        appendProjectPathToConfigName = false
        ideConfigGenerated(true)
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${BuildConfig.MINECRAFT_VERSION}")
    implementation("net.fabricmc:fabric-loader:${BuildConfig.FABRIC_LOADER_VERSION}")

    api("net.uku3lig:ukulib-fabric:${BuildConfig.UKULIB_VERSION}")

    // compileOnly("maven.modrinth:bedrockify:${project.property("bedrockify_version")}")
}

modrinth {
    loaders.add("quilt")
}