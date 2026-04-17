package com.assclk9000.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.assclk9000.app.data.model.ClickAction
import com.assclk9000.app.data.model.ClickProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {

    @Query("SELECT * FROM click_profiles ORDER BY updatedAt DESC")
    fun getAllProfiles(): Flow<List<ClickProfile>>

    @Query("SELECT * FROM click_profiles WHERE id = :id")
    fun getProfileById(id: Long): Flow<ClickProfile?>

    @Query("SELECT * FROM click_actions WHERE profileId = :profileId ORDER BY orderIndex ASC")
    fun getActionsForProfile(profileId: Long): Flow<List<ClickAction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ClickProfile): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(action: ClickAction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActions(actions: List<ClickAction>)

    @Update
    suspend fun updateProfile(profile: ClickProfile)

    @Update
    suspend fun updateAction(action: ClickAction)

    @Delete
    suspend fun deleteProfile(profile: ClickProfile)

    @Delete
    suspend fun deleteAction(action: ClickAction)

    @Query("DELETE FROM click_actions WHERE profileId = :profileId")
    suspend fun deleteActionsForProfile(profileId: Long)
}
