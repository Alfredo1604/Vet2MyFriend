# Guía de las rutas y los modelos

Esta entrega conecta el catálogo local con rutas internas. La UI recibe estado y envía acciones; las decisiones y las llamadas externas se resuelven fuera de los composables.

## Qué significa cada nombre

Un módulo es una unidad que Gradle compila y que declara sus propias dependencias en build.gradle.kts. Las carpetas core, data y feature agrupan módulos. Por ejemplo, core/domain y data/routing sí son módulos; model y repository son paquetes dentro de uno.

Un repositorio de datos es un contrato que ofrece información a la aplicación y oculta su origen. Es distinto del repositorio Git que guarda el historial del proyecto.

La dirección de dependencias es:

~~~text
app ────────────→ feature:directory ──→ core:domain
 │
 ├──────────────→ data:directory ─────→ core:domain
 └──────────────→ data:routing ───────→ core:domain
~~~

app conoce las implementaciones porque las construye y conecta. feature conoce las interfaces de dominio. data implementa esas interfaces. Así, un cambio de proveedor no obliga a reescribir la pantalla ni a importar HTTP o Room en el ViewModel.

## Los modelos de clínica

Clinic reemplaza a ClinicSummary. No hacía falta mantener dos modelos casi iguales para este catálogo. Incluye id, nombre, dirección, municipio, coordenadas opcionales y servicios. El identificador permite distinguir clínicas con nombres parecidos.

Tu campo location era String. Ahora municipality guarda el municipio como texto y location es GeoPoint?, que contiene latitud y longitud validadas. null significa que faltan coordenadas verificadas.

ClinicService conserva tus conceptos de precio, servicio y médico. El médico es opcional porque todavía no sabemos su nombre. El precio también puede faltar: null significa desconocido y cero significa gratuito. BigDecimal evita perder precisión monetaria. Las reglas requieren un servicio con nombre, un precio no negativo y, si hay médico, un nombre no vacío.

model contiene modelos; repository contiene contratos. No es obligatorio llamar interfaces a una carpeta. repository comunica qué responsabilidad tienen esas interfaces. Las reglas SOLID dependen de las responsabilidades y las dependencias, además del orden de carpetas.

## Qué hace cada pieza nueva

| Pieza | Responsabilidad |
|---|---|
| RoutingRepository | Contrato para obtener una ruta a partir de origen, destino y medio de transporte. |
| GeocodingRepository | Contrato para buscar direcciones y devolver candidatos, sin elegir uno por el usuario. |
| LocationProvider | Contrato para obtener una posición actual; no conoce la pantalla de permisos. |
| RouteRequest / Route / RouteResult | Entrada, geometría con distancia y tiempo, y resultado o motivo de error. |
| GeoapifyClient | Ejecuta HTTP con una conexión reutilizable, tiempo límite, límite de respuesta y cancelación. |
| GeoapifyJson | Traduce GeoJSON a nuestros modelos y valida sus datos. |
| GeoapifyRepositories | Crea las dos implementaciones de Geoapify con un mismo cliente HTTP. |
| RouteViewModel | Coordina las acciones del usuario y publica RouteUiState. |
| RouteAction | Acciones explícitas: seleccionar clínica, cambiar medio, elegir punto, buscar dirección y calcular. |
| ClinicMap | Dibuja clínicas, origen, destino y segmentos de la ruta. |
| RoutePlanner | Presenta controles y resultados; recibe estado y una función para enviar acciones. |
| AndroidLocationProvider | Implementa LocationProvider con los servicios de ubicación de Android. |
| LocationPermissionRequest | Solicita permiso desde la Activity cuando se usa Mi ubicación. |
| Vet2MyFriendApplication | Construye e inyecta las implementaciones. Es inyección manual por constructor. |

No se añadió Hilt: para este tamaño la composición manual hace explícito de dónde sale cada dependencia y permite sustituirla en pruebas. No hace falta crear una interfaz para cada clase; se usan en los límites que conviene sustituir: catálogo, rutas, direcciones y ubicación.

## Cómo viaja una acción

1. Pulsas Calcular ruta.
2. La pantalla envía RouteAction.Calculate.
3. RouteViewModel comprueba que hay dos puntos distintos y una selección terminada.
4. RoutingRepository recibe un RouteRequest.
5. La implementación Geoapify construye una petición con walk, bicycle o drive.
6. El proveedor devuelve GeoJSON. GeoapifyJson convierte sus coordenadas y valida distancia y tiempo.
7. El ViewModel publica el resultado. Compose lo observa y ClinicMap dibuja los segmentos.

