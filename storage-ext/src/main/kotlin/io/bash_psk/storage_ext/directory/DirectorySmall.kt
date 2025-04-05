package io.bash_psk.storage_ext.directory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.bash_psk.storage_ext.storage.DirectoryData

@Composable
fun DirectorySmall(
    directoryData: () -> DirectoryData,
    onDirectoryClick: (directory: DirectoryData) -> Unit = {}
) {

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        enabled = directoryData().isFolder,
        onClick = {

            onDirectoryClick(directoryData())
        }
    ) {

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 4.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {

            Icon(
                modifier = Modifier
                    .weight(weight = 0.30f)
                    .size(size = 48.dp),
                imageVector = when (directoryData().isFolder) {

                    true -> Icons.Filled.Folder
                    false -> Icons.AutoMirrored.Filled.Article
                },
                contentDescription = "Directory Thumbnail"
            )

            Text(
                modifier = Modifier.weight(weight = 0.64f),
                text = directoryData().name,
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}