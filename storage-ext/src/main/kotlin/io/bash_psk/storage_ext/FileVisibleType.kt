package io.bash_psk.storage_ext

import android.util.Log
import java.io.File

enum class FileVisibleType(val label: String = "") {

    PUBLIC(label = "Public"),
    HIDDEN(label = "Hidden");

    companion object {

        fun getFileVisibleType(file: File): FileVisibleType {

            return try {

                when (file.isHidden) {

                    true -> HIDDEN
                    false -> PUBLIC
                }
            } catch (exception: Exception) {

                Log.w("StorageExt", exception.message, exception)
                PUBLIC
            }
        }
    }
}