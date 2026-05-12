package com.ksheera.sagara.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    private val indianFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    fun format(amount: Double): String = indianFormat.format(amount)
    fun formatSimple(amount: Double): String = "₹%.2f".format(amount)
    fun formatCompact(amount: Double): String = when {
        amount >= 100_000 -> "₹%.1fL".format(amount / 100_000)
        amount >= 1_000 -> "₹%.1fK".format(amount / 1_000)
        else -> "₹%.0f".format(amount)
    }
}
