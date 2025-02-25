package com.leeweeder.unitimetable.di

import com.leeweeder.unitimetable.MainActivityViewModel
import com.leeweeder.unitimetable.ui.HomeViewModel
import com.leeweeder.unitimetable.ui.instructor.InstructorDialogViewModel
import com.leeweeder.unitimetable.ui.schedule.ScheduleEntryDialogViewModel
import com.leeweeder.unitimetable.ui.subject.SubjectDialogViewModel
import com.leeweeder.unitimetable.ui.timetable_setup.TimeTableNameViewModel
import com.leeweeder.unitimetable.ui.timetable_setup.TimetableSetupViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        MainActivityViewModel(get(), get(), get(), get(), get(), get())
    }

    viewModel {
        HomeViewModel(get(), get(), get(), get(), get())
    }

    viewModel {
        TimetableSetupViewModel(get(), get(), get())
    }

    viewModel {
        TimeTableNameViewModel(get(), get())
    }

    viewModel {
        ScheduleEntryDialogViewModel(get(), get(), get(), get())
    }

    viewModel {
        SubjectDialogViewModel(get(), get())
    }

    viewModel {
        InstructorDialogViewModel(get(), get())
    }
}