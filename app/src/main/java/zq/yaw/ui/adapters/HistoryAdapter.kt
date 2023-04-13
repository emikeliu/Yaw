package zq.yaw.ui.adapters

import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
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
import org.w3c.dom.Text
import zq.yaw.R
import zq.yaw.databinding.ItemHistoryInfoBinding
import zq.yaw.utils.JavaUtils
import zq.yaw.utils.TimeUtils
import zq.yaw.utils.shareString
import java.util.*

class HistoryAdapter(
    private val context: Context,
    private val sql: SQLiteOpenHelper,
    private val onClickCallback: (String) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryItemViewHolder>() {
    inner class HistoryItemViewHolder(v: View) : ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.title)
        val icon: ImageView = v.findViewById(R.id.icon)
        val url: TextView = v.findViewById(R.id.url)
        val card: CardView = v.findViewById(R.id.card)
        val time: TextView = v.findViewById(R.id.time)
    }

    private var nowAt = 0

    data class Item(val icon: Bitmap?, val url: String, val time: String, val title: String) {
        override fun equals(other: Any?): Boolean {
            return this.url == (other as Item).url
        }

        override fun hashCode(): Int {
            return url.hashCode()
        }
    }

    private val data = ArrayList<Item>()

    init {
        queryMore()
    }

    private fun queryMore() {
        val cursor = sql.readableDatabase.rawQuery(
            "select time, url, icon_base64, title from history order by time desc limit $nowAt, 10",
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
                Item(
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryItemViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return HistoryItemViewHolder(v)
    }

    override fun getItemCount(): Int {
        return data.size
    }



    override fun onBindViewHolder(holder: HistoryItemViewHolder, position: Int) {
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
                removeRecord(data[position], position)
                showingDialog?.cancel()
            }
            detailsBinding.share.setOnClickListener {
                shareString(context, data[position].url)
            }
            showingDialog = dialog.show()
            true
        }
        holder.time.text = JavaUtils.getDateFromNanoTime(data[position].time.toLong())
        holder.title.text = data[position].title
        holder.url.text = data[position].url
    }

    private fun removeRecord(item: Item, pos: Int) {
        data.remove(item)
        sql.writableDatabase.execSQL("delete from history where time = ${item.time}")
        notifyItemRemoved(pos)
        notifyItemRangeChanged(pos, data.size - pos)
        nowAt--
    }
}