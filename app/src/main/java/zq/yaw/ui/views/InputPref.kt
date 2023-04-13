package zq.yaw.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import zq.yaw.databinding.SimpleInputLayoutBinding

class InputPref(context: Context, attr: AttributeSet) :  SimplePref(context, attr) {
    private var initText: String? = ""

    fun setInitText(initText: String) {
        this.initText = initText
    }
    private var hint: String? = null
    fun setHint(hint: String) {
        this.hint = hint
    }
    private val list = ArrayList<(String) -> Unit>()
    fun addOnChangeListener(listener: (String) -> Unit) {
        list.add(listener)
    }
    init {
        setOnClickListener {
                    val dv = SimpleInputLayoutBinding.inflate(LayoutInflater.from(context))
                    dv.hintTarget.hint = hint
                    if (initText != null)
                        dv.et.setText(initText)
                    else dv.et.setText(sub.text.toString())
                    val builder = MaterialAlertDialogBuilder(context).setTitle(title.text).setView(dv.root)
                        .setPositiveButton("确定") { _, _ ->
                            list.forEach {
                                it.invoke(dv.et.text.toString())
                            }
                        }.setNegativeButton("取消") { d, _ ->
                            d.cancel()
                        }
                    builder.show()
        }
    }

}