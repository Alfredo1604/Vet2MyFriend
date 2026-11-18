# Vet2MyFriend

Aplicación Android comunitaria para encontrar clínicas veterinarias gratuitas o de bajo costo y consultar sus servicios. Proyecto personal gratuito, sin fines de lucro. Cobertura inicial: dos centros reportados en Guadalupe, Nuevo León.

## Estado actual

- Proyecto Android compilable con Kotlin y Jetpack Compose.
- Bienvenida inicial, estructura principal y directorio local con Room.
- Lista y ficha de clínicas con servicios, precios conocidos y datos pendientes de confirmar.
- Google Maps integrado y rutas internas con Geoapify para caminar, bicicleta y carro.
- Origen por ubicación del dispositivo, dirección o selección manual en el mapa.
- Contratos de dominio, inyección por constructor y estado de UI con flujo unidireccional.

Los servicios son reportes iniciales, sujetos a cambios. Las ubicaciones precisas del catálogo necesitan verificación; la búsqueda permite elegir un resultado y revisar o corregir el marcador.

Las cuentas, sincronización con backend, diario, rutas guardadas y navegación continua están pendientes. El backend todavía no está elegido. Algunas secciones de la navegación principal muestran un estado provisional.

## Módulos

| Módulo | Responsabilidad |
|---|---|
| app | Inicio de la aplicación, navegación principal, permisos y composición de dependencias. |
| core:domain | Modelos, reglas de negocio y contratos de catálogo, rutas, direcciones y ubicación. |
| data:directory | Persistencia local, semillas, relaciones y migraciones Room. |
| data:routing | Cliente HTTP, interpretación de GeoJSON e implementaciones Geoapify. |
| feature:directory | Pantallas, mapa y ViewModels del directorio y del planificador de rutas. |

## Ejecutar

1. Abrir la raíz del proyecto en Android Studio y sincronizar Gradle. El proyecto usa Android SDK 37 y minSdk 24.
2. Usar secrets.properties.example como referencia para crear secrets.properties en la raíz. Completar MAPS_API_KEY y GEOAPIFY_API_KEY desde el editor local.
3. Restringir la clave de Google a Maps SDK for Android, al paquete com.alfredorosas.vet2myfriend y al SHA-1 del certificado usado para compilar.
4. Ejecutar app en un dispositivo o emulador con Google Play services.

secrets.properties está excluido de Git. Una clave usada directamente en Android llega al APK: para distribuir la clave REST de rutas se prevé usar un backend. La cuota gratuita de Geoapify se comparte entre los usuarios de la aplicación.

## Validación

23 pruebas JVM aprobadas, compilación debug y Android Lint sin errores. Se comprobaron respuestas reales para los tres medios de transporte y para las dos búsquedas de dirección.

El propietario confirmó que la app compila y funciona en ejecución. Las pruebas Android de migración están implementadas y compiladas; su ejecución automatizada sigue pendiente porque ADB reportó el emulador offline.

Desde PowerShell en la raíz:

~~~powershell
.\gradlew.bat :app:assembleDebug :core:domain:test :data:routing:test :feature:directory:testDebugUnitTest
.\gradlew.bat :data:directory:assembleDebugAndroidTest :app:lintDebug
~~~

## Documentación

- [Guía de implementación: rutas, modelos y dependencias](docs/guia-rutas-y-modelos.md)
- [Mapas, cuota, configuración y comprobaciones](docs/maps-and-routing.md)
- [Producto y entregas: planeación inicial](01-producto.md)
- [Arquitectura: planeación inicial](02-arquitectura.md)
- [Reglas y datos: planeación inicial](03-reglas-y-datos.md)
- [Calidad y operación: planeación inicial](04-calidad.md)

Los documentos de planeación inicial conservan propuestas anteriores. La guía de implementación describe el código actual. La licencia del código todavía está pendiente de decidir; que la app sea gratuita no define una licencia de código abierto.
