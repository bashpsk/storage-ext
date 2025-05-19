package io.bash_psk.storage_ext

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.util.Log
import androidx.core.text.isDigitsOnly
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File

object StorageExt {

    fun getStorageVolumes(context: Context): Flow<ImmutableList<StorageVolumeData>> {

        return flow {

            val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager

            val storageVolumeList = try {

                when {

                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {

                        storageManager.storageVolumes.map { volume ->

                            val path = volume.directory?.path ?: ""

                            StorageVolumeData(
                                name = volume.getDescription(context),
                                path = path,
                                totalSize = getTotalMemory(path = path),
                                availableSize = getFreeMemory(path = path),
                                usedSize = getUsedMemory(path = path),
                                volumeType = getVolumeType(volume = volume)
                            )
                        }.toImmutableList()
                    }

                    else -> {

                        getStorageDirectories(context = context).map { path ->

                            storageManager.getStorageVolume(File(path))?.let { volume ->

                                StorageVolumeData(
                                    name = volume.getDescription(context),
                                    path = path,
                                    totalSize = getTotalMemory(path = path),
                                    availableSize = getFreeMemory(path = path),
                                    usedSize = getUsedMemory(path = path),
                                    volumeType = getVolumeType(volume = volume)
                                )
                            } ?: StorageVolumeData()
                        }.toImmutableList()
                    }
                }.filter { volumeData -> volumeData.path.isNotEmpty() }.toImmutableList()
            } catch (exception: Exception) {

                Log.e("StorageExt", exception.message, exception)
                persistentListOf()
            }

            emit(value = storageVolumeList)
        }.flowOn(context = Dispatchers.IO)
    }

    fun getDirectoryFiles(path: String): Flow<ImmutableList<DirectoryFile>> {

        return flow {

            val directoryFileList = try {

                File(path).listFiles()?.map { file ->

                    when (file.isFile) {

                        true -> DirectoryFile.File(
                            name = file.name,
                            path = file.path,
                            extension = file.extension,
                            visibleType = getFileVisibleType(file = file),
                            fileType = getFileType(file = file)
                        )

                        false -> DirectoryFile.Folder(
                            name = file.name,
                            path = file.path,
                            visibleType = getFileVisibleType(file = file)
                        )
                    }
                }?.toImmutableList() ?: persistentListOf()
            } catch (exception: Exception) {

                Log.e("StorageExt", exception.message, exception)
                persistentListOf()
            }

            emit(value = directoryFileList)
        }.flowOn(context = Dispatchers.IO)
    }

    fun makeFolderOrFile(
        parentPath: String,
        name: String,
        isFolder: Boolean,
        visibleType: FileVisibleType = FileVisibleType.PUBLIC
    ): MakeFileResult {

        return try {

            val sourceFile = when (visibleType) {

                FileVisibleType.PUBLIC -> File(parentPath, name)
                FileVisibleType.HIDDEN -> File(parentPath, ".$name")
                FileVisibleType.UNKNOWN -> File(parentPath, name)
            }

            when (sourceFile.exists()) {

                true -> {

                    val directoryFile = when (sourceFile.isFile) {

                        true -> DirectoryFile.File(
                            name = sourceFile.name,
                            path = sourceFile.path,
                            extension = sourceFile.extension,
                            visibleType = getFileVisibleType(file = sourceFile),
                            fileType = getFileType(file = sourceFile)
                        )

                        false -> DirectoryFile.Folder(
                            name = sourceFile.name,
                            path = sourceFile.path,
                            visibleType = getFileVisibleType(file = sourceFile)
                        )
                    }

                    MakeFileResult.Exist(directoryFile = directoryFile)
                }

                false -> {

                    val result = when (isFolder) {

                        true -> sourceFile.mkdirs()
                        false -> sourceFile.createNewFile()
                    }

                    val directoryFile = when (sourceFile.isFile) {

                        true -> DirectoryFile.File(
                            name = sourceFile.name,
                            path = sourceFile.path,
                            extension = sourceFile.extension,
                            visibleType = getFileVisibleType(file = sourceFile),
                            fileType = getFileType(file = sourceFile)
                        )

                        false -> DirectoryFile.Folder(
                            name = sourceFile.name,
                            path = sourceFile.path,
                            visibleType = getFileVisibleType(file = sourceFile)
                        )
                    }

                    when (result) {

                        true -> MakeFileResult.Success(directoryFile = directoryFile)
                        false -> MakeFileResult.Failed(message = "Directory File Does Not Created!")
                    }
                }
            }
        } catch (exception: Exception) {

            Log.e("StorageExt", exception.message, exception)
            MakeFileResult.Failed(message = exception.message ?: "Unknown Error")
        }
    }

