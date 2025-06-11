package io.bashpsk.storageext.archive

import kotlinx.collections.immutable.ImmutableList

sealed interface ArchiveSplitState {

    data object Init : ArchiveSplitState

    data class Failed(val message: String) : ArchiveSplitState

    data class Running(
        val entryName: String,
        val filePath: String,
        val totalSize: Long,
        val savedSize: Long,
        val partCount: Int
    ) : ArchiveSplitState

    data class Finished(val paths: ImmutableList<String>): ArchiveSplitState
}