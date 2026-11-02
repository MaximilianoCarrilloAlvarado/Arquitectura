/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PedidoArchivoRepository {

    private final Path archivo;
    private final Gson gson = new Gson();

    public PedidoArchivoRepository() {
        Path rutaMVC = Paths.get("ADA7_MVC", "DB", "pedidos.txt");

        if (Files.isDirectory(Paths.get("ADA7_MVC"))) {
            archivo = rutaMVC;
        } else {
            archivo = Paths.get("DB", "pedidos.txt");
        }
    }

    public List<Pedido> cargarTodos() {
        List<Pedido> pedidos = new ArrayList<>();

        try {
            if (!Files.exists(archivo)) {
                return pedidos;
            }

            List<String> lineas = Files.readAllLines(
                    archivo, StandardCharsets.UTF_8);

            for (String linea : lineas) {
                if (linea.isBlank()) {
                    continue;
                }

                JsonObject json = JsonParser.parseString(linea)
                        .getAsJsonObject();

                String cliente = json.get("cliente").getAsString();
                JsonArray productosJson =
                        json.getAsJsonArray("productos");

                List<Producto> productos = new ArrayList<>();

                for (JsonElement elemento : productosJson) {
                    JsonObject p = elemento.getAsJsonObject();

                    productos.add(new Producto(
                            p.get("nombre").getAsString(),
                            p.get("precio").getAsDouble(),
                            p.get("cantidad").getAsInt(),
                            p.get("existencia").getAsInt()
                    ));
                }

                Pedido pedido = new Pedido(cliente, productos);

                pedido.asignarId(json.get("id").getAsInt());
                pedido.setSubtotal(json.get("subtotal").getAsDouble());
                pedido.setDescuento(json.get("descuento").getAsDouble());
                pedido.setImpuestos(json.get("impuestos").getAsDouble());
                pedido.setTotal(json.get("total").getAsDouble());
                pedido.setEstado(json.get("estado").getAsString());

                pedidos.add(pedido);
            }

            return pedidos;

        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException(
                    "No se pudieron cargar los pedidos desde "
                    + archivo.toAbsolutePath(), e);
        }
    }

    public void guardarTodos(List<Pedido> pedidos) {
        List<String> lineas = new ArrayList<>();

        for (Pedido pedido : pedidos) {
            JsonObject json = new JsonObject();

            json.addProperty("id", pedido.getId());
            json.addProperty("cliente", pedido.getCliente());

            JsonArray productosJson = new JsonArray();

            for (Producto producto : pedido.getProductos()) {
                JsonObject p = new JsonObject();

                p.addProperty("nombre", producto.getNombre());
                p.addProperty("precio", producto.getPrecio());
                p.addProperty("cantidad", producto.getCantidad());
                p.addProperty("existencia", producto.getExistencia());

                productosJson.add(p);
            }

            json.add("productos", productosJson);
            json.addProperty("subtotal", pedido.getSubtotal());
            json.addProperty("descuento", pedido.getDescuento());
            json.addProperty("impuestos", pedido.getImpuestos());
            json.addProperty("total", pedido.getTotal());
            json.addProperty("estado", pedido.getEstado());

            lineas.add(gson.toJson(json));
        }

        try {
            Files.createDirectories(archivo.getParent());

            Files.write(
                    archivo,
                    lineas,
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudieron guardar los pedidos en "
                    + archivo.toAbsolutePath(), e);
        }
    }
}