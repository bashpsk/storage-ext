package io.bash_psk.storage_ext

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
data class FileData(
    val uuid: String = Uuid.random().toString(),
    val title: String = "",
    val path: String = "",
    val uri: String = "",
    val extension: String = "",
    val visibleType: FileVisibleType = FileVisibleType.PUBLIC,
    val fileType: FileType = FileType.UNKNOWN,
    val size: Long = 0L,
    val modifiedDate: Long = 0L,
    val storage: StorageVolumeData = StorageVolumeData()
) : Parcelable