package com.example.chesschain

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.chesschain.ui.theme.ChessChainTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChessChainTheme {
                ChessChainApp()
            }
        }
    }
}

data class User(
    val id: String,
    val name: String,
    val rating: Int,
    val walletAddress: String? = null
)

data class MatchOffer(
    val id: String,
    val opponentName: String,
    val opponentRating: Int,
    val latencyMs: Int,
    val wagerToken: String,
    val wagerAmount: Double
)

data class TokenSwapQuote(
    val fromToken: String,
    val toToken: String,
    val rate: Double,
    val networkFee: Double
)

sealed class NavDestination(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    data object Dashboard : NavDestination("dashboard", "Inicio", Icons.Rounded.Home)
    data object Matchmaking : NavDestination("matchmaking", "Jugar", Icons.Rounded.People)
    data object Wallet : NavDestination("wallet", "Billetera", Icons.Rounded.Verified)
    data object Swap : NavDestination("swap", "Swap", Icons.Rounded.CurrencyExchange)
    data object Security : NavDestination("security", "Seguridad", Icons.Rounded.Bolt)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessChainApp() {
    var activeUser by rememberSaveable { mutableStateOf<User?>(null) }
    val navController = rememberNavController()

    if (activeUser == null) {
        LoginScreen(onLogin = { user ->
            activeUser = user
        })
        return
    }

    val destinations = listOf(
        NavDestination.Dashboard,
        NavDestination.Matchmaking,
        NavDestination.Wallet,
        NavDestination.Swap,
        NavDestination.Security
    )

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text("ChessChain", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { /* notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                destinations.forEach { destination ->
                    NavigationBarItem(
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) },
                        selected = currentDestination?.route == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = NavDestination.Dashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(NavDestination.Dashboard.route) {
                DashboardScreen(user = activeUser!!)
            }
            composable(NavDestination.Matchmaking.route) {
                MatchmakingScreen(user = activeUser!!)
            }
            composable(NavDestination.Wallet.route) {
                WalletScreen(user = activeUser!!, onWalletUpdate = { updated ->
                    activeUser = updated
                })
            }
            composable(NavDestination.Swap.route) {
                SwapScreen()
            }
            composable(NavDestination.Security.route) {
                SecurityScreen()
            }
        }
    }
}

@Composable
fun LoginScreen(onLogin: (User) -> Unit) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Inicia sesión para sincronizar tu elo y tu billetera", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(24.dp))
        ChessInputField(value = username, onValueChange = { username = it }, label = "Usuario o correo")
        Spacer(modifier = Modifier.height(12.dp))
        ChessInputField(value = password, onValueChange = { password = it }, label = "Contraseña", isPassword = true)
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                loading = true
                coroutineScope.launch {
                    delay(900)
                    onLogin(User(id = "1", name = username.ifBlank { "Invitado" }, rating = 1420))
                }
            },
            enabled = !loading && username.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Verificando..." else "Entrar")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = { onLogin(User("guest", "Invitado", 1200)) }, modifier = Modifier.fillMaxWidth()) {
            Text("Entrar como invitado")
        }
    }
}

@Composable
fun DashboardScreen(user: User) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        item {
            Text("Hola, ${'$'}{user.name}", style = MaterialTheme.typography.headlineMedium)
            Text("Elo sincronizado: ${'$'}{user.rating}", color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
        }
        item {
            HighlightCard(title = "Racha impecable", subtitle = "+15 puntos esta semana")
            Spacer(Modifier.height(16.dp))
        }
        item {
            SectionTitle("Líneas recomendadas")
        }
        items(listOf("Defensa Siciliana", "Ataque Inglés", "Eslava")) { opening ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(opening, fontWeight = FontWeight.Bold)
                        Text("+6% winrate", color = MaterialTheme.colorScheme.primary)
                    }
                    Icon(Icons.Default.Lock, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun MatchmakingScreen(user: User) {
    val offers = remember {
        listOf(
            MatchOffer("1", "Carla", 1480, 42, "BNB", 0.05),
            MatchOffer("2", "Leo", 1522, 35, "BUSD", 12.0),
            MatchOffer("3", "Ryo", 1610, 80, "BNB", 0.15)
        )
    }
    var searching by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var currentMatch by remember { mutableStateOf<MatchOffer?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SectionTitle("Emparejamiento inteligente")
        Text("Estado: ${'$'}{if (searching) "Buscando rival..." else "Listo"}")
        Spacer(Modifier.height(12.dp))
        Button(onClick = {
            searching = true
            coroutineScope.launch {
                delay(1800)
                currentMatch = offers.random()
                searching = false
            }
        }) {
            Text(if (searching) "Cancel" else "Buscar oponente rated")
        }
        currentMatch?.let { match ->
            Spacer(Modifier.height(16.dp))
            MatchCard(match)
        }
        Spacer(Modifier.height(24.dp))
        SectionTitle("Retos destacados")
        Spacer(Modifier.height(12.dp))
        offers.forEach { offer ->
            MatchCard(offer)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun WalletScreen(user: User, onWalletUpdate: (User) -> Unit) {
    var connected by rememberSaveable { mutableStateOf(user.walletAddress != null) }
    var walletAddress by rememberSaveable { mutableStateOf(user.walletAddress ?: "") }
    var balance by remember { mutableStateOf(1.62) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SectionTitle("Billetera BNB Chain")
        Text(if (connected) "Conectado" else "Desconectado", color = if (connected) MaterialTheme.colorScheme.primary else Color.Gray)
        Spacer(Modifier.height(12.dp))
        Button(onClick = {
            connected = !connected
            walletAddress = if (connected) "0x93A1...bE7" else ""
            onWalletUpdate(user.copy(walletAddress = walletAddress.takeIf { connected }))
        }) {
            Text(if (connected) "Desconectar" else "Conectar billetera")
        }
        Spacer(Modifier.height(16.dp))
        if (connected) {
            Text("Dirección: ${'$'}walletAddress")
            Spacer(Modifier.height(8.dp))
            Text("Saldo estimado: ${'$'}balance BNB")
            Spacer(Modifier.height(24.dp))
            SectionTitle("Historial de depósitos")
            listOf("0.25 BNB", "35 BUSD", "0.5 BNB").forEach {
                Text("• ${'$'}it en stake antifraude")
            }
        } else {
            Text("Conecta tu billetera para habilitar apuestas en línea y retiros instantáneos.")
        }
    }
}

@Composable
fun SwapScreen() {
    var amount by rememberSaveable { mutableStateOf(0.1f) }
    val quote by remember(amount) {
        mutableStateOf(
            TokenSwapQuote(
                fromToken = "BNB",
                toToken = "CHECK",
                rate = 125.0,
                networkFee = 0.0004
            )
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SectionTitle("Intercambio instantáneo")
        Text("${'$'}amount ${'$'}{quote.fromToken} → ${'$'}{String.format("%.2f", amount * quote.rate)} ${'$'}{quote.toToken}")
        Slider(value = amount, onValueChange = { amount = it }, valueRange = 0.01f..1f)
        Text("Tarifa de red: ${'$'}{quote.networkFee} ${'$'}{quote.fromToken}")
        Spacer(Modifier.height(16.dp))
        Button(onClick = { /* trigger swap */ }, modifier = Modifier.fillMaxWidth()) {
            Text("Ejecutar swap con protección MEV")
        }
        Spacer(Modifier.height(24.dp))
        SectionTitle("Cotizaciones dinámicas")
        listOf(124.5, 125.2, 124.9).forEach { rate ->
            Text("• Liquidez agregada: ${'$'}rate CHECK/BNB")
        }
    }
}

