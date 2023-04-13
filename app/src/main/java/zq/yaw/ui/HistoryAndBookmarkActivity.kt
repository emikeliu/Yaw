package zq.yaw.ui

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import zq.yaw.R
import zq.yaw.databinding.ActivityHistoryBookmarkBinding
import zq.yaw.utils.YawSQLiteHelper
import zq.yaw.ui.adapters.MarkPagerAdapter

class HistoryAndBookmarkActivity : BaseActivity() {
    private lateinit var binding: ActivityHistoryBookmarkBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val sql = YawSQLiteHelper.sql
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBookmarkBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.back.setOnClickListener {
            setResult(0)
            finish()
        }
        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.history -> {
                    binding.pager.currentItem = 1
                    binding.title.setText(R.string.history)
                }
                R.id.bookmark -> {
                    binding.pager.currentItem = 0
                    binding.title.setText(R.string.bookmark)
                }
            }
            true
        }
        binding.pager.adapter = MarkPagerAdapter(this)
        binding.pager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 0) binding.clearAll.visibility = View.GONE
                else binding.clearAll.visibility = View.VISIBLE
                binding.bottomNav.selectedItemId = if (position == 0) R.id.bookmark else R.id.history
            }
        })
        binding.clearAll.setOnClickListener {
            MaterialAlertDialogBuilder(this).setTitle("注意").setMessage("真的要清除全部历史记录吗?")
                .setPositiveButton("确定") { _, _ ->
                    sql.clearAll()
                    finish()
                }.setNegativeButton("取消", null).show()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(0)
            finish()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}