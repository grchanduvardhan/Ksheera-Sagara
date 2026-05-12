package com.ksheera.sagara.ui.expense

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
import com.ksheera.sagara.data.entity.ExpenseEntry
import com.ksheera.sagara.databinding.FragmentExpenseBinding
import com.ksheera.sagara.utils.Constants
import com.ksheera.sagara.utils.CurrencyUtils
import com.ksheera.sagara.utils.DateUtils
import java.util.*

class ExpenseFragment : Fragment() {

    private var _binding: FragmentExpenseBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExpenseViewModel by viewModels()
    private lateinit var adapter: ExpenseAdapter
    private var selectedDate = System.currentTimeMillis()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeEntries()
        binding.fabAddExpense.setOnClickListener { showAddExpenseSheet() }
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter(
            onDelete = { entry ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Delete Expense")
                    .setMessage("Delete this expense entry?")
                    .setPositiveButton("Delete") { _, _ -> viewModel.deleteEntry(entry) }
                    .setNegativeButton("Cancel", null)
                    .show()
            },
            onEdit = { entry -> showAddExpenseSheet(entry) }
        )
        binding.rvExpenses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExpenses.adapter = adapter
    }

    private fun observeEntries() {
        viewModel.filteredEntries.observe(viewLifecycleOwner) { entries ->
            adapter.submitList(entries)
            val total = entries.sumOf { it.amount }
            binding.tvTotalExpenseLabel.text = "Total Expenses: ${CurrencyUtils.formatSimple(total)}"
            binding.tvEmptyState.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showAddExpenseSheet(existing: ExpenseEntry? = null) {
        val sheet = BottomSheetDialog(requireContext())
        val sheetView = layoutInflater.inflate(R.layout.sheet_add_expense, null)
        sheet.setContentView(sheetView)

        val etDate = sheetView.findViewById<EditText>(R.id.etDate)
        val etDescription = sheetView.findViewById<EditText>(R.id.etDescription)
        val etAmount = sheetView.findViewById<EditText>(R.id.etAmount)
        val spinnerCategory = sheetView.findViewById<Spinner>(R.id.spinnerCategory)
        val spinnerCow = sheetView.findViewById<Spinner>(R.id.spinnerCow)
        val btnSave = sheetView.findViewById<Button>(R.id.btnSave)
        val tvTitle = sheetView.findViewById<TextView>(R.id.tvSheetTitle)

        // Category spinner
        spinnerCategory.adapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_spinner_item, Constants.EXPENSE_CATEGORIES)
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

        // Populate existing
        if (existing != null) {
            tvTitle.text = "Edit Expense"
            etDescription.setText(existing.description)
            etAmount.setText(existing.amount.toString())
            val catIdx = Constants.EXPENSE_CATEGORIES.indexOf(existing.category)
            if (catIdx >= 0) spinnerCategory.setSelection(catIdx)
        }

        // Cow spinner
        var cowList = listOf<com.ksheera.sagara.data.entity.Cow>()
        viewModel.activeCows.observe(viewLifecycleOwner) { cows ->
            cowList = cows
            val names = listOf("General Farm") + cows.map { it.name }
            spinnerCow.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
                .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            if (existing != null && existing.cowId != 0L) {
                val idx = cows.indexOfFirst { it.id == existing.cowId }
                if (idx >= 0) spinnerCow.setSelection(idx + 1)
            }
        }

        btnSave.setOnClickListener {
            val amount = etAmount.text.toString().toDoubleOrNull()
            val desc = etDescription.text.toString().trim()
            if (amount == null || desc.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val selectedCowId = if (spinnerCow.selectedItemPosition == 0) 0L
            else cowList.getOrNull(spinnerCow.selectedItemPosition - 1)?.id ?: 0L

            val entry = ExpenseEntry(
                id = existing?.id ?: 0,
                date = selectedDate,
                category = Constants.EXPENSE_CATEGORIES[spinnerCategory.selectedItemPosition],
                description = desc,
                amount = amount,
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
