package com.example.deviceinfo

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class InfoItem(val title: String, val value: String)
data class InfoCategory(val name: String, val items: List<InfoItem>)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val data = collectDeviceInfo(this)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DeviceInfoScreen(categories = data)
                }
            }
        }
    }

    private fun collectDeviceInfo(context: Context): List<InfoCategory> {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)

        val totalRamGb = memInfo.totalMem / (1024 * 1024 * 1024.0)
        val availRamGb = memInfo.availMem / (1024 * 1024 * 1024.0)

        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct: Float = (level * 100) / scale.toFloat()
        val tempTenths = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val batteryTemp = tempTenths / 10.0

        return listOf(
            InfoCategory(
                name = "Cihaz & Sistem",
                items = listOf(
                    InfoItem("Marka", Build.MANUFACTURER.uppercase()),
                    InfoItem("Model", Build.MODEL),
                    InfoItem("Cihaz Kodu", Build.DEVICE),
                    InfoItem("Android Sürümü", Build.VERSION.RELEASE),
                    InfoItem("API Seviyesi", Build.VERSION.SDK_INT.toString()),
                    InfoItem("Güvenlik Yaması", Build.VERSION.SECURITY_PATCH)
                )
            ),
            InfoCategory(
                name = "İşlemci (SoC)",
                items = listOf(
                    InfoItem("Donanım / Kart", Build.HARDWARE),
                    InfoItem("SoC Üreticisi", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Build.SOC_MANUFACTURER else "N/A"),
                    InfoItem("SoC Modeli", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Build.SOC_MODEL else "N/A"),
                    InfoItem("Mimari (ABIs)", Build.SUPPORTED_ABIS.joinToString(", ")),
                    InfoItem("Çekirdek Sayısı", Runtime.getRuntime().availableProcessors().toString())
                )
            ),
            InfoCategory(
                name = "Bellek & Batarya",
                items = listOf(
                    InfoItem("Toplam RAM", "%.2f GB".format(totalRamGb)),
                    InfoItem("Kullanılabilir RAM", "%.2f GB".format(availRamGb)),
                    InfoItem("Pil Seviyesi", "%d%%".format(batteryPct.toInt())),
                    InfoItem("Pil Sıcaklığı", "%.1f °C".format(batteryTemp))
                )
            )
        )
    }
}

@Composable
fun DeviceInfoScreen(categories: List<InfoCategory>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(categories) { category ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = category.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    category.items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = item.title, color = Color.Gray, fontSize = 14.sp)
                            Text(
                                text = item.value,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
