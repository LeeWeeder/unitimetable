package com.leeweeder.timetable.feature_widget.di

import com.leeweeder.timetable.feature_widget.UnitimetableWidget
import org.koin.dsl.module

val unitimetableWidgetModule = module {
    single { UnitimetableWidget(get(), get()) }
}