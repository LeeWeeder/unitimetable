package com.leeweeder.timetable.domain.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    indices = [
        Index(value = ["code", "description"], unique = true)
    ]
)
data class Subject(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val description: String,
    val code: String,
    val dateAdded: Long = System.currentTimeMillis()
)