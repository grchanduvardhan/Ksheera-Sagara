package com.ksheera.sagara.ui.income

import android.graphics.Color
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ksheera.sagara.R
import com.ksheera.sagara.data.entity.MilkEntry
import com.ksheera.sagara.utils.CurrencyUtils
import com.ksheera.sagara.utils.DateUtils

class MilkEntryAdapter(
    private val onDelete: (MilkEntry) -> Unit,
    private val onEdit: (MilkEntry) -> Unit
) : ListAdapter<MilkEntry, MilkEntryAdapter.ViewHolder>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<MilkEntry>() {
            override fun areItemsTheSame(a: MilkEntry, b: MilkEntry) = a.id == b.id
            override fun areContentsTheSame(a: MilkEntry, b: MilkEntry) = a == b
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvSession: TextView = view.findViewById(R.id.tvSession)
        val tvLiters: TextView = view.findViewById(R.id.tvLiters)
        val tvFat: TextView = view.findViewById(R.id.tvFat)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_milk_entry, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = getItem(position)
        holder.tvDate.text = DateUtils.format(entry.date)
        holder.tvSession.text = entry.session
        holder.tvSession.setTextColor(
            if (entry.session == "Morning") Color.parseColor("#FF8F00") else Color.parseColor("#1565C0")
        )
        holder.tvLiters.text = "%.2f L".format(entry.liters)
        holder.tvFat.text = "Fat: %.1f%%".format(entry.fatPercentage)
        holder.tvAmount.text = CurrencyUtils.formatSimple(entry.totalPayment)
        holder.btnEdit.setOnClickListener { onEdit(entry) }
        holder.btnDelete.setOnClickListener { onDelete(entry) }
    }
}
