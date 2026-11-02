package Modelo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InventarioModelo {

    private static final String CONTENIDO_INICIAL = String.join(System.lineSeparator(),
            "# nombre|precio|existencia",
            "Laptop|1200.00|5",
            "Cuaderno|80.00|10",
            "Pluma|15.00|20",
            "Mouse|350.00|8",
            "Teclado|700.00|6") + System.lineSeparator();

    private final Path archivo;

    public InventarioModelo() {
        archivo = resolverArchivo();
    }

    public void reiniciar() {
        try {
            Files.createDirectories(archivo.getParent());
            Files.writeString(archivo, CONTENIDO_INICIAL);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo inicializar el inventario", e);
        }
    }

    public List<ProductoInventario> listarDisponibles() {
        return new ArrayList<>(cargar().values());
    }

    public ProductoInventario buscar(String nombre) {
        if (nombre == null) {
            return null;
        }
        return cargar().get(normalizar(nombre));
    }

    public void descontar(List<Producto> productos) {
        Map<String, ProductoInventario> inventario = cargar();
        Map<String, Integer> cantidades = new LinkedHashMap<>();

        for (Producto producto : productos) {
            String clave = normalizar(producto.getNombre());
            cantidades.merge(clave, producto.getCantidad(), Integer::sum);
        }

        for (Map.Entry<String, Integer> entrada : cantidades.entrySet()) {
            ProductoInventario disponible = inventario.get(entrada.getKey());
            if (disponible == null) {
                throw new IllegalArgumentException(
                        "El producto no existe en el inventario: " + entrada.getKey());
            }
            if (entrada.getValue() > disponible.getExistencia()) {
                throw new IllegalArgumentException("No hay existencias suficientes de "
                        + disponible.getNombre() + ". Disponibles: " + disponible.getExistencia());
            }
            disponible.reducirExistencia(entrada.getValue());
        }
        guardar(inventario.values());
    }

    private Map<String, ProductoInventario> cargar() {
        Map<String, ProductoInventario> inventario = new LinkedHashMap<>();
        try {
            for (String linea : Files.readAllLines(archivo)) {
                if (linea.isBlank() || linea.startsWith("#")) {
                    continue;
                }
                String[] datos = linea.split("\\|");
                if (datos.length != 3) {
                    throw new IllegalStateException("Formato inválido en inventario.txt");
                }
                ProductoInventario producto = new ProductoInventario(
                        datos[0].trim(), Double.parseDouble(datos[1].trim()),
                        Integer.parseInt(datos[2].trim()));
                inventario.put(normalizar(producto.getNombre()), producto);
            }
            return inventario;
        } catch (IOException | NumberFormatException e) {
            throw new IllegalStateException("No se pudo leer el inventario", e);
        }
    }

    private void guardar(Iterable<ProductoInventario> productos) {
        List<String> lineas = new ArrayList<>();
        lineas.add("# nombre|precio|existencia");
        for (ProductoInventario producto : productos) {
            lineas.add(String.format(Locale.US, "%s|%.2f|%d", producto.getNombre(),
                    producto.getPrecio(), producto.getExistencia()));
        }
        try {
            Files.write(archivo, lineas);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo actualizar el inventario", e);
        }
    }

    private Path resolverArchivo() {
        Path desdeFuenteActual = Paths.get("src", "DB", "inventario.txt");
        if (Files.exists(desdeFuenteActual)) {
            return desdeFuenteActual;
        }
        Path desdeRaizProyecto = Paths.get("ADA7_MVC", "DB", "inventario.txt");
        if (Files.exists(desdeRaizProyecto)) {
            return desdeRaizProyecto;
        }
        return Paths.get("DB", "inventario.txt");
    }

    private String normalizar(String nombre) {
        return nombre.trim().toLowerCase(Locale.ROOT);
    }
}