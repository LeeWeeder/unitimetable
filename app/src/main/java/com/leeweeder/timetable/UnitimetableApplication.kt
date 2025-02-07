package com.leeweeder.timetable

import android.app.Application
import com.leeweeder.timetable.di.databaseModule
import com.leeweeder.timetable.di.repositoryModule
import com.leeweeder.timetable.di.viewModelModule
import com.leeweeder.timetable.feature_widget.di.unitimetableWidgetModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class UnitimetableApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@UnitimetableApplication)
            modules(
                listOf(
                    databaseModule,
                    repositoryModule,
                    viewModelModule,
                    unitimetableWidgetModule
                )
            )
        }
    }
}