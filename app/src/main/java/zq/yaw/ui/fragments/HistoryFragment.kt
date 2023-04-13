package zq.yaw.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import zq.yaw.databinding.FragmentHistoryBinding
import zq.yaw.utils.YawSQLiteHelper
import zq.yaw.ui.adapters.HistoryAdapter
import zq.yaw.utils.setResultAndExit

class HistoryFragment : Fragment() {
    private lateinit var binding: FragmentHistoryBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sql = YawSQLiteHelper.sql
        binding.historyView.adapter = HistoryAdapter(requireContext(), sql) {
            setResultAndExit(it, requireActivity())
        }
        binding.historyView.layoutManager = LinearLayoutManager(requireContext())

    }
}