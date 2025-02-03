package com.leeweeder.timetable.data.source.instructor

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