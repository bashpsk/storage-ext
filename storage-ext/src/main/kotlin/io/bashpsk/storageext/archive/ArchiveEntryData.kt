package io.bashpsk.storageext.archive

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Immutable
@Parcelize
@Serializable
data class ArchiveEntryData(
    val title: String
) : Parcelable