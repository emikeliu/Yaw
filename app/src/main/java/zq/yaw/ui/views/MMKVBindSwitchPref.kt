package zq.yaw.ui.views

import android.content.Context
import android.util.AttributeSet
import com.tencent.mmkv.MMKV

class MMKVBindSwitchPref(context: Context, attr: AttributeSet) : SwitchPref(context, attr) {
    private lateinit var mmkv: MMKV
    private lateinit var propName: String

    fun setMMKV(settings: MMKV) {
        this.mmkv = settings
    }

    fun setPropName(propName: String) {
        this.propName = propName
    }

    fun init() {
        setSwitch(mmkv.decodeBool(propName))
        addOnChangeListener {
            mmkv.encode(propName, it)
            setSwitch(it)
        }
    }
}