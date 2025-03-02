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