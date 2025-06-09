package io.bash_psk.storage_ext

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.util.Log
import androidx.core.net.toUri
import androidx.core.text.isDigitsOnly
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.coroutineContext

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
                                title = volume.getDescription(context),
                                path = path,
                                totalSize = getTotalMemory(path = path),
                                availableSize = getFreeMemory(path = path),
                                usedSize = getUsedMemory(path = path),
                                volumeType = StorageVolumeType.getVolumeType(volume = volume)
                            )
                        }.toImmutableList()
                    }

                    else -> {

                        getStorageDirectories(context = context).map { path ->

                            storageManager.getStorageVolume(File(path))?.let { volume ->

                                StorageVolumeData(
                                    title = volume.getDescription(context),
                                    path = path,
                                    totalSize = getTotalMemory(path = path),
                                    availableSize = getFreeMemory(path = path),
                                    usedSize = getUsedMemory(path = path),
                                    volumeType = StorageVolumeType.getVolumeType(volume = volume)
                                )
                            } ?: StorageVolumeData()
                        }.toImmutableList()
                    }
                }.filter { volumeData -> volumeData.path.isNotEmpty() }.toImmutableList()
            } catch (exception: Exception) {

                coroutineContext.ensureActive()
                Log.w("StorageExt", exception.message, exception)
                persistentListOf()
            }

            emit(value = storageVolumeList)
        }.flowOn(context = Dispatchers.IO)
    }

    fun getDirectoryFiles(context: Context, path: String): Flow<DirectoryFileData> {

        return flow {

            val folderList = MutableStateFlow(value = persistentListOf<DirectoryData>())
            val fileList = MutableStateFlow(value = persistentListOf<FileData>())

            try {

                val storageVolumeList = getStorageVolumes(
                    context = context
                ).toList().flatten().distinctBy { storage -> storage.path }.toImmutableList()

                File(path).listFiles()?.forEach { fileItem ->

                    when (fileItem.isFile) {

                        true -> getFileData(
                            file = fileItem,
                            storageVolumes = storageVolumeList
                        )?.let { newFileData ->

                            fileList.update { filesOld -> filesOld.add(element = newFileData) }
                        }

                        false -> getDirectoryData(
                            file = fileItem,
                            storageVolumes = storageVolumeList
                        )?.let { newDirectoryData ->

                            folderList.update { foldersOld -> foldersOld.add(newDirectoryData) }
                        }
                    }
                }

                val directoryData = getDirectoryData(
                    path = path,
                    storageVolumes = storageVolumeList
                ) ?: DirectoryData()

                val storageVolume = findStorageVolumeData(
                    path = path,
                    storageVolumes = storageVolumeList
                ) ?: StorageVolumeData()

                val newDirectoryFileData = DirectoryFileData(
                    folders = folderList.value.toImmutableList(),
                    files = fileList.value.toImmutableList(),
                    storage = storageVolume,
                    directory = directoryData
                )

                emit(value = newDirectoryFileData)
            } catch (exception: Exception) {

                val newDirectoryFileData = DirectoryFileData()

                coroutineContext.ensureActive()
                Log.w("StorageExt", exception.message, exception)
                emit(value = newDirectoryFileData)
            }
        }.flowOn(context = Dispatchers.IO)
    }

    fun findStorageVolumeData(
        path: String,
        storageVolumes: ImmutableList<StorageVolumeData> = persistentListOf()
    ): StorageVolumeData? {

        return findStorageVolumeData(file = File(path), storageVolumes = storageVolumes)
    }

    fun findStorageVolumeData(
        file: File,
        storageVolumes: ImmutableList<StorageVolumeData> = persistentListOf()
    ): StorageVolumeData? {

        return storageVolumes.find { storage -> file.path.startsWith(storage.path) }
    }

    fun getDirectoryData(
        path: String,
        storageVolumes: ImmutableList<StorageVolumeData> = persistentListOf()
    ): DirectoryData? {

        return getDirectoryData(file = File(path), storageVolumes = storageVolumes)
    }

    fun getDirectoryData(
        file: File,
        storageVolumes: ImmutableList<StorageVolumeData> = persistentListOf()
    ): DirectoryData? {

        return try {

            val storageVolume = findStorageVolumeData(
                file = file,
                storageVolumes = storageVolumes
            ) ?: StorageVolumeData()

            val folders = file.listFiles()?.count { folder -> folder.isDirectory } ?: 0
            val files = file.listFiles()?.count { file -> file.isFile } ?: 0

            DirectoryData(
                title = file.name,
                path = file.path,
                uri = file.toUri().toString(),
                visibleType = FileVisibleType.getFileVisibleType(file = file),
                folders = folders,
                files = files,
                modifiedDate = file.lastModified(),
                storage = storageVolume
            )
        } catch (exception: Exception) {

            Log.w("StorageExt", exception.message, exception)
            null
        }
    }

    fun getFileData(
        path: String,
        storageVolumes: ImmutableList<StorageVolumeData> = persistentListOf()
    ): FileData? {

        return getFileData(file = File(path), storageVolumes = storageVolumes)
    }

    fun getFileData(
        file: File,
        storageVolumes: ImmutableList<StorageVolumeData> = persistentListOf()
    ): FileData? {

        return try {

            val storageVolume = findStorageVolumeData(
                file = file,
                storageVolumes = storageVolumes
            ) ?: StorageVolumeData()

            FileData(
                title = file.name,
                path = file.path,
                uri = file.toUri().toString(),
                extension = file.extension,
                visibleType = FileVisibleType.getFileVisibleType(file = file),
                fileType = FileType.getFileType(extension = file.extension),
                size = file.length(),
                modifiedDate = file.lastModified(),
                storage = storageVolume
            )
        } catch (exception: Exception) {

            Log.w("StorageExt", exception.message, exception)
            null
        }
    }

    suspend fun makeFolderOrFile(
        parentPath: String,
        name: String,
        isFolder: Boolean,
        visibleType: FileVisibleType = FileVisibleType.PUBLIC,
        onFileResult: (result: MakeFileResult) -> Unit
    ) {

        withContext(context = Dispatchers.IO) {

            try {

                val fileName = name.trimStart { it == '.' }

                val sourceFile = when (visibleType) {

                    FileVisibleType.PUBLIC -> File(parentPath, fileName)
                    FileVisibleType.HIDDEN -> File(parentPath, ".$fileName")
                }

                val result = when (sourceFile.exists()) {

                    true -> MakeFileResult.Exist(path = sourceFile.path, name = sourceFile.name)

                    false -> {

                        val result = when (isFolder) {

                            true -> sourceFile.mkdirs()
                            false -> sourceFile.createNewFile()
                        }

                        when (result) {

                            true -> MakeFileResult.Success(sourceFile.path, sourceFile.name)
                            false -> MakeFileResult.Failed("Directory File Does Not Created!")
                        }
                    }
                }

                onFileResult(result)
            } catch (exception: Exception) {

                coroutineContext.ensureActive()
                Log.e("StorageExt", exception.message, exception)
                onFileResult(MakeFileResult.Failed(message = exception.message ?: "Unknown Error"))
            }
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

            Log.w("StorageExt", exception.message, exception)
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

            Log.w("StorageExt", exception.message, exception)
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

            Log.w("StorageExt", exception.message, exception)
            persistentListOf()
        }
    }

    private fun getExternalFilesDirs(context: Context): ImmutableList<File> {

        return context.getExternalFilesDirs(null).toList().toImmutableList()
    }

    fun getTotalMemory(path: String): Long {

        return getTotalMemory(file = File(path))
    }

    fun getTotalMemory(file: File): Long {

        return try {

            file.totalSpace
        } catch (exception: Exception) {

            Log.w("StorageExt", exception.message, exception)
            0L
        }
    }

    fun getFreeMemory(path: String): Long {

        return getFreeMemory(file = File(path))
    }

    fun getFreeMemory(file: File): Long {

        return try {

            file.freeSpace
        } catch (exception: Exception) {

            Log.w("StorageExt", exception.message, exception)
            0L
        }
    }

    fun getUsedMemory(path: String): Long {

        return getUsedMemory(file = File(path))
    }

    fun getUsedMemory(file: File): Long {

        return try {

            file.totalSpace - file.freeSpace
        } catch (exception: Exception) {

            Log.w("StorageExt", exception.message, exception)
            0L
        }
    }
}