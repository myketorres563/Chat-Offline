package es.iesfranciscodelosrios.chat;

import jakarta.xml.bind.JAXBException;

import java.io.IOException;
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

    // TODO Fase 4: utilizar Stream API para contar las palabras de la conversación.
    public Map<String, Long> estadisticas(String usuarioA, String usuarioB) throws JAXBException {
        return consultarConversacion(usuarioA, usuarioB).stream()
                .flatMap(m -> Arrays.stream(m.getContenido().toLowerCase()
                        .split("[^\\p{L}\\p{N}]+")))
                .filter(palabra -> !palabra.isEmpty())
                .collect(Collectors.groupingBy(palabra -> palabra, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (x, y) -> x, LinkedHashMap::new));
    }

    // TODO Fase 4: exportar la conversación a un archivo de texto.
    public void exportarConversacion(String usuarioA, String usuarioB, Path destino)
            throws JAXBException, IOException {
        List<String> lineas = consultarConversacion(usuarioA, usuarioB).stream()
                .map(m -> "[" + m.getFecha() + "] " + m.getEmisor() + " -> "
                        + m.getDestinatario() + ": " + m.getContenido())
                .toList();
        Files.write(destino, lineas); // UTF-8 por defecto
    }
}