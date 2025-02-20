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
    suspend fun getSubjectInstructorById(id: Int): SubjectInstructorCrossRefWithDetails?

    @Query("SELECT * FROM subjectinstructorcrossref WHERE id = :id")
    suspend fun getSubjectInstructorCrossRefById(id: Int): SubjectInstructorCrossRef?

    @Query("DELETE FROM subjectinstructorcrossref WHERE id = :id")
    suspend fun deleteSubjectInstructorCrossRefById(id: Int)

    @Query("SELECT * FROM subjectinstructorcrossref WHERE subjectId = :subjectId ")
    suspend fun getSubjectInstructorCrossRefsBySubjectId(subjectId: Int): List<SubjectInstructorCrossRef>

    @Query("SELECT * FROM subjectinstructorcrossref WHERE instructorId = :instructorId")
    suspend fun getSubjectInstructorCrossRefsByInstructorId(instructorId: Int): List<SubjectInstructorCrossRef>

    @Insert
    suspend fun insertSubjectInstructorCrossRefs(subjectInstructorCrossRefs: List<SubjectInstructorCrossRef>)

    @Query("""
        SELECT * FROM subjectinstructorcrossref
        WHERE id IN (:ids)
        AND instructorId IS NULL
    """)
    suspend fun getNullInstructorCrossRefIds(ids: List<Int>): List<SubjectInstructorCrossRef>

    @Update
    suspend fun updateSubjectInstructorCrossRefs(refs: List<SubjectInstructorCrossRef>)
}