package io.bash_psk.storage_ext

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

enum class FileType(
    val label: String = "",
    val extension: ImmutableList<String> = persistentListOf()
) {

    ANDROID("APK", persistentListOf("apk", "aab")),
    ARCHIVE("Archive", persistentListOf("zip", "rar", "7z", "tar", "gz", "xz")),
    AUDIO("Audio", persistentListOf("mp3", "wav", "aac", "flac", "ogg", "m4a")),
    BACK_UP("Back-Up", persistentListOf("bak", "backup", "dump", "bkp", "tmp")),
    BINARY("Binary", persistentListOf("bin", "dat", "dll", "so", "class", "sys", "config", "ini")),
    DISK_IMAGE("Disk Image", persistentListOf("iso", "img", "dmg")),
    DOCUMENT("Document", persistentListOf("doc", "docx", "odt", "rtf")),
    CODE("Code", CodeFileExtensions),
    E_BOOK("E-Book", persistentListOf("epub", "mobi", "azw3")),
    EXECUTABLE("Executable", persistentListOf("exe", "msi", "dmg", "deb")),
    FONT("Font", persistentListOf("ttf", "otf", "woff", "woff2")),
    GAME_DATA("Game Data", persistentListOf("dat", "sav", "cfg", "pak", "obb")),
    GIF("Gif", persistentListOf("gif")),
    IMAGE("Image", persistentListOf("jpg", "jpeg", "png", "bmp", "webp")),
    LOG_FILE("Log File", persistentListOf("log", "trace", "audit")),
    PDF("PDF", persistentListOf("pdf")),
    PRESENTATION("Presentation", persistentListOf("ppt", "pptx", "odp")),
    SKETCH("Sketch", persistentListOf("pde", "sketch", "ai", "psd")),
    SPREADSHEET("Spreadsheet", persistentListOf("xls", "xlsx", "csv", "ods")),
    SUB_TITLE("Sub-Title", persistentListOf("srt", "sub", "ass", "vtt", "ssa")),
    THREE_D_MODELS("3D Models", persistentListOf("blend", "obj", "fbx", "dae", "gltf", "glb")),
    TEXT("Text", persistentListOf("txt", "md", "log", "diff", "patch", "in", "out")),
    UNKNOWN("Unknown", persistentListOf()),
    VIDEO("Video", persistentListOf("mp4", "avi", "mkv", "mov", "wmv", "flv")),
    VECTOR("Vector", persistentListOf("svg", "eps", "cdr"));

    companion object {

        fun getFileType(extension: String): FileType {

            return FileType.entries.firstOrNull { fileType ->

                fileType.extension.contains(extension.lowercase())
            } ?: UNKNOWN
        }
    }
}