package com.leeweeder.timetable.di

import androidx.room.Room
import com.leeweeder.timetable.data.data_source.AppDatabase
import org.koin.dsl.module

val databaseModule = module {
    single<AppDatabase> {
        Room.databaseBuilder(
            get(),
            AppDatabase::class.java,
            "timetable.db"
        ).build()
    }
    
    single { get<AppDatabase>().timeTableDao }
    single { get<AppDatabase>().sessionDao }
    single { get<AppDatabase>().subjectDao }
    single { get<AppDatabase>().instructorDao }
}