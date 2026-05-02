package com.example.formulacalc.model

data class Formula(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val expression: String
)
