package com.example.steprange.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.steprange.data.model.AppSettings
import com.example.steprange.data.model.RangeDirection
import com.example.steprange.data.model.StepRangeResult
import com.example.steprange.domain.math.StepRangeFormatter
import com.example.steprange.ui.theme.AccentSuccess
import com.example.steprange.ui.theme.AccentWarning

@Composable
fun RangeSummaryCard(
    result: StepRangeResult,
    settings: AppSettings,
    modifier: Modifier = Modifier
) {
    val dec = settings.decimalPlaces
    val mode = settings.roundingMode

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
            Text(
                text = "RANGE SUMMARY",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            SummaryRow(
                label = "Start",
                value = StepRangeFormatter.format(result.start, dec, mode)
            )
            DividerLine()

            SummaryRow(
                label = if (result.endReached) "End" else "Requested End",
                value = StepRangeFormatter.format(result.end, dec, mode)
            )
            DividerLine()

            SummaryRow(
                label = "Increment",
                value = if (result.direction == RangeDirection.CONSTANT) "—" else StepRangeFormatter.format(result.increment, dec, mode)
            )
            DividerLine()

            SummaryRow(
                label = "Values",
                value = "${result.valueCount}"
            )
            DividerLine()

            SummaryRow(
                label = "First Value",
                value = StepRangeFormatter.format(result.firstValue, dec, mode)
            )
            DividerLine()

            SummaryRow(
                label = "Last Value",
                value = StepRangeFormatter.format(result.lastValue, dec, mode)
            )
            DividerLine()

            SummaryRow(
                label = "End Reached",
                value = if (result.endReached) "Yes" else "No",
                valueColor = if (result.endReached) AccentSuccess else AccentWarning
            )
            DividerLine()

            SummaryRow(
                label = "Minimum",
                value = StepRangeFormatter.format(result.minimum, dec, mode)
            )
            DividerLine()

            SummaryRow(
                label = "Maximum",
                value = StepRangeFormatter.format(result.maximum, dec, mode)
            )
            DividerLine()

            SummaryRow(
                label = "Sum",
                value = StepRangeFormatter.format(result.sum, dec, mode),
                isEmphasized = true
            )
            DividerLine()

            SummaryRow(
                label = "Average",
                value = StepRangeFormatter.format(result.average, dec, mode),
                isEmphasized = true
            )
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    isEmphasized: Boolean = false,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = if (isEmphasized) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isEmphasized) FontWeight.Bold else FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = valueColor
        )
    }
}

@Composable
private fun DividerLine() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        thickness = 0.5.dp
    )
}
