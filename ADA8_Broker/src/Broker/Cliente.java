/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Broker;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Scanner;

public class Cliente {

    private static final Gson GSON = new Gson();

    private final String ipBroker;
    private final int puertoBroker;
    private final Scanner scanner;

    public Cliente(String ipBroker, int puertoBroker) {
        this.ipBroker = ipBroker;
        this.puertoBroker = puertoBroker;
        this.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java Cliente.Cliente <ipBroker:puertoBroker>");
            return;
        }
        String[] partes = args[0].split(":", -1);
        if (partes.length != 2) {
            System.out.println("Direccion del Broker invalida. Formato esperado: ip:puerto");
            return;
        }
        String ip = partes[0];
        int puerto;
        try {
            puerto = Integer.parseInt(partes[1]);
        } catch (NumberFormatException e) {
            System.out.println("Puerto del Broker invalido: " + partes[1]);
            return;
        }
        new Cliente(ip, puerto).ejecutarMenu();
    }

    public void ejecutarMenu() {
        boolean continuar = true;
        while (continuar) {
            try {
                mostrarMenu();
                int opcion = leerEntero("Seleccione una opcion: ");
                switch (opcion) {
                    case 1:
                        registrarPedido();
                        break;
                    case 2:
                        consultarPedido();
                        break;
                    case 3:
                        listarPedidos();
                        break;
                    case 4:
                        System.out.println("El inventario no es un servicio del Broker; "
                                + "consulte los pedidos o registre uno nuevo.");
                        break;
                    case 0:
                        continuar = false;
                        break;
                    default:
                        System.out.println("[ERROR] Opcion no valida");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("[ERROR] " + e.getMessage());
            }
        }
        System.out.println("Hasta luego.");
    }

    private void mostrarMenu() {
        System.out.println("\n=== GESTION DE PEDIDOS (Cliente / Broker) ===");
        System.out.println("1. Registrar pedido");
        System.out.println("2. Consultar pedido");
        System.out.println("3. Listar pedidos");
        System.out.println("4. Inventario (informativo)");
        System.out.println("0. Salir");
    }

    // ------------------- Operaciones -------------------

    private void registrarPedido() {
        System.out.print("Cliente: ");
        String cliente = scanner.nextLine().trim();
        int cantidad = leerEntero("Cuantos productos desea agregar? ");
        if (cantidad <= 0) {
            throw new IllegalArgumentException("Debe agregar al menos un producto");
        }

        JsonArray productos = new JsonArray();
        for (int i = 1; i <= cantidad; i++) {
            System.out.println("Producto " + i + ":");
            System.out.print("  Nombre: ");
            String nombre = scanner.nextLine().trim();
            int cant = leerEntero("  Cantidad: ");
            JsonObject p = new JsonObject();
            p.addProperty("nombre", nombre);
            p.addProperty("cantidad", cant);
            productos.add(p);
        }

        JsonObject argumentos = new JsonObject();
        argumentos.addProperty("cliente", cliente);
        argumentos.add("productos", productos);

        JsonObject respuesta = ejecutar("registrarPedido", argumentos);
        mostrarRespuesta(respuesta, "registrarPedido");
    }

    private void consultarPedido() {
        int id = leerEntero("Identificador del pedido: ");
        JsonObject argumentos = new JsonObject();
        argumentos.addProperty("id", id);
        JsonObject respuesta = ejecutar("consultarPedido", argumentos);
        mostrarRespuesta(respuesta, "consultarPedido");
    }

    private void listarPedidos() {
        JsonObject respuesta = ejecutar("listarPedidos", new JsonObject());
        mostrarRespuesta(respuesta, "listarPedidos");
    }

    // ------------------- Protocolo -------------------

    /**
     * Envia al Broker una solicitud "ejecutar" y devuelve la respuesta ya parseada.
     * El formato exacto del mensaje es:
     *   {"servicio":"ejecutar","parametros":{"servicio":<n>,"argumentos":<a>}}
     */
    private JsonObject ejecutar(String nombreServicio, JsonObject argumentos) {
        JsonObject parametros = new JsonObject();
        parametros.addProperty("servicio", nombreServicio);
        parametros.add("argumentos", argumentos != null ? argumentos : new JsonObject());

        JsonObject solicitud = new JsonObject();
        solicitud.addProperty("servicio", "ejecutar");
        solicitud.add("parametros", parametros);

        try {
            return enviar(solicitud);
        } catch (IOException e) {
            return errorLocal("ejecutar", "No se pudo contactar al Broker: " + e.getMessage());
        }
    }

    private JsonObject enviar(JsonObject solicitud) throws IOException {
        try (Socket socket = new Socket(ipBroker, puertoBroker);
                BufferedReader entrada = new BufferedReader(new InputStreamReader(
                        socket.getInputStream(), StandardCharsets.UTF_8));
                BufferedWriter salida = new BufferedWriter(new OutputStreamWriter(
                        socket.getOutputStream(), StandardCharsets.UTF_8))) {

            salida.write(GSON.toJson(solicitud));
            salida.newLine();
            salida.flush();

            String linea = entrada.readLine();
            if (linea == null) {
                throw new IOException("El Broker cerro la conexion sin responder");
            }
            try {
                JsonElement elemento = JsonParser.parseString(linea);
                if (!elemento.isJsonObject()) {
                    throw new IOException("Respuesta del Broker no es un objeto JSON");
                }
                return elemento.getAsJsonObject();
            } catch (JsonParseException e) {
                throw new IOException("Respuesta del Broker no es JSON valido", e);
            }
        }
    }

    // ------------------- Presentacion -------------------

    private void mostrarRespuesta(JsonObject respuesta, String servicioEsperado) {
        String estado = respuesta.has("estado") && !respuesta.get("estado").isJsonNull()
                ? respuesta.get("estado").getAsString() : null;

        if (!"OK".equals(estado)) {
            String mensaje = respuesta.has("mensaje") && !respuesta.get("mensaje").isJsonNull()
                    ? respuesta.get("mensaje").getAsString()
                    : "Error desconocido";
            System.out.println("[ERROR] " + mensaje);
            return;
        }

        JsonObject resultado = respuesta.has("resultado") && respuesta.get("resultado").isJsonObject()
                ? respuesta.getAsJsonObject("resultado") : new JsonObject();

        JsonElement resp = resultado.has("respuesta") ? resultado.get("respuesta") : null;

        switch (servicioEsperado) {
            case "registrarPedido":
                System.out.println("\nPedido registrado correctamente.");
                imprimirPedido(resp);
                break;
            case "consultarPedido":
                imprimirPedido(resp);
                break;
            case "listarPedidos":
                imprimirLista(resp);
                break;
            default:
                System.out.println(resultado.toString());
        }
    }

    private void imprimirPedido(JsonElement pedido) {
        if (pedido == null || !pedido.isJsonObject()) {
            System.out.println("(sin datos del pedido)");
            return;
        }
        JsonObject p = pedido.getAsJsonObject();
        int id = p.has("id") ? p.get("id").getAsInt() : 0;
        String cliente = p.has("cliente") ? p.get("cliente").getAsString() : "";
        System.out.printf(Locale.US, "Pedido: %d%n", id);
        System.out.println("Cliente: " + cliente);
        if (p.has("productos") && p.get("productos").isJsonArray()) {
            for (JsonElement e : p.getAsJsonArray("productos")) {
                JsonObject prod = e.getAsJsonObject();
                String n = prod.has("nombre") ? prod.get("nombre").getAsString() : "";
                int c = prod.has("cantidad") ? prod.get("cantidad").getAsInt() : 0;
                double precio = prod.has("precio") ? prod.get("precio").getAsDouble() : 0.0;
                System.out.printf(Locale.US, "- %s x%d: $%.2f%n", n, c, precio * c);
            }
        }
        imprimirNumero(p, "subtotal", "Subtotal");
        imprimirNumero(p, "descuento", "Descuento");
        imprimirNumero(p, "impuestos", "Impuestos");
        imprimirNumero(p, "total", "Total");
        if (p.has("estado")) {
            System.out.println("Estado: " + p.get("estado").getAsString());
        }
    }

    private void imprimirLista(JsonElement resp) {
        if (resp == null || !resp.isJsonArray() || resp.getAsJsonArray().isEmpty()) {
            System.out.println("No hay pedidos registrados.");
            return;
        }
        System.out.println("\n=== PEDIDOS REGISTRADOS ===");
        for (JsonElement e : resp.getAsJsonArray()) {
            imprimirPedido(e);
            System.out.println();
        }
    }

    private static void imprimirNumero(JsonObject o, String campo, String etiqueta) {
        if (o.has(campo) && !o.get(campo).isJsonNull()) {
            System.out.printf(Locale.US, "%s: $%.2f%n", etiqueta, o.get(campo).getAsDouble());
        }
    }

    // ------------------- Utilidades -------------------

    private int leerEntero(String mensaje) {
        System.out.print(mensaje);
        String linea = scanner.nextLine().trim();
        try {
            return Integer.parseInt(linea);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Debe escribir un numero entero valido");
        }
    }

    private static JsonObject errorLocal(String servicio, String mensaje) {
        JsonObject respuesta = new JsonObject();
        respuesta.addProperty("servicio", servicio);
        respuesta.addProperty("estado", "ERROR");
        respuesta.addProperty("mensaje", mensaje);
        return respuesta;
    }
}