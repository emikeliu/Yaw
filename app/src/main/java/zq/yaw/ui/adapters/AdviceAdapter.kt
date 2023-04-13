package zq.yaw.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import zq.yaw.R
import zq.yaw.utils.YawSQLiteHelper
import zq.yaw.utils.updateUi

class AdviceAdapter(
    private val sql: YawSQLiteHelper,
    private val onClickCallback: (String) -> Unit
) : RecyclerView.Adapter<AdviceAdapter.HistoryItemViewHolder>() {
    inner class HistoryItemViewHolder(v: View) : ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.title)
        val icon: ImageView = v.findViewById(R.id.icon)
        val url: TextView = v.findViewById(R.id.url)
        val card: CardView = v.findViewById(R.id.card)
        val time: TextView = v.findViewById(R.id.time)
    }

    private var data = ArrayList<HistoryAdapter.Item>()

    @SuppressLint("NotifyDataSetChanged")
    fun queryStartsWith(prefix: String) {
        data.clear()
        notifyDataSetChanged()
        CoroutineScope(Dispatchers.IO).launch {
            sql.queryStartsWith(prefix) {
                data = it.apply {
                    sortWith { i1, i2 ->
                        i1.url.length - i2.url.length
                    }
                }
                updateUi {
                    notifyDataSetChanged()
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryItemViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return HistoryItemViewHolder(v)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: HistoryItemViewHolder, position: Int) {
        data[position].icon?.let { holder.icon.setImageBitmap(it) }
        holder.card.setOnClickListener {
            onClickCallback.invoke(data[position].url)
        }
        holder.time.visibility = View.GONE
        holder.title.text = data[position].title
        holder.url.text = data[position].url
    }

    fun clearData() {
        data = ArrayList()
    }
}