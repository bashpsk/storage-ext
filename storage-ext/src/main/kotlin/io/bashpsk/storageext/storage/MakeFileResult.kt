package io.bashpsk.storageext.storage

sealed class MakeFileResult {

    data class Failed(val message: String) : MakeFileResult()

    data class Exist(val path: String, val name: String) : MakeFileResult()

    data class Success(val path: String, val name: String) : MakeFileResult()
}