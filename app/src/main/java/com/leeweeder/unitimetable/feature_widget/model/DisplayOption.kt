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

package com.leeweeder.unitimetable.feature_widget.model

enum class DisplayOption(val label: String) {
    SUBJECT_CODE("Subject Code"),
    INSTRUCTOR("Instructor");

    companion object {
        val DEFAULT = setOf(SUBJECT_CODE, INSTRUCTOR)

        fun fromLabel(label: String): DisplayOption? =
            entries.find { it.label == label }

        fun fromString(labels: Set<String>): Set<DisplayOption> =
            labels.mapNotNull { label -> fromLabel(label) }.toSet()
    }

}

fun Set<DisplayOption>.toStringSet() = this.map { it.label }.toSet()