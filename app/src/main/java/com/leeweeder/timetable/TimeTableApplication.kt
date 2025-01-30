package com.leeweeder.timetable

import android.app.Application
import com.leeweeder.timetable.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TimeTableApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TimeTableApplication)
            modules(appModule)
        }
    }
}