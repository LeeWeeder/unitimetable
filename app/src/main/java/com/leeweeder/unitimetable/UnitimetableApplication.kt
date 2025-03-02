/*
 * Copyright (C) 2025 Lyniel Jhay G. Maquilan (@LeeWeeder)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

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