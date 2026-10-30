package es.iesfranciscodelosrios.chat;

import jakarta.xml.bind.JAXBException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GestorChat {
    private final GestorXML gestorXML = new GestorXML();

    // TODO Fase 1: comprobar duplicados y registrar el usuario en XML.
    public void registrarUsuario(String nombre) throws JAXBException {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        ListaUsuarios lista = gestorXML.leerUsuarios();
        boolean existe = lista.getUsuarios().stream()
                .anyMatch(u -> u.getNombre().equalsIgnoreCase(nombre.trim()));
        if (existe) {
            throw new IllegalArgumentException("El usuario ya existe.");
        }
        lista.getUsuarios().add(new Usuario(nombre.trim()));
        gestorXML.guardarUsuarios(lista);
    }

    // TODO Fase 2: validar usuarios, crear mensaje con fecha y persistirlo.
    public void enviarMensaje(String emisor, String destinatario, String contenido) throws JAXBException {
        if (contenido == null || contenido.isBlank()) {
            throw new IllegalArgumentException("El mensaje no puede estar vacío.");
        }
        ListaUsuarios usuarios = gestorXML.leerUsuarios();
        boolean existeEmisor = usuarios.getUsuarios().stream()
                .anyMatch(u -> u.getNombre().equalsIgnoreCase(emisor));
        boolean existeDestinatario = usuarios.getUsuarios().stream()
                .anyMatch(u -> u.getNombre().equalsIgnoreCase(destinatario));
        if (!existeEmisor || !existeDestinatario) {
            throw new IllegalArgumentException("El emisor o el destinatario no están registrados.");
        }

        Mensaje mensaje = new Mensaje(emisor, destinatario, contenido,
                LocalDateTime.now().withNano(0).toString()); // ISO-8601 sin nanosegundos

        ListaMensajes lista = gestorXML.leerMensajes();
        lista.getMensajes().add(mensaje);
        gestorXML.guardarMensajes(lista);
    }

    // TODO Fase 3: filtrar mensajes en ambos sentidos (A -> B y B -> A).
    public List<Mensaje> consultarConversacion(String usuarioA, String usuarioB) throws JAXBException {
        return gestorXML.leerMensajes().getMensajes().stream()
                .filter(m ->
                        (m.getEmisor().equalsIgnoreCase(usuarioA) && m.getDestinatario().equalsIgnoreCase(usuarioB))
                                || (m.getEmisor().equalsIgnoreCase(usuarioB) && m.getDestinatario().equalsIgnoreCase(usuarioA)))
                .toList();
    }

    // TODO Fase 4: utilizar Stream API para contar mensajes por emisor.
    public Map<String, Long> estadisticas(String usuarioA, String usuarioB) throws JAXBException {
        return consultarConversacion(usuarioA, usuarioB).stream()
                .collect(Collectors.groupingBy(Mensaje::getEmisor, Collectors.counting()));
    }

    // TODO Fase 4: exportar la conversación a un archivo de texto.
    public void exportarConversacion(String usuarioA, String usuarioB, Path destino)
            throws JAXBException, IOException {
        List<String> lineas = consultarConversacion(usuarioA, usuarioB).stream()
                .map(m -> "[" + m.getFecha() + "] " + m.getEmisor() + " -> "
                        + m.getDestinatario() + ": " + m.getContenido()
                        + (m.getAdjuntoRuta() != null
                        ? " (adjunto: " + m.getAdjuntoNombre()
                        + (Files.exists(Path.of(m.getAdjuntoRuta())) ? "" : " [NO DISPONIBLE]") + ")"
                        : ""))
                .toList();
        Files.write(destino, lineas); // UTF-8 por defecto
    }

    public long totalMensajes(String usuarioA, String usuarioB) throws JAXBException {
        return consultarConversacion(usuarioA, usuarioB).stream().count();
    }

    public Map<String, Long> palabrasMasComunes(String usuarioA, String usuarioB, int limite)
            throws JAXBException {
        return consultarConversacion(usuarioA, usuarioB).stream()
                .flatMap(m -> Arrays.stream(m.getContenido().toLowerCase().split("[^\\p{L}\\p{N}]+")))
                .filter(p -> p.length() > 2)
                .collect(Collectors.groupingBy(p -> p, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limite)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (x, y) -> x, LinkedHashMap::new));
    }

    private static final Path CARPETA_MEDIA = Path.of("media");
    private static final long TAMANO_MAXIMO = 5 * 1024 * 1024; // 5 MB
    private static final List<String> EXTENSIONES_PERMITIDAS =
            List.of("jpg", "jpeg", "png", "gif", "pdf", "txt", "zip", "mp3", "mp4");

    // TODO Fase 5: validar extensión/tamaño, copiar con InputStream/OutputStream
