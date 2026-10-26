# Reglas de negocio y datos

Reglas propuestas para MVP; identificadores estables para vincular pruebas y cambios.

## Catálogo y evidencia
- RN-01: consulta sin cuenta ni ubicación obligatoria.
- RN-02: publicación requiere nombre, dirección/localidad, fuente y servicio gratuito identificado o reporte general de gratuidad pendiente de desglose. Este último no acredita filtros de servicios concretos. Coordenadas válidas para mapa; ficha sin coordenadas puede estar en lista.
- RN-03: publicación DRAFT/PUBLISHED/WITHDRAWN distinta de estado operativo. Cierre temporal puede conservar ficha con aviso.
- RN-04: costo por servicio FREE/PAID/CONDITIONAL/UNKNOWN. Condicional exige explicación; desconocido no es cero. Consulta gratuita no implica cirugía, insumos ni medicamentos gratuitos.
- RN-05: filtro gratuito incluye FREE reportado mostrando fuente/fecha. Condicional en filtro separado; reporte general sin desglose no acredita procedimientos específicos.
- RN-06: procedencia por campo: oficial, personal, visitante, conflicto o desconocido. Fechas de publicación/observación/revisión separadas. Revisión web no es visita ni garantía de vigencia.
- RN-07: contradicciones no se resuelven por mayoría automática. Moderador conserva evidencia y resuelve; si no puede, mostrar conflicto relevante.

## Horarios
- RN-08: cada día es UNKNOWN, CLOSED u OPEN con intervalos. Sin dato significa desconocido. Zona IANA del centro: America/Monterrey en Guadalupe; nunca implícita del teléfono.
- RN-09: intervalos [inicio, fin), no solapados e inicio < fin. Nocturnos divididos por día. Pausa solo entre intervalos conocidos del mismo día.
- RN-10: excepciones por fecha sustituyen completamente el horario semanal. Festivos no confirmados no se presentan como atención garantizada.
- RN-11: estados: atención según horario reportado, pausa, cerrado según horario, desconocido, conflicto o cierre temporal reportado. Cierre temporal vigente tiene prioridad; conflicto horario impide afirmar atención.
- RN-12: horario no garantiza personal/cupo/insumos. Servicio puede requerir cita u horario propio. Mostrar fecha, sin caducidad universal inventada.
- RN-13: recalcular al volver al primer plano y en próxima transición; estado abierto no queda congelado hasta sincronizar.

## Cercanía y aportaciones
- RN-14: distancia geográfica aproximada, no tiempo ni longitud de ruta. Sin ubicación, búsqueda manual/orden por nombre.
- RN-15: indicaciones requieren destino confiable; entrada distinta del centro del predio. walking/driving externos; alternativa navegador/copiar dirección. No garantizar accesibilidad ni seguridad de ruta.
- RN-16: reportes privados PENDING/APPROVED/REJECTED/NEEDS_INFORMATION; sin cambios automáticos al catálogo. Moderador, motivo y fecha auditados; aprobación crea revisión.
- RN-17: centro o propuesta, categoría, texto limitado y fecha de observación opcional. Validar en cliente/servidor, sin HTML activo ni archivos en MVP. Contacto opcional privado, con finalidad explícita.
- RN-18: endpoint con validación, límites de abuso e idempotencyKey. Sin cuenta no equivale a endpoint sin protección; concretar mecanismo antiabuso antes de recepción pública. Nadie lista reportes públicamente.
- RN-19: solo respuesta del servidor permite mostrar recibido. Reintento con igual clave no duplica; borrador sin conexión no equivale a envío.

## Modelo lógico propuesto
| Entidad | Datos principales |
|---|---|
| clinics | UUID, nombre, dirección, municipio, coordenadas/entrada opcionales, zona, teléfono, publicación, estado operativo, revisión |
| service_types | ID, código estable, nombre |
| clinic_services | clínica, servicio, costo, condiciones, cita, insumos conocidos |
| schedule_days | centro/servicio, día, UNKNOWN/CLOSED/OPEN |
| schedule_intervals | día, inicio/fin local |
| schedule_exceptions | fecha, estado e intervalos de reemplazo |
| evidence | entidad/campo, fuente, referencia, fechas publicación/observación/revisión, resolución |
| reports | ID, centro opcional, propuesta, estado, contacto privado, idempotencyKey, timestamps |
| catalog_revisions | revisión y snapshot público consistente |
| audit_log | actor administrador, acción, entidad, cambios y fecha |

FK y UUID estables. Timestamps técnicos UTC; horario recurrente local con zona. Coordenadas con rangos y orden latitud/longitud validado. Snapshot público excluye reportantes/notas privadas. Migraciones SQL versionadas. Esquema físico posterior debe imponer propietario exclusivo centro/servicio de horarios y validar estados/intervalos.

## Aceptación esencial
1. Día conocido 09–13 y 15–17: 12:59 atención; 13:00 pausa; 15:00 atención; 17:00 cerrado según horario.
2. Día desconocido 10:00: desconocido; no inferir días vecinos.
3. Excepción de cierre sustituye día normal; conflicto/cierre temporal tienen prioridad.
4. Consulta FREE y cirugía UNKNOWN: solo consulta coincide como gratuita.
5. Reporte de cobro pendiente no cambia catálogo automáticamente.
6. Red caída conserva cache con fecha; sin cache muestra error/reintento.
7. Misma clave de envío crea una recepción; fallo no muestra recibido.
8. Ubicación denegada conserva lista/búsqueda/detalle.
