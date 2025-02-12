package com.leeweeder.timetable.data.data_source.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.leeweeder.timetable.domain.model.SubjectInstructorCrossRef
import com.leeweeder.timetable.domain.relation.SubjectInstructorCrossRefWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectInstructorCrossRefDao {
    @Update
    suspend fun updateSubjectInstructorCrossRef(subjectInstructorCrossRef: SubjectInstructorCrossRef)

    @Insert
    suspend fun insertSubjectInstructorCrossRef(subjectInstructorCrossRef: SubjectInstructorCrossRef): Long

    @Transaction
    @Query("SELECT * FROM subject_instructor_view")
    fun observeSubjectInstructors(): Flow<List<SubjectInstructorCrossRefWithDetails>>

    @Transaction
    @Query("SELECT * FROM subject_instructor_view WHERE id = :id")
    suspend fun getSubjectInstructorWithId(id: Int): SubjectInstructorCrossRefWithDetails?
}