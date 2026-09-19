package com.example.flydigicooler

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.flydigicooler.ble.FlydigiCoolerManager
import com.example.flydigicooler.ui.CoolerControlScreen
import com.example.flydigicooler.ui.CoolerViewModel
import com.example.flydigicooler.ui.theme.CoolerAppTheme

class MainActivity : ComponentActivity() {

    private lateinit var coolerManager: FlydigiCoolerManager
    private lateinit var viewModel: CoolerViewModel

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            viewModel.startScan()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        coolerManager = FlydigiCoolerManager(applicationContext)

        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return CoolerViewModel(coolerManager) as T
            }
        })[CoolerViewModel::class.java]

        setContent {
            CoolerAppTheme {
                CoolerControlScreen(viewModel = viewModel)
            }
        }

        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }
        requestPermissionLauncher.launch(permissions)
    }
}