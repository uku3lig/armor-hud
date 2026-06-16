object BuildConfig {
    const val MINECRAFT_VERSION: String = "26.2"
    const val FABRIC_LOADER_VERSION: String = "0.19.3"
    const val NEOFORGE_VERSION: String = "26.2.0.0-beta"
    const val UKULIB_VERSION: String = "2.1.0+26.2"
    const val BEDROCKIFY_VERSION: String = "1.11.7+mc26.1"

    const val MOD_VERSION: String = "0.11.0"

    const val MODRINTH_PROJECT_ID: String = "wF189hn9"

    fun createVersionString(): String {
        return "$MOD_VERSION+mc$MINECRAFT_VERSION"
    }
}