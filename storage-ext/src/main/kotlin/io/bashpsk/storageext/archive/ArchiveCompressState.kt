package io.bashpsk.storageext.archive

sealed interface ArchiveCompressState {

    data object Init : ArchiveCompressState

    data class Failed(val message: String) : ArchiveCompressState

    data class Running(
        val fileName: String,
        val filePath: String,
        val totalSize: Long,
        val savedSize: Long
    ) : ArchiveCompressState

    data class Finished(
        val archiveName: String,
        val archivePath: String,
        val archiveSize: Long
    ): ArchiveCompressState
}