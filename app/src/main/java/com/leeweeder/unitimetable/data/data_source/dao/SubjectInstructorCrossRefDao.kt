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

package com.leeweeder.unitimetable.data.data_source.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.leeweeder.unitimetable.domain.model.SubjectInstructorCrossRef
import com.leeweeder.unitimetable.domain.relation.SubjectInstructorCrossRefWithDetails
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