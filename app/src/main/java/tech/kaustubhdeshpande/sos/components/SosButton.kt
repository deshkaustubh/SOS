package tech.kaustubhdeshpande.sos.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SosButton(
    isActive: Boolean,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {

    val sosString = if (isActive) "STOP SOS" else "SEND SOS"
    val sosIcon = if (isActive) Icons.Filled.Close else Icons.Filled.LocationOn

    Box(
        modifier = modifier
            .size(200.dp)
            .combinedClickable(
                onClick = {},
                onLongClick = onLongPress
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFef4444),
            tonalElevation = 6.dp
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Icon(
                        imageVector = sosIcon,
                        contentDescription = sosString,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = sosString,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.padding(vertical = 2.dp))
                    Text(
                        text = "PRESS & HOLD 3s",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun SosButtonPreview() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SosButton(onLongPress = {}, isActive = false)
    }
}