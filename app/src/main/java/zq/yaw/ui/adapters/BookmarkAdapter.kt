package zq.yaw.ui.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import zq.yaw.R
import zq.yaw.databinding.ItemHistoryInfoBinding
import zq.yaw.utils.YawSQLiteHelper
import zq.yaw.utils.shareString
import java.util.*

class BookmarkAdapter(
    private val sql: YawSQLiteHelper,
    private val onClickCallback: (String) -> Unit
) : RecyclerView.Adapter<BookmarkAdapter.BookmarkItemViewHolder>() {
    private var nowAt = 0
    private lateinit var context: Context
    private var data = ArrayList<HistoryAdapter.Item>()
    inner class BookmarkItemViewHolder(v: View) : ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.title)
        val icon: ImageView = v.findViewById(R.id.icon)
        val url: TextView = v.findViewById(R.id.url)
        val card: CardView = v.findViewById(R.id.card)
        val time: TextView = v.findViewById(R.id.time)
    }
    init {
        queryMore()
    }
    private fun queryMore() {
        val cursor = sql.readableDatabase.rawQuery(
            "select time, url, icon_base64, title from bookmarks order by time desc limit $nowAt, 10",
            null
        )
        cursor.moveToFirst()
        while (cursor.moveToNext()) {
            val time = cursor.getString(0)
            val url = cursor.getString(1)
            var iconByte: ByteArray? = null
            cursor.getString(2)?.let { iconByte = Base64.getMimeDecoder().decode(it) }
            val title = cursor.getString(3)
            data.add(
                HistoryAdapter.Item(
                    iconByte?.let { BitmapFactory.decodeByteArray(it, 0, it.size) },
                    url,
                    time.toString(),
                    title ?: url
                )
            )
        }
        cursor.close()
        nowAt += 10
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkItemViewHolder {
        if (!::context.isInitialized) context = parent.context
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return BookmarkItemViewHolder(v)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: BookmarkItemViewHolder, position: Int) {
        if (position == data.size - 1) {
            queryMore()
            android.os.Handler(Looper.getMainLooper()).post {
                notifyItemRangeInserted(nowAt, 10)
                Log.d("Adapter", "Added 10 registers")
            }
        }
        data[position].icon?.let { holder.icon.setImageBitmap(it) }
        holder.card.setOnClickListener {
            onClickCallback.invoke(data[position].url)
        }
        holder.time.visibility = View.GONE
        holder.title.text = data[position].title
        holder.url.text = data[position].url
        holder.card.setOnLongClickListener {
            val detailsBinding = ItemHistoryInfoBinding.inflate(LayoutInflater.from(context))
            val dialog = MaterialAlertDialogBuilder(context).setView(detailsBinding.root)
                .setPositiveButton("确定") { d, _ ->
                    d.cancel()
                }
            var showingDialog: androidx.appcompat.app.AlertDialog? = null
            detailsBinding.title.text = data[position].title
            detailsBinding.url.text = data[position].url
            detailsBinding.visitTime.text = data[position].time
            detailsBinding.delete.setOnClickListener {
                removeRecord(position)
                showingDialog?.cancel()
            }
            detailsBinding.share.setOnClickListener {
                shareString(context, data[position].url)
            }
            showingDialog = dialog.show()
            true
        }

    }

    private fun removeRecord(pos: Int) {
        val item = data[pos]
        data.remove(item)
        sql.writableDatabase.execSQL("delete from bookmarks where time = ${item.time}")
        notifyItemRemoved(pos)
        notifyItemRangeChanged(pos, data.size - pos)
        nowAt--
    }

}