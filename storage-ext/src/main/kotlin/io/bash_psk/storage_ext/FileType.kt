package io.bash_psk.storage_ext

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

enum class FileType(
    val label: String = "",
    val extension: ImmutableList<String> = persistentListOf()
) {

    ARCHIVE(label = "Archive", extension = persistentListOf("zip", "rar", "7z")),
    AUDIO(label = "Audio", extension = persistentListOf("mp3", "wav", "flac")),
    DOCUMENT(label = "Document", extension = persistentListOf("pdf", "doc", "docx", "txt")),
    IMAGE(label = "Image", extension = persistentListOf("jpg", "jpeg", "png", "gif")),
    VIDEO(label = "Video", extension = persistentListOf("mp4", "avi", "mkv", "mov")),
    UNKNOWN(label = "Unknown", extension = persistentListOf())
}