package com.ksheera.sagara.ui.income

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ksheera.sagara.R
import com.ksheera.sagara.data.entity.MilkEntry
import com.ksheera.sagara.databinding.FragmentIncomeBinding
import com.ksheera.sagara.utils.CurrencyUtils
import com.ksheera.sagara.utils.DateUtils
import java.util.*

class IncomeFragment : Fragment() {

    private var _binding: FragmentIncomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: IncomeViewModel by viewModels()
    private lateinit var adapter: MilkEntryAdapter
    private var selectedDate = System.currentTimeMillis()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentIncomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeEntries()
        binding.fabAddIncome.setOnClickListener { showAddEntrySheet() }
    }

    private fun setupRecyclerView() {
        adapter = MilkEntryAdapter(
            onDelete = { entry ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Delete Entry")
                    .setMessage("Delete this milk entry?")
                    .setPositiveButton("Delete") { _, _ -> viewModel.deleteEntry(entry) }
                    .setNegativeButton("Cancel", null)
                    .show()
            },
            onEdit = { entry -> showAddEntrySheet(entry) }
        )
        binding.rvMilkEntries.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMilkEntries.adapter = adapter
    }

    private fun observeEntries() {
        viewModel.filteredEntries.observe(viewLifecycleOwner) { entries ->
            adapter.submitList(entries)
            val total = entries.sumOf { it.totalPayment }
            val liters = entries.sumOf { it.liters }
            binding.tvTotalIncomeLabel.text = "Total: ${CurrencyUtils.formatSimple(total)}  |  ${String.format("%.2f", liters)} L"
            binding.tvEmptyState.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showAddEntrySheet(existing: MilkEntry? = null) {
        val sheet = BottomSheetDialog(requireContext())
        val sheetView = layoutInflater.inflate(R.layout.sheet_add_income, null)
        sheet.setContentView(sheetView)

        val etDate = sheetView.findViewById<EditText>(R.id.etDate)
        val etLiters = sheetView.findViewById<EditText>(R.id.etLiters)
        val etFat = sheetView.findViewById<EditText>(R.id.etFat)
        val etSnf = sheetView.findViewById<EditText>(R.id.etSnf)
        val etRate = sheetView.findViewById<EditText>(R.id.etRate)
        val etPayment = sheetView.findViewById<EditText>(R.id.etTotalPayment)
        val spinnerSession = sheetView.findViewById<Spinner>(R.id.spinnerSession)
        val spinnerCow = sheetView.findViewById<Spinner>(R.id.spinnerCow)
        val btnSave = sheetView.findViewById<Button>(R.id.btnSave)
        val tvTitle = sheetView.findViewById<TextView>(R.id.tvSheetTitle)

        // Session spinner
        val sessions = listOf("Morning", "Evening")
        spinnerSession.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sessions)
            .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        // Date picker
        selectedDate = existing?.date ?: System.currentTimeMillis()
        etDate.setText(DateUtils.format(selectedDate))
        etDate.setOnClickListener {
            val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
            DatePickerDialog(requireContext(), { _, y, m, d ->
                cal.set(y, m, d)
                selectedDate = cal.timeInMillis
                etDate.setText(DateUtils.format(selectedDate))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Auto-calculate payment
        fun calcPayment() {
            val liters = etLiters.text.toString().toDoubleOrNull() ?: return
            val rate = etRate.text.toString().toDoubleOrNull() ?: return
            etPayment.setText(String.format("%.2f", liters * rate))
        }
        etLiters.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) calcPayment() }
        etRate.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) calcPayment() }

        // Populate existing
        if (existing != null) {
            tvTitle.text = "Edit Milk Entry"
            etLiters.setText(existing.liters.toString())
            etFat.setText(existing.fatPercentage.toString())
            etSnf.setText(existing.snfPercentage.toString())
            etRate.setText(existing.ratePerLiter.toString())
            etPayment.setText(existing.totalPayment.toString())
            spinnerSession.setSelection(if (existing.session == "Evening") 1 else 0)
        }

        // Cow spinner
        var cowList = listOf<com.ksheera.sagara.data.entity.Cow>()
        viewModel.activeCows.observe(viewLifecycleOwner) { cows ->
            cowList = cows
            val cowNames = listOf("General (No specific cow)") + cows.map { it.name }
            spinnerCow.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, cowNames)
                .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            if (existing != null && existing.cowId != 0L) {
                val idx = cows.indexOfFirst { it.id == existing.cowId }
                if (idx >= 0) spinnerCow.setSelection(idx + 1)
            }
        }

        btnSave.setOnClickListener {
            val liters = etLiters.text.toString().toDoubleOrNull()
            val fat = etFat.text.toString().toDoubleOrNull()
            val snf = etSnf.text.toString().toDoubleOrNull() ?: 0.0
            val rate = etRate.text.toString().toDoubleOrNull()
            val payment = etPayment.text.toString().toDoubleOrNull()

            if (liters == null || fat == null || rate == null || payment == null) {
                Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedCowId = if (spinnerCow.selectedItemPosition == 0) 0L
            else cowList.getOrNull(spinnerCow.selectedItemPosition - 1)?.id ?: 0L

            val entry = MilkEntry(
                id = existing?.id ?: 0,
                date = selectedDate,
                liters = liters,
                fatPercentage = fat,
                snfPercentage = snf,
                ratePerLiter = rate,
                totalPayment = payment,
                session = sessions[spinnerSession.selectedItemPosition],
                cowId = selectedCowId
            )
            if (existing == null) viewModel.addEntry(entry) else viewModel.updateEntry(entry)
            sheet.dismiss()
        }

        sheet.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
