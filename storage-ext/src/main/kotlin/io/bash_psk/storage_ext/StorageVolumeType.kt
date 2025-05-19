package io.bash_psk.storage_ext

enum class StorageVolumeType(val label: String = "") {

    INTERNAL(label = "Internal"),
    SD_CARD(label = "SD Card"),
    OTG(label = "OTG"),
    UNKNOWN(label = "Unknown")
}