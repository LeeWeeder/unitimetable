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

package com.leeweeder.unitimetable.data.repository

import com.leeweeder.unitimetable.data.data_source.dao.SessionDao
import com.leeweeder.unitimetable.domain.model.Session
import com.leeweeder.unitimetable.domain.repository.SessionRepository

class SessionRepositoryImpl(
    private val sessionDao: SessionDao
) : SessionRepository {

    override suspend fun updateSession(id: Int, label: String?) {
        sessionDao.updateSession(id = id, label = label)
    }

    override suspend fun updateSession(id: Int, crossRefId: Int) {
        sessionDao.updateSession(id = id, crossRefId = crossRefId)
    }

    override suspend fun updateSessions(sessions: List<Session>) {
        sessionDao.updateSessions(sessions)
    }
}