package io.bash_psk.storage_ext

fun hasValidFolderName(name: String): Boolean {

    val folderRegex = Regex("[<>:\"/\\\\|?*]")
    val ignoreDotRegex = Regex("^\\.")

    return !name.contains(folderRegex) && !name.matches(ignoreDotRegex) && name.length <= 127
}