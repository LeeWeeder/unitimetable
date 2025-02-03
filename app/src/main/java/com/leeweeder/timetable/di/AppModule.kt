package com.leeweeder.timetable.di

import androidx.room.Room
import com.leeweeder.timetable.MainActivityViewModel
import com.leeweeder.timetable.data.DataStoreRepository
import com.leeweeder.timetable.data.DefaultDataStoreRepository
import com.leeweeder.timetable.data.source.TimeTableDatabase
import com.leeweeder.timetable.data.source.instructor.InstructorDataSource
import com.leeweeder.timetable.data.source.session.SessionDataSource
import com.leeweeder.timetable.data.source.subject.SubjectDataSource
import com.leeweeder.timetable.data.source.timetable.TimeTableDataSource
import com.leeweeder.timetable.ui.HomeViewModel
import com.leeweeder.timetable.ui.timetable_setup.GetTimeTableNameViewModel
import com.leeweeder.timetable.ui.timetable_setup.TimeTableSetupViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<TimeTableDatabase> {
        Room.databaseBuilder(
            get(),
            TimeTableDatabase::class.java,
            "timetable.db"
        ).build()
    }

    single {
        TimeTableDataSource(get<TimeTableDatabase>().timeTableDao)
    }

    single {
        val database = get<TimeTableDatabase>()
        SessionDataSource(database.sessionDao)
    }

    single {
        SubjectDataSource(get<TimeTableDatabase>().subjectDao)
    }

    single {
        InstructorDataSource(get<TimeTableDatabase>().instructorDao)
    }

    single<DataStoreRepository> {
        DefaultDataStoreRepository(get())
    }

    viewModel {
        MainActivityViewModel(get())
    }

    viewModel {
        HomeViewModel(get(), get(), get(), get(), get())
    }

    viewModel {
        TimeTableSetupViewModel(get(), get(), get(), get())
    }

    viewModel {
        GetTimeTableNameViewModel(get())
    }
}