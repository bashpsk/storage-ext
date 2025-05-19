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
data class DirectoryData(
    val uuid: String = Uuid.random().toString(),
    val name: String = "",
    val path: String = "",
    val uri: String = "",
    val visibleType: FileVisibleType = FileVisibleType.UNKNOWN,
    val folders: Int = 0,
    val files: Int = 0,
    val modifiedDate: Long = 0L
) : Parcelable