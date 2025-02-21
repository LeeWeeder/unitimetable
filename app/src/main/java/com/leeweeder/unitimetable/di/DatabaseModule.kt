package com.leeweeder.unitimetable.di

import androidx.room.Room
import com.leeweeder.unitimetable.data.data_source.AppDatabase
import org.koin.dsl.module

val databaseModule = module {
    single<AppDatabase> {
        Room.databaseBuilder(
            get(),
            AppDatabase::class.java,
            "timetable.db"
        )
            .addCallback(AppDatabase.callback)
            .build()
    }

    single { get<AppDatabase>().timeTableDao }
    single { get<AppDatabase>().sessionDao }
    single { get<AppDatabase>().subjectDao }
    single { get<AppDatabase>().instructorDao }
    single { get<AppDatabase>().subjectInstructorCrossRefDao }
}