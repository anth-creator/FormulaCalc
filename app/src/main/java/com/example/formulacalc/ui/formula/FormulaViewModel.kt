package com.example.formulacalc.ui.formula

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.formulacalc.data.FormulaRepository
import com.example.formulacalc.model.Formula
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FormulaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FormulaRepository(application)

    private val _formulas = MutableStateFlow<List<Formula>>(emptyList())
    val formulas: StateFlow<List<Formula>> = _formulas.asStateFlow()

    private val _showCreateDialog = MutableStateFlow(false)
    val showCreateDialog: StateFlow<Boolean> = _showCreateDialog.asStateFlow()

    init {
        loadFormulas()
    }

    fun loadFormulas() {
        _formulas.value = repository.loadFormulas()
    }

    fun showCreateDialog() {
        _showCreateDialog.value = true
    }

    fun hideCreateDialog() {
        _showCreateDialog.value = false
    }

    fun saveFormula(name: String, expression: String) {
        if (name.isNotBlank() && expression.isNotBlank()) {
            repository.saveFormula(Formula(name = name.trim(), expression = expression.trim()))
            loadFormulas()
            hideCreateDialog()
        }
    }

    fun deleteFormula(id: Long) {
        repository.deleteFormula(id)
        loadFormulas()
    }

    fun evaluateFormula(expression: String, variables: Map<String, Double>): Double {
        var expr = expression.replace(" ", "")
        for ((name, value) in variables) {
            expr = expr.replace(name, value.toString())
        }
        return evaluate(expr)
    }

    fun parseVariables(expression: String): List<String> {
        val regex = Regex("[a-zA-Z_][a-zA-Z0-9_]*")
        val reserved = setOf("sin", "cos", "tan", "log", "sqrt", "abs", "PI", "E", "pi", "e")
        return regex.findAll(expression)
            .map { it.value }
            .filter { it !in reserved }
            .distinct()
    }

    private fun evaluate(expression: String): Double {
        return try {
            evaluateExpression(expression)
        } catch (_: Exception) {
            Double.NaN
        }
    }

    private fun evaluateExpression(expr: String): Double {
        var index = 0

        fun parseExpression(): Double {
            var result = parseTerm()
            while (index < expr.length) {
                when (expr[index]) {
                    '+' -> { index++; result += parseTerm() }
                    '-' -> { index++; result -= parseTerm() }
                    else -> break
                }
            }
            return result
        }

        fun parseTerm(): Double {
            var result = parseFactor()
            while (index < expr.length) {
                when (expr[index]) {
                    '*' -> { index++; result *= parseFactor() }
                    '/' -> {
                        index++
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Division by zero")
                        result /= divisor
                    }
                    else -> break
                }
            }
            return result
        }

        fun parseFactor(): Double {
            if (index >= expr.length) throw IllegalArgumentException("Unexpected end")

            if (expr[index] == '-') {
                index++
                return -parseFactor()
            }

            if (expr[index] == '(') {
                index++
                val result = parseExpression()
                if (index < expr.length && expr[index] == ')') {
                    index++
                }
                return result
            }

            val start = index
            while (index < expr.length && (expr[index].isDigit() || expr[index] == '.')) {
                index++
            }
            if (start == index) throw IllegalArgumentException("Expected number at position $index")
            return expr.substring(start, index).toDouble()
        }

        val result = parseExpression()
        return result
    }
}
