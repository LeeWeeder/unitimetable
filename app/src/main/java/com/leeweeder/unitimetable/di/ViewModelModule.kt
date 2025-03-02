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
        MainActivityViewModel(get(), get(), get(), get(), get())
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