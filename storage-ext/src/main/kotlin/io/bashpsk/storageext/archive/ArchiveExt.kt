package io.bashpsk.storageext.archive

import android.util.Log
import io.bashpsk.storageext.storage.StorageExt
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.toList
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.coroutines.coroutineContext

object ArchiveExt {

    fun setMakeArchiveFile(
        destinationPath: String,
        paths: ImmutableList<String>,
        compressionLevel: CompressionLevel = CompressionLevel.NO_COMPRESSION
    ): Flow<ArchiveCompressState> {

        return flow {

            try {

                val pathFileList = paths.filter { path -> File(path).exists() }.toImmutableList()
                val folderList = pathFileList.filter { path -> File(path).isDirectory }
                val fileList = pathFileList.filter { path -> File(path).isFile }
                val totalSize = StorageExt.getFileSize(paths = pathFileList)

                val destinationFile = File(destinationPath)

                ZipOutputStream(FileOutputStream(destinationPath)).apply {

                    setLevel(compressionLevel.level)
                }.use { zipOutputStream ->

                    folderList.forEach { folder ->

                        val archiveProgressFlow = addFolderIntoArchive(
                            destinationPath = destinationPath,
                            zipOutputStream = zipOutputStream,
                            folder = folder,
                            totalSize = totalSize
                        )

                        emitAll(flow = archiveProgressFlow)
                    }

                    fileList.forEach { file ->

                        val archiveProgressFlow = addFileIntoArchive(
                            destinationPath = destinationPath,
                            zipOutputStream = zipOutputStream,
                            path = file,
                            entryName = null,
                            totalSize = totalSize
                        )

                        emitAll(flow = archiveProgressFlow)
                    }
                }

                val finished = ArchiveCompressState.Finished(
                    archiveName = destinationFile.name,
                    archivePath = destinationFile.path,
                    archiveSize = destinationFile.length()
                )

                emit(value = finished)
            } catch (exception: Exception) {

                coroutineContext.ensureActive()
                emit(value = ArchiveCompressState.Failed(message = exception.message ?: "Unknown"))
                Log.e("StorageExt", exception.message, exception)
            }
        }.flowOn(context = Dispatchers.IO)
    }

