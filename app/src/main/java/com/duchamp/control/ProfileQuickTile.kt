package com.duchamp.control

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class ProfileQuickTile : TileService() {

    private val profiles = listOf("powersave", "balanced", "performance", "gaming")
    private val labels = listOf("Pil", "Dengeli", "Performans", "Oyun")

    override fun onStartListening() {
        super.onStartListening()
        AppPrefs.init(applicationContext)
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        AppPrefs.init(applicationContext)
        val current = AppPrefs.quickTileProfileId
        val idx = profiles.indexOf(current)
        val next = profiles[(idx + 1) % profiles.size]
        AppPrefs.quickTileProfileId = next
        val profile = PerformanceProfiles.presets.find { it.id == next }
        if (profile != null) {
            PerformanceProfiles.apply(profile)
        }
        updateTile()
    }

    private fun updateTile() {
        val profileId = AppPrefs.quickTileProfileId
        val idx = profiles.indexOf(profileId).coerceAtLeast(0)
        qsTile?.apply {
            label = "DT: ${labels[idx]}"
            state = Tile.STATE_ACTIVE
            updateTile()
        }
    }
}
