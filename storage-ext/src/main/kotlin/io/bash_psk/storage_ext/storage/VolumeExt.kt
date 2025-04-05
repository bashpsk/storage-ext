package io.bash_psk.storage_ext.storage

val VolumeData.toDirectoryData: DirectoryData
    get() = DirectoryData(name = name, path = path, parent = path, isFolder = true)