    private fun addFolderIntoArchive(
        destinationPath: String,
        zipOutputStream: ZipOutputStream,
        folder: String,
        totalSize: Long
    ): Flow<ArchiveCompressState> {

        return flow {

            try {

                val folderFile = File(folder)

                folderFile.walkTopDown().forEach { file ->

                    when {

                        file.isFile -> {

                            val entryName = "${folderFile.name}/${
                                folderFile.toURI().relativize(file.toURI()).path
                            }"

                            val runningFlow = addFileIntoArchive(
                                destinationPath = destinationPath,
                                zipOutputStream = zipOutputStream,
                                path = file.path,
                                entryName = entryName,
                                totalSize = totalSize
                            )

                            emitAll(flow = runningFlow)
                        }
                    }
                }
            } catch (exception: Exception) {

                coroutineContext.ensureActive()
                emit(value = ArchiveCompressState.Failed(message = exception.message ?: "Unknown"))
                Log.e("StorageExt", exception.message, exception)
            }
        }.flowOn(context = Dispatchers.IO)
    }

    private fun addFileIntoArchive(
        destinationPath: String,
        zipOutputStream: ZipOutputStream,
        path: String,
        entryName: String?,
        totalSize: Long
    ): Flow<ArchiveCompressState> {

        return flow {

            try {

                val destinationFile = File(destinationPath)
                val sourceFile = File(path)

                FileInputStream(path).use { fileInputStream ->

                    val zipEntry = ZipEntry(entryName ?: sourceFile.name).apply {

                        method = ZipEntry.DEFLATED
                    }

                    val running = ArchiveCompressState.Running(
                        fileName = sourceFile.name,
                        filePath = sourceFile.path,
                        totalSize = totalSize,
                        savedSize = destinationFile.length()
                    )

                    emit(value = running)
                    zipOutputStream.putNextEntry(zipEntry)
                    fileInputStream.copyTo(zipOutputStream)
                    zipOutputStream.closeEntry()
                }
            } catch (exception: Exception) {

                coroutineContext.ensureActive()
                emit(value = ArchiveCompressState.Failed(message = exception.message ?: "Unknown"))
                Log.e("StorageExt", exception.message, exception)
            }
        }.flowOn(context = Dispatchers.IO)
    }

    fun setExtractArchiveFile(
        destinationPath: String,
        path: String
    ): Flow<ArchiveExtractState> {

        return flow {

            try {

                val destinationFile = File(destinationPath)
                val sourceFile = File(path)

                ZipInputStream(FileInputStream(path)).use { zipInputStream ->

                    var zipEntry: ZipEntry?

                    while (zipInputStream.nextEntry.also { entry -> zipEntry = entry } != null) {

                        val outputFile = File(destinationPath, zipEntry!!.name)

                        val running = ArchiveExtractState.Running(
                            entryName = zipEntry.name,
                            filePath = sourceFile.path,
                            totalSize = sourceFile.length(),
                            savedSize = StorageExt.getFileSize(path = destinationFile.path)
                        )

                        emit(value = running)

                        when (zipEntry.isDirectory) {

                            true -> outputFile.mkdirs()

                            false -> {

                                outputFile.parentFile?.mkdirs()

                                FileOutputStream(outputFile).use { fileOutputStream ->

                                    zipInputStream.copyTo(fileOutputStream)
                                }
                            }
                        }

                        zipInputStream.closeEntry()
                    }
                }

                val finished = ArchiveExtractState.Finished(
                    folderName = destinationFile.name,
                    folderPath = destinationFile.path
                )

                emit(value = finished)
            } catch (exception: Exception) {

                coroutineContext.ensureActive()
                emit(value = ArchiveExtractState.Failed(message = exception.message ?: "Unknown"))
                Log.e("StorageExt", exception.message, exception)
            }
        }.flowOn(context = Dispatchers.IO)
    }

    fun setMakeSplitArchiveFile(
        destinationPath: String,
        path: String,
        chunkSize: Long
    ): Flow<ArchiveSplitState> {

        return flow {

            try {

                val destinationFile = File(destinationPath)
                val sourceFile = File(path)

                FileInputStream(path).use { fileInputStream ->

                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var bytesRead: Int
                    var partNumber = 1
                    var currentSize = 0L
                    val partCount = (sourceFile.length() / chunkSize).toInt()

                    var partFile = validatePartFile(path = sourceFile.path, partNumber = partNumber)
                    var fileOutputStream = FileOutputStream(partFile)

                    while (fileInputStream.read(buffer).also { bytesRead = it } > 0) {

                        val running = ArchiveSplitState.Running(
                            entryName = partFile.name,
                            filePath = partFile.path,
                            totalSize = sourceFile.length(),
                            savedSize = currentSize,
                            partCount = partCount
                        )

                        emit(value = running)

                        when (currentSize + bytesRead > chunkSize) {

                            true -> {

                                val spaceLeft = (chunkSize - currentSize).toInt()

                                fileOutputStream.write(buffer, 0, spaceLeft)
                                fileOutputStream.close()
                                currentSize = 0L
                                partNumber++
                                partFile = validatePartFile(sourceFile.path, partNumber)
                                fileOutputStream = FileOutputStream(partFile)
                                fileOutputStream.write(buffer, spaceLeft, bytesRead - spaceLeft)
                                currentSize += (bytesRead - spaceLeft)
                            }

                            false -> {

                                fileOutputStream.write(buffer, 0, bytesRead)
                                currentSize += bytesRead
                            }
                        }
                    }

                    val finalPartName = "${
                        sourceFile.nameWithoutExtension
                    } - Part.${sourceFile.extension}"

                    fileOutputStream.close()
                    partFile.renameTo(File(sourceFile.parent, finalPartName))
                }

                val splitFile = "${sourceFile.nameWithoutExtension} - Part.${sourceFile.extension}"

                val archivePartList = getSplitArchiveParts(
                    path = File(sourceFile.parentFile, splitFile).path
                ).toList().flatten().distinctBy { it }.toImmutableList()

                val finished = ArchiveSplitState.Finished(paths = archivePartList)

                emit(value = finished)
            } catch (exception: Exception) {

                coroutineContext.ensureActive()
                emit(value = ArchiveSplitState.Failed(message = exception.message ?: "Unknown"))
                Log.e("StorageExt", exception.message, exception)
            }
        }.flowOn(context = Dispatchers.IO)
    }

    fun setMergeSingleArchive(
        destinationPath: String,
        paths: ImmutableList<String>
    ): Flow<ArchiveMergeState> {

        return flow {

            try {

                val destinationFile = File(destinationPath)

                FileOutputStream(destinationPath).use { fileOutputStream ->

                    paths.forEach { path ->

                        val partFile = File(path)

                        val running = ArchiveMergeState.Running(
                            entryName = partFile.name,
                            filePath = partFile.path,
                            totalSize = StorageExt.getFileSize(paths),
                            savedSize = destinationFile.length(),
                            partCount = paths.size
                        )

                        emit(value = running)

                        FileInputStream(path).use { fileInputStream ->

                            fileInputStream.copyTo(fileOutputStream)
                        }
                    }
                }

                val finished = ArchiveMergeState.Finished(
                    archiveName = destinationFile.name,
                    archivePath = destinationFile.path,
                    archiveSize = destinationFile.length()
                )

                emit(value = finished)
            } catch (exception: Exception) {

                coroutineContext.ensureActive()
                emit(value = ArchiveMergeState.Failed(message = exception.message ?: "Unknown"))
                Log.e("StorageExt", exception.message, exception)
            }
        }.flowOn(context = Dispatchers.IO)
    }

    fun getSplitArchiveParts(path: String): Flow<ImmutableList<String>> {

        return flow {

            try {

                val sourceFile = File(path)

                val partFileList = sourceFile.parentFile?.listFiles()?.filter { partFile ->

                    val fileNameRegex = Regex(
                        pattern = "${Regex.escape(sourceFile.name)}\\.z\\d{2}",
                        option = RegexOption.IGNORE_CASE
                    )

                    partFile.name == sourceFile.name || partFile.name.matches(fileNameRegex)
                }?.sortedBy { file ->

                    val extension = file.extension.lowercase()

                    when {

                        extension == "zip" -> Int.MAX_VALUE
                        extension.startsWith("z") -> extension.drop(1).toIntOrNull() ?: 0
                        else -> 0
                    }
                }?.map { file ->

                    file.path
                }?.toImmutableList() ?: persistentListOf()

                emit(value = partFileList)
            } catch (exception: Exception) {

                coroutineContext.ensureActive()
                emit(value = persistentListOf())
                Log.e("StorageExt", exception.message, exception)
            }
        }.flowOn(context = Dispatchers.IO)
    }

    fun hasArchiveEncrypted(path: String): Boolean {

        return try {

            FileInputStream(path).use { fileInputStream ->

                ZipInputStream(fileInputStream).use { zipInputStream ->

                    zipInputStream.nextEntry
                    zipInputStream.nextEntry
                }
            }

            false
        } catch (exception: ZipException) {

            exception.message?.contains("encrypted", ignoreCase = true) == true
        } catch (exception: Exception) {

            Log.e("StorageExt", exception.message, exception)
            false
        }
    }

    fun hasArchiveSplit(path: String): Boolean {

        val sourceFile = File(path)

        return sourceFile.parentFile?.listFiles()?.any { partFile ->

            val fileNameRegex = Regex(
                pattern = "${Regex.escape(sourceFile.name)}\\.z\\d{2}",
                option = RegexOption.IGNORE_CASE
            )

            sourceFile.path != partFile.path && partFile.name.matches(fileNameRegex)
        } == true
    }

    private fun validatePartFile(path: String, partNumber: Int): File {

        return try {

            val sourceFile = File(path)

            File(
                sourceFile.parent,
                "${sourceFile.nameWithoutExtension} - Part.${
                    sourceFile.extension
                }.z%02d".format(partNumber)
            )
        } catch (exception: Exception) {

            Log.e("StorageExt", exception.message, exception)
            File(path)
        }
    }
}