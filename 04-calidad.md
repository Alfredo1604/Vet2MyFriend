# Calidad, Git y operación

## Programación
Responsabilidades pequeñas, composición, inmutabilidad y nombres del negocio. Interfaces en fronteras sustituibles, no por cada clase. DTO/Room internos a data; dominio sin anotaciones de persistencia. Structured concurrency, scopes con dueño y cancelación propagada. Sin GlobalScope ni bloqueo de UI; CancellationException no se transforma en error de negocio. Errores tipados con mensajes útiles.

## Pruebas proporcionales
- Unitarias: horarios, límites, excepciones y filtros con tiempo controlado.
- ViewModel: filtros, datos con error de refresco y recepción de reportes usando fakes.
- Room/repositorio: transacciones, migraciones y cache preservada ante fallo.
- Backend: RLS, lectura publicada, reportes privados, límites e idempotencia; no administrador no edita catálogo.
- UI: recorrido, ubicación denegada, lectura sin conexión y envío. Accesibilidad manual con lector, fuentes grandes, contraste y controles etiquetados.

Sin porcentaje arbitrario ni tests de getters. Priorizar comportamientos que manden a un lugar incorrecto o pierdan reportes.

## Seguridad y operación
RLS en tablas expuestas: lectura pública solo publicada; sin escritura directa pública. Reportes mediante endpoint protegido. Clave privilegiada Supabase solo servidor; clave publicable no sustituye permisos. Mapas restringido por paquete/certificado y APIs. Desarrollo/producción separados.

Sin secretos en Git/logs/APK/ejemplos. Sin ubicación histórica ni en segundo plano. No registrar coordenadas precisas/contactos en telemetría. Definir retención/eliminación antes de activar contacto opcional.

Migraciones versionadas, respaldo y restauración comprobados antes de producción. Presupuesto y alertas: alertas no son topes. Cuotas donde existan y vigilancia de pausa/disponibilidad del plan gratuito. Verificar compatibilidad del SDK cliente Supabase al implementarlo.

## Git y publicación
Repositorio local main, cambios pequeños y revisión de diff. Ramas breves; PR/CI cuando exista remoto. Visibilidad/licencia pendientes: gratis no significa código abierto. CI futura: debug, unit tests y Android Lint; instrumentadas en entorno disponible. Una herramienta de formato, sin duplicarlas.

Antes de tienda: requisitos vigentes Play/targetSdk, pruebas de cuenta, privacidad/declaración de datos coherente con SDK, atribuciones de mapas, firma segura y piloto. No afirmar afiliación municipal ni garantía de urgencias/cupo.

## Próxima implementación
1. Elegir nombre/applicationId y ubicación definitiva.
2. Comprobar JDK/Android SDK y versiones estables compatibles.
3. Crear cuatro módulos y recorrido con fakes, sin credenciales de producción.
4. Implementar y probar disponibilidad.
5. Añadir esquema, RLS, Room y sincronización; después reportes.

No afirmar pruebas/compilación Android mientras este repositorio solo contenga documentación.
