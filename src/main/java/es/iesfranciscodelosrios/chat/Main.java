package es.iesfranciscodelosrios.chat;

import jakarta.xml.bind.JAXBException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        GestorChat chat = new GestorChat();

        System.out.println("Chat Offline - Proyecto UD1");

        // TODO: implementar menú repetitivo con las opciones:
        // 1. Registrar usuario
        // 2. Enviar mensaje
        // 3. Consultar conversación
        // 4. Estadísticas
        // 5. Exportar conversación
        // 0. Salir
        int opcion = -1;
        while (opcion != 0) {
            System.out.println("\n--- MENÚ ---");
            System.out.println("1. Registrar usuario");
            System.out.println("2. Enviar mensaje");
            System.out.println("3. Consultar conversación");
            System.out.println("4. Estadísticas");
            System.out.println("5. Exportar conversación");
            System.out.println("0. Salir");
            System.out.print("Opción: ");

            try {
                opcion = Integer.parseInt(teclado.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Introduce un número válido.");
                continue;
            }

            try {
                switch (opcion) {
                    case 1 -> {
                        System.out.print("Nombre del usuario: ");
                        String nombre = teclado.nextLine();
                        chat.registrarUsuario(nombre);
                        System.out.println("Usuario registrado.");
                    }
                    case 2 -> {
                        System.out.print("Emisor: ");
                        String emisor = teclado.nextLine();
                        System.out.print("Destinatario: ");
                        String destinatario = teclado.nextLine();
                        System.out.print("Mensaje: ");
                        String contenido = teclado.nextLine();
                        chat.enviarMensaje(emisor, destinatario, contenido);
                        System.out.println("Mensaje enviado.");
                    }
                    case 3 -> {
                        System.out.print("Usuario A: ");
                        String a = teclado.nextLine();
                        System.out.print("Usuario B: ");
                        String b = teclado.nextLine();
                        List<Mensaje> conversacion = chat.consultarConversacion(a, b);
                        if (conversacion.isEmpty()) {
                            System.out.println("No hay mensajes entre esos usuarios.");
                        } else {
                            for (Mensaje m : conversacion) {
                                System.out.println("[" + m.getFecha() + "] " + m.getEmisor() + " -> "
                                        + m.getDestinatario() + ": " + m.getContenido());
                            }
                        }
                    }
                    case 4 -> {
                        System.out.print("Usuario A: ");
                        String a = teclado.nextLine();
                        System.out.print("Usuario B: ");
                        String b = teclado.nextLine();
                        Map<String, Long> stats = chat.estadisticas(a, b);
                        if (stats.isEmpty()) {
                            if (chat.consultarConversacion(a, b).isEmpty()) {
                                System.out.println("No hay mensajes entre esos usuarios.");
                            }
                        } else {
                            stats.entrySet().stream().limit(10)
                                    .forEach(e -> System.out.println(e.getKey() + ": " + e.getValue()));
                        }
                    }
                    case 5 -> {
                        System.out.print("Usuario A: ");
                        String a = teclado.nextLine();
                        System.out.print("Usuario B: ");
                        String b = teclado.nextLine();
                        System.out.print("Archivo de destino (ej. conversacion.txt): ");
                        String ruta = teclado.nextLine();
                        chat.exportarConversacion(a, b, Path.of(ruta));
                        System.out.println("Conversación exportada.");
                    }
                    case 0 -> System.out.println("Hasta pronto.");
                    default -> System.out.println("Opción no válida.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (JAXBException e) {
                System.out.println("Error con el XML: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("Error con el archivo: " + e.getMessage());
            }
        }

        teclado.close();
    }
}