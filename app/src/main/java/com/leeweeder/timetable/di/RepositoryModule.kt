package com.leeweeder.timetable.di

import com.leeweeder.timetable.data.repository.DataStoreRepositoryImpl
import com.leeweeder.timetable.data.repository.InstructorRepositoryImpl
import com.leeweeder.timetable.data.repository.SessionRepositoryImpl
import com.leeweeder.timetable.data.repository.SubjectInstructorRepositoryImpl
import com.leeweeder.timetable.data.repository.SubjectRepositoryImpl
import com.leeweeder.timetable.data.repository.TimeTableRepositoryImpl
import com.leeweeder.timetable.domain.repository.DataStoreRepository
import com.leeweeder.timetable.domain.repository.InstructorRepository
import com.leeweeder.timetable.domain.repository.SessionRepository
import com.leeweeder.timetable.domain.repository.SubjectInstructorRepository
import com.leeweeder.timetable.domain.repository.SubjectRepository
import com.leeweeder.timetable.domain.repository.TimeTableRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<TimeTableRepository> { TimeTableRepositoryImpl(get(), get()) }
    single<DataStoreRepository> { DataStoreRepositoryImpl(get()) }
    single<SessionRepository> { SessionRepositoryImpl(get()) }
    single<SubjectRepository> { SubjectRepositoryImpl(get(), get(), get()) }
    single<InstructorRepository> { InstructorRepositoryImpl(get()) }
    single<SubjectInstructorRepository> { SubjectInstructorRepositoryImpl(get(), get()) }
}