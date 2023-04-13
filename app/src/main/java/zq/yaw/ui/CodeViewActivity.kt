package zq.yaw.ui

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import zq.yaw.BuildConfig
import zq.yaw.R
import zq.yaw.utils.StringBinder

class CodeViewActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_code)
        val textView = findViewById<TextView>(R.id.code)
        val back = findViewById<LinearLayout>(R.id.back)
        back.setOnClickListener {
            finish()
        }
        textView.text = (intent?.extras?.getBinder("str") as StringBinder).trans
    }
}