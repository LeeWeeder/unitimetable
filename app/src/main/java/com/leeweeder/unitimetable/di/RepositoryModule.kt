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