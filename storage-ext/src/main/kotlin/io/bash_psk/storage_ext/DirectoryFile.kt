package io.bash_psk.storage_ext

sealed class DirectoryFile() {

    data class File(
        val name: String = "",
        val path: String = "",
        val extension: String = "",
        val visibleType: FileVisibleType = FileVisibleType.UNKNOWN,
        val fileType: FileType = FileType.UNKNOWN
    ) : DirectoryFile()

    data class Folder(
        val name: String = "",
        val path: String = "",
        val visibleType: FileVisibleType = FileVisibleType.UNKNOWN
    ) : DirectoryFile()
}