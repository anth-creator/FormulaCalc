package com.example.formulacalc.data

import android.content.Context
import com.example.formulacalc.model.Formula
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FormulaRepository(context: Context) {
    private val prefs = context.getSharedPreferences("formulas", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveFormula(formula: Formula) {
        val formulas = loadFormulas().toMutableList()
        formulas.add(formula)
        saveFormulas(formulas)
    }

    fun deleteFormula(id: Long) {
        val formulas = loadFormulas().toMutableList()
        formulas.removeAll { it.id == id }
        saveFormulas(formulas)
    }

    fun loadFormulas(): List<Formula> {
        val json = prefs.getString("formula_list", null) ?: return emptyList()
        val type = object : TypeToken<List<Formula>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun saveFormulas(formulas: List<Formula>) {
        prefs.edit().putString("formula_list", gson.toJson(formulas)).apply()
    }
}
