package zq.yaw.ui.views

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import zq.yaw.R

open class SimplePref(context: Context, attr: AttributeSet?) : RelativeLayout(context, attr) {
    constructor(context: Context) : this(context, null)

    private val v = LayoutInflater.from(context).inflate(R.layout.simple_pref, this, true)
    protected val title: TextView = v.findViewById(R.id.title)
    protected val sub: TextView = v.findViewById(R.id.sub_title)
    protected val layout: LinearLayout = v.findViewById(R.id.layout)
    protected val icon: ImageView = v.findViewById(R.id.icon)



    fun setTitles(title: String, subTitle: String) {
        this.title.text = title
        this.sub.text = subTitle
    }

    fun setSubTitle(subTitle: String) {
        this.sub.text = subTitle
    }

    fun setIcon(res: Int) {
        icon.setImageResource(res)
    }

    fun setIcon(bitmap: Bitmap) {
        icon.setImageBitmap(bitmap)
    }

    fun setOnClickListener(listener: (View) -> Unit) {
        layout.setOnClickListener(listener)
    }


}