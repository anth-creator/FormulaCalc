package com.example.formulacalc.ui.formula

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.formulacalc.model.Formula

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormulaScreen(viewModel: FormulaViewModel = viewModel()) {
    val formulas by viewModel.formulas.collectAsState()
    val showCreateDialog by viewModel.showCreateDialog.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (formulas.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Functions,
                    contentDescription = null,
                    modifier = Modifier.height(64.dp).width(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "暂无公式",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "点击右下角 + 创建新公式",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(formulas, key = { it.id }) { formula ->
                    FormulaCard(
                        formula = formula,
                        onDelete = { viewModel.deleteFormula(formula.id) },
                        onCalculate = { vars -> viewModel.evaluateFormula(formula.expression, vars) },
                        parseVariables = { viewModel.parseVariables(formula.expression) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { viewModel.showCreateDialog() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "创建公式")
        }
    }

    if (showCreateDialog) {
        CreateFormulaDialog(
            onDismiss = { viewModel.hideCreateDialog() },
            onSave = { name, expr -> viewModel.saveFormula(name, expr) }
        )
    }
}

@Composable
private fun FormulaCard(
    formula: Formula,
    onDelete: () -> Unit,
    onCalculate: (Map<String, Double>) -> Double,
    parseVariables: () -> List<String>
) {
    var expanded by remember { mutableStateOf(false) }
    val variables = remember(formula.expression) { parseVariables() }
    val variableValues = remember(formula.expression) { mutableStateMapOf<String, String>() }
    var result by remember(formula.expression) { mutableStateOf<String?>(null) }

    Card(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        formula.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        formula.expression,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    if (variables.isEmpty()) {
                        Text(
                            "此公式没有变量，可直接计算",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        variables.forEach { varName ->
                            OutlinedTextField(
                                value = variableValues[varName] ?: "",
                                onValueChange = { variableValues[varName] = it },
                                label = { Text(varName) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    Button(
                        onClick = {
                            val vars = if (variables.isEmpty()) {
                                emptyMap()
                            } else {
                                variableValues.mapValues { (_, v) -> v.toDoubleOrNull() ?: 0.0 }
                            }
                            val calcResult = onCalculate(vars)
                            result = if (calcResult.isNaN() || calcResult.isInfinite()) {
                                "计算错误"
                            } else {
                                formatResult(calcResult)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("计算")
                    }

                    result?.let {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "结果: $it",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateFormulaDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var expression by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var exprError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("创建公式") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("公式名称") },
                    isError = nameError,
                    supportingText = if (nameError) {{ Text("名称不能为空") }} else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = expression,
                    onValueChange = {
                        expression = it
                        exprError = false
                    },
                    label = { Text("公式表达式") },
                    placeholder = { Text("例如: a+b*c 或 (x+y)/2") },
                    isError = exprError,
                    supportingText = if (exprError) {{ Text("表达式不能为空") }} else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    nameError = name.isBlank()
                    exprError = expression.isBlank()
                    if (!nameError && !exprError) {
                        onSave(name, expression)
                    }
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

private fun formatResult(value: Double): String {
    val longVal = value.toLong()
    return if (value == longVal.toDouble()) longVal.toString() else {
        val s = value.toString()
        if (s.length > 12) "%.8f".format(value).trimEnd('0').trimEnd('.') else s
    }
}
