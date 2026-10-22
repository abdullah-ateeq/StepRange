package com.example.steprange.ui.home

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.steprange.domain.math.StepRangeFormatter
import com.example.steprange.ui.StepRangeUiState
import com.example.steprange.ui.home.components.FindValueCard
import com.example.steprange.ui.home.components.FormulaCard
import com.example.steprange.ui.home.components.RangeInputCard
import com.example.steprange.ui.home.components.RangeSummaryCard
import com.example.steprange.ui.home.components.SteppedRangeResultCard
import com.example.steprange.ui.home.components.TermListCard

@Composable
fun HomeScreen(
    uiState: StepRangeUiState,
    onStartChanged: (String) -> Unit,
    onIncrementChanged: (String) -> Unit,
    onEndChanged: (String) -> Unit,
    onIncludeEndChanged: (Boolean) -> Unit,
    onGenerate: () -> Unit,
    onCancel: () -> Unit,
    onClear: () -> Unit,
    onSaveToHistory: () -> Unit,
    onFindValueIndexChanged: (String) -> Unit,
    onCalculateValueAtIndex: () -> Unit,
    onToggleFormulaExpanded: () -> Unit,
    onClearSnackbar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    // Handle snackbar messages
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            onClearSnackbar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen Header
            Column {
                Text(
                    text = "StepRange",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Generate values using a custom increment.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // General Error Message Banner (if any)
            if (uiState.errorMessage != null) {
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            // Primary Inputs
            RangeInputCard(
                startInput = uiState.startInput,
                incrementInput = uiState.incrementInput,
                endInput = uiState.endInput,
                includeEnd = uiState.includeEnd,
                startError = uiState.startError,
                incrementError = uiState.incrementError,
                endError = uiState.endError,
                isCalculating = uiState.isCalculating,
                onStartChanged = onStartChanged,
                onIncrementChanged = onIncrementChanged,
                onEndChanged = onEndChanged,
                onIncludeEndChanged = onIncludeEndChanged,
                onGenerate = onGenerate,
                onCancel = onCancel,
                onClear = onClear
            )

            // Results Section (Results remain on Home!)
            AnimatedVisibility(
                visible = uiState.result != null,
                enter = fadeIn() + scaleIn(initialScale = 0.95f),
                exit = fadeOut() + scaleOut(targetScale = 0.95f)
            ) {
                uiState.result?.let { res ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Result Card with preview and actions
                        SteppedRangeResultCard(
                            result = res,
                            warningMessage = uiState.warningMessage,
                            settings = uiState.settings,
                            onCopy = {
                                val textToCopy = StepRangeFormatter.formatCopyValuesOnly(
                                    values = res.values,
                                    decimalPlaces = uiState.settings.decimalPlaces,
                                    roundingMode = uiState.settings.roundingMode.mode
                                )
                                clipboardManager.setText(AnnotatedString(textToCopy))
                            },
                            onShare = {
                                val shareText = StepRangeFormatter.formatShareText(
                                    result = res,
                                    decimalPlaces = uiState.settings.decimalPlaces,
                                    roundingMode = uiState.settings.roundingMode.mode
                                )
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share Stepped Range")
                                context.startActivity(shareIntent)
                            },
                            onSave = onSaveToHistory
                        )

                        // Term List (Index -> Value)
                        TermListCard(
                            values = res.values,
                            settings = uiState.settings
                        )

                        // Range Summary Card
                        RangeSummaryCard(
                            result = res,
                            settings = uiState.settings
                        )

                        // Formula & Explanation Section
                        FormulaCard(
                            result = res,
                            steps = uiState.calculationSteps,
                            isExpanded = uiState.isFormulaExpanded,
                            settings = uiState.settings,
                            onToggleExpand = onToggleFormulaExpanded
                        )

                        // Direct Index Calculation (Find Value)
                        FindValueCard(
                            indexInput = uiState.findValueIndex,
                            result = uiState.findValueResult,
                            error = uiState.findValueError,
                            onIndexChanged = onFindValueIndexChanged,
                            onCalculate = onCalculateValueAtIndex
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
