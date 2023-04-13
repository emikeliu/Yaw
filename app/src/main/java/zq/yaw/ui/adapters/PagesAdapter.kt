package zq.yaw.ui.adapters

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.card.MaterialCardView
import zq.yaw.R
import zq.yaw.ui.MainActivity
import zq.yaw.ui.fragments.PageFragment
import zq.yaw.utils.WatchableArrayList
import zq.yaw.utils.getColorAccent
import java.util.*

class PagesAdapter(
    private val activity: MainActivity,
    private val list: WatchableArrayList<PageFragment>,
    private val switchPageCallback: (Int) -> Unit,
    private val closePageCallback: (Int, PagesAdapter) -> Unit,
    private val afterClickCallback: () -> Unit
) : Adapter<PagesAdapter.PagesViewHolder>() {
    inner class PagesViewHolder(v: View) : ViewHolder(v) {
        lateinit var title: TextView
        lateinit var icon: ImageView
        lateinit var closeTab: ImageView
        lateinit var pic: ImageView
        lateinit var card: MaterialCardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagesViewHolder {
        return PagesViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_page, parent, false)
        )
    }
    override fun onBindViewHolder(holder: PagesViewHolder, position: Int) {
        holder.closeTab = holder.itemView.findViewById(R.id.close_tab)
        holder.title = holder.itemView.findViewById(R.id.title)
        holder.icon = holder.itemView.findViewById(R.id.icon)
        holder.pic = holder.itemView.findViewById(R.id.pic)
        holder.title.text = list[holder.adapterPosition].propertiesPerSite.title
        holder.card = holder.itemView.findViewById(R.id.card)
        holder.closeTab.setOnClickListener {
            closePageCallback(holder.adapterPosition, this)
        }
        holder.pic.setOnClickListener {
            Log.d("pos", position.toString())
            switchPageCallback(holder.adapterPosition)
            afterClickCallback.invoke()
        }
        holder.pic.setImageBitmap(list[holder.adapterPosition].getPic())
        if (list[holder.adapterPosition].isIncognitoMode()) {
            holder.icon.setImageResource(R.drawable.baseline_visibility_off_24)
        } else if (list[holder.adapterPosition].propertiesPerSite.icon == null) {
            holder.icon.setImageResource(R.drawable.baseline_language_24)
        } else {
            holder.icon.setImageBitmap(list[position].propertiesPerSite.icon)
        }
        if (list[holder.adapterPosition] == activity.getNowShowingFragment()) {
            holder.card.strokeColor = getColorAccent(activity)
            holder.card.strokeWidth = 6
        } else {
            holder.card.strokeColor = Color.GRAY
            holder.card.strokeWidth = 3
        }
    }

    override fun getItemCount() = list.size

    fun itemMove(startPosition: Int, endPosition: Int) {
        Collections.swap(list, startPosition, endPosition)
        notifyItemMoved(startPosition, endPosition)
        if ((endPosition - startPosition) < 0) (endPosition - startPosition)
        else (endPosition - startPosition)
    }

    fun nowAt(): Int {
        return list.indexOf(activity.getNowShowingFragment())
    }

}

