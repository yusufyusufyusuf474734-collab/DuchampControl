package com.duchamp.control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.duchamp.control.AppState
import com.duchamp.control.MainViewModel

@Composable
fun HardwareScreen(state: AppState, vm: MainViewModel, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionCard("SoC - MT6897", Icons.Default.Memory) {
                InfoRow("Çip", "MediaTek Dimensity 8300 Ultra")
                InfoRow("Süreç", "4nm TSMC")
                InfoRow("CPU", "1x3.35GHz A715 + 3x3.20GHz A715 + 4x2.20GHz A510")
                InfoRow("GPU", "Mali G615-MC6")
                InfoRow("Mimari", "ARMv9-A (arm64)")
                InfoRow("ABI", "arm64-v8a")
                InfoRow("Vulkan", "1.3")
                InfoRow("OpenGL ES", "AEP")
            }
        }

        item {
            SectionCard("Kamera", Icons.Default.CameraAlt) {
                InfoRow("Ana Kamera", "64MP f/1.7 OIS PDAF (1/2.0\")")
                InfoRow("Geniş Açı", "8MP f/2.2 120°")
                InfoRow("Makro", "2MP f/2.4")
                InfoRow("Ön Kamera", "16MP f/2.4 (1/3.06\")")
                InfoRow("Video", "4K@30fps, 1080p@60fps")
                InfoRow("OIS", "Ana kamera")
            }
        }

        item {
            SectionCard("Bağlantı", Icons.Default.Bluetooth) {
                InfoRow("Bluetooth", "BT Audio AIDL (aptX, LDAC)")
                InfoRow("Wi-Fi", "802.11ax (Wi-Fi 6), Direct, P2P, NAN")
                InfoRow("NFC", "NXP - HCE, ESE, UICC, Mifare")
                InfoRow("USB", "MediaTek USB Gadget + Audio Accessory")
                InfoRow("GPS", "GPS, GLONASS, BeiDou, Galileo")
                InfoRow("5G", "Sub-6GHz")
            }
        }

        item {
            SectionCard("Sensörler", Icons.Default.Sensors) {
                InfoRow("İvmeölçer", "android.hardware.sensor.accelerometer")
                InfoRow("Jiroskop", "android.hardware.sensor.gyroscope")
                InfoRow("Pusula", "android.hardware.sensor.compass")
                InfoRow("Yakınlık", "android.hardware.sensor.proximity")
                InfoRow("Işık", "android.hardware.sensor.light")
                InfoRow("Adım Sayacı", "android.hardware.sensor.stepcounter")
                InfoRow("Adım Dedektörü", "android.hardware.sensor.stepdetector")
                InfoRow("Head Tracker", "android.hardware.sensor.dynamic.head_tracker")
                InfoRow("HAL", "Xiaomi Multihal v2")
            }
        }

        item {
            SectionCard("Depolama & Bellek", Icons.Default.Storage) {
                InfoRow("RAM", "8/12GB LPDDR5X")
                InfoRow("Depolama", "256/512GB UFS 4.0")
                InfoRow("Dosya Sistemi", "EROFS (vendor) / EXT4 (data)")
                InfoRow("Dinamik Bölümler", "Evet (9.6GB super)")
                InfoRow("A/B OTA", "Virtual A/B (VABC)")
            }
        }

        item {
            SectionCard("Güç", Icons.Default.Power) {
                InfoRow("Batarya", "5000/5500 mAh Li-Po")
                InfoRow("Hızlı Şarj", "Hypercharge")
                InfoRow("Güç HAL", "libperfmgr (Pixel Power HAL)")
                InfoRow("Termal HAL", "android.hardware.thermal-service.pixel")
                InfoRow("Vibratör", "QTI AIDL V2")
            }
        }

        item {
            SectionCard("Kernel Modülleri (${state.kernelModules.size})", Icons.Default.Code) {
                if (state.kernelModules.isEmpty()) {
                    Text("Modül bilgisi alınamadı")
                } else {
                    state.kernelModules.take(30).forEach { mod ->
                        Text(
                            mod,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }
                    if (state.kernelModules.size > 30) {
                        Text(
                            "... ve ${state.kernelModules.size - 30} modül daha",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
