# Integración con ChatGPT/Codex

Esta carpeta contiene un ejemplo mínimo para usar la API de OpenAI (ChatGPT/Codex) desde Python.

Requisitos:
- Python 3.8 o superior
- Una clave de OpenAI (OPENAI_API_KEY)

Instalación:
1. Crea y activa un entorno virtual (opcional pero recomendado): python -m venv .venv && source .venv/bin/activate
2. Instala dependencias: pip install -r codex/requirements.txt
3. Copia codex/.env.example a codex/.env y añade tu clave OPENAI_API_KEY.

Uso:
- Ejecuta el script de ejemplo: python codex/example_codex.py --prompt "Escribe una función en Python que invierta una lista"
- O ejecuta sin argumentos y escribe tu prompt cuando se te pida.

Notas:
- No incluyas claves en el repositorio. Usa variables de entorno o el archivo .env (gitignored).
- Este ejemplo usa el modelo de Chat completions (gpt-3.5-turbo). Si prefieres un modelo Codex legacy, cambia el parámetro model en el script.