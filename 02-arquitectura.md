# Arquitectura y decisiones

## ADR-001: MVVM y UDF
Compose representa UiState inmutable y envía acciones mediante callbacks. ViewModel produce StateFlow<UiState>; UI observa con collectAsStateWithLifecycle. UDF describe el flujo y MVVM responsabilidades; son compatibles.

Acción → ViewModel → repositorio/reglas → estado → Compose.

UI sin DAO/Supabase; ViewModel sin Activity, Context, permisos ni SDK de mapas. Datos guardados pueden coexistir con error de refresco. SavedStateHandle para ID/filtros pequeños; Room para persistencia. Navegar con identificadores.

Abrir Maps/marcador es acción UI. Resultados importantes, como reporte recibido, son estado con acuse explícito; no depender de eventos efímeros que se pierdan al recrear pantalla.

## ADR-002: dominio pequeño, sí
Android considera opcional el dominio para lógica compleja/reutilizada. Su definición no equivale a Clean Architecture clásica. Adoptamos una variante con inversión de dependencias: contratos/modelos en dominio e implementaciones en data. Es elección del proyecto, no obligación de Android.

Justificación: lista, mapa y detalle comparten horarios partidos, excepciones y desconocidos. Aislar esa regla facilita consistencia y pruebas.

Inicialmente:
- CalculateAvailability: regla pura que recibe horario, zona, instante y estado operativo; devuelve disponibilidad y próxima transición determinable.
- MatchesFreeService: regla de filtro conservando procedencia de gratuidad.
- ClinicRepository y ReportRepository: contratos mínimos.

Lecturas sencillas llaman directamente al repositorio desde ViewModel. No crear casos de uso que solo reenvían cada método ni interfaz por clase. Dominio sin Compose, Room, Supabase ni Google Maps; reloj/instante controlable. Mapeos solo donde cambian las representaciones, no tres copias idénticas por dogma.

## ADR-003: modularización inicial propuesta
| Módulo | Responsabilidad | Dependencias internas |
|---|---|---|
| :app | Host, navegación y ensamblaje DI | :feature:directory, :data:directory, :core:domain |
| :feature:directory | Lista, mapa, detalle, reporte y ViewModels | :core:domain |
| :core:domain | Modelos, contratos y reglas puras | Ninguna |
| :data:directory | Repositorios, Room, Supabase, sincronización y mapeos | :core:domain |

Cuatro módulos Gradle; no uno por pantalla. App conecta implementaciones con contratos mediante Hilt. Feature no depende de data. Dominio Kotlin/JVM puro inicialmente, no KMP. Mapa encapsulado en feature.

Extraer design system al compartir componentes entre features; database/network cuando varias áreas los compartan; reportes cuando gane complejidad propia. Sin ciclos, dependencias entre features, módulos vacíos, common gigante ni división api/impl prematura.

## ADR-004: lectura local y sincronización
Supabase/PostgreSQL es catálogo editorial remoto; Room, fuente única de lectura persistente del cliente. Repositorios exponen Flow de Room y refrescan desde red. UI nunca mezcla cache y resultados remotos directos.

Piloto: snapshot público completo con revisión; validar y reemplazar en transacción. Fallo/descarga incompleta no borra cache. Reemplazo refleja retiros. Último éxito y error separados. Sin conexión se muestra antigüedad; no prometer revocación inmediata de datos guardados.

Al crecer: paginación consistente o cambios incrementales con cursor/tombstones. Inicialmente refresco al iniciar/a demanda; no WorkManager sin necesidad. Reportes requieren conexión; borrador no es recepción. Sin envío automático en segundo plano todavía.

Catálogo propio con fuentes independientes; no almacenar contenido de Places como directorio propio. Ubicación efímera en memoria. Distancia geográfica local basta en piloto; PostGIS cuando volumen/consultas remotas lo justifiquen.

## ADR-005: stack
Kotlin, Compose/Material 3, Coroutines/Flow, ViewModel, Room, Hilt, Supabase, Maps SDK Android y Maps URLs. Elegir una solución Jetpack de navegación estable compatible al crear proyecto; DataStore si surgen preferencias.

Kotlin DSL, Version Catalog y Gradle Wrapper; comprobar versiones estables compatibles de JDK/AGP/Kotlin/Compose, minSdk y targetSdk, no fijarlas de memoria. Plugins de convención al repetir configuración. Sin versiones dinámicas ni framework MVI adicional.

No servidor Node.js inicial; endpoint protegido pequeño para reportes. KMP futuro exige revisar DI, mapas, permisos, persistencia y navegación; separación no garantiza migración automática.

## Fuentes oficiales consultadas el 2026-10-26
- https://developer.android.com/topic/architecture/recommendations
- https://developer.android.com/topic/architecture/domain-layer
- https://developer.android.com/topic/modularization/patterns
- https://developer.android.com/topic/architecture/ui-layer/events
- https://developer.android.com/topic/architecture/data-layer/offline-first

Módulos, backend y reglas editoriales son decisiones propias, no mandatos de esas guías.
