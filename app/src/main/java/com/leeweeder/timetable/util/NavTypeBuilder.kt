package com.leeweeder.timetable.util

import android.os.Bundle
import androidx.navigation.NavType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

inline fun <reified T : Any?> serializableType(isNullableAllowed: Boolean) =
    object : NavType<T>(isNullableAllowed = isNullableAllowed) {
        private val json = Json

        override fun get(bundle: Bundle, key: String) =
            bundle.getString(key)?.let<String, T>(json::decodeFromString)

        override fun parseValue(value: String): T = json.decodeFromString(value)

        override fun serializeAsValue(value: T): String = json.encodeToString(value)

        override fun put(bundle: Bundle, key: String, value: T) {
            bundle.putString(key, json.encodeToString(value))
        }
    }