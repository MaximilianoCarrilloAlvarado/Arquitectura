package Broker;

import Modelo.Pedido;
import Modelo.PedidoModelo;
import Modelo.Producto;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ManejadorSolicitudServidor implements Runnable {

    private static final Gson GSON = new Gson();

    private final Socket conexion;
    private final PedidoModelo modelo;

    public ManejadorSolicitudServidor(Socket conexion, PedidoModelo modelo) {
        this.conexion = conexion;
        this.modelo = modelo;
    }

    @Override
    public void run() {
        try (Socket socket = conexion;
                BufferedReader entrada = new BufferedReader(new InputStreamReader(
                        socket.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter salida = new PrintWriter(socket.getOutputStream(), true,
                        StandardCharsets.UTF_8)) {
            String linea = entrada.readLine();
            if (linea == null) {
                return;
            }

            JsonObject respuesta;
            try {
                respuesta = procesar(linea);
            } catch (JsonParseException | IllegalArgumentException | IllegalStateException e) {
                respuesta = crearError(obtenerServicio(linea), e.getMessage());
            }

            String servicio = respuesta.has("servicio")
                    ? respuesta.get("servicio").getAsString() : "?";
            String estado = respuesta.has("estado")
                    ? respuesta.get("estado").getAsString() : "?";
            if ("OK".equals(estado)) {
                System.out.println("  [OK] " + servicio + " -> respuesta enviada al Broker");
            } else {
                String mensaje = respuesta.has("mensaje")
                        ? respuesta.get("mensaje").getAsString() : "";
                System.out.println("  [ERROR] " + servicio + " -> " + mensaje);
            }

            salida.println(GSON.toJson(respuesta));
        } catch (IOException e) {
            System.err.println("Error de comunicacion con el Broker: " + e.getMessage());
        }
    }

    private JsonObject procesar(String linea) {
        JsonObject solicitud = JsonParser.parseString(linea).getAsJsonObject();
        String servicio = texto(solicitud, "servicio");
        System.out.println("Solicitud recibida: " + servicio);

        JsonObject parametros = solicitud.has("parametros")
                && solicitud.get("parametros").isJsonObject()
                ? solicitud.getAsJsonObject("parametros") : new JsonObject();

        switch (servicio) {
            case "registrarPedido":
                return registrarPedido(parametros);
            case "consultarPedido":
                return consultarPedido(parametros);
            case "listarPedidos":
                return listarPedidos();
            default:
                return crearError(servicio, "Servicio no reconocido por el servidor");
        }
    }

    private JsonObject registrarPedido(JsonObject parametros) {
        try {
            String cliente = texto(parametros, "cliente");
            if (!parametros.has("productos") || !parametros.get("productos").isJsonArray()) {
                throw new IllegalArgumentException("Debe proporcionar la lista de productos");
            }

            JsonArray productosJson = parametros.getAsJsonArray("productos");
            List<Producto> productos = new ArrayList<>();
            for (JsonElement elemento : productosJson) {
                JsonObject producto = elemento.getAsJsonObject();
                productos.add(new Producto(
                        texto(producto, "nombre"),
                        0.0,
                        entero(producto, "cantidad"),
                        0));
            }

            Pedido registrado = modelo.registrarPedido(new Pedido(cliente, productos));
            System.out.println("    Pedido #" + registrado.getId()
                    + " registrado para " + registrado.getCliente()
                    + " (total $" + String.format(java.util.Locale.US, "%.2f", registrado.getTotal()) + ")");
            return crearOk("registrarPedido", GSON.toJsonTree(registrado));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return crearError("registrarPedido", e.getMessage());
        }
    }

    private JsonObject consultarPedido(JsonObject parametros) {
        try {
            int id = entero(parametros, "id");
            Pedido pedido = modelo.consultarPedido(id);
            if (pedido == null) {
                throw new IllegalArgumentException("Pedido no encontrado");
            }
            System.out.println("    Pedido #" + id + " consultado");
            return crearOk("consultarPedido", GSON.toJsonTree(pedido));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return crearError("consultarPedido", e.getMessage());
        }
    }

    private JsonObject listarPedidos() {
        List<Pedido> pedidos = modelo.listarPedidos();
        System.out.println("    Listando " + pedidos.size() + " pedido(s)");
        return crearOk("listarPedidos", GSON.toJsonTree(pedidos));
    }

    public static void enviarRegistro(String ipBroker, int puertoBroker,
            String nombreServicio, String ipServidor, int puertoServidor) throws IOException {
        JsonObject parametros = new JsonObject();
        parametros.addProperty("nombre", nombreServicio);
        parametros.addProperty("ip", ipServidor);
        parametros.addProperty("puerto", puertoServidor);

        JsonObject solicitud = new JsonObject();
        solicitud.addProperty("servicio", "registrar");
        solicitud.add("parametros", parametros);

        try (Socket socket = new Socket(ipBroker, puertoBroker);
                BufferedReader entrada = new BufferedReader(new InputStreamReader(
                        socket.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter salida = new PrintWriter(socket.getOutputStream(), true,
                        StandardCharsets.UTF_8)) {
            salida.println(GSON.toJson(solicitud));
            String linea = entrada.readLine();
            if (linea == null) {
                throw new IOException("El Broker cerro la conexion sin responder");
            }
            JsonObject respuesta = JsonParser.parseString(linea).getAsJsonObject();
            if ("ERROR".equals(respuesta.has("estado")
                    ? respuesta.get("estado").getAsString() : null)) {
                throw new IOException("El Broker rechazo el registro: "
                        + respuesta.get("mensaje").getAsString());
            }
        } catch (JsonParseException | IllegalStateException e) {
            throw new IOException("Respuesta invalida del Broker", e);
        }
    }

    private static JsonObject crearOk(String servicio, JsonElement resultado) {
        JsonObject respuesta = new JsonObject();
        respuesta.addProperty("servicio", servicio);
        respuesta.addProperty("estado", "OK");
        respuesta.add("resultado", resultado);
        return respuesta;
    }

    private static JsonObject crearError(String servicio, String mensaje) {
        JsonObject respuesta = new JsonObject();
        respuesta.addProperty("servicio", servicio);
        respuesta.addProperty("estado", "ERROR");
        respuesta.addProperty("mensaje", mensaje == null ? "Error interno del servidor" : mensaje);
        return respuesta;
    }

    private static String obtenerServicio(String linea) {
        try {
            JsonObject solicitud = JsonParser.parseString(linea).getAsJsonObject();
            return solicitud.has("servicio") ? solicitud.get("servicio").getAsString() : "desconocido";
        } catch (JsonParseException | IllegalStateException e) {
            return "desconocido";
        }
    }

    private static String texto(JsonObject objeto, String campo) {
        if (!objeto.has(campo) || !objeto.get(campo).isJsonPrimitive()
                || !objeto.getAsJsonPrimitive(campo).isString()) {
            throw new IllegalArgumentException("Falta el campo de texto: " + campo);
        }
        return objeto.get(campo).getAsString();
    }

    private static int entero(JsonObject objeto, String campo) {
        if (!objeto.has(campo) || !objeto.get(campo).isJsonPrimitive()
                || !objeto.getAsJsonPrimitive(campo).isNumber()) {
            throw new IllegalArgumentException("Falta el campo numerico: " + campo);
        }
        return objeto.get(campo).getAsInt();
    }
}