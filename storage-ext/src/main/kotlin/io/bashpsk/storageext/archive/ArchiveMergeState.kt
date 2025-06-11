package io.bashpsk.storageext.archive

sealed interface ArchiveMergeState {

    data object Init : ArchiveMergeState

    data class Failed(val message: String) : ArchiveMergeState

    data class Running(
        val entryName: String,
        val filePath: String,
        val totalSize: Long,
        val savedSize: Long,
        val partCount: Int
    ) : ArchiveMergeState

    data class Finished(
        val archiveName: String,
        val archivePath: String,
        val archiveSize: Long
    ): ArchiveMergeState
}