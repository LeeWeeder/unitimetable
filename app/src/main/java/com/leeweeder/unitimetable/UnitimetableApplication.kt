package com.leeweeder.unitimetable

import android.app.Application
import com.leeweeder.unitimetable.di.databaseModule
import com.leeweeder.unitimetable.di.repositoryModule
import com.leeweeder.unitimetable.di.viewModelModule
import com.leeweeder.unitimetable.feature_widget.di.widgetConfigurationModule
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
                    widgetConfigurationModule
                )
            )
        }
    }
}