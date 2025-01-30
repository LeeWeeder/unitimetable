package com.leeweeder.timetable.data.source.instructor

import kotlinx.coroutines.flow.Flow

class InstructorDataSource(private val dao: InstructorDao) {

    suspend fun getInstructors(): List<Instructor> {
        return dao.getInstructors()
    }

    fun observeInstructors(): Flow<List<Instructor>> {
        return dao.observeInstructors()
    }

    suspend fun deleteInstructor(instructor: Instructor) {
        dao.deleteInstructor(instructor)
    }

    suspend fun upsertInstructor(instructor: Instructor) {
        dao.deleteInstructor(instructor)
    }

}