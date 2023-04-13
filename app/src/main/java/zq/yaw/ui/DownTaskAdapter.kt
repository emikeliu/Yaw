package zq.yaw.ui

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import zq.yaw.R
import zq.yaw.databinding.ItemDownTaskBinding
import zq.yaw.utils.updateUi
import kotlin.math.roundToInt

class DownTaskAdapter(
    private val removeCallback: (id: Long) -> Unit
) :
    Adapter<DownTaskAdapter.MyViewHolder>() {
    private var list = ArrayList<DownloadActivity.DownTask>()
    private val map = HashMap<Int, MyViewHolder>()

    @SuppressLint("NotifyDataSetChanged")
    fun setData(list: ArrayList<DownloadActivity.DownTask>) {
        val size = this.list.size
        this.list = list
        if (size != list.size)
            updateUi {
                notifyDataSetChanged()
            }
        updateUi {
            for (i in 0 until list.size) {
                map[i]?.let { setStatus(it, i) }
            }
        }
    }

    class MyViewHolder(val binding: ItemDownTaskBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemDownTaskBinding.inflate(LayoutInflater.from(parent.context))
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        map[position] = holder
//        holder.binding.card.setOnClickListener {
//            val f = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/" + list[position].title)
//            val uri = Uri.fromFile(f)
//            val intent = Intent(Intent.ACTION_VIEW, uri)
//            try {
//                context.startActivity(intent)
//            } catch (e: ActivityNotFoundException) {
//                MaterialAlertDialogBuilder(context)
//                    .setMessage("没有应用可以打开此文件。")
//                    .setPositiveButton("确定", null).show()
//            }
//        }
        setStatus(holder, position)
    }

    private fun setStatus(holder: MyViewHolder, position: Int) {
        holder.binding.close.setOnClickListener {
            removeCallback.invoke(list[position].downId.toLong())
            //notifyItemRemoved(position)
            //notifyItemRangeChanged(position, list.size)
        }
        holder.binding.title.text = list[position].title
        if (list[position].title.endsWith(".apk")) {
            holder.binding.icon.setImageResource(R.drawable.baseline_android_24)
        }
        holder.binding.url.text = list[position].url
        holder.binding.progress.progress =
            ((list[position].size.toInt() * 1.0 / list[position].sizeTotal.toInt() * 1.0) * 100).also {
                Log.d("progress", it.toString())
            }.roundToInt()
        holder.binding.progress.invalidate()
    }
}