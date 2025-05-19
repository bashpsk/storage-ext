package io.bash_psk.storage_ext

enum class FileVisibleType(val label: String = "") {

    PUBLIC(label = "Public"),
    HIDDEN(label = "Hidden"),
    UNKNOWN(label = "Unknown")
}