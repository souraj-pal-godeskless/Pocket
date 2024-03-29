package com.cureius.pocket.feature_pot.presentation.pots.components

import androidx.compose.ui.graphics.vector.ImageVector
import com.cureius.pocket.feature_dashboard.domain.PotType

data class PotItem(
    val icon: ImageVector,
    val name: String,
    val capacity: Double,
    val fill: Double,
    val type: PotType
)