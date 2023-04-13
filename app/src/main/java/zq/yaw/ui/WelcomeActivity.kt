package zq.yaw.ui

import android.content.Intent
import android.os.Bundle
import androidx.cardview.widget.CardView
import com.tencent.mmkv.MMKV
import zq.yaw.R
import zq.yaw.utils.Globals

class WelcomeActivity : BaseActivity() {
    val settings: MMKV? = Globals.settings
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        settings?.putString("homepage", "$\$DEFAULT")
        settings?.putString("search_engine", "cn.bing.com/search?q=%%")
        settings?.putBoolean("js", true)
        settings?.putBoolean("finished_first", true)
        val card = findViewById<CardView>(R.id.start)
        card.setOnClickListener {
            finish()
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}