package zq.yaw.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import zq.yaw.ui.fragments.PageFragment
import zq.yaw.utils.WatchableArrayList

class MainModel(application: Application) : AndroidViewModel(application) {
    val pagesList = WatchableArrayList<PageFragment>()

}