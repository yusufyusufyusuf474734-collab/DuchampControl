package com.duchamp.control.core.domain.repository

import com.duchamp.control.core.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface - Domain katmanı data katmanından bağımsız
 */
interface DeviceRepository {
    
    // Observables
    fun observeCpuState(): Flow<CpuState>
    fun observeGpuState(): Flow<GpuState>
    fun observeBatteryState(): Flow<BatteryState>
    fun observeMemoryState(): Flow<MemoryState>
    fun observeThermalZones(): Flow<List<ThermalZone>>
    
    // CPU Operations
    suspend fun setCpuGovernor(governor: String): Result<Unit>
    suspend fun setCpuMaxFreq(freqMhz: Int): Result<Unit>
    suspend fun setCpuMinFreq(freqMhz: Int): Result<Unit>
    suspend fun setCpuCoreOnline(coreId: Int, online: Boolean): Result<Unit>
    suspend fun getAvailableCpuGovernors(): Result<List<String>>
    suspend fun getAvailableCpuFrequencies(): Result<List<Int>>
    
    // GPU Operations
    suspend fun setGpuGovernor(governor: String): Result<Unit>
    suspend fun setGpuMaxFreq(freqMhz: Int): Result<Unit>
    suspend fun setGpuMinFreq(freqMhz: Int): Result<Unit>
    suspend fun getAvailableGpuGovernors(): Result<List<String>>
    suspend fun getAvailableGpuFrequencies(): Result<List<Int>>
    
    // Battery Operations
    suspend fun setChargeLimit(percent: Int): Result<Unit>
    suspend fun setFastCharge(enabled: Boolean): Result<Unit>
    
    // Memory Operations
    suspend fun setSwappiness(value: Int): Result<Unit>
    suspend fun clearMemory(): Result<Unit>
    
    // Profile Operations
    suspend fun applyProfile(profile: PerformanceProfile): Result<Unit>
    suspend fun getProfiles(): Result<List<PerformanceProfile>>
    suspend fun saveCustomProfile(profile: PerformanceProfile): Result<Unit>
    suspend fun deleteCustomProfile(profileId: String): Result<Unit>
    
    // System
    suspend fun isRooted(): Boolean
    suspend fun checkRootAccess(): Result<Boolean>
}
