package zq.yaw.utils

import com.tencent.mmkv.MMKV
import zq.yaw.ui.fragments.PageFragment

class PageFragmentFactory {
    companion object {
        lateinit var settings: MMKV
    }

    fun new(): PageFragment {
        return PageFragment(
            settings.getString("homepage", "$\$DEFAULT")!!
        )
    }

    fun new(initPage: String): PageFragment {
        return PageFragment(initPage)
    }

    fun newIncognito(): PageFragment {
        return PageFragment(true)
    }
}