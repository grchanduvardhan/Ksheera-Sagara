package com.ksheera.sagara.ui.cowanalysis

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ksheera.sagara.R
import com.ksheera.sagara.data.entity.Cow
import com.ksheera.sagara.databinding.FragmentCowAnalysisBinding

class CowAnalysisFragment : Fragment() {

    private var _binding: FragmentCowAnalysisBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CowAnalysisViewModel by viewModels()
    private lateinit var adapter: CowProfitAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCowAnalysisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeData()
        binding.fabAddCow.setOnClickListener { showAddCowSheet() }
    }

    private fun setupRecyclerView() {
        adapter = CowProfitAdapter(
            onDelete = { data ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Remove Cow")
                    .setMessage("Remove ${data.cow.name} from your farm?")
                    .setPositiveButton("Remove") { _, _ ->
                        viewModel.deleteCow(data.cow.copy(isActive = false))
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            },
            onEdit = { data -> showAddCowSheet(data.cow) }
        )
        binding.rvCows.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCows.adapter = adapter
    }

    private fun observeData() {
        viewModel.cowProfitList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvEmptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE

            // Best cow badge
            val best = list.firstOrNull()
            if (best != null && list.size > 1) {
                binding.cardBestCow.visibility = View.VISIBLE
                binding.tvBestCowName.text = "🏆 Most Profitable: ${best.cow.name}"
                binding.tvBestCowProfit.text = "₹%.2f profit".format(best.netProfit)
            } else {
                binding.cardBestCow.visibility = View.GONE
            }
        }
    }

    private fun showAddCowSheet(existing: Cow? = null) {
        val sheet = BottomSheetDialog(requireContext())
        val sheetView = layoutInflater.inflate(R.layout.sheet_add_cow, null)
        sheet.setContentView(sheetView)

        val etName = sheetView.findViewById<EditText>(R.id.etCowName)
        val etBreed = sheetView.findViewById<EditText>(R.id.etBreed)
        val etTag = sheetView.findViewById<EditText>(R.id.etTagNumber)
        val btnSave = sheetView.findViewById<Button>(R.id.btnSave)
        val tvTitle = sheetView.findViewById<TextView>(R.id.tvSheetTitle)

        if (existing != null) {
            tvTitle.text = "Edit Cow"
            etName.setText(existing.name)
            etBreed.setText(existing.breed)
            etTag.setText(existing.tagNumber)
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter cow name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val cow = Cow(
                id = existing?.id ?: 0,
                name = name,
                breed = etBreed.text.toString().trim(),
                tagNumber = etTag.text.toString().trim(),
                isActive = true
            )
            if (existing == null) viewModel.addCow(cow) else viewModel.updateCow(cow)
            sheet.dismiss()
        }
        sheet.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
