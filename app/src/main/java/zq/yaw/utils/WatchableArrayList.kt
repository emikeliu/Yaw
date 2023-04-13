package zq.yaw.utils

class WatchableArrayList<T> : ArrayList<T>() {
    private val watchers = ArrayList<(Int) -> Unit>()

    override fun add(element: T): Boolean {
        val result = super.add(element)
        watchers.forEach { it.invoke(size) }
        return result
    }

    override fun remove(element: T): Boolean {
        val result = super.remove(element)
        watchers.forEach { it.invoke(size) }
        return result
    }

    fun setOnSizeChangeListener(listener: (Int) -> Unit) {
        watchers.add(listener)
    }

    override fun removeAt(index: Int): T {
        val result = super.removeAt(index)
        watchers.forEach { it.invoke(size) }
        return result
    }
}