@Composable
fun SecurityScreen() {
    val riskScore = 12
    val checks = listOf(
        "Verificación biométrica" to true,
        "Análisis anti-bot" to true,
        "Tiempo de respuesta estable" to true,
        "Patrón de movimientos sospechoso" to false
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SectionTitle("Centro antifraude")
        Text("Riesgo actual ${'$'}riskScore/100", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        checks.forEach { (label, ok) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = if (ok) MaterialTheme.colorScheme.surface else Color(0xFFFFE0E0))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(label, fontWeight = FontWeight.SemiBold)
                        Text(if (ok) "Sin anomalías" else "Se recomienda revisión", color = if (ok) MaterialTheme.colorScheme.primary else Color.Red)
                    }
                    Icon(if (ok) Icons.Default.Security else Icons.Default.Lock, contentDescription = null)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = { /* share report */ }, modifier = Modifier.fillMaxWidth()) {
            Text("Compartir reporte a soporte")
        }
    }
}

@Composable
fun MatchCard(offer: MatchOffer) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("${'$'}{offer.opponentName} • ${'$'}{offer.opponentRating}", fontWeight = FontWeight.Bold)
            Text("Latencia ${'$'}{offer.latencyMs}ms", color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text("Wager ${'$'}{offer.wagerAmount} ${'$'}{offer.wagerToken}")
            Spacer(Modifier.height(8.dp))
            Button(onClick = { /* challenge */ }, modifier = Modifier.fillMaxWidth()) {
                Text("Retar ahora")
            }
        }
    }
}

@Composable
fun HighlightCard(title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.Black, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color.Black)
            }
            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                tint = Color.Black
            )
        }
    }
}

@Composable
fun ChessInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false
) {
    androidx.compose.material3.OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (isPassword) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None
    )
}

@Composable
fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}
