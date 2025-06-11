package io.bashpsk.storageext.archive

enum class CompressionLevel(val label: String, val level: Int) {

    NO_COMPRESSION(label = "No Compression", 0),
    SUPER_FAST(label = "Super Fast", 1),
    FAST(label = "Fast", 3),
    MEDIUM(label = "Medium", 5),
    DEFAULT(label = "Default", 6),
    HIGH(label = "High", 7),
    ULTRA(label = "Ultra", 9);
}