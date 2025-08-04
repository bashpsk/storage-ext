package io.bashpsk.storageext.storage

import android.util.Log
import java.io.File

enum class FileVisibleType(val label: String = "") {

    PUBLIC(label = "Public"),
    HIDDEN(label = "Hidden");

    companion object {

        fun getFileVisibleType(file: File): FileVisibleType {

            return try {

                if (file.isHidden) HIDDEN else PUBLIC
            } catch (exception: Exception) {

                Log.w("StorageExt", exception.message, exception)
                PUBLIC
            }
        }
    }
}