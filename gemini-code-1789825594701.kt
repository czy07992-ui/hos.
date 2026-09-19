package com.example.flydigicooler.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flydigicooler.ble.BleConnectionState
import com.example.flydigicooler.ble.FlydigiCoolerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CoolerMode(val displayName: String) {
    SILENT("静音"),
    AUTO("极智"),
    OVERCLOCK("极寒")
}

data class CoolerUiState(
    val isConnected: Boolean = false,
    val isScanning: Boolean = false,
    val deviceName: String = "未连接设备",
    val statusText: String = "正在就绪",
    val currentTemperature: Float = 0.0f,
    val fanSpeedRpm: Int = 0,
    val currentMode: CoolerMode? = CoolerMode.AUTO,
    val isRgbEnabled: Boolean = true
)

class CoolerViewModel(
    private val coolerManager: FlydigiCoolerManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoolerUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            coolerManager.connectionState.collect { state ->
                when (state) {
                    is BleConnectionState.Connected -> {
                        _uiState.update {
                            it.copy(
                                isConnected = true,
                                isScanning = false,
                                deviceName = state.deviceName,
                                statusText = "已连接"
                            )
                        }
                    }
                    is BleConnectionState.Scanning -> {
                        _uiState.update { it.copy(isScanning = true, statusText = "正在扫描...") }
                    }
                    is BleConnectionState.Connecting -> {
                        _uiState.update { it.copy(statusText = "正在连接 ${state.deviceName}...") }
                    }
                    is BleConnectionState.Disconnected -> {
                        _uiState.update {
                            it.copy(
                                isConnected = false,
                                isScanning = false,
                                statusText = "未连接"
                            )
                        }
                    }
                    is BleConnectionState.Error -> {
                        _uiState.update {
                            it.copy(
                                isConnected = false,
                                isScanning = false,
                                statusText = state.message
                            )
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            coolerManager.telemetryFlow.collect { telemetry ->
                _uiState.update {
                    it.copy(
                        currentTemperature = telemetry.temperature,
                        fanSpeedRpm = telemetry.fanRpm
                    )
                }
            }
        }
    }

    fun startScan() {
        coolerManager.startScan()
    }

    fun setMode(mode: CoolerMode) {
        _uiState.update { it.copy(currentMode = mode) }
        val hexCmd = when (mode) {
            CoolerMode.SILENT -> "AA010155"
            CoolerMode.AUTO -> "AA010255"
            CoolerMode.OVERCLOCK -> "AA010355"
        }
        coolerManager.sendCommand(hexCmd)
    }

    fun toggleRgb(enabled: Boolean) {
        _uiState.update { it.copy(isRgbEnabled = enabled) }
        val hexCmd = if (enabled) "AA020155" else "AA020055"
        coolerManager.sendCommand(hexCmd)
    }

    override fun onCleared() {
        super.onCleared()
        coolerManager.close()
    }
}