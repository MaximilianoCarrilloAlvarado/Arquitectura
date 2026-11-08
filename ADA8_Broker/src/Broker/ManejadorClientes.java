package Broker;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class ManejadorClientes implements Runnable {

    private final Socket cliente;
    private final RegistroServicios registro;

    public ManejadorClientes(Socket cliente, RegistroServicios registro) {
        this.cliente = cliente;
        this.registro = registro;
    }

    @Override
    public void run() {
        try (Socket conexion = cliente;
                BufferedReader entrada = new BufferedReader(new InputStreamReader(
                        conexion.getInputStream(), StandardCharsets.UTF_8));
                BufferedWriter salida = new BufferedWriter(new OutputStreamWriter(
                        conexion.getOutputStream(), StandardCharsets.UTF_8))) {
            String linea = entrada.readLine();
            if (linea == null) {
                return;
            }

            String respuesta;
            try {
                JsonElement solicitud = JsonParser.parseString(linea);
                if (!solicitud.isJsonObject()) {
                    throw new IllegalArgumentException("La solicitud debe ser un objeto JSON");
                }
                respuesta = procesar(solicitud.getAsJsonObject()).toString();
            } catch (JsonParseException | IllegalArgumentException | IllegalStateException e) {
                respuesta = error("desconocido", "Solicitud invalida: " + e.getMessage()).toString();
            }

            salida.write(respuesta);
            salida.newLine();
            salida.flush();
        } catch (IOException e) {
            System.err.println("Error atendiendo conexion: " + e.getMessage());
        }
    }

    private JsonObject procesar(JsonObject solicitud) {
        String operacion = texto(solicitud, "servicio");
        JsonObject parametros = objeto(solicitud, "parametros");

        switch (operacion) {
             case "describe":
                return describir(parametros);
            case "registrar":
                return registrar(parametros);
            case "listar":
                return listar(parametros);
            case "ejecutar":
                return ejecutar(parametros);
            default:
                return error(operacion, "Servicio del Broker inexistente");
        }
    }

    private JsonObject registrar(JsonObject parametros) {
        String nombre = texto(parametros, "nombre");
        String ip = texto(parametros, "ip");
        int puerto = entero(parametros, "puerto");
        if (nombre.isBlank() || ip.isBlank() || puerto < 1 || puerto > 65535) {
            return error("registrar", "Parametros invalidos");
        }

        RegistroServicios.ServicioRegistrado registrado =
                registro.registrar(nombre, ip, puerto);
        JsonObject resultado = new JsonObject();
        resultado.addProperty("id", registrado.getId());
        return correcto("registrar", resultado);
    }

    private JsonObject listar(JsonObject parametros) {
        String filtro = parametros.has("buscar") ? texto(parametros, "buscar") : null;
        JsonArray resultado = new JsonArray();
        for (RegistroServicios.ServicioRegistrado servicio : registro.listar(filtro)) {
            JsonObject elemento = new JsonObject();
            elemento.addProperty("nombre", servicio.getNombre());
            resultado.add(elemento);
        }
        return correcto("listar", resultado);
    }
    
      private JsonObject describir(JsonObject parametros) {
        JsonArray servicios = new JsonArray();
        for (RegistroServicios.ServicioRegistrado s : registro.listar(null)) {
            JsonObject o = new JsonObject();
            o.addProperty("nombre", s.getNombre());
            o.addProperty("ip", s.getIp());
            o.addProperty("puerto", s.getPuerto());
            o.add("parametros", catalogoParametros(s.getNombre()));
            servicios.add(o);
        }
        return correcto("describe", servicios);
    }

    private static JsonArray catalogoParametros(String nombreServicio) {
        Map<String, JsonArray> catalogo = new LinkedHashMap<>();
        catalogo.put("registrarPedido", params(
                campo("cliente", "string", true),
                campo("productos", "array<{nombre:string, cantidad:int}>", true)));
        catalogo.put("consultarPedido", params(
                campo("id", "int", true)));
        catalogo.put("listarPedidos", params());
        catalogo.put("describe", params());

        JsonArray definicion = catalogo.get(nombreServicio);
        return definicion != null ? definicion : params();
    }

    private static JsonArray params(JsonObject... campos) {
        JsonArray arr = new JsonArray();
        for (JsonObject c : campos) {
            arr.add(c);
        }
        return arr;
    }

    private static JsonObject campo(String nombre, String tipo, boolean obligatorio) {
        JsonObject c = new JsonObject();
        c.addProperty("nombre", nombre);
        c.addProperty("tipo", tipo);
        c.addProperty("obligatorio", obligatorio);
        return c;
    }

    private JsonObject ejecutar(JsonObject parametros) {
        String nombre = texto(parametros, "servicio");
        JsonObject argumentos = parametros.has("argumentos") && parametros.get("argumentos").isJsonObject()
        ? parametros.getAsJsonObject("argumentos")
        : new JsonObject();
        RegistroServicios.ServicioRegistrado destino = registro.buscar(nombre);
        if (destino == null) {
            return error("ejecutar", "Servicio no registrado");
        }

        JsonObject solicitudRemota = new JsonObject();
        solicitudRemota.addProperty("servicio", nombre);
        solicitudRemota.add("parametros", argumentos);

        try (Socket servidor = new Socket()) {
            servidor.connect(new InetSocketAddress(destino.getIp(), destino.getPuerto()), 3000);
            servidor.setSoTimeout(5000);
            try (BufferedReader entrada = new BufferedReader(new InputStreamReader(
                    servidor.getInputStream(), StandardCharsets.UTF_8));
                    BufferedWriter salida = new BufferedWriter(new OutputStreamWriter(
                            servidor.getOutputStream(), StandardCharsets.UTF_8))) {
                salida.write(solicitudRemota.toString());
                salida.newLine();
                salida.flush();

                String respuesta = entrada.readLine();
                if (respuesta == null) {
                    return error("ejecutar", "El servidor no respondio");
                }
                JsonObject respuestaRemota = JsonParser.parseString(respuesta).getAsJsonObject();
                if (!"OK".equals(respuestaRemota.has("estado")
                        ? respuestaRemota.get("estado").getAsString() : null)) {
                    String mensaje = respuestaRemota.has("mensaje")
                            ? respuestaRemota.get("mensaje").getAsString()
                            : "Error del servicio remoto";
                    return error("ejecutar", mensaje);
                }

                JsonObject resultado = new JsonObject();
                resultado.addProperty("servicio", nombre);
                resultado.add("respuesta", respuestaRemota.has("resultado")
                        ? respuestaRemota.get("resultado") : JsonNull.INSTANCE);
                return correcto("ejecutar", resultado);
            }
        } catch (IOException | JsonParseException | IllegalStateException e) {
            return error("ejecutar", "No es posible contactar al servidor");
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

    private static JsonObject objeto(JsonObject padre, String campo) {
        if (!padre.has(campo) || !padre.get(campo).isJsonObject()) {
            throw new IllegalArgumentException("Falta el objeto: " + campo);
        }
        return padre.getAsJsonObject(campo);
    }

    private static JsonObject correcto(String servicio, JsonElement resultado) {
        JsonObject respuesta = new JsonObject();
        respuesta.addProperty("servicio", servicio);
        respuesta.addProperty("estado", "OK");
        respuesta.add("resultado", resultado);
        return respuesta;
    }

    private static JsonObject error(String servicio, String mensaje) {
        JsonObject respuesta = new JsonObject();
        respuesta.addProperty("servicio", servicio);
        respuesta.addProperty("estado", "ERROR");
        respuesta.addProperty("mensaje", mensaje == null ? "Error interno del servidor" : mensaje);
        return respuesta;
    }
}
