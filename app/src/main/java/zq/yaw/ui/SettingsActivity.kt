package zq.yaw.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isInvisible
import com.tencent.mmkv.MMKV
import zq.yaw.R
import zq.yaw.databinding.ActivitySettingsBinding
import zq.yaw.ui.views.MMKVBindSwitchPref

class SettingsActivity : BaseActivity() {
    private lateinit var mmkv: MMKV
    private lateinit var binding: ActivitySettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MMKV.initialize(this)
        mmkv = MMKV.mmkvWithID("settings")!!
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.back.setOnClickListener {
            finish()
        }
        homepage()
        searchEngine()
        forceAllowZoom()
        enableJs()
        autoRotate()
        initPropsByIdAndMMKV(
            R.id.force_allow_zoom to "force_allow_zoom",
            R.id.enable_js to "js",
            R.id.fullscreen_auto_rotate to "fullscreen_auto_rotate"
        )
        about()
    }



    private fun autoRotate() {
        binding.fullscreenAutoRotate.setIcon(R.drawable.baseline_crop_rotate_24)
        binding.fullscreenAutoRotate.setTitles("全屏自动转屏", "网页请求全屏时旋转为横屏")
    }

    private fun initPropsByIdAndMMKV(vararg props: Pair<Int, String>) {
        props.forEach {
            val prop = binding.root.findViewById<MMKVBindSwitchPref>(it.first)
            prop.setMMKV(mmkv)
            prop.setPropName(it.second)
            prop.init()
        }
    }

    private fun about() {
        binding.about.setTitles("关于Yaw", "Yaw的一切信息！")
        binding.about.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
        binding.about.setIcon(R.drawable.outline_info_24)
    }

    private fun enableJs() {
        binding.enableJs.setTitles("启用Javascript", "如非必要请不要关闭此选项")
        binding.enableJs.setIcon(R.drawable.bigger_js)
    }

    private fun forceAllowZoom() {
        binding.forceAllowZoom.setTitles("强制启用缩放", "将会覆写网页设置")
        binding.forceAllowZoom.setIcon(R.drawable.baseline_zoom_in_24)

    }

    private fun homepage() {
        val now = mmkv.getString("homepage", "$\$DEFAULT")!!
        binding.homePage.setTitles("主页", now)
        if (now == "$\$DEFAULT") {
            binding.homePage.setSubTitle("默认主页")
            binding.homePage.setInitText("")
        }
        binding.homePage.setHint("在此输入主页网址(留空以使用默认主页)")
        binding.homePage.setIcon(R.drawable.sharp_home_24)
        binding.searchEngine.setInitText(now)
        binding.homePage.addOnChangeListener {
            if (it.trim().isEmpty()) {
                mmkv.putString("homepage", "$\$DEFAULT")
                binding.homePage.setSubTitle("默认主页")
                binding.homePage.setInitText("")

            } else {
                mmkv.putString("homepage", it)
                binding.homePage.setSubTitle(it)
                binding.homePage.setInitText(it)
            }
        }
    }
    private fun searchEngine() {
        val now = mmkv.getString("search_engine", "cn.bing.com/search?q=%%")!!
        binding.searchEngine.setInitText(now)
        binding.searchEngine.setTitles("搜索引擎", now)
        binding.searchEngine.setHint("用\"%%\"代替关键词")
        binding.searchEngine.setIcon(R.drawable.baseline_search_24)
        binding.searchEngine.addOnChangeListener {
                mmkv.putString("search_engine", it)
                binding.searchEngine.setSubTitle(it)
                binding.searchEngine.setInitText(it)
        }
    }
}