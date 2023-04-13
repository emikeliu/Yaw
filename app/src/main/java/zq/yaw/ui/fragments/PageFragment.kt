package zq.yaw.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActionBar.LayoutParams
import android.app.DownloadManager
import android.app.DownloadManager.Request
import android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.net.http.SslCertificate
import android.os.*
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.webkit.GeolocationPermissions.Callback
import android.webkit.WebSettings.*
import android.webkit.WebView.HitTestResult.*
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tencent.mmkv.MMKV
import zq.yaw.R
import zq.yaw.databinding.FragmentPageBinding
import zq.yaw.ui.MainActivity
import zq.yaw.utils.*

@Suppress("DEPRECATION")
class PageFragment() : Fragment() {
    companion object {
        const val DEFAULT_HOMEPAGE = "file:///android_asset/homepage.html"
        val supportSchemes = arrayOf("https://", "http://", "ftp://", "ws://", "wss://")
        lateinit var settings: MMKV
    }

    constructor(initPage: String) : this() {
        this.initPage = initPage
    }
    constructor(incognito: Boolean): this() {
        this.incognito = incognito
    }

    inner class MyJSInterface {
        @JavascriptInterface
        fun openInput() {
            updateUi {
                nestedActivity.binding.input.performClick()
            }
        }
    }

    data class GeoRequestBean(val callback: Callback, val url: String, val retain: Boolean) {
        fun invoke() = callback.invoke(url, true, retain)
    }

    private lateinit var initPage: String
    private lateinit var geoRequest: GeoRequestBean
    private lateinit var binding: FragmentPageBinding
    private lateinit var webView: WebView

    private var incognito = false

