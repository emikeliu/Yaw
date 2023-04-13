package zq.yaw.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import zq.yaw.ui.adapters.HistoryAdapter
import java.lang.ref.WeakReference
import java.util.*

class YawSQLiteHelper private constructor(
    context: Context,
    dbName: String,
    factory: CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, dbName, factory, version), java.io.Serializable {
    companion object {
        lateinit var context: WeakReference<Context>
        lateinit var sql: YawSQLiteHelper
        val bookmarkSql by lazy {
            makeBookmarkSqlHelper()
        }
        fun makeSqlHelper() {
            sql = YawSQLiteHelper(context.get()!!, "history", null, 1)
        }
        fun makeBookmarkSqlHelper(): YawSQLiteHelper {
            return YawSQLiteHelper(context.get()!!, "bookmarks", null, 1)
        }
    }

    private var writable: SQLiteDatabase = writableDatabase
    private var readable: SQLiteDatabase = readableDatabase
    override fun onCreate(db: SQLiteDatabase?) {
        if (databaseName == "history")
            db?.execSQL("create table history (time long primary key, url ntext, icon_base64 ntext, title ntext)")
        if (databaseName == "bookmarks")
            db?.execSQL("create table bookmarks (time long primary key, url ntext, icon_base64 ntext, title ntext)")
    }

    fun insertRecord(time: Long, url: String, title: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            writable.execSQL(
                "insert into ${databaseName}(time, url, title)" +
                        "values(${time}, ?, ?)", arrayOf(url, title)
            )
        }

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

    fun addIcon(icon: Bitmap?, time: Long) {
        val ib64String = bitmapToBase64(icon)
        CoroutineScope(Dispatchers.IO).launch {
            writable.execSQL(
                "update $databaseName " +
                        "set icon_base64 = '$ib64String' where time=$time"
            )
        }

    }

    fun clearAll() {
        CoroutineScope(Dispatchers.IO).launch {
            writable.execSQL("delete from history")
        }
    }

    fun addTitle(title: String?, time: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            writable.execSQL(
                "update $databaseName " +
                        "set title = ? where time=$time", arrayOf(title)
            )
        }

    }

    fun queryStartsWith(prefix: String, callback: (ArrayList<HistoryAdapter.Item>) -> Unit) {
        val list = ArrayList<HistoryAdapter.Item>()
        val cursor = readable.rawQuery(
            "select distinct url, title, icon_base64 from history where url like ? limit 5",
            arrayOf(
                "%$prefix%"
            )
        )
        while (cursor.moveToNext()) {
            var iconByte: ByteArray? = null
            cursor.getString(2)?.let { iconByte = Base64.getMimeDecoder().decode(it) }
            HistoryAdapter.Item(
                iconByte?.let { BitmapFactory.decodeByteArray(it, 0, it.size) },
                cursor.getString(0), "", cursor.getString(1)
            ).also {
                list.add(it)
            }
        }
        cursor.close()
        callback.invoke(list)
    }

}