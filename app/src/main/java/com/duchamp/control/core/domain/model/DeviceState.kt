package com.duchamp.control.core.domain.model

/**
 * Core domain models - Business logic katmanı
 */

data class CpuState(
    val governor: String = "",
    val currentFreqMhz: Int = 0,
    val maxFreqMhz: Int = 0,
    val minFreqMhz: Int = 0,
    val usage: Float = 0f,
    val temperature: Float = 0f,
    val cores: List<CpuCore> = emptyList()
)

data class CpuCore(
    val id: Int,
    val online: Boolean,
    val currentFreqMhz: Int,
    val usage: Float
)

data class GpuState(
    val governor: String = "",
    val currentFreqMhz: Int = 0,
    val maxFreqMhz: Int = 0,
    val minFreqMhz: Int = 0,
    val usage: Float = 0f,
    val temperature: Float = 0f
)

data class BatteryState(
    val level: Int = 0,
    val temperature: Float = 0f,
    val voltage: Float = 0f,
    val current: Float = 0f,
    val status: BatteryStatus = BatteryStatus.UNKNOWN,
    val health: BatteryHealth = BatteryHealth.UNKNOWN,
    val chargeLimit: Int = 100,
    val fastChargeEnabled: Boolean = false
)

enum class BatteryStatus { CHARGING, DISCHARGING, FULL, NOT_CHARGING, UNKNOWN }
enum class BatteryHealth { GOOD, OVERHEAT, DEAD, OVER_VOLTAGE, COLD, UNKNOWN }

data class MemoryState(
    val totalMb: Long = 0,
    val availableMb: Long = 0,
    val usedMb: Long = 0,
    val usagePercent: Float = 0f,
    val swappiness: Int = 60,
    val zramEnabled: Boolean = false,
    val zramSizeMb: Long = 0
)

data class ThermalZone(
    val name: String,
    val temperature: Float,
    val type: String
)

data class PerformanceProfile(
    val id: String,
    val name: String,
    val description: String,
    val cpuGovernor: String,
    val cpuMaxFreqMhz: Int,
    val cpuMinFreqMhz: Int,
    val gpuGovernor: String,
    val gpuMaxFreqMhz: Int,
    val gpuMinFreqMhz: Int,
    val isCustom: Boolean = false
)
