package com.leeweeder.timetable.feature_widget.di

import com.leeweeder.timetable.feature_widget.Widget
import org.koin.dsl.module

val widgetModule = module {
    single { Widget(get(), get()) }
}