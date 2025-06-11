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
data class StorageVolumeData(
    val uuid: String = Uuid.random().toString(),
    val title: String = "",
    val path: String = "",
    val totalSize: Long = 0L,
    val availableSize: Long = 0L,
    val usedSize: Long = 0L,
    val volumeType: StorageVolumeType = StorageVolumeType.UNKNOWN
) : Parcelable