package com.example.mealx.ui.screens.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScanViewModel : ViewModel() {

    // Scan state
    private val _scanState = MutableStateFlow(ScanState())
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    // Scan result
    private val _scanResult = MutableStateFlow<String?>(null)
    val scanResult: StateFlow<String?> = _scanResult.asStateFlow()

    // Barcode types
    enum class BarcodeType {
        QR_CODE,
        BARCODE,
        DATA_MATRIX,
        PDF417,
        UNKNOWN
    }

    data class ScanState(
        val isScanning: Boolean = false,
        val hasCameraPermission: Boolean = false,
        val isCameraInitialized: Boolean = false,
        val lastScanTime: Long = 0L,
        val scanCount: Int = 0
    )

    fun setScanning(isScanning: Boolean) {
        _scanState.value = _scanState.value.copy(isScanning = isScanning)
    }

    fun setCameraPermission(granted: Boolean) {
        _scanState.value = _scanState.value.copy(hasCameraPermission = granted)
    }

    fun setCameraInitialized(initialized: Boolean) {
        _scanState.value = _scanState.value.copy(isCameraInitialized = initialized)
    }

    fun updateScanResult(result: String) {
        viewModelScope.launch {
            _scanResult.value = result
            _scanState.value = _scanState.value.copy(
                lastScanTime = System.currentTimeMillis(),
                scanCount = _scanState.value.scanCount + 1
            )
            Log.d("ScanViewModel", "Scan result updated: $result")
        }
    }

    fun clearScanResult() {
        _scanResult.value = null
    }

    fun resetScanState() {
        _scanState.value = ScanState()
        _scanResult.value = null
    }

    fun detectBarcodeType(barcodeData: String): BarcodeType {
        return when {
            barcodeData.startsWith("http://") || barcodeData.startsWith("https://") -> BarcodeType.QR_CODE
            barcodeData.matches(Regex("^[0-9]{8,13}$")) -> BarcodeType.BARCODE // EAN/UPC codes
            barcodeData.length <= 50 && barcodeData.matches(Regex("^[A-Za-z0-9+/=]+$")) -> BarcodeType.DATA_MATRIX
            barcodeData.length > 50 && barcodeData.contains("|") -> BarcodeType.PDF417
            else -> BarcodeType.UNKNOWN
        }
    }

    fun formatBarcodeData(barcodeData: String): String {
        val type = detectBarcodeType(barcodeData)
        return when (type) {
            BarcodeType.QR_CODE -> "🔗 URL: $barcodeData"
            BarcodeType.BARCODE -> "📦 Barcode: $barcodeData"
            BarcodeType.DATA_MATRIX -> "📊 Data Matrix: $barcodeData"
            BarcodeType.PDF417 -> "📄 PDF417: $barcodeData"
            BarcodeType.UNKNOWN -> "❓ Unknown format: $barcodeData"
        }
    }
}