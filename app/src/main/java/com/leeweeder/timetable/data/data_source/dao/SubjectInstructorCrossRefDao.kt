package com.leeweeder.timetable.data.data_source.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.leeweeder.timetable.domain.model.SubjectInstructorCrossRef
import com.leeweeder.timetable.domain.relation.SubjectInstructorWithId
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectInstructorCrossRefDao {
    @Upsert
    suspend fun upsertSubjectInstructorCrossRef(subjectInstructorCrossRef: SubjectInstructorCrossRef): Long

    @Transaction
    @Query("SELECT * FROM subjectinstructorcrossref")
    fun observeSubjectInstructors(): Flow<List<SubjectInstructorWithId>>

    @Transaction
    @Query("SELECT * FROM subjectinstructorcrossref WHERE id = :id")
    suspend fun getSubjectInstructorWithId(id: Int): SubjectInstructorWithId?
}