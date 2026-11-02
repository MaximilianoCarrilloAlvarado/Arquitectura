/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.ada8_broker;

/**
 *
 * @author ojeda
 */
import Modelo.Pedido;
import Modelo.PedidoModelo;
import Modelo.Producto;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ManejadorSolicitudServidor implements Runnable {

    private static final Gson gson = new Gson();

    private final Socket conexion;
    private final PedidoModelo modelo;

    public ManejadorSolicitudServidor(
            Socket conexion, PedidoModelo modelo) {
        this.conexion = conexion;
        this.modelo = modelo;
    }

    @Override
    public void run() {
        try (
            Socket socket = conexion;
            BufferedReader entrada = new BufferedReader(
                new InputStreamReader(
                    socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter salida = new PrintWriter(
                new OutputStreamWriter(
                    socket.getOutputStream(), StandardCharsets.UTF_8),
                true)
        ) {
            String linea = entrada.readLine();

            if (linea == null) {
                return;
            }

            JsonObject respuesta;

            try {
                respuesta = procesar(linea);
            } catch (Exception e) {
                respuesta = crearError(
                    obtenerServicio(linea), e.getMessage());
            }

            salida.println(gson.toJson(respuesta));

        } catch (IOException e) {
            System.err.println(
                "Error de comunicación con el Broker: " + e.getMessage());
        }
    }

    private JsonObject procesar(String linea) {
        JsonObject solicitud =
            JsonParser.parseString(linea).getAsJsonObject();

        String servicio = solicitud.get("servicio").getAsString();

        JsonObject parametros = solicitud.has("parametros")
                && solicitud.get("parametros").isJsonObject()
                ? solicitud.getAsJsonObject("parametros")
                : new JsonObject();

        switch (servicio) {
            case "registrarPedido":
                return registrarPedido(parametros);

            case "consultarPedido":
                return consultarPedido(parametros);

            case "listarPedidos":
                return listarPedidos();

            default:
                return crearError(
                    servicio, "Servicio no reconocido por el servidor");
        }
    }

    private JsonObject registrarPedido(JsonObject parametros) {
        try {
            String cliente =
                parametros.get("cliente").getAsString();

            JsonArray productosJson =
                parametros.getAsJsonArray("productos");

            if (productosJson == null) {
                throw new IllegalArgumentException(
                    "Debe proporcionar la lista de productos");
            }

            List<Producto> productos = new ArrayList<>();

            for (JsonElement elemento : productosJson) {
                JsonObject p = elemento.getAsJsonObject();

                String nombre = p.get("nombre").getAsString();
                int cantidad = p.get("cantidad").getAsInt();

                // El modelo consulta el precio y la existencia reales
                // en InventarioModelo; estos valores son provisionales.
                productos.add(new Producto(
                    nombre, 0.0, cantidad, 0));
            }

            Pedido pedido = new Pedido(cliente, productos);

            // Valida, consulta inventario, calcula importes y persiste.
            Pedido registrado = modelo.registrarPedido(pedido);

            return crearOk(
                "registrarPedido", gson.toJsonTree(registrado));

        } catch (Exception e) {
            return crearError("registrarPedido", e.getMessage());
        }
    }

    private JsonObject consultarPedido(JsonObject parametros) {
        try {
            int id = parametros.get("id").getAsInt();

            Pedido pedido = modelo.consultarPedido(id);

            if (pedido == null) {
                throw new IllegalArgumentException(
                    "Pedido no encontrado");
            }

            return crearOk(
                "consultarPedido", gson.toJsonTree(pedido));

        } catch (Exception e) {
            return crearError("consultarPedido", e.getMessage());
        }
    }

    private JsonObject listarPedidos() {
        try {
            JsonElement pedidos =
                gson.toJsonTree(modelo.listarPedidos());

            return crearOk("listarPedidos", pedidos);

        } catch (Exception e) {
            return crearError("listarPedidos", e.getMessage());
        }
    }

    private static JsonObject crearOk(
            String servicio, JsonElement resultado) {

        JsonObject respuesta = new JsonObject();
        respuesta.addProperty("servicio", servicio);
        respuesta.addProperty("estado", "OK");
        respuesta.add("resultado", resultado);
        return respuesta;
    }

    private static JsonObject crearError(
            String servicio, String mensaje) {

        JsonObject respuesta = new JsonObject();
        respuesta.addProperty("servicio", servicio);
        respuesta.addProperty("estado", "ERROR");
        respuesta.addProperty(
            "mensaje",
            mensaje == null ? "Error interno del servidor" : mensaje);
        return respuesta;
    }

    private static String obtenerServicio(String linea) {
        try {
            JsonObject json =
                JsonParser.parseString(linea).getAsJsonObject();

            if (json.has("servicio")) {
                return json.get("servicio").getAsString();
            }
        } catch (Exception ignored) {
            // La solicitud podría no ser JSON válido.
        }

        return "desconocido";
    }

    public static void enviarRegistro(
            String ipBroker,
            int puertoBroker,
            String nombreServicio,
            String ipServidor,
            int puertoServidor) throws IOException {

        JsonObject parametros = new JsonObject();
        parametros.addProperty("nombre", nombreServicio);
        parametros.addProperty("ip", ipServidor);
        parametros.addProperty("puerto", puertoServidor);

        JsonObject solicitud = new JsonObject();
        solicitud.addProperty("servicio", "registrar");
        solicitud.add("parametros", parametros);

        try (
            Socket socket = new Socket(ipBroker, puertoBroker);
            BufferedReader entrada = new BufferedReader(
                new InputStreamReader(
                    socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter salida = new PrintWriter(
                new OutputStreamWriter(
                    socket.getOutputStream(), StandardCharsets.UTF_8),
                true)
        ) {
            salida.println(gson.toJson(solicitud));

            String linea = entrada.readLine();

            if (linea == null) {
                throw new IOException(
                    "El Broker cerró la conexión sin responder");
            }

            JsonObject respuesta =
                JsonParser.parseString(linea).getAsJsonObject();

            if (respuesta.has("estado")
                    && "ERROR".equals(
                        respuesta.get("estado").getAsString())) {
                throw new IOException(
                    "El Broker rechazó el registro: "
                    + respuesta.get("mensaje").getAsString());
            }
        }
    }
}