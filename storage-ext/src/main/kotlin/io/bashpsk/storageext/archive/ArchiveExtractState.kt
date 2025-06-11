package io.bashpsk.storageext.archive

sealed interface ArchiveExtractState {

    data object Init : ArchiveExtractState

    data class Failed(val message: String) : ArchiveExtractState

    data class Running(
        val entryName: String,
        val filePath: String,
        val totalSize: Long,
        val savedSize: Long
    ) : ArchiveExtractState

    data class Finished(
        val folderName: String,
        val folderPath: String
    ): ArchiveExtractState
}