package com.ksheera.sagara.ui.summary

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.ksheera.sagara.databinding.FragmentSummaryBinding
import com.ksheera.sagara.utils.CurrencyUtils
import com.ksheera.sagara.utils.PdfExportUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class SummaryFragment : Fragment() {

    private var _binding: FragmentSummaryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SummaryViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMonthNavigation()
        observeData()
        binding.btnExportPdf.setOnClickListener { exportPdf() }
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

    private fun observeData() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.milkEntries.observe(viewLifecycleOwner) { milkEntries ->
            val expenseEntries = viewModel.expenseEntries.value ?: emptyList()
            val totalIncome = milkEntries.sumOf { it.totalPayment }
            val totalExpense = expenseEntries.sumOf { it.amount }
            val netProfit = totalIncome - totalExpense
            val totalLiters = milkEntries.sumOf { it.liters }
            val profitPerLiter = if (totalLiters > 0) netProfit / totalLiters else 0.0

            binding.tvMonthLabel.text = viewModel.getMonthLabel()
            binding.tvSummaryIncome.text = CurrencyUtils.formatSimple(totalIncome)
            binding.tvSummaryExpense.text = CurrencyUtils.formatSimple(totalExpense)
            binding.tvSummaryProfit.text = CurrencyUtils.formatSimple(netProfit)
            binding.tvSummaryLiters.text = "%.2f L".format(totalLiters)
            binding.tvSummaryProfitLiter.text = "${CurrencyUtils.formatSimple(profitPerLiter)}/L"
            binding.tvEntryCount.text = "${milkEntries.size} milk entries  |  ${expenseEntries.size} expense entries"

            // Expense breakdown
            val fodder = expenseEntries.filter { it.category == "Fodder" }.sumOf { it.amount }
            val medical = expenseEntries.filter { it.category == "Medical" }.sumOf { it.amount }
            val labor = expenseEntries.filter { it.category == "Labor" }.sumOf { it.amount }
            val other = expenseEntries.filter { it.category == "Other" }.sumOf { it.amount }
            binding.tvFodder.text = CurrencyUtils.formatSimple(fodder)
            binding.tvMedical.text = CurrencyUtils.formatSimple(medical)
            binding.tvLabor.text = CurrencyUtils.formatSimple(labor)
            binding.tvOther.text = CurrencyUtils.formatSimple(other)

            // Profit/loss suggestion
            val suggestion = when {
                netProfit > 0 && profitPerLiter > 5 -> "✅ Great month! Your farm is highly profitable. Consider expanding."
                netProfit > 0 -> "✅ Profitable month. Consider switching to home-grown fodder to boost margins."
                netProfit < 0 && fodder > medical -> "⚠️ Loss detected! Fodder cost is your biggest expense. Try home-grown fodder."
                netProfit < 0 -> "⚠️ Loss detected! Review your expenses. Reduce vet visits where possible."
                else -> "ℹ️ Break even this month. Track more months to find trends."
            }
            binding.tvSuggestion.text = suggestion
        }

        viewModel.expenseEntries.observe(viewLifecycleOwner) { _ ->
            // re-trigger via milk observer (combined)
        }
    }

    private fun exportPdf() {
        val milkEntries = viewModel.milkEntries.value ?: emptyList()
        val expenseEntries = viewModel.expenseEntries.value ?: emptyList()
        val monthLabel = viewModel.getMonthLabel()

        lifecycleScope.launch {
            binding.btnExportPdf.isEnabled = false
            binding.btnExportPdf.text = "Generating..."
            val file = withContext(Dispatchers.IO) {
                PdfExportUtils.generateMonthlySummary(requireContext(), milkEntries, expenseEntries, monthLabel)
            }
            binding.btnExportPdf.isEnabled = true
            binding.btnExportPdf.text = "Export PDF"
            if (file != null) {
                Toast.makeText(requireContext(), "PDF generated!", Toast.LENGTH_SHORT).show()
                PdfExportUtils.sharePdf(requireContext(), file)
            } else {
                Toast.makeText(requireContext(), "Failed to generate PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
