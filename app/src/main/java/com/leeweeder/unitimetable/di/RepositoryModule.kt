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

import com.leeweeder.unitimetable.data.repository.DataStoreRepositoryImpl
import com.leeweeder.unitimetable.data.repository.InstructorRepositoryImpl
import com.leeweeder.unitimetable.data.repository.SessionRepositoryImpl
import com.leeweeder.unitimetable.data.repository.SubjectInstructorRepositoryImpl
import com.leeweeder.unitimetable.data.repository.SubjectRepositoryImpl
import com.leeweeder.unitimetable.data.repository.TimetableRepositoryImpl
import com.leeweeder.unitimetable.domain.repository.DataStoreRepository
import com.leeweeder.unitimetable.domain.repository.InstructorRepository
import com.leeweeder.unitimetable.domain.repository.SessionRepository
import com.leeweeder.unitimetable.domain.repository.SubjectInstructorRepository
import com.leeweeder.unitimetable.domain.repository.SubjectRepository
import com.leeweeder.unitimetable.domain.repository.TimetableRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<TimetableRepository> { TimetableRepositoryImpl(get(), get()) }
    single<DataStoreRepository> { DataStoreRepositoryImpl(get()) }
    single<SessionRepository> { SessionRepositoryImpl(get()) }
    single<SubjectRepository> { SubjectRepositoryImpl(get(), get(), get()) }
    single<InstructorRepository> { InstructorRepositoryImpl(get(), get()) }
    single<SubjectInstructorRepository> { SubjectInstructorRepositoryImpl(get(), get()) }
}