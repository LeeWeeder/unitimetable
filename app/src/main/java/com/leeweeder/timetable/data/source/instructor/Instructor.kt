package com.leeweeder.timetable.data.source.instructor

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Instructor(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)