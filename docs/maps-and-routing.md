# Mapas y rutas de Vet2MyFriend

Actualizado el 17 de noviembre de 2026.

## Implementación actual

Google Maps SDK para Android muestra el mapa. Geoapify calcula rutas y busca direcciones por HTTP. La ruta se dibuja con polilíneas dentro de Vet2MyFriend; elegir A pie, Bici o Carro ya no abre Google Maps.

Se muestran distancia y duración estimadas. Esta entrega permite planear un recorrido; navegación continua, voz, seguimiento y recálculo quedan para otra entrega.

## Uso

1. Abrir Inicio → Lista y elegir una clínica y un medio de transporte.
2. Elegir el origen con Mi ubicación, por dirección o tocando el mapa.
3. Si la clínica todavía no tiene coordenadas verificadas, buscar su dirección y revisar los resultados, o elegir su entrada en el mapa.
4. Pulsar Calcular ruta. Cambiar un punto o el medio de transporte elimina el recorrido anterior; volver a calcular requiere otra pulsación.

La búsqueda está limitada a México y muestra hasta tres resultados. Un resultado puede corresponder a un edificio, una calle o una zona. No se selecciona automáticamente el primero. Revisar el marcador y, si hace falta, corregirlo en el mapa. Elegir un punto para un recorrido no lo guarda como ubicación oficial de la clínica.

La ubicación se pide únicamente al pulsar Mi ubicación. Se acepta ubicación aproximada y se muestra la precisión estimada. No se solicita ubicación en segundo plano. El origen y la geometría del recorrido se mantienen en memoria; no se guardan en Room. Al calcular se transmiten ambos extremos a Geoapify. Buscar una dirección también envía ese texto al proveedor. El mapa y el cálculo necesitan conexión; el catálogo local sigue disponible sin conexión.

## Configuración local

Completar MAPS_API_KEY y GEOAPIFY_API_KEY en secrets.properties, usando secrets.properties.example como referencia. El archivo real está excluido de Git. Sin clave de rutas se muestra un mensaje de configuración pendiente.

La clave Android de Google debe restringirse por paquete, SHA-1 y Maps SDK for Android. Las restricciones Android de Google no protegen una clave REST de Geoapify. En esta etapa la clave Geoapify llega al APK para permitir el desarrollo sin servidor; para distribuir a terceros conviene trasladarla a un backend con límites de uso. No registrar URLs de solicitudes, claves ni posiciones en logs.

## Cuota y atribución

El plan gratuito de Geoapify publica 3,000 créditos diarios por cuenta y hasta 5 solicitudes por segundo. Una ruta básica de dos puntos cercanos suele costar un crédito; búsquedas y otros servicios comparten la cuota. No existe una cuota independiente por usuario de la app.

Las consultas se hacen por pulsación, sin autocompletado por cada letra, recálculo por movimiento del mapa ni reintentos automáticos de red. Una respuesta 429 muestra el límite alcanzado. La app no contrata ni cambia planes del proveedor.

Se muestran enlaces Powered by Geoapify y © OpenStreetMap junto a los resultados, y se conserva la atribución del mapa de Google. Google Routes API no participa en este flujo.

## Verificación

- Compilación debug de la app y análisis Android Lint correctos, sin errores.
- 23 pruebas JVM aprobadas: 11 de dominio, 6 de integración HTTP con servidor simulado y 6 del estado de rutas.
- Llamadas reales con respuesta HTTP 200 para caminar, bicicleta y carro entre puntos públicos de prueba en Guadalupe.
- Búsqueda real de las dos direcciones: Geoapify respondió, pero Los Pequeños Amigos devolvió resultados a nivel de calle; no se tomaron como coordenadas verificadas.
- El propietario confirmó que la app compila y funciona en ejecución.
- Room versión 3, con esquemas exportados y migraciones 1 → 2 → 3. El APK de pruebas de migración se compila; su ejecución automatizada sigue pendiente porque ADB reporta el emulador offline.

## Fuentes oficiales

- [Geoapify Routing API](https://apidocs.geoapify.com/docs/routing/)
- [Geoapify Geocoding API](https://apidocs.geoapify.com/docs/geocoding/)
- [Cuotas y atribución del plan gratuito](https://www.geoapify.com/pricing/)
- [Integración de Geoapify con Google Maps y otros mapas](https://www.geoapify.com/)
- [Permisos de ubicación en Android](https://developer.android.com/develop/sensors-and-location/location/permissions)
- [FusedLocationProviderClient](https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderClient)
- [Configuración de Maps SDK](https://developers.google.com/maps/documentation/android-sdk/get-api-key)
