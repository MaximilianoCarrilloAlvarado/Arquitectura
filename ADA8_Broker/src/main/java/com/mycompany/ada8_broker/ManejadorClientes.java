/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.ada8_broker;

import java.net.Socket;

/**
 *
 * @author ojeda
 */
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ManejadorClientes implements Runnable {

    private final Socket cliente;
    private final RegistroServicios registro;

    public ManejadorClientes(
            Socket cliente, RegistroServicios registro) {
        this.cliente = cliente;
        this.registro = registro;
    }

    @Override
    public void run() {
        try (Socket conexion = cliente;
             BufferedReader entrada = new BufferedReader(
                 new InputStreamReader(
                     conexion.getInputStream(),
                     StandardCharsets.UTF_8));
             BufferedWriter salida = new BufferedWriter(
                 new OutputStreamWriter(
                     conexion.getOutputStream(),
                     StandardCharsets.UTF_8))) {

            String linea = entrada.readLine();

            if (linea == null) {
                return;
            }

            String respuesta;

            try {
                JsonElement json = JsonParser.parseString(linea);

                if (!json.isJsonObject()) {
                    throw new IllegalArgumentException(
                        "La solicitud debe ser un objeto JSON");
                }

                respuesta = procesar(json.getAsJsonObject()).toString();

            } catch (Exception e) {
                respuesta = error("desconocido",
                    "Solicitud inválida: " + e.getMessage()).toString();
            }

            salida.write(respuesta);
            salida.newLine();
            salida.flush();

        } catch (Exception e) {
            System.err.println(
                "Error atendiendo conexión: " + e.getMessage());
        }
    }

    private JsonObject procesar(JsonObject solicitud) {
        String operacion = texto(solicitud, "servicio");
        JsonObject parametros = objeto(solicitud, "parametros");

        switch (operacion) {
            case "registrar":
                return registrar(parametros);

            case "listar":
                return listar(parametros);

            case "ejecutar":
                return ejecutar(parametros);

            default:
                return error(operacion,
                    "Servicio del Broker inexistente");
        }
    }

    private JsonObject registrar(JsonObject parametros) {
        String nombre = texto(parametros, "nombre");
        String ip = texto(parametros, "ip");
        int puerto = entero(parametros, "puerto");

        if (nombre.isBlank() || ip.isBlank()
                || puerto < 1 || puerto > 65535) {
            return error("registrar", "Parámetros inválidos");
        }

        RegistroServicios.ServicioRegistrado registrado =
                registro.registrar(nombre, ip, puerto);

        JsonObject resultado = new JsonObject();
        resultado.addProperty("id", registrado.getId());

        return correcto("registrar", resultado);
    }

    private JsonObject listar(JsonObject parametros) {
        String buscar = null;

        if (parametros.has("buscar")) {
            buscar = texto(parametros, "buscar");
        }

        JsonArray resultado = new JsonArray();

        for (RegistroServicios.ServicioRegistrado servicio
                : registro.listar(buscar)) {
            JsonObject elemento = new JsonObject();
            elemento.addProperty("nombre", servicio.getNombre());
            resultado.add(elemento);
        }

        return correcto("listar", resultado);
    }

    private JsonObject ejecutar(JsonObject parametros) {
        String nombre = texto(parametros, "servicio");

        JsonObject argumentos = objeto(parametros, "argumentos");

        RegistroServicios.ServicioRegistrado destino =
                registro.buscar(nombre);

        if (destino == null) {
            return error("ejecutar", "Servicio no registrado");
        }

        JsonObject solicitudRemota = new JsonObject();
        solicitudRemota.addProperty("servicio", nombre);
        solicitudRemota.add("parametros", argumentos);

        try (Socket servidor = new Socket()) {
            servidor.connect(new InetSocketAddress(
                    destino.getIp(), destino.getPuerto()), 3000);
            servidor.setSoTimeout(5000);

            try (BufferedReader entrada = new BufferedReader(
                    new InputStreamReader(
                        servidor.getInputStream(),
                        StandardCharsets.UTF_8));
                 BufferedWriter salida = new BufferedWriter(
                    new OutputStreamWriter(
                        servidor.getOutputStream(),
                        StandardCharsets.UTF_8))) {

                salida.write(solicitudRemota.toString());
                salida.newLine();
                salida.flush();

                String lineaRespuesta = entrada.readLine();

                if (lineaRespuesta == null) {
                    return error("ejecutar",
                        "El servidor no respondió");
                }

                JsonObject respuestaRemota =
                        JsonParser.parseString(lineaRespuesta)
                                  .getAsJsonObject();

                if (!respuestaRemota.has("estado")
                        || !"OK".equals(
                            respuestaRemota.get("estado").getAsString())) {
                    String mensaje = respuestaRemota.has("mensaje")
                            ? respuestaRemota.get("mensaje").getAsString()
                            : "Error del servicio remoto";

                    return error("ejecutar", mensaje);
                }

                JsonObject resultado = new JsonObject();
                resultado.addProperty("servicio", nombre);

                JsonElement valor = respuestaRemota.has("resultado")
                        ? respuestaRemota.get("resultado")
                        : com.google.gson.JsonNull.INSTANCE;

                resultado.add("respuesta", valor);

                return correcto("ejecutar", resultado);
            }

        } catch (Exception e) {
            return error("ejecutar",
                "No es posible contactar al servidor");
        }
    }

    private static String texto(JsonObject objeto, String campo) {
        if (!objeto.has(campo)
                || !objeto.get(campo).isJsonPrimitive()
                || !objeto.getAsJsonPrimitive(campo).isString()) {
            throw new IllegalArgumentException(
                "Falta el campo de texto: " + campo);
        }

        return objeto.get(campo).getAsString();
    }

    private static int entero(JsonObject objeto, String campo) {
        if (!objeto.has(campo)
                || !objeto.get(campo).isJsonPrimitive()
                || !objeto.getAsJsonPrimitive(campo).isNumber()) {
            throw new IllegalArgumentException(
                "Falta el campo numérico: " + campo);
        }

        try {
            return objeto.get(campo).getAsInt();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "Número inválido: " + campo);
        }
    }

    private static JsonObject objeto(
            JsonObject padre, String campo) {
        if (!padre.has(campo) || !padre.get(campo).isJsonObject()) {
            throw new IllegalArgumentException(
                "Falta el objeto: " + campo);
        }

        return padre.getAsJsonObject(campo);
    }

    private static JsonObject correcto(
            String servicio, JsonElement resultado) {
        JsonObject respuesta = new JsonObject();
        respuesta.addProperty("servicio", servicio);
        respuesta.addProperty("estado", "OK");
        respuesta.add("resultado", resultado);
        return respuesta;
    }

    private static JsonObject error(
            String servicio, String mensaje) {
        JsonObject respuesta = new JsonObject();
        respuesta.addProperty("servicio", servicio);
        respuesta.addProperty("estado", "ERROR");
        respuesta.addProperty("mensaje", mensaje);
        return respuesta;
    }
}
