package zq.yaw.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.Window
import androidx.recyclerview.widget.LinearLayoutManager
import com.tencent.mmkv.MMKV
import zq.yaw.databinding.ActivityEditUrlBinding
import zq.yaw.utils.YawSQLiteHelper
import zq.yaw.ui.adapters.AdviceAdapter

class EditUrlActivity : BaseActivity() {
    private lateinit var binding: ActivityEditUrlBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditUrlBinding.inflate(layoutInflater)
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        setContentView(binding.root)
        binding.clearText.setOnClickListener {
            binding.url.setText("")
        }
        val settings = MMKV.mmkvWithID("settings")
        binding.goTo.setOnClickListener {
            if (!checkIfUrl(binding.url.text.toString())) {
                val engine = settings?.decodeString("search_engine", "")!!
                if (engine.indexOf("%%") != -1) {
                    val str = engine.replace("%%", binding.url.text?.trim().toString())
                    val i = Intent().putExtra("url", str)
                    setResult(1, i)
                    finish()
                }

            }
            val i = Intent().putExtra("url", binding.url.text.toString())
            setResult(1, i)
            finish()
        }
        binding.url.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                binding.goTo.performClick()
            }
            true
        }
        binding.url.requestFocus()
        val sql = YawSQLiteHelper.sql
        binding.rec.adapter = AdviceAdapter(sql) {
            val i = Intent().putExtra("url", it)
            setResult(1, i)
            finish()
        }
        binding.url.setOnEditTextKeyBackListener {
            finish()
        }
        binding.rec.layoutManager = LinearLayoutManager(this)
        binding.url.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //Do nothing.
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //Do nothing.
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun afterTextChanged(s: Editable?) {
                (binding.rec.adapter as AdviceAdapter).clearData()
                (binding.rec.adapter as AdviceAdapter).queryStartsWith(s.toString())
                (binding.rec.adapter as AdviceAdapter).notifyDataSetChanged()
            }
        })
    }

    fun checkIfUrl(url: String): Boolean {
        if (url.trim().contains(" ")) return false
        if (!url.contains(".")) return false
        return true
    }

}