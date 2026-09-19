package com.example.flydigicooler.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface BleConnectionState {
    object Disconnected : BleConnectionState
    object Scanning : BleConnectionState
    data class Connecting(val deviceName: String) : BleConnectionState
    data class Connected(val deviceName: String) : BleConnectionState
    data class Error(val message: String) : BleConnectionState
}

data class CoolerTelemetry(
    val temperature: Float,
    val fanRpm: Int
)

class FlydigiCoolerManager(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    private var bluetoothGatt: BluetoothGatt? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    // 标准 BLE 透传 Service 与 Characteristic UUID
    private val SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
    private val CHAR_WRITE_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
    private val CHAR_NOTIFY_UUID = UUID.fromString("0000ffe2-0000-1000-8000-00805f9b34fb")
    private val CLIENT_CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    private val _connectionState = MutableStateFlow<BleConnectionState>(BleConnectionState.Disconnected)
    val connectionState = _connectionState.asStateFlow()

    private val _telemetryFlow = MutableSharedFlow<CoolerTelemetry>()
    val telemetryFlow = _telemetryFlow.asSharedFlow()

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val name = device.name ?: ""
            if (name.contains("Flydigi", ignoreCase = true) || name.matches(Regex(".*B[6-9]X?.*", RegexOption.IGNORE_CASE))) {
                stopScan()
                connectToDevice(device)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            _connectionState.value = BleConnectionState.Error("扫描失败: $errorCode")
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        val scanner = bluetoothAdapter?.bluetoothLeScanner
        if (scanner == null || bluetoothAdapter?.isEnabled != true) {
            _connectionState.value = BleConnectionState.Error("蓝牙未开启")
            return
        }
        _connectionState.value = BleConnectionState.Scanning
        scanner.startScan(scanCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        _connectionState.value = BleConnectionState.Connecting(device.name ?: "未知设备")
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                _connectionState.value = BleConnectionState.Disconnected
                close()
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                enableNotifications(gatt)
                val deviceName = gatt.device.name ?: "Flydigi Cooler"
                _connectionState.value = BleConnectionState.Connected(deviceName)
            }
        }

        @Deprecated("Deprecated in Android 13")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            parsePayload(characteristic.value)
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
            parsePayload(value)
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableNotifications(gatt: BluetoothGatt) {
        val service = gatt.getService(SERVICE_UUID) ?: return
        val notifyChar = service.getCharacteristic(CHAR_NOTIFY_UUID) ?: return
        gatt.setCharacteristicNotification(notifyChar, true)

        val descriptor = notifyChar.getDescriptor(CLIENT_CONFIG_DESCRIPTOR)
        descriptor?.let {
            it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(it)
        }
    }

    private fun parsePayload(bytes: ByteArray?) {
        if (bytes == null || bytes.size < 4) return
        val tempRaw = bytes[2].toInt() and 0xFF
        val temp = tempRaw.toFloat()
        val rpm = if (bytes.size >= 5) {
            ((bytes[3].toInt() and 0xFF) shl 8) or (bytes[4].toInt() and 0xFF)
        } else {
            5200
        }

        scope.launch {
            _telemetryFlow.emit(CoolerTelemetry(temp, rpm))
        }
    }

    @SuppressLint("MissingPermission")
    fun sendCommand(hex: String) {
        val gatt = bluetoothGatt ?: return
        val service = gatt.getService(SERVICE_UUID) ?: return
        val writeChar = service.getCharacteristic(CHAR_WRITE_UUID) ?: return

        writeChar.value = hexToBytes(hex)
        writeChar.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        gatt.writeCharacteristic(writeChar)
    }

    @SuppressLint("MissingPermission")
    fun close() {
        bluetoothGatt?.close()
        bluetoothGatt = null
    }

    private fun hexToBytes(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4) +
                    Character.digit(s[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}