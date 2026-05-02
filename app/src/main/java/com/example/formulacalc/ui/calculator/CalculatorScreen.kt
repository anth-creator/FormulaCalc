package com.example.formulacalc.ui.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.formulacalc.ui.theme.CalculatorBackground
import com.example.formulacalc.ui.theme.CalculatorButtonDark
import com.example.formulacalc.ui.theme.FunctionGray
import com.example.formulacalc.ui.theme.OperatorOrange

@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel = viewModel()) {
    val display by viewModel.display.collectAsState()
    val expression by viewModel.expression.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CalculatorBackground)
    ) {
        // Display area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = expression,
                color = Color(0xFF8E8E93),
                fontSize = 24.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = display,
                color = Color.White,
                fontSize = 56.sp,
                fontWeight = FontWeight.Light,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End
            )
        }

        // Button grid - 5 rows x 4 columns, all equal size
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val buttonRows = listOf(
                listOf("C" to "fn", "+/-" to "fn", "%" to "fn", "÷" to "op"),
                listOf("7" to "n", "8" to "n", "9" to "n", "×" to "op"),
                listOf("4" to "n", "5" to "n", "6" to "n", "-" to "op"),
                listOf("1" to "n", "2" to "n", "3" to "n", "+" to "op"),
                listOf("0" to "n", "." to "n", "⌫" to "fn", "=" to "op")
            )

            buttonRows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { (label, type) ->
                        CalcButton(
                            label = label,
                            isOperator = type == "op",
                            isFunction = type == "fn",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                when (label) {
                                    "C" -> viewModel.onClear()
                                    "⌫" -> viewModel.onBackspace()
                                    "+/-" -> viewModel.onToggleSign()
                                    "%" -> viewModel.onPercentage()
                                    "=" -> viewModel.onEquals()
                                    "+", "-", "×", "÷" -> viewModel.onOperator(label)
                                    "." -> viewModel.onDecimal()
                                    else -> viewModel.onDigit(label)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalcButton(
    label: String,
    isOperator: Boolean,
    isFunction: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val bgColor = when {
        isOperator -> OperatorOrange
        isFunction -> FunctionGray
        else -> CalculatorButtonDark
    }
    val textColor = when {
        isFunction -> Color.Black
        else -> Color.White
    }

    Button(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(percent = 50),
        colors = ButtonDefaults.buttonColors(containerColor = bgColor),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = label,
            fontSize = when {
                label.length > 2 -> 18.sp
                label == "=" -> 32.sp
                else -> 26.sp
            },
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}
