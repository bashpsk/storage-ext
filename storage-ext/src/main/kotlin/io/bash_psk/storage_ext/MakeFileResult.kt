package io.bash_psk.storage_ext

sealed class MakeFileResult {

    data class Failed(val message: String) : MakeFileResult()

    data class Exist(val directoryFile: DirectoryFile) : MakeFileResult()

    data class Success(val directoryFile: DirectoryFile) : MakeFileResult()
}