GeoJSON usa longitud, latitud; los waypoints de esta API usan latitud, longitud. Se prueban ambos órdenes para evitar rutas en otro lugar del mundo. Los segmentos se conservan separados para no unir artificialmente tramos diferentes.

Al cambiar origen, destino o medio se cancela la petición y se incrementa una revisión. La revisión impide mostrar una respuesta vieja incluso si una fuente ignora la cancelación. Lo mismo protege búsquedas y ubicación frente a resultados tardíos. Las pruebas cubren esas carreras.

## Base local y servicios

Room pasa a versión 3. clinic_services es otra tabla relacionada con clinics mediante clinicId. Su clave combina clínica y servicio: el mismo servicio puede existir en clínicas diferentes. El precio se guarda como texto decimal y se convierte a BigDecimal; SQL REAL no representa todos los importes decimales exactamente.

Las lecturas de clínica y servicios usan una transacción. Al reemplazar los servicios, borrar los anteriores e insertar los nuevos también ocurre dentro de una transacción: se completa todo o se revierte todo.

La migración crea la tabla sin borrar clínicas. Incluye reportes iniciales y corrige únicamente direcciones que todavía conservan el texto de la semilla anterior. Preserva nombres, coordenadas y direcciones editadas.

La Hacienda tiene consulta, vacunación, esterilización, cirugía y corte de pelo reportados como gratuitos. Los Pequeños Amigos tiene consulta y vacunación, según tus visitas descritas. No se inventaron médicos ni se añadieron adopción o resguardo. La ficha aclara que son reportes y que disponibilidad y costos pueden cambiar.

Una base de datos local no exige datos escritos en Kotlin. Aquí existe una semilla pequeña para arrancar con información útil. Más adelante el backend podrá administrar el catálogo y la app actualizará Room. En un backend SQL también pueden existir semillas; los cambios habituales del catálogo deberían entrar por una herramienta administrativa o API, sin recompilar la app.

## Dependencias y números elegidos

Se añadieron versiones publicadas como estables: OkHttp 5.3.2, kotlinx.serialization JSON 1.9.0 y Play services Location 21.4.0. Se conservó Maps Compose 6.12.2 para mantener el toolchain Kotlin existente. No se añadieron dependencias alpha, beta o RC.

En ubicación se acepta una posición de hasta 10 segundos de antigüedad, se permiten 15 segundos para conseguir una nueva y se corta la espera de la aplicación a los 20 segundos. Son decisiones ajustables de experiencia de uso; no son restricciones del negocio ni valores obligatorios del SDK. Las constantes tienen nombre en AndroidLocationProvider.

El cliente HTTP tiene 20 segundos de espera máxima. No reintenta automáticamente para evitar duplicar consultas o consumir cuota sin una nueva acción. Los créditos pertenecen a la cuenta Geoapify y se comparten entre todos los usuarios.

## Cómo agregar algo tú

Para añadir una regla, empieza por un ejemplo de comportamiento y decide quién debe cumplirla. Una regla sobre precios pertenece al modelo; una consulta a un proveedor pertenece a data; un mensaje o una selección visual pertenece a feature.

Para cambiar un proveedor de rutas, implementa RoutingRepository en data y entrega esa implementación desde Application. La pantalla sigue usando el mismo contrato.

Para crear otro módulo: crea su carpeta y build.gradle.kts, inclúyelo en settings.gradle.kts y declara qué módulo necesita consumirlo. Para Android usa Android Library; para lógica pura puede ser Kotlin/JVM. Android Studio crea las carpetas src/main y src/test con las plantillas. En un módulo escrito a mano son carpetas normales: Gradle interpreta esos nombres.

Para cambiar el esquema Room: agrega o modifica entidades, incrementa la versión, escribe la migración y conserva el esquema exportado. Prueba que una instalación anterior conserve sus datos. Agregar un servicio como dato a una clínica no debería requerir otra versión de esquema.

## Qué comprobar antes de integrar la rama

Ejecutar assembleDebug, los tests de core:domain y data:routing, testDebugUnitTest de feature:directory, assembleDebugAndroidTest de data:directory y lintDebug de app.

En un dispositivo: negar ubicación y elegir un origen manual; conceder ubicación aproximada; calcular en los tres medios; cambiar destino mientras carga; buscar una dirección ambigua; cerrar la ruta; comprobar que la ficha conserva servicios después de actualizar desde la versión anterior. No desinstales para probar una migración: borrar la instalación elimina el caso que se necesita comprobar.

Para distribuir la app, el siguiente paso técnico será mover la clave REST a un backend y controlar la cuota compartida. No hace falta decidir FastAPI o .NET para conservar los contratos actuales.
