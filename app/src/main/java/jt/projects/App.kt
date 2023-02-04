package jt.projects

import android.app.Application
import jt.projects.stopwatch.utils.TimestampProvider

class App : Application() {

    companion object {
        lateinit var instance: App
    }

    val timestampProvider = object : TimestampProvider {
        override fun getMs(): Long = System.currentTimeMillis()
    }


    override fun onCreate() {
        instance = this
        super.onCreate()
    }

}