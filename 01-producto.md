# Producto y entregas

## Propósito y alcance
App gratuita sin fines de lucro, con calidad de producción. Piloto en Guadalupe, Nuevo León; Android primero. KMP/iOS/escritorio posteriores. Consulta sin cuenta; administrador revisa datos.

MVP:
1. Lista y mapa, búsqueda por nombre/zona y filtro por servicio gratuito reportado.
2. Ubicación opcional en primer plano, distancia aproximada y búsqueda manual equivalente.
3. Detalle con servicios, condiciones, horarios, contacto, procedencia y fechas por dato.
4. Indicaciones externas a pie/carro y llamada si existe teléfono.
5. Correcciones/propuestas privadas con moderación y confirmación de recepción del servidor.
6. Lectura del último catálogo sincronizado sin conexión, con fecha visible.
7. Administración inicial en Supabase Dashboard y acciones protegidas; panel propio posterior.

Fuera de MVP: navegación propia, diagnóstico, garantía de urgencias, citas, pagos, cuentas públicas, reseñas, fotos, chat y notificaciones.

## Datos iniciales
- Av. la Hacienda 1312: dirección/nombre de usuario y captura. Atención reportada 09–13 y 15–17; días desconocidos. Personal habría indicado gratuidad de lo ofrecido, sin desglose confirmado. No inventar procedimientos.
- Los Pequeños Amigos: usuario reporta vacunación y consulta gratuitas. Fuentes municipales consultadas publican consulta, vacunación/desparasitación y esterilización gratuitas; lunes a viernes 08–17; actualización declarada 2025-06-13. Pausas y vigencia operativa pendientes.

Fechas de visitas no proporcionadas permanecen desconocidas. Consultar una web no es confirmar presencialmente. Coordenadas y entrada pendientes de comprobar; no extraer coordenadas inventadas de capturas. No fusionar centros por nombre parecido.

## Hitos con aceptación
| Hito | Resultado | Aceptación |
|---|---|---|
| H0 | Proyecto, Wrapper, versiones y módulos | Compila debug, lint y primera prueba pasan sin credenciales productivas |
| H1 | Lista → detalle → indicaciones con fakes | Carga/vacío/error, rotación y permisos denegados manejados |
| H2 | Supabase, Room y catálogo real | Migraciones, sincronización atómica, cache preservada ante fallo |
| H3 | Mapa, filtros, ubicación y reportes | Filtros compartidos, moderación, límites e idempotencia probados |
| H4 | Piloto y publicación | Accesibilidad, datos, privacidad, firma, respaldo y presupuesto revisados |

Sin fechas estimadas hasta conocer disponibilidad semanal. Dos registros bastan para desarrollar el recorrido.

Éxito: usuarios encuentran servicio, entienden condiciones y llegan a entrada correcta. Medir cobertura de datos y resolución de reportes sin rastrear recorridos.

Pendientes: nombre/applicationId, cuenta propietaria/remoto/licencia, presupuesto máximo, coordenadas, servicios/días exactos y frecuencia de moderación. Identidad definitiva antes de fijar paquete para publicación.
