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