package zq.yaw.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.jsoup.Connection.Base
import zq.yaw.BuildConfig
import zq.yaw.R
import zq.yaw.databinding.ActivityAboutBinding

class AboutActivity : BaseActivity() {
    private lateinit var binding: ActivityAboutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.back.setOnClickListener {
            finish()
        }
        binding.version.text = "版本 ${BuildConfig.VERSION_NAME}"
    }
}