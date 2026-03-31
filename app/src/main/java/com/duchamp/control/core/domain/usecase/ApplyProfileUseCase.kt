package com.duchamp.control.core.domain.usecase

import com.duchamp.control.core.domain.model.PerformanceProfile
import com.duchamp.control.core.domain.repository.DeviceRepository
import javax.inject.Inject

/**
 * Use Cases - Business logic'i encapsulate eder
 */
class ApplyProfileUseCase @Inject constructor(
    private val repository: DeviceRepository
) {
    suspend operator fun invoke(profile: PerformanceProfile): Result<Unit> {
        return repository.applyProfile(profile)
    }
}

class GetAvailableProfilesUseCase @Inject constructor(
    private val repository: DeviceRepository
) {
    suspend operator fun invoke(): Result<List<PerformanceProfile>> {
        return repository.getProfiles()
    }
}

class SaveCustomProfileUseCase @Inject constructor(
    private val repository: DeviceRepository
) {
    suspend operator fun invoke(profile: PerformanceProfile): Result<Unit> {
        if (profile.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Profile name cannot be empty"))
        }
        return repository.saveCustomProfile(profile)
    }
}

class MonitorCpuUseCase @Inject constructor(
    private val repository: DeviceRepository
) {
    operator fun invoke() = repository.observeCpuState()
}

class MonitorGpuUseCase @Inject constructor(
    private val repository: DeviceRepository
) {
    operator fun invoke() = repository.observeGpuState()
}

class MonitorBatteryUseCase @Inject constructor(
    private val repository: DeviceRepository
) {
    operator fun invoke() = repository.observeBatteryState()
}

class MonitorMemoryUseCase @Inject constructor(
    private val repository: DeviceRepository
) {
    operator fun invoke() = repository.observeMemoryState()
}
