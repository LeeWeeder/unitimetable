package com.leeweeder.timetable.data.source.instructor

import kotlinx.coroutines.flow.Flow

class InstructorDataSource(private val dao: InstructorDao) {

    suspend fun getInstructors(): List<Instructor> {
        return dao.getInstructors()
    }

    suspend fun getInstructorById(id: Int): Instructor? {
        return dao.getInstructorById(id)
    }

    fun observeInstructors(): Flow<List<Instructor>> {
        return dao.observeInstructors()
    }

    suspend fun deleteInstructor(instructor: Instructor) {
        dao.deleteInstructor(instructor)
    }

    suspend fun upsertInstructor(instructor: Instructor): Int {
        return dao.upsertInstructor(instructor).toInt()
    }

}