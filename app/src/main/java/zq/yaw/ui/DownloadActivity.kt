package zq.yaw.ui

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import zq.yaw.databinding.ActivityDownloadBinding
import java.util.*

class DownloadActivity : BaseActivity() {
    data class DownTask(
        var downId: String,
        var title: String,
        var url: String,
        var size: String,
        var sizeTotal: String,
        var localAddress: String
    )

    private lateinit var binding: ActivityDownloadBinding
    private val timer = Timer()

    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        binding.info.setOnClickListener {
            MaterialAlertDialogBuilder(this).setMessage("由于Yaw不会请求其不应拥有的权限(例如: 管理所有文件权限)，因此，您不能在下载页面打开下载的文件。请至当前系统下载管理处打开。")
                .setPositiveButton("确定", null).show()
        }
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val list = ArrayList<DownTask>()
                val cursor = manager.query(DownloadManager.Query())

                while (cursor.moveToNext()) {
                    val downId =
                        cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_ID))
                    val title =
                        cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE))
                    val address =
                        cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI))
                    val localAddress =
                        cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                    val size =
                        cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val sizeTotal =
                        cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    list += DownTask(downId, title, address, size, sizeTotal, localAddress)
                }
                if (binding.rec.adapter != null)
                    (binding.rec.adapter as DownTaskAdapter).setData(list)
            }
        }, 10, 700)
        binding.rec.adapter = DownTaskAdapter {
            manager.remove(it)
        }
        binding.rec.layoutManager = LinearLayoutManager(this)
        binding.back.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
    }
}