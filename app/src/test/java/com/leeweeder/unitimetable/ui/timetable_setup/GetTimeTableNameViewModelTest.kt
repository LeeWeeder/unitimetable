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

package com.leeweeder.unitimetable.ui.timetable_setup

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