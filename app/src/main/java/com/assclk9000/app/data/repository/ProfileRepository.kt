package com.assclk9000.app.data.repository

import com.assclk9000.app.data.db.ProfileDao
import com.assclk9000.app.data.model.ClickAction
import com.assclk9000.app.data.model.ClickProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val profileDao: ProfileDao
) {

    fun getAllProfiles(): Flow<List<ClickProfile>> {
        return profileDao.getAllProfiles()
    }

    fun getProfileById(id: Long): Flow<ClickProfile?> {
        return profileDao.getProfileById(id)
    }

    fun getActionsForProfile(profileId: Long): Flow<List<ClickAction>> {
        return profileDao.getActionsForProfile(profileId)
    }

    suspend fun insertProfile(profile: ClickProfile): Long = withContext(Dispatchers.IO) {
        profileDao.insertProfile(profile)
    }

    suspend fun insertAction(action: ClickAction): Long = withContext(Dispatchers.IO) {
        profileDao.insertAction(action)
    }

    suspend fun insertActions(actions: List<ClickAction>) = withContext(Dispatchers.IO) {
        profileDao.insertActions(actions)
    }

    suspend fun updateProfile(profile: ClickProfile) = withContext(Dispatchers.IO) {
        profileDao.updateProfile(profile)
    }

    suspend fun updateAction(action: ClickAction) = withContext(Dispatchers.IO) {
        profileDao.updateAction(action)
    }

    suspend fun deleteProfile(profile: ClickProfile) = withContext(Dispatchers.IO) {
        profileDao.deleteProfile(profile)
    }

    suspend fun deleteAction(action: ClickAction) = withContext(Dispatchers.IO) {
        profileDao.deleteAction(action)
    }

    suspend fun deleteActionsForProfile(profileId: Long) = withContext(Dispatchers.IO) {
        profileDao.deleteActionsForProfile(profileId)
    }
}
