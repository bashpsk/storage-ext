package io.bash_psk.storage_ext.storage

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
    val parent: String = "",
    val isFolder: Boolean = false
) : Parcelable