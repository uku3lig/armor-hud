object BuildConfig {
    const val MINECRAFT_VERSION: String = "26.1-snapshot-2"
    const val FABRIC_LOADER_VERSION: String = "0.18.4"
    const val NEOFORGE_VERSION: String = "26.1.0.0-alpha.5+snapshot-2"
    const val UKULIB_VERSION: String = "2.0.0-alpha.1+mc26.1-snapshot-2-build.293"

    const val MOD_VERSION: String = "0.11.0-alpha.1"

    const val MODRINTH_PROJECT_ID: String = "wF189hn9"

    fun createVersionString(): String {
        return "$MOD_VERSION+mc$MINECRAFT_VERSION"
    }
}