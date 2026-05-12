package com.ksheera.sagara.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val fileFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    fun format(timestamp: Long): String = displayFormat.format(Date(timestamp))
    fun formatMonthYear(timestamp: Long): String = monthYearFormat.format(Date(timestamp))
    fun formatForFile(timestamp: Long): String = fileFormat.format(Date(timestamp))

    fun startOfMonth(year: Int, month: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun endOfMonth(year: Int, month: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month, cal.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun currentMonthStart(): Long {
        val cal = Calendar.getInstance()
        return startOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
    }

    fun currentMonthEnd(): Long {
        val cal = Calendar.getInstance()
        return endOfMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
    }

    fun todayMillis(): Long = System.currentTimeMillis()
}
