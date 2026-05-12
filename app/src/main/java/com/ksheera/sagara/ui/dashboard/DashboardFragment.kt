package com.ksheera.sagara.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import com.ksheera.sagara.R
import com.ksheera.sagara.databinding.FragmentDashboardBinding
import com.ksheera.sagara.utils.CurrencyUtils
import java.util.*

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMonthNavigation()
        observeDashboard()
    }

    private fun setupMonthNavigation() {
        binding.btnPrevMonth.setOnClickListener {
            val cal = Calendar.getInstance()
            cal.set(viewModel.selectedYear.value ?: cal.get(Calendar.YEAR),
                    viewModel.selectedMonth.value ?: cal.get(Calendar.MONTH), 1)
            cal.add(Calendar.MONTH, -1)
            viewModel.setMonthYear(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
        }
        binding.btnNextMonth.setOnClickListener {
            val cal = Calendar.getInstance()
            cal.set(viewModel.selectedYear.value ?: cal.get(Calendar.YEAR),
                    viewModel.selectedMonth.value ?: cal.get(Calendar.MONTH), 1)
            cal.add(Calendar.MONTH, 1)
            viewModel.setMonthYear(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
        }
    }

    private fun observeDashboard() {
        viewModel.dashboardData.observe(viewLifecycleOwner) { data ->
            binding.tvMonthLabel.text = data.monthLabel

            binding.tvTotalIncome.text = CurrencyUtils.formatSimple(data.totalIncome)
            binding.tvTotalExpense.text = CurrencyUtils.formatSimple(data.totalExpense)
            binding.tvNetProfit.text = CurrencyUtils.formatSimple(data.netProfit)
            binding.tvTotalLiters.text = "%.2f L".format(data.totalLiters)
            binding.tvProfitPerLiter.text = CurrencyUtils.formatSimple(data.profitPerLiter) + "/L"
            binding.tvAvgFat.text = "%.2f%%".format(data.avgFat)

            // Health indicator
            if (data.netProfit >= 0) {
                binding.tvHealthIndicator.text = "● PROFITABLE"
                binding.tvHealthIndicator.setTextColor(Color.parseColor("#2E7D32"))
                binding.cardHealth.setCardBackgroundColor(Color.parseColor("#E8F5E9"))
            } else {
                binding.tvHealthIndicator.text = "● AT LOSS"
                binding.tvHealthIndicator.setTextColor(Color.parseColor("#C62828"))
                binding.cardHealth.setCardBackgroundColor(Color.parseColor("#FFEBEE"))
            }

            setupPieChart(data)
        }
    }

    private fun setupPieChart(data: DashboardData) {
        val entries = mutableListOf<PieEntry>()
        if (data.fodderExpense > 0) entries.add(PieEntry(data.fodderExpense.toFloat(), "Fodder"))
        if (data.medicalExpense > 0) entries.add(PieEntry(data.medicalExpense.toFloat(), "Medical"))
        if (data.laborExpense > 0) entries.add(PieEntry(data.laborExpense.toFloat(), "Labor"))
        if (data.otherExpense > 0) entries.add(PieEntry(data.otherExpense.toFloat(), "Other"))

        if (entries.isEmpty()) {
            binding.pieChart.visibility = View.GONE
            binding.tvNoPieData.visibility = View.VISIBLE
            return
        }

        binding.pieChart.visibility = View.VISIBLE
        binding.tvNoPieData.visibility = View.GONE

        val colors = listOf(
            Color.parseColor("#FF8F00"),
            Color.parseColor("#C62828"),
            Color.parseColor("#1565C0"),
            Color.parseColor("#558B2F")
        )

        val dataSet = PieDataSet(entries, "Expenses").apply {
            this.colors = colors
            valueTextSize = 12f
            valueTextColor = Color.WHITE
        }
        binding.pieChart.apply {
            this.data = PieData(dataSet)
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 45f
            setHoleColor(Color.WHITE)
            setCenterText("Expenses")
            setCenterTextSize(14f)
            legend.isEnabled = true
            animateY(800)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
