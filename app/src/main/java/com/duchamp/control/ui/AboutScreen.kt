package com.duchamp.control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Hero kart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Memory, null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("DimensityTool",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(4.dp))
                    Text("v1.0  ·  MT6897 Sistem Kontrol Merkezi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        StatusBadge("Sinan Aslan", MaterialTheme.colorScheme.primary)
                        StatusBadge("Apache 2.0", MaterialTheme.colorScheme.secondary)
                        StatusBadge("Root Gerekli", MaterialTheme.colorScheme.tertiary)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Poco X6 Pro 5G  ·  Redmi K70E",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
            }
        }

        // Uygulama açıklaması
        item {
            SectionCard("Uygulama Hakkında", Icons.Default.Info) {
                Text(
                    "DimensityTool, MediaTek Dimensity 8300 Ultra (MT6897) işlemcili cihazlar için " +
                    "özel olarak geliştirilmiş kapsamlı bir sistem kontrol ve optimizasyon uygulamasıdır. " +
                    "Root erişimi ile donanımın tüm katmanlarına erişerek CPU, GPU, bellek, ağ, " +
                    "termal ve güvenlik ayarlarını tek bir arayüzden yönetmenizi sağlar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "LineageOS / AOSP tabanlı ROM'lar için optimize edilmiştir. " +
                    "mt6897-devs device tree'si baz alınarak geliştirilmiştir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }

        // Özellik grupları
        item {
            SectionCard("CPU & GPU Kontrolü", Icons.Default.Memory,
                badge = "8 çekirdek") {
                FeatureList(listOf(
                    "MT6897 cluster yapısı: Little (A510) / Big (A715) / Prime (A715)",
                    "Her cluster için bağımsız max/min frekans ayarı",
                    "Çekirdek bazlı online/offline kontrolü (cpu0-7)",
                    "CPU governor seçimi (schedutil, performance, powersave...)",
                    "GPU Mali G615-MC6 governor ve frekans kontrolü",
                    "MTK EAS/HMP scheduler parametreleri",
                    "CPU input boost ve sched_boost kontrolü",
                    "KernelKit FreqUnlock modül entegrasyonu"
                ))
            }
        }

        item {
            SectionCard("Performans Profilleri", Icons.Default.Tune, badge = "4 profil") {
                FeatureList(listOf(
                    "Pil Tasarrufu — Maksimum pil ömrü",
                    "Dengeli — Günlük kullanım için ideal",
                    "Performans — Yüksek performans modu",
                    "Oyun — Maksimum FPS ve düşük gecikme",
                    "Uygulama başına profil atama",
                    "Zamanlayıcı ile otomatik profil değiştirme",
                    "Uyku modu: ekran kapanınca/açılınca profil değiştirme",
                    "Termal throttle: sıcaklık eşiğinde otomatik profil"
                ))
            }
        }

        item {
            SectionCard("Canlı İzleme & Benchmark", Icons.Default.ShowChart) {
                FeatureList(listOf(
                    "CPU, GPU, RAM, sıcaklık gerçek zamanlı grafikleri",
                    "Anlık indirme/yükleme hız monitörü",
                    "Şarj akımı ve voltaj geçmişi grafikleri",
                    "FPS overlay (oyun sırasında ekranda gösterim)",
                    "CPU tek/çok çekirdek benchmark",
                    "RAM bant genişliği testi",
                    "Depolama okuma hız testi"
                ))
            }
        }

        item {
            SectionCard("Batarya Yönetimi", Icons.Default.BatteryFull) {
                FeatureList(listOf(
                    "Şarj limiti ayarı (%60-%100)",
                    "Hızlı şarj (Hypercharge) kontrolü",
                    "Pil tüketim tahmini (kalan süre hesabı)",
                    "Şarj akımı ve voltaj geçmişi",
                    "Bekleme / Normal / Yoğun kullanım senaryoları",
                    "Doze modu yönetimi ve istisna listesi"
                ))
            }
        }

        item {
            SectionCard("Bellek & Depolama", Icons.Default.Storage) {
                FeatureList(listOf(
                    "RAM kullanım izleme (LPDDR5X)",
                    "Swappiness ayarı (0-200)",
                    "ZRAM boyutu ve sıkıştırma algoritması (lz4/lzo/zstd)",
                    "I/O scheduler seçimi (UFS 4.0)",
                    "Depolama bölüm kullanım analizi"
                ))
            }
        }

        item {
            SectionCard("Ağ & Güvenlik", Icons.Default.Security) {
                FeatureList(listOf(
                    "TCP congestion algoritması (BBR, CUBIC, Reno)",
                    "DNS sunucu yönetimi (Google, Cloudflare, Quad9, AdGuard)",
                    "Wi-Fi güç tasarrufu kontrolü",
                    "VPN bağlantı durumu izleme",
                    "Firewall (iptables) uygulama bazlı kural yönetimi",
                    "Hosts dosyası editörü (reklam engelleme)",
                    "SELinux modu kontrolü",
                    "Root erişim logları",
                    "Kullanıcı CA sertifika yönetimi"
                ))
            }
        }

        item {
            SectionCard("Sistem & Araçlar", Icons.Default.Settings) {
                FeatureList(listOf(
                    "Magisk / KernelSU modül yönetimi",
                    "Kernel tweaks (dirty ratio, TCP, VM parametreleri)",
                    "Termal bölge izleme (30+ sensör)",
                    "Logcat görüntüleyici",
                    "Donanım bilgisi (sensörler, kamera, ekran)",
                    "Ayar yedekleme ve geri yükleme (JSON)",
                    "Fabrika sıfırlama",
                    "Ekran yenileme hızı, HBM, DC Dimming kontrolü",
                    "Ses (Dolby, hoparlör/mikrofon gain)",
                    "IR Blaster test aracı"
                ))
            }
        }

        // Gereksinimler
        item {
            SectionCard("Gereksinimler", Icons.Default.CheckCircle) {
                val reqs = listOf(
                    Icons.Default.PhoneAndroid to ("Cihaz" to "Poco X6 Pro 5G / Redmi K70E"),
                    Icons.Default.Memory       to ("SoC"   to "MediaTek Dimensity 8300 Ultra (MT6897)"),
                    Icons.Default.Android      to ("Android" to "14+ (API 34)"),
                    Icons.Default.AdminPanelSettings to ("Root" to "Magisk veya KernelSU"),
                    Icons.Default.Code         to ("ROM"   to "LineageOS / AOSP tabanlı"),
                    Icons.Default.Storage      to ("Depolama" to "~50 MB")
                )
                reqs.forEach { (icon, pair) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(10.dp))
                        Text(pair.first,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f))
                        Text(pair.second,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface)
                    }
                    if (pair != reqs.last().second) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            thickness = 0.5.dp)
                    }
                }
            }
        }

        // Uyarı
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Önemli Uyarılar",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(10.dp))
                    listOf(
                        "Yalnızca deneyimli kullanıcılar için tasarlanmıştır.",
                        "Yanlış sistem ayarları cihazın kararsız çalışmasına yol açabilir.",
                        "CPU/GPU frekans kilitleme aşırı ısınmaya neden olabilir.",
                        "SELinux Permissive modu güvenlik açıklarına yol açabilir.",
                        "Root erişimi cihazın garantisini geçersiz kılabilir.",
                        "Geliştirici kullanımdan doğacak zararlardan sorumlu tutulamaz."
                    ).forEach { w ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("·", color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(end = 6.dp))
                            Text(w, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }
        }

        // Lisans & kaynak
        item {
            SectionCard("Lisans & Kaynak", Icons.Default.Source) {
                InfoRow("Lisans", "Apache 2.0")
                InfoRow("Kaynak Kod", "github.com/yusufyusufyusuf474734-collab")
                InfoRow("Device Tree", "mt6897-devs/device_xiaomi_duchamp")
                InfoRow("ROM Tabanı", "LineageOS 21")
                InfoRow("Dil", "Kotlin + Jetpack Compose")
                InfoRow("Min SDK", "API 34 (Android 14)")
            }
        }

        // İmza
        item {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Sinan Aslan tarafından geliştirildi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center)
                    Spacer(Modifier.height(2.dp))
                    Text("DimensityTool © 2024  ·  MT6897",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                        textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
fun FeatureList(items: List<String>) {
    items.forEach { text ->
        Row(
            modifier = Modifier.padding(vertical = 3.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraSmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                modifier = Modifier.padding(top = 3.dp).size(6.dp)
            ) {}
            Spacer(Modifier.width(10.dp))
            Text(text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
