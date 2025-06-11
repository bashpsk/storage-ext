package io.bashpsk.storageext.storage

fun hasValidFolderName(name: String): Boolean {

    val folderRegex = Regex("[<>:\"/\\\\|?*]")
    val ignoreDotRegex = Regex("^\\.")

    return !name.contains(folderRegex) && !name.matches(ignoreDotRegex) && name.length <= 127
}