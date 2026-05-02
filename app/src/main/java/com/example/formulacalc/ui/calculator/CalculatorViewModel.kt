package com.example.formulacalc.ui.calculator

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CalculatorViewModel : ViewModel() {

    private val _display = MutableStateFlow("0")
    val display: StateFlow<String> = _display.asStateFlow()

    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private var currentInput = ""
    private var currentOperator = ""
    private var firstOperand = 0.0
    private var shouldResetInput = false
    private var lastResult = 0.0

    fun onDigit(digit: String) {
        if (shouldResetInput) {
            currentInput = ""
            shouldResetInput = false
        }
        if (currentInput == "0" && digit != ".") {
            currentInput = digit
        } else if (currentInput == "-0") {
            currentInput = "-$digit"
        } else if (currentInput.length < 12) {
            currentInput += digit
        }
        updateDisplay()
    }

    fun onDecimal() {
        if (shouldResetInput) {
            currentInput = "0."
            shouldResetInput = false
            updateDisplay()
            return
        }
        if (!currentInput.contains(".")) {
            currentInput = if (currentInput.isEmpty()) "0." else currentInput + "."
            updateDisplay()
        }
    }

    fun onOperator(op: String) {
        if (currentInput.isNotEmpty()) {
            if (currentOperator.isNotEmpty() && !shouldResetInput) {
                onEquals()
                shouldResetInput = true
            }
            firstOperand = currentInput.toDoubleOrNull() ?: 0.0
            currentOperator = op
            shouldResetInput = true
            _expression.value = "$firstOperand $op"
        } else if (currentOperator.isEmpty() && _display.value != "0") {
            firstOperand = _display.value.toDoubleOrNull() ?: 0.0
            currentOperator = op
            shouldResetInput = true
            _expression.value = "$firstOperand $op"
        } else {
            currentOperator = op
            _expression.value = "$firstOperand $op"
        }
    }

    fun onEquals() {
        if (currentOperator.isNotEmpty()) {
            val secondOperand = if (currentInput.isNotEmpty()) {
                currentInput.toDoubleOrNull() ?: 0.0
            } else {
                firstOperand
            }
            _expression.value = "${_expression.value} $secondOperand ="
            val result = when (currentOperator) {
                "+" -> firstOperand + secondOperand
                "-" -> firstOperand - secondOperand
                "×" -> firstOperand * secondOperand
                "÷" -> if (secondOperand != 0.0) firstOperand / secondOperand else Double.NaN
                else -> secondOperand
            }
            currentInput = formatResult(result)
            lastResult = result
            firstOperand = 0.0
            currentOperator = ""
            shouldResetInput = true
            updateDisplay()
        }
    }

    fun onClear() {
        currentInput = ""
        firstOperand = 0.0
        currentOperator = ""
        shouldResetInput = false
        _display.value = "0"
        _expression.value = ""
    }

    fun onBackspace() {
        if (shouldResetInput) return
        if (currentInput.isNotEmpty()) {
            currentInput = currentInput.dropLast(1)
            updateDisplay()
        }
    }

    fun onPercentage() {
        val value = if (currentInput.isNotEmpty()) {
            currentInput.toDoubleOrNull() ?: 0.0
        } else {
            _display.value.toDoubleOrNull() ?: 0.0
        }
        currentInput = formatResult(value / 100.0)
        updateDisplay()
    }

    fun onToggleSign() {
        if (currentInput.isNotEmpty()) {
            currentInput = if (currentInput.startsWith("-")) {
                currentInput.removePrefix("-")
            } else {
                "-$currentInput"
            }
            updateDisplay()
        }
    }

    private fun updateDisplay() {
        _display.value = currentInput.ifEmpty { "0" }
    }

    private fun formatResult(value: Double): String {
        if (value.isNaN()) return "错误"
        if (value.isInfinite()) return "错误"
        val longVal = value.toLong()
        return if (value == longVal.toDouble()) {
            longVal.toString()
        } else {
            val formatted = value.toString()
            if (formatted.length > 12) {
                String.format("%.8f", value).trimEnd('0').trimEnd('.')
            } else {
                formatted
            }
        }
    }
}
