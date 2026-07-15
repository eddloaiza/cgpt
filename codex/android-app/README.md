# ChessChain Android Demo

Aplicación Android inspirada en el estilo de chess.com que demuestra cómo podría verse una integración de ajedrez competitivo con billeteras de BNB Chain, emparejamiento en línea, swaps de tokens y un centro antifraude.

## Características
- **Inicio de sesión** tradicional o modo invitado.
- **Dashboard** con rating, racha y recomendaciones.
- **Emparejamiento en línea** con retadores destacados y wagers tokenizados.
- **Conexión de billetera BNB Chain** y visualización de saldo.
- **Intercambio de tokens** `BNB → CHECK` con tarifa dinámica.
- **Centro antifraude** que resume chequeos biométricos, anti-bots y patrones sospechosos.

## Stack técnico
- Kotlin + Jetpack Compose + Material 3.
- Navegación con `navigation-compose`.
- Servicios simulados para conexión de billetera, emparejamiento y swaps.

## Cómo ejecutar
1. Abre la carpeta `android-app/` con Android Studio Flamingo o superior.
2. Sincroniza el proyecto Gradle.
3. Ejecuta la app en un emulador o dispositivo con Android 8.0 (API 26) o superior.

> Nota: las integraciones con BNB Chain, el intercambiador y el antifraude son simulaciones listas para conectarse a APIs reales.
