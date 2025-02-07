package com.leeweeder.timetable.ui.timetable_setup

import org.junit.Test

class GetTimeTableNameViewModelTest {

}

class CountTimeTableWithDefaultNamesTest {

    @Test
    fun `should return 1 for names contain default name`() {
        val names = listOf(
            "Timetable",
            "Test",
            "Main"
        )

        assert(countTimeTableWithDefaultNames(names) == 1)
    }

    @Test
    fun `should return 2 for names contain default name`() {
        val names = listOf(
            "Timetable",
            "Timetable (2)",
            "Main"
        )

        assert(countTimeTableWithDefaultNames(names) == 2)
    }
}