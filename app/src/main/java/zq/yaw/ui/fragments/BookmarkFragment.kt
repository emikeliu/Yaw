package zq.yaw.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import zq.yaw.databinding.FragmentBookmarkBinding
import zq.yaw.utils.YawSQLiteHelper
import zq.yaw.ui.adapters.BookmarkAdapter
import zq.yaw.utils.setResultAndExit

class BookmarkFragment : Fragment() {
    private lateinit var binding: FragmentBookmarkBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookmarkBinding.inflate(inflater)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sql = YawSQLiteHelper.bookmarkSql
        binding.bookmarkView.adapter = BookmarkAdapter(sql) {
            setResultAndExit(it, requireActivity())
        }
        binding.bookmarkView.layoutManager = LinearLayoutManager(requireContext())
    }
}