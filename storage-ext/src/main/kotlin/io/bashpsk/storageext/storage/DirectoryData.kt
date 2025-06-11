package io.bashpsk.storageext.storage

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Immutable
@Parcelize
@Serializable
data class DirectoryData(
    val uuid: String = Uuid.random().toString(),
    val title: String = "",
    val path: String = "",
    val uri: String = "",
    val visibleType: FileVisibleType = FileVisibleType.PUBLIC,
    val folders: Int = 0,
    val files: Int = 0,
    val modifiedDate: Long = 0L,
    val storage: StorageVolumeData = StorageVolumeData()
) : Parcelable