package es.iesfranciscodelosrios.chat;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import java.io.File;

public class GestorXML {

    private static final File ARCHIVO_USUARIOS = new File("usuarios.xml");
    private static final File ARCHIVO_MENSAJES = new File("mensajes.xml");

    // TODO Fase 1: guardar usuarios.xml usando JAXB (Marshaller).
    public void guardarUsuarios(ListaUsuarios usuarios) throws JAXBException {
        JAXBContext ctx = JAXBContext.newInstance(ListaUsuarios.class);
        Marshaller m = ctx.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true); // XML con sangrías
        m.marshal(usuarios, ARCHIVO_USUARIOS);
    }

    // TODO Fase 1: recuperar usuarios.xml usando JAXB (Unmarshaller).
    // Si no existe el archivo, devolver una ListaUsuarios vacía.
    public ListaUsuarios leerUsuarios() throws JAXBException {
        if (!ARCHIVO_USUARIOS.exists()) {
            return new ListaUsuarios(); // primer arranque
        }
        JAXBContext ctx = JAXBContext.newInstance(ListaUsuarios.class);
        Unmarshaller u = ctx.createUnmarshaller();
        return (ListaUsuarios) u.unmarshal(ARCHIVO_USUARIOS);
    }

    // TODO Fase 2: guardar mensajes.xml usando JAXB.
    public void guardarMensajes(ListaMensajes mensajes) throws JAXBException {
        JAXBContext ctx = JAXBContext.newInstance(ListaMensajes.class);
        Marshaller m = ctx.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(mensajes, ARCHIVO_MENSAJES);
    }

    // TODO Fase 2: recuperar mensajes.xml usando JAXB.
    // Si no existe el archivo, devolver una ListaMensajes vacía.
    public ListaMensajes leerMensajes() throws JAXBException {
        if (!ARCHIVO_MENSAJES.exists()) {
            return new ListaMensajes();
        }
        JAXBContext ctx = JAXBContext.newInstance(ListaMensajes.class);
        Unmarshaller u = ctx.createUnmarshaller();
        return (ListaMensajes) u.unmarshal(ARCHIVO_MENSAJES);
    }
}

