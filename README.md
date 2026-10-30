# Chat Offline — Plantilla de partida

**2.º DAM · Acceso a Datos · UD1**

Esta plantilla proporciona **las clases de modelo completas** y **los métodos de lógica sin implementar**. No es una solución terminada.

## Requisitos
- JDK 17 o posterior
- Maven

## Ejecutar
Desde la carpeta del proyecto:

```bash
mvn compile exec:java
```

## Orden de desarrollo
1. `GestorXML`: guardar y leer usuarios con JAXB; `GestorChat.registrarUsuario`.
2. `GestorXML`: guardar y leer mensajes; `GestorChat.enviarMensaje`.
3. `GestorChat.consultarConversacion`: recuperar mensajes entre dos usuarios en ambos sentidos.
4. `GestorChat.estadisticas` con Java Stream API para contar palabras y `exportarConversacion` a TXT.
5. `Main`: construir y probar el menú progresivamente, desde la fase 1.

Los métodos pendientes lanzan `UnsupportedOperationException` deliberadamente. Sustituid esa línea por vuestra implementación.

`usuarios.xml` y `mensajes.xml` deben generarse desde Java. No es necesario crearlos manualmente.

### Comprobaciones mínimas
- Registrar dos usuarios, cerrar el programa y comprobar que siguen existiendo al reabrirlo.
- Enviar mensajes en ambos sentidos, cerrar y comprobar que persisten.
- Consultar solo los mensajes entre dos usuarios.
- Contar palabras con Stream API y exportar la conversación a TXT.
