package zq.yaw.ui

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfDocument.Page
import android.net.http.SslCertificate
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.CookieManager
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar
import com.tencent.mmkv.MMKV
import org.apache.commons.lang3.StringEscapeUtils
import org.jsoup.Jsoup
import zq.yaw.R
import zq.yaw.databinding.*
import zq.yaw.utils.YawSQLiteHelper
import zq.yaw.ui.adapters.PagesAdapter
import zq.yaw.ui.fragments.PageFragment
import zq.yaw.ui.fragments.PageFragment.Companion.DEFAULT_HOMEPAGE
import zq.yaw.utils.*
import java.lang.ref.WeakReference

class MainActivity : BaseActivity() {
    private lateinit var model: MainModel
    lateinit var binding: ActivityMainBinding
    private lateinit var nowDisplayingFragment: PageFragment
    private lateinit var settings: MMKV
    private lateinit var sql: YawSQLiteHelper
    private val selectUrlActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == 1) {
                it.data?.getStringExtra("url")?.let { i -> nowDisplayingFragment.loadUrl(i) }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        model = ViewModelProvider(this)[MainModel::class.java]
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initMMKV()
        checkFirst()
        initDb()
        initTopBar()
        watchPages()
        whetherToAddFirstPage()
        switchPage(0)
    }

    private fun checkFirst() {
        if (!settings.decodeBool("finished_first")) {
            startActivity(Intent(this, WelcomeActivity::class.java))
        }
    }


    private fun initMMKV() {
        MMKV.initialize(this)
        settings = MMKV.mmkvWithID("settings")!!
        Globals.settings = settings
        PageFragmentFactory.settings = settings
        PageFragment.settings = settings
    }

    private fun initDb() {
        YawSQLiteHelper.context = WeakReference(application)
        YawSQLiteHelper.makeSqlHelper()
        sql = YawSQLiteHelper.sql
    }

    private fun initTopBar() {
        //if (settings.decodeBool("force_dark")) setTheme(R.style.Theme_Yaw_Dark)
        binding.pageSwitch.setOnClickListener {
            showPages()
        }
        binding.settings.setOnClickListener {
            showMore()
        }
        binding.home.setOnClickListener {
            nowDisplayingFragment.loadUrl()
        }
        binding.certInfo.setOnClickListener {
            val innerBinding = CertInfoBinding.inflate(layoutInflater)
            val certProps = nowDisplayingFragment.getCert()
            certProps?.issuedBy?.let {
                innerBinding.safeIcon.setImageResource(R.drawable.outline_lock_24)
                innerBinding.safeInfo.text = "连接已加密"
                innerBinding.certInfoButton.visibility = View.VISIBLE
                innerBinding.certInfoButton.setOnClickListener {
                    nowDisplayingFragment.getCert()?.let { i -> showCertDialog(i) }
                }

            }
            innerBinding.webSettingButton.setOnClickListener {
                showWebSettingsDialog()
            }
            PopupWindow(innerBinding.root, screenWidth(), dp2px(140), true).apply {
                animationStyle = R.style.Popup_Window
                showAsDropDown(binding.topBar)
            }
        }
        binding.url.setOnClickListener {
            binding.input.performClick()
        }
        binding.input.setOnClickListener {
            val i = Intent(this, EditUrlActivity::class.java)
            selectUrlActivityLauncher.launch(i, ActivityOptionsCompat.makeSceneTransitionAnimation(this))
        }
    }

    private fun showWebSettingsDialog() {
        val url = nowDisplayingFragment.getUrl()
        val cookies = CookieManager.getInstance().getCookie(url)
        var cit = 0
        if (cookies != null) {
            cit = cookies.split(";").size
        }
        MaterialAlertDialogBuilder(this)
            .setMessage("此网站使用了${cit}条Cookie。")
            .setPositiveButton("确定") { d, _ ->
                d.cancel()
            }.setNeutralButton("清除Cookie") { _, _ ->
                JavaUtils.deleteCookiesForDomain(url)
            }.show()
    }

    private fun showCertDialog(cert: SslCertificate) {
        MaterialAlertDialogBuilder(this).setTitle("证书信息").setMessage(
            "证书颁发者: \n"
                    + "\tCN: ${cert.issuedBy.cName} \n"
                    + "\tOU: ${cert.issuedBy.uName} \n"
                    + "\tO: ${cert.issuedBy.oName} \n"
                    + "颁发给: \n"
                    + "\tCN: ${cert.issuedTo.cName} \n"
                    + "\tOU: ${cert.issuedTo.oName} \n"
                    + "\tO: ${cert.issuedTo.uName} \n"
        ).setPositiveButton("确定") { d, _ ->
            d.cancel()
        }.show()
    }

    private fun watchPages() {
        model.pagesList.setOnSizeChangeListener {
            binding.pageNum.text = it.toString()
        }
    }

    private fun whetherToAddFirstPage() {
        model.pagesList.add(PageFragmentFactory().new())
    }

    private fun switchPage(which: Int) {
        updateUi {
            if (::nowDisplayingFragment.isInitialized)
                takeOffInfo()
            val tx = supportFragmentManager.beginTransaction()
            if (::nowDisplayingFragment.isInitialized) {
                tx.hide(nowDisplayingFragment)
            }
            val toSwitch = model.pagesList[if (which >= model.pagesList.size) (model.pagesList.size - 1) else which]
            if (!toSwitch.isAdded) {
                tx.add(R.id.web_view_container, toSwitch)
            }
            tx.show(toSwitch)
            nowDisplayingFragment = toSwitch
            wearInfo(nowDisplayingFragment)
            tx.commitAllowingStateLoss()
            //adapter?.notifyItemRangeChanged(0, pagesList.size)
        }
    }

    private fun closePageRaw(page: PageFragment) {
        updateUi {
            val index = model.pagesList.indexOf(page)
            val tx = supportFragmentManager.beginTransaction()
            tx.remove(page)
            page.onDestroy()
            model.pagesList.remove(page)
            if (model.pagesList.size == 0) {
                finish()
                return@updateUi
            }
            tx.commitNow()
            val where = model.pagesList.indexOf(nowDisplayingFragment)
            //adapter.notifyItemRangeChanged(0, pagesList.size)
            if (where > index) {
                switchPage(where)
            } else if (where == -1) {
                switchPage(index)
            }

        }

    }

    private fun closePage(which: Int, adapter: PagesAdapter) {
        updateUi {
            val tx = supportFragmentManager.beginTransaction()
            tx.remove(model.pagesList[which])
            model.pagesList[which].onDestroy()
            model.pagesList.remove(model.pagesList[which])
            Log.d(MainActivity::class.simpleName, "which is $which")
            if (model.pagesList.size == 0) {
                if (switchPagePopup.isShowing) switchPagePopup.dismiss()
                finish()
                return@updateUi
            }
            tx.commitNow()
            val where = model.pagesList.indexOf(nowDisplayingFragment)
            adapter.notifyItemRemoved(which)
            //adapter.notifyItemRangeChanged(0, pagesList.size)
            if (where > which) {
                switchPage(where)
            } else if (where == -1) {
                switchPage(which)
                updateUi {
                    adapter.notifyItemRangeChanged(0, model.pagesList.size)
                }

            }

        }
    }

    private fun addPage(incognito: Boolean = false) {
        if (incognito) {
            model.pagesList.add(PageFragmentFactory().newIncognito())
            Snackbar.make(nowDisplayingFragment.requireView(), "新建了无痕标签页", LENGTH_LONG).show()
        }
        else model.pagesList.add(PageFragmentFactory().new())
        switchPage(model.pagesList.size - 1)
    }

    private fun takeOffInfo() {
        nowDisplayingFragment.certStatus.removeObservers(this)
        nowDisplayingFragment.loadingPercent.removeObservers(this)
        nowDisplayingFragment.url.removeObservers(this)
        nowDisplayingFragment.topBarColor.removeObservers(this)
    }

    private fun wearInfo(page: PageFragment) {
        page.url.observe(this) {
            binding.url.text = it
            if (it.startsWith(DEFAULT_HOMEPAGE) && settings.decodeBool("js")) {
                binding.url.visibility = View.INVISIBLE
                binding.input.visibility = View.INVISIBLE
            } else {
                binding.input.visibility = View.VISIBLE
                binding.url.visibility = View.VISIBLE

            }
        }
        page.loadingPercent.observe(this) {
            binding.indicator.progress = it
            binding.indicator.visibility = if (it == 100) View.INVISIBLE else View.VISIBLE
        }
        page.certStatus.observe(this) {
            if (it) binding.certInfo.setImageResource(R.drawable.outline_lock_24)
            else binding.certInfo.setImageResource(R.drawable.round_warning_amber_24)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (nowDisplayingFragment.canGoBack())
            nowDisplayingFragment.goBack()
            closePageRaw(nowDisplayingFragment)
            return true
        }
        return false

    }

    fun openInNewTab(isForeground: Boolean, url: String) {
        updateUi {
            val tx = supportFragmentManager.beginTransaction()
            val newFragment = PageFragmentFactory().new(url)
            if (!newFragment.isAdded)
                tx.add(R.id.web_view_container, newFragment)
            model.pagesList.add(newFragment)
            //To make website load in background.
            if (isForeground) tx.show(newFragment).hide(newFragment)
            tx.commit()
            if (!isForeground) switchPage(model.pagesList.size - 1)
        }
    }

    lateinit var morePopup: PopupWindow
    private fun showMore() {
        val innerBinding = MenuMoreBinding.inflate(layoutInflater)
        innerBinding.historyAndBookmarks.setOnClickListener {
            selectUrlActivityLauncher.launch(Intent(this, HistoryAndBookmarkActivity::class.java))
            morePopup.dismiss()
        }
        fun updateForwardStatus() {
            if (!nowDisplayingFragment.canGoForward()) {
                innerBinding.goForward.isClickable = false
                innerBinding.goForwardPic.imageTintList = ColorStateList.valueOf(Color.GRAY)
            } else {
                innerBinding.goForward.isClickable = true
                innerBinding.goForwardPic.imageTintList =
                    ColorStateList.valueOf(getColorAccent(this))
            }
        }
        innerBinding.addToBookmark.setOnClickListener {
            val icon = nowDisplayingFragment.propertiesPerSite.icon
            val title = nowDisplayingFragment.propertiesPerSite.title
            val iBinding = DialogAddBookmarkBinding.inflate(layoutInflater)
            if (icon != null)
                iBinding.icon.setImageBitmap(icon)
            iBinding.title.text = title
            MaterialAlertDialogBuilder(this).setTitle("添加此网页到书签吗").setView(iBinding.root)
                .setPositiveButton("确定") { d, v ->
                val time = System.currentTimeMillis()
                YawSQLiteHelper.bookmarkSql.insertRecord(time, nowDisplayingFragment.getUrl(), title)
                YawSQLiteHelper.bookmarkSql.addIcon(icon, time)
            }.setNegativeButton("取消", null).show()
        }
        innerBinding.down.setOnClickListener {
            startActivity(Intent(this, DownloadActivity::class.java))
        }
        innerBinding.settings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        innerBinding.goForward.setOnClickListener {
            if (nowDisplayingFragment.canGoForward()) nowDisplayingFragment.goForward()
            updateForwardStatus()
        }
        //Should after setOnClickListener cause which will make it clickable.
        updateForwardStatus()
        innerBinding.cancelOrRefresh.setOnClickListener {
            if (nowDisplayingFragment.isLoading.value!!) {
                nowDisplayingFragment.stop()
            } else {
                nowDisplayingFragment.reload()
            }
            morePopup.dismiss()
        }
        nowDisplayingFragment.isLoading.observe(this) {
            if (it) {
                innerBinding.crPic.setImageResource(R.drawable.baseline_close_24)
                innerBinding.crText.text = "停止"
            } else {
                innerBinding.crPic.setImageResource(R.drawable.baseline_refresh_24)
                innerBinding.crText.text = "刷新"
            }
        }

        innerBinding.tools.setOnClickListener {
            showToolsWindow()
            morePopup.dismiss()
        }
        PopupWindow(innerBinding.root, screenWidth(), dp2px(160), true).apply {
            animationStyle = R.style.Popup_Window
            showAsDropDown(binding.topBar)
            morePopup = this
        }.setOnDismissListener {
            nowDisplayingFragment.isLoading.removeObservers(this)
        }
        initPcMode(innerBinding)

    }

    private fun initPcMode(
        innerBinding: MenuMoreBinding,
    ) {
        innerBinding.pcMode.setStrokeColor(ColorStateList.valueOf(getColorAccent(this)))
        innerBinding.pcMode.strokeWidth = if (settings.decodeBool("pc_mode")) 5 else 0
        innerBinding.pcMode.setOnClickListener {
            settings.encode("pc_mode", !settings.decodeBool("pc_mode"))
            nowDisplayingFragment.refreshManually()
            morePopup.dismiss()
        }
    }

    private lateinit var toolsPopup: PopupWindow
    lateinit var searchPopup: PopupWindow
    private fun showToolsWindow() {
        val innerBinding = MenuToolsBinding.inflate(layoutInflater)
        innerBinding.share.setOnClickListener {
            shareString(this, nowDisplayingFragment.getUrl())
            toolsPopup.dismiss()
        }
        innerBinding.back.setOnClickListener {
            toolsPopup.dismiss()
            showMore()
        }
        innerBinding.darkMode.strokeWidth = if (settings.decodeBool("force_dark")) 5 else 0
        innerBinding.darkMode.setOnClickListener {
            settings.encode("force_dark", !settings.decodeBool("force_dark"))
            nowDisplayingFragment.refreshManually()
            toolsPopup.dismiss()
        }
        innerBinding.save.setOnClickListener {
            nowDisplayingFragment.savePage()
        }
        innerBinding.code.setOnClickListener {
            nowDisplayingFragment.getWebCode {
                val s = StringEscapeUtils.unescapeEcmaScript(it)
                startActivity(Intent(this@MainActivity, CodeViewActivity::class.java).apply {
                    putExtras(Bundle().apply {
                        putBinder("str", StringBinder().setString(Jsoup.parse(s).outerHtml()))
                    })
                })
            }
        }
        innerBinding.search.setOnClickListener {
            val searchBinding = DialogSearchBinding.inflate(layoutInflater)
            searchBinding.keyWord.addTextChangedListener {
                nowDisplayingFragment.startSearch(it.toString())
            }
            searchBinding.next.setOnClickListener {
                nowDisplayingFragment.nextResult()
            }
            searchBinding.forward.setOnClickListener {
                nowDisplayingFragment.forwardResult()
            }
            searchBinding.close.setOnClickListener {
                searchPopup.dismiss()
            }
            PopupWindow(searchBinding.root, screenWidth(), dp2px(60), true).apply {
                searchPopup = this
                showAtLocation(window.decorView, Gravity.TOP, 0, 0)
                setOnDismissListener {
                    nowDisplayingFragment.clearResult()
                }
            }
            toolsPopup.dismiss()
        }
        innerBinding.qrcode.setOnClickListener {
            val i = ImageView(this)
            i.setImageBitmap(JavaUtils.createQrCode(nowDisplayingFragment.getUrl()))
            MaterialAlertDialogBuilder(this).setTitle("网页二维码").setView(i).show()
        }
        innerBinding.evaluteJs.setOnClickListener {
            val jsBinding = DialogJsEvaluateBinding.inflate(layoutInflater)
            jsBinding.exec.setOnClickListener {
                nowDisplayingFragment.evaluateJs(jsBinding.jsEva.text.toString()) {
                    if (it != "null")
                        MaterialAlertDialogBuilder(this).setTitle("执行结果").setMessage(it).show()
                }
                jsBinding.jsEva.setText("")

            }
            MaterialAlertDialogBuilder(this).setTitle("执行Javascript")
                .setView(jsBinding.root)
                .setOnDismissListener {
                    nowDisplayingFragment.consoleLog.removeObservers(this)
                }.show()
            nowDisplayingFragment.consoleLog.observe(this) {
                jsBinding.console.text = it
            }
        }
        PopupWindow(innerBinding.root, screenWidth(), dp2px(160), true).apply {
            animationStyle = R.style.Popup_Window
            showAsDropDown(binding.topBar)
            toolsPopup = this
        }
    }

    private lateinit var switchPagePopup: PopupWindow
    private fun showPages() {
        val innerBinding = PopupPageSwitchBinding.inflate(layoutInflater)
        innerBinding.pagesList.adapter =
            PagesAdapter(this, model.pagesList, ::switchPage, ::closePage) {
                switchPagePopup.dismiss()
            }
        innerBinding.pagesList.layoutManager = LinearLayoutManager(this, HORIZONTAL, false)
        innerBinding.addNewTab.setOnClickListener {
            addPage()
            switchPagePopup.dismiss()
        }
        innerBinding.addNewIncognitoTab.setOnClickListener {
            addPage(true)
            switchPagePopup.dismiss()
        }
        val helperCallback = object : ItemTouchHelper.Callback() {
            private lateinit var adapter: PagesAdapter
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                adapter = (recyclerView.adapter as PagesAdapter)
                return makeMovementFlags(
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or
                            ItemTouchHelper.RIGHT,
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN
                )
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val startPosition = viewHolder.adapterPosition
                val endPosition = target.adapterPosition
                adapter.itemMove(startPosition, endPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                (viewHolder as PagesAdapter.PagesViewHolder).closeTab.performClick()
            }

        }
        val helper = ItemTouchHelper(helperCallback)
        val nowAt = (innerBinding.pagesList.adapter as PagesAdapter).nowAt()
        Log.d("nowAt", nowAt.toString())
        innerBinding.pagesList.scrollToPosition((innerBinding.pagesList.adapter as PagesAdapter).nowAt())
        helper.attachToRecyclerView(innerBinding.pagesList)
        switchPagePopup = PopupWindow(innerBinding.root, screenWidth(), dp2px(250), true).apply {
            animationStyle = R.style.Popup_Window
            if (!::switchPagePopup.isInitialized || !switchPagePopup.isShowing)
                showAsDropDown(binding.topBar)
        }
    }

    private fun dp2px(dpValue: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpValue.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()
    }

    private fun screenWidth() = window.decorView.width

    fun addIcon(icon: Bitmap?, time: Long) {
        sql.addIcon(icon, time)
    }

    fun insertHistory(time: Long, url: String, title: String?) {
        sql.insertRecord(time, url, title)

    }

    fun addTitle(title: String?, time: Long) {
        sql.addTitle(title, time)
    }

    fun enterFullScreen(v: View?) {
        if (settings.decodeBool("fullscreen_auto_rotate"))
            turnLandscape()
        window.insetsController?.hide(WindowInsets.Type.statusBars())
        window.insetsController?.hide(WindowInsets.Type.navigationBars())
        window.insetsController?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        binding.fullscreen.visibility = View.VISIBLE
        binding.fullscreen.addView(v, MATCH_PARENT, MATCH_PARENT)
    }

    private fun turnLandscape() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    private fun turnPortrait() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    fun quitFullScreen() {
        window.insetsController?.show(WindowInsets.Type.statusBars())
        window.insetsController?.show(WindowInsets.Type.navigationBars())
        binding.fullscreen.visibility = View.GONE
        if (settings.decodeBool("fullscreen_auto_rotate"))
            turnPortrait()
    }

    override fun onResume() {
        if (::nowDisplayingFragment.isInitialized)
            nowDisplayingFragment.onHiddenChanged(false)
        Log.d("Main", "onResume()")
        super.onResume()
    }

    fun getNowShowingFragment() = nowDisplayingFragment
}