    private val requestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                geoRequest.invoke()
            }
        }
    val propertiesPerSite = PropertiesPerSite()
    val loadingPercent = MutableLiveData(100)
    val url = MutableLiveData("")
    val isLoading = MutableLiveData(true)
    val certStatus = MutableLiveData(false)
    val topBarColor = MutableLiveData<String?>(null)
    private val nestedActivity by lazy {
        requireActivity() as MainActivity
    }
    private val downloadManager by lazy {
        nestedActivity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }
    val consoleLog = MutableLiveData("")


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentPageBinding.inflate(inflater).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(this.toString(), "do")
        binding.webViewContainer.addView(
            WebView(nestedActivity.applicationContext).also { webView = it },
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        )
        loadInitUrl()
        checkSettings()
    }

    private fun loadInitUrl() {
        initWebView()
        if (!::initPage.isInitialized) {
            initPage = settings.decodeString("home_page", "$\$DEFAULT")!!
        }
        if (initPage == "$\$DEFAULT") {
            webView.loadUrl("file:///android_asset/homepage.html")
            webView.addJavascriptInterface(MyJSInterface(), "system")
        } else webView.loadUrl(initPage)
    }

    fun getPic(): Bitmap {
        if (!isAdded) return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val width = nestedActivity.window.decorView.width
        val bm = Bitmap.createBitmap(width, width / 14 * 13, Bitmap.Config.ARGB_8888)
        val c = Canvas(bm)
        if (::webView.isInitialized) binding.webViewContainer.draw(c)
        return bm
    }

    override fun onDestroy() {
        webView.destroy()
        webView.webChromeClient = null
        binding.webViewContainer.removeAllViews()
        super.onDestroy()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden && ::webView.isInitialized) {
            checkSettings()
        }
    }

    private fun checkPcMode() {
        if (settings.decodeBool("pc_mode")) {
            webView.settings.userAgentString =
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 Edg/110.0.0.0"
        } else {
            webView.settings.userAgentString =
                "Mozilla/5.0 (Linux; Android 13; V2231A; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/110.0.0.0 Mobile Safari/537.36 VivoBrowser/13.6.21.0"
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {

        with(webView.settings) {
            setSupportZoom(true)
            loadWithOverviewMode = true
            useWideViewPort = true
            displayZoomControls = false
            builtInZoomControls = true
            javaScriptEnabled = settings.decodeBool("js")
            domStorageEnabled = true
            mixedContentMode = MIXED_CONTENT_ALWAYS_ALLOW

            if (incognito) {
                domStorageEnabled = false
                databaseEnabled = false
                cacheMode = LOAD_NO_CACHE
                mixedContentMode = MIXED_CONTENT_NEVER_ALLOW
            }

        }
        checkSettings()
        setClients()
        webView.setDownloadListener { url, _, _, mimetype, contentLength ->
            showWhetherDownload(url, mimetype, contentLength)
            isLoading.value = false
        }
        webView.setOnLongClickListener {
            val result = webView.hitTestResult
            Log.d("type", result.type.toString())
            when (result.type) {
                SRC_ANCHOR_TYPE, SRC_IMAGE_ANCHOR_TYPE, IMAGE_TYPE -> {
                    showLongClickDialog(result.extra)
                    true
                }
//                UNKNOWN_TYPE -> {
//                    val message = Message.obtain()
//                    message.target = handler
//                    webView.requestFocusNodeHref(message)
//                    true
//                }
                else -> false
            }
        }

    }

    private fun showWhetherDownload(url: String?, mimetype: String?, size: Long) {
        MaterialAlertDialogBuilder(requireContext()).setTitle("确定要下载此文件吗")
            .setMessage("下载地址: $url\n文件大小: ${size.ton()}")
            .setPositiveButton("确定") { d, _ ->
                invokeSystemService(url, mimetype)
                d.cancel()
            }.setNegativeButton("取消") { d, _ ->
                d.cancel()
            }.setNeutralButton("复制下载地址") { _, _ ->
                url?.let { putToClipboard(requireContext(), it) }
            }.show()
    }

    private fun invokeSystemService(url: String?, mimetype: String?) {
        val manager: DownloadManager =
            context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimetype)
        val name = JavaUtils.getFileName(url) + ".$ext"
        val request =
            Request(Uri.parse(url)).setMimeType(mimetype).setTitle(name)
                .setNotificationVisibility(VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(DIRECTORY_DOWNLOADS, name)
        manager.enqueue(request)
    }

    private fun showLongClickDialog(url: String?) {
        var innerDialog: AlertDialog? = null
        val menuView = LayoutInflater.from(nestedActivity).inflate(R.layout.dialog_long_click, null)
        menuView.findViewById<CardView>(R.id.open_in_new_tab).setOnClickListener {
            url?.let { r -> nestedActivity.openInNewTab(false, r) }
            innerDialog?.cancel()
        }
        menuView.findViewById<CardView>(R.id.open_in_new_tab_background).setOnClickListener {
            url?.let { r -> nestedActivity.openInNewTab(true, r) }
            innerDialog?.cancel()
        }
        menuView.findViewById<CardView>(R.id.copy_url).setOnClickListener {
            val manager = (requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
            manager.setPrimaryClip(ClipData.newPlainText("url", url))
            innerDialog?.cancel()
        }

//        menuView.findViewById<CardView>(R.id.download).setOnClickListener {
//            url?.let {
//                val manager: DownloadManager =
//                    context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//                val request =
//                    Request(Uri.parse(url))
//                        .setNotificationVisibility(VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//                request.setDestinationInExternalPublicDir(DIRECTORY_DOWNLOADS, "/")
//
//                manager.enqueue(request)
//            }
//            innerDialog?.cancel()
//        }
        innerDialog =
            MaterialAlertDialogBuilder(nestedActivity).setView(menuView).setTitle(url).show()
    }

    private fun setClients() {
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                if (supportSchemes.none { request?.url.toString().startsWith(it) }) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(request?.url.toString()))
                    if (!propertiesPerSite.noMoreOpen)
                        MaterialAlertDialogBuilder(nestedActivity).setTitle("此网页想要打开如下链接").setMessage(request?.url.toString())
                            .setPositiveButton("允许") { _, _ ->
                                try {
                                    startActivity(intent)
                                } catch (e: ActivityNotFoundException) {
                                    MaterialAlertDialogBuilder(nestedActivity).setTitle("无法处理此链接")
                                        .setMessage("没有应用可以处理此链接:。")
                                        .setPositiveButton("确定", null).show()
                                }
                            }.setNegativeButton("阻止创建更多消息") { _, _ ->
                                propertiesPerSite.noMoreOpen = true
                            }.show()

                    return true
                }
                loadingPercent.value = 10
                isLoading.value = true
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                if (dark && webView.settings.forceDark != FORCE_DARK_ON)
                    webView.settings.forceDark = FORCE_DARK_ON
                else if(!dark && webView.settings.forceDark != FORCE_DARK_OFF)
                    webView.settings.forceDark = FORCE_DARK_OFF
                isLoading.value = true
                propertiesPerSite.reset()
                this@PageFragment.url.value = url
                certStatus.value = (webView.certificate?.issuedBy != null)
                if (!incognito)
                    nestedActivity.insertHistory(
                        applyTime(System.currentTimeMillis()),
                        webView.url!!,
                        webView.title
                    )

                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                isLoading.value = false
                certStatus.value = (webView.certificate?.issuedBy != null)
                overrideWebScaleSettings()
                super.onPageFinished(view, url)
            }
        }
        webView.webChromeClient = object : WebChromeClient() {

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                loadingPercent.value = newProgress
                super.onProgressChanged(view, newProgress)
            }

            override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                nestedActivity.addIcon(icon, propertiesPerSite.lastTime)
                super.onReceivedIcon(view, icon)
                propertiesPerSite.icon = icon
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                nestedActivity.addTitle(title, propertiesPerSite.lastTime)
                propertiesPerSite.title = title
                super.onReceivedTitle(view, title)
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                if (!isHidden) {
                    consoleLog.value = consoleLog.value + "${consoleMessage?.message()}\n"
                }
                return false
            }

            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                propertiesPerSite.jsTimes++
                if (!propertiesPerSite.noMoreAlert)
                    MaterialAlertDialogBuilder(nestedActivity).setTitle("来自网页的消息")
                        .setMessage(message).setPositiveButton("确定") { d, _ ->
                            d.cancel()
                        }.apply {
                            if (propertiesPerSite.jsTimes >= 3)
                                setNegativeButton("阻止网页创建更多消息") { _, _ ->
                                    propertiesPerSite.noMoreAlert = true
                                }
                        }.show()
                return super.onJsAlert(view, url, message, result)
            }

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                nestedActivity.enterFullScreen(view)
                super.onShowCustomView(view, callback)
            }

            override fun onHideCustomView() {
                nestedActivity.quitFullScreen()
                super.onHideCustomView()
            }

            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: Callback?
            ) {
                MaterialAlertDialogBuilder(requireContext()).setTitle("位置请求")
                    .setMessage("$origin 请求获取你的位置信息。").setPositiveButton("拒绝") { d, _ ->
                        callback?.invoke(origin, false, true)
                        d.cancel()
                    }.setNeutralButton("允许") { d, _ ->
                        if (checkGeoPermission(callback, origin!!))
                            callback?.invoke(origin, true, true)
                        d.cancel()
                    }.show()
            }
        }
    }

    private fun overrideWebScaleSettings() {
        if (allowForceZoom)
            webView.evaluateJavascript(
                "var o0oo0oo0o0o0o0o = '';" +
                        "document.getElementsByName('viewport')[0].getAttribute('content').split(',').forEach( r => {if(!r.includes(\"user-scalable\") && !r.includes('scale')) o0oo0oo0o0o0o0o = o0oo0oo0o0o0o0o + \",\" + r});" +
                        "document.getElementsByName('viewport')[0].content = o0oo0oo0o0o0o0o.substring(1)"
            ) {}
    }

    //true: granted
    private fun checkGeoPermission(callback: Callback?, url: String): Boolean {
        if (nestedActivity.checkPermission(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Process.myPid(),
                Process.myUid()
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            this.geoRequest = callback?.let { GeoRequestBean(it, url, true) }!!
            requestLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return false
        }
        return true
    }

    private fun checkSettings() {
        checkIncognito()
        checkPcMode()
        allowForceZoom()
        checkDark()
        checkJs()
    }

    private fun checkJs() {
        webView.settings.javaScriptEnabled = settings.decodeBool("js")
    }

    private fun checkIncognito() {
        updateUi {
            CookieManager.getInstance().setAcceptCookie(!incognito)
            Log.d("cookie status", "changed to setAcceptCookie(${!incognito})")
        }
    }

    private var dark = false
    private fun checkDark() {
        dark = settings.decodeBool("force_dark", DefaultProps.forceDark)
    }

    private var allowForceZoom = false
    private fun allowForceZoom() {
        allowForceZoom = settings.decodeBool("force_allow_zoom", DefaultProps.forceAllowZoom)
    }

    fun isIncognitoMode() = incognito
    fun canGoBack() = webView.canGoBack()
    fun goBack() = webView.goBack()
    fun loadUrl(url: String) = webView.loadUrl(url)
    fun canGoForward() = webView.canGoForward()
    fun goForward() = webView.goForward()
    fun stop() = webView.stopLoading()
    fun reload() = webView.reload()
    private fun applyTime(time: Long): Long {
        propertiesPerSite.lastTime = time
        return time
    }

    inner class CustomizedWebView(context: Context) : WebView(context)

    fun refreshManually() {
        checkSettings()
        webView.reload()
    }

    fun getUrl(): String {
        return webView.url!!
    }

    fun getCert(): SslCertificate? {
        return webView.certificate
    }

    fun getWebCode(callback: ValueCallback<String>) =
        webView.evaluateJavascript("document.documentElement.outerHTML", callback)

    fun evaluateJs(code: String, callback: (String) -> Unit) {
        webView.evaluateJavascript(code) {
            callback(it)
        }
    }


    fun loadUrl() {
        val homepage = settings.decodeString("homepage", DEFAULT_HOMEPAGE)!!
        if (homepage == "$\$DEFAULT")
            webView.loadUrl(DEFAULT_HOMEPAGE)
        else
            webView.loadUrl(homepage)
    }

    fun startSearch(key: String) {
        webView.findAllAsync(key)
    }

    fun nextResult() = webView.findNext(false)
    fun forwardResult() = webView.findNext(true)
    fun clearResult() {
        webView.clearMatches()
    }

    fun savePage() {
        val path = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS)
        webView.saveWebArchive(path.absolutePath + "/" + propertiesPerSite.title + ".mht", false) {
            Toast.makeText(nestedActivity, "$it 已保存至下载文件夹", LENGTH_SHORT).show()
        }
    }
}