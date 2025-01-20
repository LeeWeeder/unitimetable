package com.leeweeder.timetable.data

import androidx.compose.ui.graphics.Color

sealed class Course(val code: String, val instructor: String, val color: Color) {
    data object PElec3: Course("P Elec 3", "Jonathan Giltendez", Color.Red)
    data object CC225: Course("CC 225", "Apple Illustrisimo", Color.Black)
    data object PC224: Course("PC 224", "Fatima Cordova", Color.Blue)
    data object GECTCW: Course("GEC-TCW", "Diane Ver Vargas", Color.Cyan)
    data object PC223: Course("PC 223", "Jimdel Dela Cruz", Color.Gray)
    data object AP3: Course("AP 3", "Charena Cueva", Color.Green)
}