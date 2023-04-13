package zq.yaw.utils

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.TypedValue
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.IOException


fun updateUi(function: () -> Unit) {
    CoroutineScope(Dispatchers.Main).launch {
        function.invoke()
    }
}

fun putToClipboard(context: Context, content: String) {
    val clipBoard =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipBoard.setPrimaryClip(ClipData.newPlainText("yaw", content))
    Toast.makeText(context, "已复制到剪切板", Toast.LENGTH_SHORT).show()
}

fun Long.ton(): String {
    var dVal = this.toDouble()
    val tonArray = arrayOf("B", "KB", "MB", "GB")
    var pos = 0
    while (dVal >= 1024 && pos < 4) {
        dVal /= 1024
        pos++
    }
    return String.format("%.2f ${tonArray[pos]}", dVal)
}

fun setResultAndExit(url: String, activity: Activity) {
    activity.setResult(1, Intent().putExtra("url", url))
    activity.finish()
}

fun getColorAccent(context: Context): Int {
    val typedValue = TypedValue()
    context.theme.resolveAttribute(android.R.attr.colorAccent, typedValue, true)
    return typedValue.data
}


fun bitmapToBase64(bitmap: Bitmap?): String? {
    var result: String? = null
    var os: ByteArrayOutputStream? = null
    try {
        if (bitmap != null) {
            os = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
            try {
                os.flush()
                os.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val bitmapBytes: ByteArray = os.toByteArray()
            result = java.util.Base64.getMimeEncoder().encodeToString(bitmapBytes)
        }
    } finally {
        try {
            if (os != null) {
                os.flush()
                os.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    return result

}

fun shareString(context: Context, content: String) {
    context.startActivity(Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_SUBJECT, "URL")
        putExtra(Intent.EXTRA_TEXT, content)
        type = "text/plain"
        Intent.createChooser(this, "Share")
    })
}

