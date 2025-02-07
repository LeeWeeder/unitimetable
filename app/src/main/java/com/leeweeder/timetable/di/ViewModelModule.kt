package com.leeweeder.timetable.di

import com.leeweeder.timetable.MainActivityViewModel
import com.leeweeder.timetable.ui.HomeViewModel
import com.leeweeder.timetable.ui.subjects.SubjectsScreenViewModel
import com.leeweeder.timetable.ui.timetable_setup.GetTimeTableNameViewModel
import com.leeweeder.timetable.ui.timetable_setup.TimeTableSetupViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        MainActivityViewModel(get())
    }

    viewModel {
        HomeViewModel(get(), get(), get(), get(), get(), get())
    }

    viewModel {
        TimeTableSetupViewModel(get(), get(), get())
    }

    viewModel {
        GetTimeTableNameViewModel(get(), get())
    }

    viewModel {
        SubjectsScreenViewModel(get())
    }
}