    private fun getStorageDirectories(context: Context): ImmutableList<String> {

        return try {

            val emulatedStorage = System.getenv("EMULATED_STORAGE_TARGET")
            val availableDirectories = hashSetOf<String>()

            when (emulatedStorage?.isNotEmpty() == true) {

                true -> getEmulatedStorageTarget()?.let(availableDirectories::add)
                false -> availableDirectories.addAll(elements = getExternalStorage(context = context))
            }

            availableDirectories.addAll(elements = getAllSecondaryStorages().toList())
            availableDirectories.toImmutableList()
        } catch (exception: Exception) {

            Log.e("StorageExt", exception.message, exception)
            persistentListOf()
        }
    }

    private fun getExternalStorage(context: Context): ImmutableList<String> {

        return getExternalFilesDirs(context = context).map { file ->

            file.absolutePath.substring(0, file.absolutePath.indexOf(string = "Android/data"))
        }.toImmutableList()
    }

    private fun getEmulatedStorageTarget(): String? {

        return try {

            val emulatedStorage = System.getenv("EMULATED_STORAGE_TARGET")
            val path = Environment.getExternalStorageDirectory().absolutePath
            val folders = path.split(File.separator)
            val lastSegment = folders.lastOrNull()
            val isNotEmpty = lastSegment?.isNotEmpty() == true
            val isDigit = lastSegment?.isDigitsOnly() == true
            var rawStorageId = ""

            when {

                isNotEmpty && isDigit -> rawStorageId = lastSegment
            }

            when (rawStorageId.isEmpty()) {

                true -> emulatedStorage
                else -> "$emulatedStorage/$rawStorageId"
            }
        } catch (exception: Exception) {

            Log.e("StorageExt", exception.message, exception)
            null
        }
    }

    private fun getAllSecondaryStorages(): ImmutableList<String> {

        return try {

            val secondaryStorage = System.getenv("SECONDARY_STORAGE")

            when (secondaryStorage?.isNotEmpty() == true) {

                true -> secondaryStorage.split(File.pathSeparator).toImmutableList()
                else -> persistentListOf()
            }
        } catch (exception: Exception) {

            Log.e("StorageExt", exception.message, exception)
            persistentListOf()
        }
    }

    private fun getExternalFilesDirs(context: Context): ImmutableList<File> {

        return context.getExternalFilesDirs(null).toList().toImmutableList()
    }

    private fun getVolumeType(volume: StorageVolume): StorageVolumeType {

        return when {

            volume.isPrimary -> StorageVolumeType.INTERNAL
            volume.isRemovable -> StorageVolumeType.SD_CARD
            else -> StorageVolumeType.OTG
        }
    }

    fun getFileVisibleType(file: File): FileVisibleType {

        return when (file.isHidden) {

            true -> FileVisibleType.HIDDEN
            false -> FileVisibleType.PUBLIC
        }
    }

    fun getFileType(file: File): FileType {

        return FileType.entries.firstOrNull { fileType ->

            fileType.extension.contains(file.extension)
        } ?: FileType.UNKNOWN
    }

    fun getTotalMemory(path: String): Long {

        return try {

            StatFs(path).blockCountLong * StatFs(path).blockSizeLong
        } catch (exception: Exception) {

            Log.e("StorageExt", exception.message, exception)
            0L
        }
    }

    fun getFreeMemory(path: String): Long {

        return try {

            StatFs(path).availableBlocksLong * StatFs(path).blockSizeLong
        } catch (exception: Exception) {

            Log.e("StorageExt", exception.message, exception)
            0L
        }
    }

    fun getUsedMemory(path: String): Long {

        return try {

            getTotalMemory(path = path) - getFreeMemory(path = path)
        } catch (exception: Exception) {

            Log.e("StorageExt", exception.message, exception)
            0L
        }
    }
}