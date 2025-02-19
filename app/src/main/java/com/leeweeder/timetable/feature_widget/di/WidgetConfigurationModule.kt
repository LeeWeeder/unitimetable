package com.leeweeder.timetable.feature_widget.di

import com.leeweeder.timetable.feature_widget.data.repository.WidgetPreferencesDataStoreRepositoryImpl
import com.leeweeder.timetable.feature_widget.domain.WidgetPreferenceDataStoreRepository
import com.leeweeder.timetable.feature_widget.ui.WidgetConfigurationScreenViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val widgetConfigurationModule = module {
    viewModel { WidgetConfigurationScreenViewModel(get(), get()) }
    single<WidgetPreferenceDataStoreRepository> { WidgetPreferencesDataStoreRepositoryImpl(get()) }
}