// a media/ y guardar los metadatos del adjunto en el mensaje XML.
    public void adjuntarArchivo(Mensaje mensaje, Path archivoOrigen) throws IOException, JAXBException {
        if (archivoOrigen == null || !Files.isRegularFile(archivoOrigen)) {
            throw new IllegalArgumentException("El archivo no existe.");
        }

        String nombre = archivoOrigen.getFileName().toString();
        int punto = nombre.lastIndexOf('.');
        String extension = punto >= 0 ? nombre.substring(punto + 1).toLowerCase() : "";
        if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
            throw new IllegalArgumentException("Extensión no permitida: " + extension);
        }

        long tamano = Files.size(archivoOrigen);
        if (tamano > TAMANO_MAXIMO) {
            throw new IllegalArgumentException("El archivo supera los 5 MB.");
        }

        ListaUsuarios usuarios = gestorXML.leerUsuarios();
        boolean existeEmisor = usuarios.getUsuarios().stream()
                .anyMatch(u -> u.getNombre().equalsIgnoreCase(mensaje.getEmisor()));
        boolean existeDestinatario = usuarios.getUsuarios().stream()
                .anyMatch(u -> u.getNombre().equalsIgnoreCase(mensaje.getDestinatario()));
        if (!existeEmisor || !existeDestinatario) {
            throw new IllegalArgumentException("El emisor o el destinatario no están registrados.");
        }

        // Copia binaria a media/ (prefijo con la hora para no pisar archivos con el mismo nombre)
        Files.createDirectories(CARPETA_MEDIA);
        Path destino = CARPETA_MEDIA.resolve(System.currentTimeMillis() + "_" + nombre);
        try (InputStream in = Files.newInputStream(archivoOrigen);
             OutputStream out = Files.newOutputStream(destino)) {
            byte[] buffer = new byte[8192];
            int leidos;
            while ((leidos = in.read(buffer)) != -1) {
                out.write(buffer, 0, leidos);
            }
        }

        // Metadatos en el mensaje (solo la referencia, no el binario)
        String tipo = Files.probeContentType(archivoOrigen);
        mensaje.setAdjuntoRuta(destino.toString());
        mensaje.setAdjuntoNombre(nombre);
        mensaje.setAdjuntoTamano(tamano);
        mensaje.setAdjuntoTipo(tipo != null ? tipo : "application/octet-stream");

        ListaMensajes lista = gestorXML.leerMensajes();
        lista.getMensajes().add(mensaje);
        gestorXML.guardarMensajes(lista);
    }


    public void exportarAdjunto(Mensaje mensaje, Path destino) throws IOException {
        if (mensaje.getAdjuntoRuta() == null) {
            throw new IllegalArgumentException("El mensaje no tiene adjunto.");
        }
        Path origen = Path.of(mensaje.getAdjuntoRuta());
        if (!Files.exists(origen)) {
            throw new IllegalArgumentException("El adjunto ya no está disponible.");
        }
        try (InputStream in = Files.newInputStream(origen);
             OutputStream out = Files.newOutputStream(destino)) {
            in.transferTo(out);
        }
    }

    public void abrirAdjunto(Mensaje mensaje) throws IOException {
        if (mensaje.getAdjuntoRuta() == null || !Files.exists(Path.of(mensaje.getAdjuntoRuta()))) {
            throw new IllegalArgumentException("El adjunto no está disponible.");
        }
        java.awt.Desktop.getDesktop().open(Path.of(mensaje.getAdjuntoRuta()).toFile());
    }
}