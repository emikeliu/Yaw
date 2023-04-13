package zq.yaw.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import zq.yaw.ui.fragments.BookmarkFragment
import zq.yaw.ui.fragments.HistoryFragment

class MarkPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return if (position == 1) HistoryFragment()
            else BookmarkFragment()
    }

}