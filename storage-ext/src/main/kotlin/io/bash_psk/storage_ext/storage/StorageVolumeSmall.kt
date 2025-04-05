package io.bash_psk.storage_ext.storage

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.bash_psk.empty_format.EmptyFormat
import kotlin.math.roundToInt

@SuppressLint("UnrememberedMutableState")
@Composable
fun StorageVolumeSmall(
    volumeData: () -> VolumeData,
    onStorageVolume: (volume: VolumeData) -> Unit = {}
) {

    val context = LocalContext.current

    val volumePercentage by rememberSaveable(
        volumeData(),
        volumeData().usedSize,
        volumeData().totalSize
    ) {
        mutableFloatStateOf(value = volumeData().usedSize.toFloat() / volumeData().totalSize)
    }

    val defaultContainerColor = MaterialTheme.colorScheme.primary

    val containerColors = mutableStateListOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer
    )

    val containerColor = remember { containerColors.randomOrNull() ?: defaultContainerColor }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = {

            onStorageVolume(volumeData())
        },
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
            contentColor = contentColorFor(backgroundColor = containerColor)
        ),
        shape = MaterialTheme.shapes.small
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(space = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = volumeData().name,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            StorageSpaceRow(
                title = { "Used" },
                space = { EmptyFormat.toFileSize(context = context, size = volumeData().usedSize) }
            )

            StorageSpaceRow(
                title = { "Free" },
                space = {

                    EmptyFormat.toFileSize(context = context, size = volumeData().availableSize)
                }
            )

            StorageSpaceRow(
                title = { "Total" },
                space = { EmptyFormat.toFileSize(context = context, size = volumeData().totalSize) }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
            ) {

                Text(
                    modifier = Modifier.alpha(alpha = 0.75f),
                    text = "${(volumePercentage * 100).roundToInt()}%",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(weight = 1.0f),
                    progress = {

                        volumePercentage
                    },
                    color = contentColorFor(backgroundColor = containerColor),
                    trackColor = contentColorFor(containerColor).copy(alpha = 0.35f),
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}