package io.bashpsk.storageext.storage

import android.os.storage.StorageVolume

enum class StorageVolumeType(val label: String = "") {

    INTERNAL(label = "Internal"),
    SD_CARD(label = "SD Card"),
    OTG(label = "OTG"),
    UNKNOWN(label = "Unknown");

    companion object {

        fun getVolumeType(volume: StorageVolume): StorageVolumeType {

            return when {

                volume.isPrimary -> INTERNAL
                volume.isRemovable -> SD_CARD
                else -> OTG
            }
        }
    }
}