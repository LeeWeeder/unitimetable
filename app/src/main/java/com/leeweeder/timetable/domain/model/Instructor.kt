package com.leeweeder.timetable.domain.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    indices = [
        Index("name", unique = true)
    ]
)
data class Instructor(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)