# Directorio veterinario comunitario

Planeación inicial: 26 de octubre de 2026. Nombre provisional. Independiente de WalletLab.

Objetivo: encontrar servicios veterinarios gratuitos con condiciones, fuentes y fechas visibles.

Este repositorio contiene documentación; todavía no contiene app Android compilable, módulos Gradle implementados ni backend desplegado.

- [Producto y entregas](docs/01-producto.md)
- [Arquitectura y decisiones](docs/02-arquitectura.md)
- [Reglas y datos](docs/03-reglas-y-datos.md)
- [Calidad y operación](docs/04-calidad.md)

Propuesta: Kotlin, Compose, MVVM con UDF, cuatro módulos, dominio pequeño, Room y Supabase. Google Maps para visualizar y Maps URLs para indicaciones externas.

Próximo hito: elegir nombre/applicationId y crear primer recorrido compilable con fakes. Remoto y licencia pendientes: gratuito no determina licencia del código.
