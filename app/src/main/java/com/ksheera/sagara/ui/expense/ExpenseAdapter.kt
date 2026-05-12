package com.ksheera.sagara.ui.expense

import android.graphics.Color
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ksheera.sagara.R
import com.ksheera.sagara.data.entity.ExpenseEntry
import com.ksheera.sagara.utils.CurrencyUtils
import com.ksheera.sagara.utils.DateUtils

class ExpenseAdapter(
    private val onDelete: (ExpenseEntry) -> Unit,
    private val onEdit: (ExpenseEntry) -> Unit
) : ListAdapter<ExpenseEntry, ExpenseAdapter.ViewHolder>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<ExpenseEntry>() {
            override fun areItemsTheSame(a: ExpenseEntry, b: ExpenseEntry) = a.id == b.id
            override fun areContentsTheSame(a: ExpenseEntry, b: ExpenseEntry) = a == b
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense_entry, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = getItem(position)
        holder.tvDate.text = DateUtils.format(entry.date)
        holder.tvCategory.text = entry.category
        holder.tvCategory.setTextColor(when (entry.category) {
            "Fodder" -> Color.parseColor("#FF8F00")
            "Medical" -> Color.parseColor("#C62828")
            "Labor" -> Color.parseColor("#1565C0")
            else -> Color.parseColor("#558B2F")
        })
        holder.tvDescription.text = entry.description
        holder.tvAmount.text = CurrencyUtils.formatSimple(entry.amount)
        holder.btnEdit.setOnClickListener { onEdit(entry) }
        holder.btnDelete.setOnClickListener { onDelete(entry) }
    }
}
