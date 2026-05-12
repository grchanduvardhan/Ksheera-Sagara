package com.ksheera.sagara.ui.cowanalysis

import android.graphics.Color
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ksheera.sagara.R
import com.ksheera.sagara.utils.CurrencyUtils

class CowProfitAdapter(
    private val onDelete: (CowProfitData) -> Unit,
    private val onEdit: (CowProfitData) -> Unit
) : ListAdapter<CowProfitData, CowProfitAdapter.ViewHolder>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<CowProfitData>() {
            override fun areItemsTheSame(a: CowProfitData, b: CowProfitData) = a.cow.id == b.cow.id
            override fun areContentsTheSame(a: CowProfitData, b: CowProfitData) = a == b
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCowName: TextView = view.findViewById(R.id.tvCowName)
        val tvBreed: TextView = view.findViewById(R.id.tvBreed)
        val tvIncome: TextView = view.findViewById(R.id.tvIncome)
        val tvExpense: TextView = view.findViewById(R.id.tvExpense)
        val tvProfit: TextView = view.findViewById(R.id.tvProfit)
        val tvLiters: TextView = view.findViewById(R.id.tvLiters)
        val tvProfitPerLiter: TextView = view.findViewById(R.id.tvProfitPerLiter)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cow_profit, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = getItem(position)
        holder.tvCowName.text = data.cow.name
        holder.tvBreed.text = if (data.cow.breed.isNotEmpty()) data.cow.breed else "Unknown breed"
        holder.tvIncome.text = "Income: ${CurrencyUtils.formatSimple(data.totalIncome)}"
        holder.tvExpense.text = "Expense: ${CurrencyUtils.formatSimple(data.totalExpense)}"
        holder.tvProfit.text = CurrencyUtils.formatSimple(data.netProfit)
        holder.tvProfit.setTextColor(
            if (data.netProfit >= 0) Color.parseColor("#2E7D32") else Color.parseColor("#C62828")
        )
        holder.tvLiters.text = "%.2f L".format(data.totalLiters)
        holder.tvProfitPerLiter.text = "${CurrencyUtils.formatSimple(data.profitPerLiter)}/L"
        holder.btnEdit.setOnClickListener { onEdit(data) }
        holder.btnDelete.setOnClickListener { onDelete(data) }
    }
}
