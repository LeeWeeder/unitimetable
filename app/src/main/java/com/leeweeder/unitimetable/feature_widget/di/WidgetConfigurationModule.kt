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

package com.leeweeder.unitimetable.feature_widget.di

import com.leeweeder.unitimetable.feature_widget.data.repository.WidgetPreferencesDataStoreRepositoryImpl
import com.leeweeder.unitimetable.feature_widget.domain.WidgetPreferenceDataStoreRepository
import com.leeweeder.unitimetable.feature_widget.ui.WidgetConfigurationScreenViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val widgetConfigurationModule = module {
    viewModel { WidgetConfigurationScreenViewModel(get()) }
    single<WidgetPreferenceDataStoreRepository> { WidgetPreferencesDataStoreRepositoryImpl(get()) }
}