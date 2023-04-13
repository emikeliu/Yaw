package zq.yaw.ui.views

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import zq.yaw.R

open class SwitchPref(context: Context, attr: AttributeSet?) : RelativeLayout(context, attr) {

    private val list = ArrayList<(Boolean) -> Unit>()
    private val v = LayoutInflater.from(context).inflate(R.layout.switch_perf, this, true)
    private val title: TextView = v.findViewById(R.id.title)
    private val sub: TextView = v.findViewById(R.id.sub_title)
    private val switch: SwitchCompat = v.findViewById(R.id._switch)
    private val layout: LinearLayout = v.findViewById(R.id.layout)
    private val icon: ImageView = v.findViewById(R.id.icon)

    fun addOnChangeListener(listener: (Boolean) -> Unit) {
        list.add(listener)
    }

    fun setSwitch(on: Boolean) {
        switch.isChecked = on
    }

    fun setTitles(title: String, subTitle: String) {
        this.title.text = title
        this.sub.text = subTitle
    }

    fun setIcon(res: Int) {
        icon.setImageResource(res)
    }

    fun setIcon(bitmap: Bitmap) {
        icon.setImageBitmap(bitmap)
    }

    init {
        switch.setOnCheckedChangeListener { v, b ->
            list.forEach {
                it.invoke(b)
            }
        }
        layout.setOnClickListener {
            switch.isChecked = !switch.isChecked
        }
    }

}