package io.bash_psk.storage_ext.storage

import android.content.Context
import android.os.Environment
import android.os.StatFs
import androidx.core.text.isDigitsOnly
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import java.io.File

object StorageExt {

    private val EXTERNAL_STORAGE = System.getenv("EXTERNAL_STORAGE")
    private val SECONDARY_STORAGES = System.getenv("SECONDARY_STORAGE")
    private val EMULATED_STORAGE_TARGET = System.getenv("EMULATED_STORAGE_TARGET")

    fun getStorageDirectories(context: Context): ImmutableList<String> {

        val availableDirectoriesSet = hashSetOf<String>()

        when (EMULATED_STORAGE_TARGET?.isNotEmpty() == true) {

            true -> availableDirectoriesSet.add(element = getEmulatedStorageTarget())
            false -> availableDirectoriesSet.addAll(elements = getExternalStorage(context = context))
        }

        availableDirectoriesSet.addAll(elements = getAllSecondaryStorages().toList())

        return availableDirectoriesSet.toImmutableList()
    }

    private fun getExternalStorage(context: Context): ImmutableList<String> {

        return getExternalFilesDirs(context = context).map { file ->

            file.absolutePath.substring(
                startIndex = 0,
                endIndex = file.absolutePath.indexOf(string = "Android/data")
            )
        }.toImmutableList()
    }

    private fun getEmulatedStorageTarget(): String {

        var rawStorageId = ""
        val path = Environment.getExternalStorageDirectory().absolutePath
        val folders = path.split(File.separator)
        val lastSegment = folders.lastOrNull()
        val isNotEmpty = lastSegment?.isNotEmpty() == true
        val isDigit = lastSegment?.isDigitsOnly() == true

        when {

            isNotEmpty && isDigit -> rawStorageId = lastSegment
        }

        return when (rawStorageId.isEmpty()) {

            true -> EMULATED_STORAGE_TARGET
            else -> "$EMULATED_STORAGE_TARGET/$rawStorageId"
        }
    }

    private fun getAllSecondaryStorages(): ImmutableList<String> {

        return when (SECONDARY_STORAGES?.isNotEmpty() == true) {

            true -> SECONDARY_STORAGES.split(File.pathSeparator).toImmutableList()
            else -> persistentListOf()
        }
    }

    private fun getExternalFilesDirs(context: Context): ImmutableList<File> {

        return context.getExternalFilesDirs(null).toList().toImmutableList()
    }

    fun getTotalMemory(path: String): Long {

        return StatFs(path).blockCountLong * StatFs(path).blockSizeLong
    }

    fun getFreeMemory(path: String): Long {

        return StatFs(path).availableBlocksLong * StatFs(path).blockSizeLong
    }

    fun getUsedMemory(path: String): Long {

        return getTotalMemory(path = path) - getFreeMemory(path = path)
    }
}