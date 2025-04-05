package io.bash_psk.storage_ext.storage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

@Composable
internal fun StorageSpaceRow(title: () -> String, space: () -> String) {

    Row(
        modifier = Modifier.fillMaxWidth(fraction = 0.90f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            modifier = Modifier
                .weight(weight = 1.0f)
                .alpha(alpha = 0.75f),
            text = title(),
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            modifier = Modifier
                .weight(weight = 1.0f)
                .alpha(alpha = 0.75f),
            text = space(),
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}