package com.example.proxymanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.Authenticator
import java.net.PasswordAuthentication
import java.net.Proxy
import java.net.InetSocketAddress
import java.net.URL
import java.net.HttpURLConnection

private val Bg = Color(0xFF080D18)
private val Panel = Color(0xFF111B2D)
private val Accent = Color(0xFF5B8CFF)
private val Muted = Color(0xFF9AAAC4)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("proxy_config", MODE_PRIVATE)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Accent, background = Bg, surface = Panel,
                    onBackground = Color.White, onSurface = Color.White
                )
            ) {
                ProxyDashboard(
                    initialHost = prefs.getString("host", "") ?: "",
                    initialPort = prefs.getString("port", "10000") ?: "10000",
                    initialUser = prefs.getString("user", "") ?: "",
                    initialCountry = prefs.getString("country", "Indonesia") ?: "Indonesia",
                    onSave = { host, port, user, country ->
                        prefs.edit().putString("host", host).putString("port", port)
                            .putString("user", user).putString("country", country).apply()
                    }
                )
            }
        }
    }
}

@Composable
private fun ProxyDashboard(
    initialHost: String, initialPort: String, initialUser: String, initialCountry: String,
    onSave: (String, String, String, String) -> Unit
) {
    var host by remember { mutableStateOf(initialHost) }
    var port by remember { mutableStateOf(initialPort) }
    var username by remember { mutableStateOf(initialUser) }
    var password by remember { mutableStateOf("") }
    var country by remember { mutableStateOf(initialCountry) }
    var proxyKind by remember { mutableStateOf("Residential") }
    var status by remember { mutableStateOf("Belum diuji") }
    var resultIp by remember { mutableStateOf("—") }
    var busy by remember { mutableStateOf(false) }
    var countryMenu by remember { mutableStateOf(false) }
    var kindMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(color = Accent.copy(alpha = .18f), shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Default.Shield, null, tint = Accent, modifier = Modifier.padding(12.dp).size(28.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("PROXY MANAGER", fontSize = 19.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text("DATAIMPULSE • CONTROL PANEL", color = Muted, fontSize = 11.sp)
            }
            Surface(color = Color(0xFF173D35), shape = RoundedCornerShape(20.dp)) {
                Text("LOCAL", color = Color(0xFF62D6A5), modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("STATUS KONEKSI", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Circle, null, tint = if (status.startsWith("Berhasil")) Color(0xFF45D6A0) else Color(0xFFFFB454), modifier = Modifier.size(10.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(status, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("IP TERDETEKSI", color = Muted, fontSize = 10.sp)
                        Text(resultIp, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("LOKASI PILIHAN", color = Muted, fontSize = 10.sp)
                        Text(country, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Text("KONFIGURASI PROXY", fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 13.sp)
        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Jenis proxy", color = Muted, fontSize = 12.sp)
                Box {
                    OutlinedButton(onClick = { kindMenu = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(proxyKind, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                    DropdownMenu(expanded = kindMenu, onDismissRequest = { kindMenu = false }) {
                        listOf("Residential", "Datacenter", "Mobile").forEach {
                            DropdownMenuItem(text = { Text(it) }, onClick = { proxyKind = it; kindMenu = false })
                        }
                    }
                }
                Text("Negara / lokasi IP", color = Muted, fontSize = 12.sp)
                Box {
                    OutlinedButton(onClick = { countryMenu = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(country, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                    DropdownMenu(expanded = countryMenu, onDismissRequest = { countryMenu = false }) {
                        listOf("Indonesia", "Singapore", "United States", "United Kingdom", "Japan", "Germany", "Netherlands").forEach {
                            DropdownMenuItem(text = { Text(it) }, onClick = { country = it; countryMenu = false })
                        }
                    }
                }
                OutlinedTextField(host, { host = it }, label = { Text("Proxy host") }, placeholder = { Text("Host dari dashboard DataImpulse") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(port, { port = it.filter(Char::isDigit) }, label = { Text("Port") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(username, { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(password, { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), singleLine = true, visualTransformation = PasswordVisualTransformation())
                Button(
                    onClick = { onSave(host, port, username, country); status = "Konfigurasi tersimpan" },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Simpan konfigurasi")
                }
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            busy = true
                            status = "Menguji koneksi..."
                            val check = testProxy(host, port, username, password)
                            status = check.first
                            resultIp = check.second
                            busy = false
                        }
                    },
                    enabled = !busy && host.isNotBlank() && port.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (busy) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Icon(Icons.Default.NetworkCheck, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (busy) "Sedang menguji..." else "Tes koneksi & cek IP")
                }
            }
        }

        Text("JENIS PROXY", fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 13.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TypeTile("Residential", "IP jaringan rumah", Modifier.weight(1f))
            TypeTile("Datacenter", "IP server", Modifier.weight(1f))
            TypeTile("Mobile", "IP seluler", Modifier.weight(1f))
        }
        Text(
            "Catatan: pilihan jenis dan negara hanya label konfigurasi. Ketersediaannya bergantung pada paket dan format endpoint dari DataImpulse. Aplikasi ini menyimpan konfigurasi dan menguji endpoint; belum mengalihkan seluruh trafik Android.",
            color = Muted, fontSize = 11.sp, lineHeight = 16.sp
        )
        Text("Proxy Manager • Versi 1.0", color = Muted, fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
private fun TypeTile(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, fontSize = 10.sp, color = Muted, lineHeight = 13.sp)
        }
    }
}

private suspend fun testProxy(host: String, portText: String, user: String, pass: String): Pair<String, String> =
    withContext(Dispatchers.IO) {
        try {
            val port = portText.toInt()
            if (port !in 1..65535 || host.isBlank()) return@withContext "Host/port tidak valid" to "—"
            Authenticator.setDefault(object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication? {
                    return if (requestorType == RequestorType.PROXY)
                        PasswordAuthentication(user, pass.toCharArray()) else null
                }
            })
            val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(host, port))
            val connection = (URL("https://api.ipify.org").openConnection(proxy) as HttpURLConnection)
            connection.connectTimeout = 12000
            connection.readTimeout = 12000
            connection.setRequestProperty("User-Agent", "ProxyManagerAndroid/1.0")
            val ip = connection.inputStream.bufferedReader().use { it.readText().trim() }
            connection.disconnect()
            "Berhasil • Proxy merespons" to ip
        } catch (e: Exception) {
            "Gagal • Periksa host, port, autentikasi" to "—"
        }
    }
