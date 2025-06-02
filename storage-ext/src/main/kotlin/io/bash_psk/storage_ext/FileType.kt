package io.bash_psk.storage_ext

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

enum class FileType(
    val label: String = "",
    val extension: ImmutableList<String> = persistentListOf()
) {

    ARCHIVE("Archive", persistentListOf("zip", "rar", "7z", "tar", "gz", "xz")),
    AUDIO("Audio", persistentListOf("mp3", "wav", "aac", "flac", "ogg", "m4a")),
    BACK_UP("Back-Up", persistentListOf("bak", "backup", "dump", "bkp", "tmp")),
    BINARY("Binary", persistentListOf("bin", "dat", "dll", "so", "class")),
    DATABASE("Database", persistentListOf("db", "sql", "sqlite")),
    DISK_IMAGE("Disk Image", persistentListOf("iso", "img", "dmg")),
    DOCUMENT("Document", persistentListOf("pdf", "doc", "docx", "odt", "rtf")),
    CAD_FILE("CAD File", persistentListOf("dwg", "dxf", "stl", "eps")),
    CODE("Code", CodeFileExtensions),
    E_BOOK("E-Book", persistentListOf("epub", "mobi", "azw3")),
    EXECUTABLE("Executable", persistentListOf("exe", "apk")),
    FONT("Font", persistentListOf("ttf", "otf", "woff", "woff2")),
    GAME_DATA_FILE("Game Data File", persistentListOf("dat", "sav", "cfg", "ini", "pak", "obb")),
    GIS_MAPS("GIS Maps", persistentListOf("kml", "kmz", "gpx", "geojson")),
    IMAGE("Image", persistentListOf("jpg", "jpeg", "png", "gif", "bmp", "svg", "webp")),
    LOG_FILE("Log File", persistentListOf("log", "trace", "audit")),
    PRESENTATION("Presentation", persistentListOf("ppt", "pptx", "odp")),
    SPREADSHEET("Spreadsheet", persistentListOf("xls", "xlsx", "csv", "ods")),
    SYSTEM("System", persistentListOf("dll", "sys", "config", "ini")),
    SUB_TITLE("Sub-Title", persistentListOf("srt", "sub", "ass", "vtt", "ssa")),
    THREE_D_MODELS("3D Models", persistentListOf("blend", "obj", "fbx", "dae", "gltf", "glb")),
    UNKNOWN("Unknown", persistentListOf()),
    VIDEO("Video", persistentListOf("mp4", "avi", "mkv", "mov", "wmv", "flv"))
}