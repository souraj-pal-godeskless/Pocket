package com.cureius.pocket.feature_pot.domain.repository

import com.cureius.pocket.feature_pot.domain.model.Pot
import kotlinx.coroutines.flow.Flow

interface PotRepository {
    fun getPots(): Flow<List<Pot>>

    suspend fun getPotById(id: Int): Pot?

    suspend fun insertPot(pot: Pot)

    suspend fun deletePot(pot: Pot)
}