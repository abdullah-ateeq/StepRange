package com.example.steprange.ui.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun RangeInputCard(
    startInput: String,
    incrementInput: String,
    endInput: String,
    includeEnd: Boolean,
    startError: String?,
    incrementError: String?,
    endError: String?,
    isCalculating: Boolean,
    onStartChanged: (String) -> Unit,
    onIncrementChanged: (String) -> Unit,
    onEndChanged: (String) -> Unit,
    onIncludeEndChanged: (Boolean) -> Unit,
    onGenerate: () -> Unit,
    onCancel: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Start Value Input
            Text(
                text = "START VALUE",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = startInput,
                onValueChange = onStartChanged,
                placeholder = { Text("e.g. 0") },
                isError = startError != null,
                supportingText = {
                    if (startError != null) {
                        Text(text = startError, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Starting value." }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Increment Input
            Text(
                text = "INCREMENT",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = incrementInput,
                onValueChange = onIncrementChanged,
                placeholder = { Text("e.g. 2 or -2") },
                isError = incrementError != null,
                supportingText = {
                    if (incrementError != null) {
                        Text(text = incrementError, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Custom increment." }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // End Value Input
            Text(
                text = "END VALUE",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = endInput,
                onValueChange = onEndChanged,
                placeholder = { Text("e.g. 10") },
                isError = endError != null,
                supportingText = {
                    if (endError != null) {
                        Text(text = endError, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Ending value." }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Endpoint Option
            Text(
                text = "ENDPOINT",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup()
                    .semantics { contentDescription = "Whether to include the ending value." },
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .selectable(
                            selected = includeEnd,
                            onClick = { onIncludeEndChanged(true) }
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = includeEnd,
                        onClick = { onIncludeEndChanged(true) }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Include End",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (includeEnd) FontWeight.SemiBold else FontWeight.Normal
                    )
                }

                Row(
                    modifier = Modifier
                        .selectable(
                            selected = !includeEnd,
                            onClick = { onIncludeEndChanged(false) }
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = !includeEnd,
                        onClick = { onIncludeEndChanged(false) }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Exclude End",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (!includeEnd) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isCalculating) {
                    Button(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "CANCEL")
                    }
                } else {
                    Button(
                        onClick = onGenerate,
                        modifier = Modifier
                            .weight(1f)
                            .semantics { contentDescription = "Generate stepped range." },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GENERATE RANGE",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedButton(
                    onClick = onClear,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "CLEAR")
                }
            }
        }
    }
}
