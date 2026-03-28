package com.duchamp.control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Uygulama başlık kartı
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Memory,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "DimensityTool",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "Versiyon 1.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            "Geliştirici: Sinan Aslan",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "MediaTek Dimensity 8300 Ultra (MT6897)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                    Text(
                        "Poco X6 Pro 5G / Redmi K70E",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Uygulama hakkında
        item {
            SectionCard("Uygulama Hakkında", Icons.Default.Info) {
                Text(
                    "DimensityTool, Poco X6 Pro 5G ve Redmi K70E cihazları için özel olarak geliştirilmiş " +
                    "kapsamlı bir sistem kontrol ve optimizasyon uygulamasıdır. Root erişimi gerektiren bu " +
                    "uygulama, cihazınızın donanım ve yazılım özelliklerini tek bir arayüzden yönetmenizi sağlar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Uygulama, LineageOS tabanlı AOSP ROM'lar için optimize edilmiştir ve " +
                    "mt6897-devs device tree'si baz alınarak geliştirilmiştir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        // Özellikler
        item {
            SectionCard("Özellikler", Icons.Default.Stars) {
                val features = listOf(
                    Icons.Default.Memory to "CPU/GPU governor ve frekans kontrolü",
                    Icons.Default.ShowChart to "Canlı sistem izleme grafikleri",
                    Icons.Default.Tune to "Performans profilleri (Pil/Dengeli/Performans/Oyun)",
                    Icons.Default.BatteryFull to "Batarya yönetimi ve şarj limiti",
                    Icons.Default.Thermostat to "Termal sensör izleme",
                    Icons.Default.Storage to "Bellek, ZRAM ve I/O optimizasyonu",
                    Icons.Default.Security to "Güvenlik durumu ve hosts editörü",
                    Icons.Default.Apps to "Uygulama yöneticisi",
                    Icons.Default.BatterySaver to "Doze istisna yönetimi",
                    Icons.Default.Schedule to "Otomatik profil zamanlayıcı",
                    Icons.Default.Palette to "Tema ve kişiselleştirme",
                    Icons.Default.Code to "Kernel tweaks ve Magisk/KSU entegrasyonu"
                )
                features.forEach { (icon, text) ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(text, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Gereksinimler
        item {
            SectionCard("Gereksinimler", Icons.Default.CheckCircle) {
                InfoRow("Cihaz", "Poco X6 Pro 5G / Redmi K70E")
                InfoRow("SoC", "MediaTek Dimensity 8300 Ultra (MT6897)")
                InfoRow("Android", "14+ (API 34)")
                InfoRow("Root", "Magisk veya KernelSU")
                InfoRow("ROM", "LineageOS / AOSP tabanlı")
                InfoRow("Minimum SDK", "34")
            }
        }

        // Uyarılar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Önemli Uyarılar",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(10.dp))
                    val warnings = listOf(
                        "Bu uygulama yalnızca deneyimli kullanıcılar için tasarlanmıştır.",
                        "Yanlış sistem ayarları cihazınızın kararsız çalışmasına veya hasar görmesine yol açabilir.",
                        "Kernel Tweaks bölümündeki değişiklikler sistem çökmesine neden olabilir.",
                        "CPU/GPU frekans kilitleme aşırı ısınmaya yol açabilir.",
                        "SELinux'u Permissive moda almak güvenlik açıklarına neden olabilir.",
                        "Uygulama verilerini silmeden önce yedek alın.",
                        "Geliştirici bu uygulamanın kullanımından doğacak zararlardan sorumlu tutulamaz.",
                        "Root erişimi cihazınızın garantisini geçersiz kılabilir."
                    )
                    warnings.forEach { warning ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("•", color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(end = 6.dp, top = 1.dp))
                            Text(warning,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }
        }

        // Sorumluluk reddi
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Gavel, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Sorumluluk Reddi",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Bu uygulama \"olduğu gibi\" sunulmaktadır. Geliştirici Sinan Aslan, " +
                        "uygulamanın kullanımından kaynaklanabilecek herhangi bir donanım hasarı, " +
                        "veri kaybı veya garanti iptali konusunda sorumluluk kabul etmez. " +
                        "Uygulamayı kullanarak bu koşulları kabul etmiş sayılırsınız.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Lisans
        item {
            SectionCard("Lisans & Kaynak", Icons.Default.Source) {
                InfoRow("Lisans", "Apache 2.0")
                InfoRow("Kaynak Kod", "github.com/yusufyusufyusuf474734-collab")
                InfoRow("Device Tree", "mt6897-devs/device_xiaomi_duchamp")
                InfoRow("ROM Tabanı", "LineageOS 21")
                Spacer(Modifier.height(8.dp))
                Text(
                    "Bu uygulama açık kaynak kodludur. Katkıda bulunmak veya kaynak kodu " +
                    "incelemek için GitHub sayfasını ziyaret edebilirsiniz.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        // İmza
        item {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Sinan Aslan tarafından geliştirildi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "DimensityTool © 2024",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
