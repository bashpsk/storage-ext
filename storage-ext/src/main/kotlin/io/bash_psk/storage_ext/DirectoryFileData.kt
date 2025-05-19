package io.bash_psk.storage_ext

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Immutable
@Parcelize
@Serializable
data class DirectoryFileData(
    val folders: ImmutableList<DirectoryData> = persistentListOf(),
    val files: ImmutableList<FileData> = persistentListOf()
) : Parcelable