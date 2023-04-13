package zq.yaw.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SelectPref(context: Context, attr: AttributeSet) : SimplePref(context, attr) {

    private lateinit var items: Array<CharSequence>

    fun setItems(items: Array<CharSequence>) {
        this.items = items
    }

    private lateinit var listener: (Int) -> Unit
    fun setOnSelectListener(listener: (Int) -> Unit) {
        this.listener = listener
    }

   fun init() {
       setOnClickListener {
           MaterialAlertDialogBuilder(context).setTitle(title.text).setItems(items) { d, v ->
               listener.invoke(v)
           }.show()
       }
   }

}