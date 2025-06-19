package com.android.developers.testing.repository

import com.android.developers.androidify.data.ConnectedDevice
import com.android.developers.androidify.data.WearDeviceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeWearDeviceRepository : WearDeviceRepository {

    private val _connectedDevice = MutableStateFlow<ConnectedDevice?>(null)
    override val connectedDevice: StateFlow<ConnectedDevice?> = _connectedDevice.asStateFlow()

    fun setDevice(device: ConnectedDevice?) {
        _connectedDevice.value = device
    }
}