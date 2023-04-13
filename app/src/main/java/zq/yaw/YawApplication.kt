package zq.yaw

import android.app.Application
import java.io.File
import java.nio.charset.Charset
import kotlin.system.exitProcess

class YawApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler())
    }

    inner class CrashHandler : Thread.UncaughtExceptionHandler {
        override fun uncaughtException(thread: Thread, ex: Throwable) {
            val f = File(this@YawApplication.dataDir.absolutePath + "/error.log")
            if (!f.exists())
                f.createNewFile()
            f.appendText(ex.stackTraceToString(), Charset.defaultCharset())
            exitProcess(0)
        }
    }
}