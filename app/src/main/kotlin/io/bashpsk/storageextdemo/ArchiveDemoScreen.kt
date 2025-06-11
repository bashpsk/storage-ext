package io.bashpsk.storageextdemo

import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import io.bashpsk.storageext.archive.ArchiveCompressState
import io.bashpsk.storageext.archive.ArchiveExt
import io.bashpsk.storageext.archive.ArchiveExtractState
import io.bashpsk.storageext.archive.ArchiveMergeState
import io.bashpsk.storageext.archive.ArchiveSplitState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun ArchiveDemoScreen() {

    val context = LocalContext.current
    val fileCoroutineScope = rememberCoroutineScope()

    var compressState by remember { mutableStateOf<ArchiveCompressState>(ArchiveCompressState.Init) }
    var extractState by remember { mutableStateOf<ArchiveExtractState>(ArchiveExtractState.Init) }
    var splitState by remember { mutableStateOf<ArchiveSplitState>(ArchiveSplitState.Init) }
    var mergeState by remember { mutableStateOf<ArchiveMergeState>(ArchiveMergeState.Init) }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = innerPadding),
            verticalArrangement = Arrangement.spacedBy(space = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            item {

                FlowRow {

                    Button(
                        onClick = {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                                Intent(
                                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                                ).apply {

                                    data = ("package:" + context.packageName).toUri()
                                }.let(context::startActivity)
                            }
                        }
                    ) {

                        Text("MANAGE PERMISSION")
                    }

                    Button(
                        onClick = {

                            fileCoroutineScope.launch(Dispatchers.IO) {

                                val downloadRoot = Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DOWNLOADS
                                )

                                val destinationFile = File(downloadRoot, "Compressed.zip")

                                val folderFileList = downloadRoot.listFiles()?.filter { file ->

                                    file.exists() && file.extension != "zip"
                                }?.map { file ->

                                    file.path
                                }?.toImmutableList() ?: persistentListOf()

                                ArchiveExt.setMakeArchiveFile(
                                    destinationPath = destinationFile.path,
                                    paths = folderFileList
                                ).collectLatest { stateLatest ->

                                    compressState = stateLatest
                                }
                            }
                        }
                    ) {

                        Text("Start Archive Compress")
                    }

                    Button(
                        onClick = {

                            fileCoroutineScope.launch(Dispatchers.IO) {

                                val downloadRoot = Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DOWNLOADS
                                )

                                val sourceFile = File(downloadRoot, "Compressed.zip")
                                val destinationFile = File(downloadRoot, "Extracted")

                                ArchiveExt.setExtractArchiveFile(
                                    destinationPath = destinationFile.path,
                                    path = sourceFile.path
                                ).collectLatest { stateLatest ->

                                    extractState = stateLatest
                                }
                            }
                        }
                    ) {

                        Text("Start Archive Extract")
                    }

                    Button(
                        onClick = {

                            fileCoroutineScope.launch(Dispatchers.IO) {

                                val downloadRoot = Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DOWNLOADS
                                )

                                val sourceFile = File(downloadRoot, "Compressed.zip")
//                                val chunkSize = 100 * 1024L * 1024L
                                val chunkSize = 100 * 1000L * 1000L

                                ArchiveExt.setMakeSplitArchiveFile(
                                    destinationPath = downloadRoot.path,
                                    path = sourceFile.path,
                                    chunkSize = chunkSize
                                ).collectLatest { stateLatest ->

                                    splitState = stateLatest
                                }
                            }
                        }
                    ) {

                        Text("Start Archive Split")
                    }

                    Button(
                        onClick = {

                            fileCoroutineScope.launch(Dispatchers.IO) {

                                val downloadRoot = Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DOWNLOADS
                                )

                                val destinationFile = File(downloadRoot, "Merged.zip")
                                val sourceFiles = persistentListOf(
                                    File(downloadRoot, "Compressed - Part.zip.z01"),
                                    File(downloadRoot, "Compressed - Part.zip")
                                ).map { file -> file.path }.toImmutableList()

                                ArchiveExt.setMergeSingleArchive(
                                    destinationPath = destinationFile.path,
                                    paths = sourceFiles
                                ).collectLatest { stateLatest ->

                                    mergeState = stateLatest
                                }
                            }
                        }
                    ) {

                        Text("Start Archive Merge")
                    }

                    Button(
                        onClick = {

                            fileCoroutineScope.launch(Dispatchers.IO) {

                                val downloadRoot = Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DOWNLOADS
                                )

                                downloadRoot.listFiles()?.forEach { file ->

                                    when {

                                        file.isFile -> Log.d(
                                            "PSK",
                                            "HAS SPLIT : ${file.name} - ${
                                                ArchiveExt.hasArchiveSplit(file.path)
                                            }"
                                        )
                                    }
                                }
                            }
                        }
                    ) {

                        Text("Check Split Files")
                    }

                    Button(
                        onClick = {

                            fileCoroutineScope.launch(Dispatchers.IO) {

                                val downloadRoot = Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DOWNLOADS
                                )

                                downloadRoot.listFiles()?.forEach { file ->

                                    when {

                                        file.isFile -> Log.d(
                                            "PSK",
                                            "HAS ENCRYPTED : ${file.name} - ${
                                                ArchiveExt.hasArchiveEncrypted(file.path)
                                            }"
                                        )
                                    }
                                }
                            }
                        }
                    ) {

                        Text("Check Encryption Files")
                    }

                    Button(
                        onClick = {

                            fileCoroutineScope.launch(Dispatchers.IO) {

                                val downloadRoot = Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DOWNLOADS
                                )

                                val sourceFile = File(downloadRoot, "Compressed - Part.zip")

                                ArchiveExt.getSplitArchiveParts(
                                    sourceFile.path
                                ).collectLatest { filesLatest ->

                                    filesLatest.forEach { path ->

                                        Log.d("PSK", "PART FILE : ${File(path).name}")
                                    }
                                }
                            }
                        }
                    ) {

                        Text("Get Split Part Files")
                    }
                }
            }

            item {

                Text( "Compress : $compressState")
            }

            item {

                Text( "Extract : $extractState")
            }

            item {

                Text( "Split : $splitState")
            }

            item {

                Text( "Merge : $mergeState")
            }
        }
    }
}