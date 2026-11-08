package Modelo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PedidoModelo {

    private static final double LIMITE_DESCUENTO = 1000.00;
    private static final double PORCENTAJE_DESCUENTO = 0.10;
    private static final double PORCENTAJE_IMPUESTO = 0.16;

    private static final String CABECERA_CSV =
            "id,cliente,estado,subtotal,descuento,impuestos,total,productos";

    private final Map<Integer, Pedido> pedidos = new LinkedHashMap<>();
    private final InventarioModelo inventario;
    private final Path archivoPedidos;
    private int siguienteId = 1;

    public PedidoModelo(InventarioModelo inventario) {
        this.inventario = inventario;
        this.archivoPedidos = resolverArchivoPedidos();
        cargarPedidos();
    }

    public PedidoModelo() {
        this(new InventarioModelo());
    }

    public InventarioModelo getInventario() {
        return inventario;
    }

    public void inicializarPedidos() {
        // En la version distribuida NO sembramos pedidos de prueba:
        // el archivo CSV es la fuente de verdad.
        // Si quieres mantener compatibilidad con la demo MVC, deja este metodo vacio.
    }

    public Pedido registrarPedido(Pedido pedido) {
        validarPedido(pedido);

        List<Producto> productosActualizados = new ArrayList<>();
        for (Producto producto : pedido.getProductos()) {
            ProductoInventario disponible = inventario.buscar(producto.getNombre());
            if (disponible == null) {
                throw new IllegalArgumentException(
                        "El producto no existe en el inventario: " + producto.getNombre());
            }
            productosActualizados.add(new Producto(disponible.getNombre(),
                    disponible.getPrecio(), producto.getCantidad(), disponible.getExistencia()));
        }
        inventario.descontar(productosActualizados);
        pedido.reemplazarProductos(productosActualizados);

        return guardarPedido(pedido);
    }

    private Pedido guardarPedido(Pedido pedido) {
        double subtotal = 0;
        for (Producto producto : pedido.getProductos()) {
            subtotal += producto.calcularImporte();
        }

        double descuento = subtotal >= LIMITE_DESCUENTO
                ? subtotal * PORCENTAJE_DESCUENTO
                : 0;
        double impuestos = (subtotal - descuento) * PORCENTAJE_IMPUESTO;

        pedido.asignarId(siguienteId++);
        pedido.setSubtotal(subtotal);
        pedido.setDescuento(descuento);
        pedido.setImpuestos(impuestos);
        pedido.setTotal(subtotal - descuento + impuestos);
        pedido.setEstado("PROCESADO");

        pedidos.put(pedido.getId(), pedido);
        anexarPedidoCsv(pedido);
        return pedido;
    }

    public Pedido consultarPedido(int id) {
        return pedidos.get(id);
    }

    public List<Pedido> listarPedidos() {
        return new ArrayList<>(pedidos.values());
    }

    private void validarPedido(Pedido pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("El pedido no puede ser nulo");
        }
        if (pedido.getCliente() == null || pedido.getCliente().trim().isEmpty()) {
            throw new IllegalArgumentException("El cliente no puede estar vacio");
        }
        if (pedido.getProductos().isEmpty()) {
            throw new IllegalArgumentException("Debe existir al menos un producto");
        }
        for (Producto producto : pedido.getProductos()) {
            if (producto.getCantidad() <= 0) {
                throw new IllegalArgumentException(
                        "La cantidad de " + producto.getNombre() + " debe ser mayor que cero");
            }
        }
    }

    // ------------------- Persistencia CSV -------------------

    private Path resolverArchivoPedidos() {
        // Mismo patron que InventarioModelo.resolverArchivo()
        Path desdeFuenteActual = Paths.get("src", "DB", "pedidos.csv");
        if (Files.exists(desdeFuenteActual)) {
            return desdeFuenteActual;
        }
        Path actual = Paths.get("").toAbsolutePath();
        for (int i = 0; i < 6 && actual != null; i++) {
            Path candidato = actual.resolve(Paths.get("src", "DB", "pedidos.csv"));
            if (Files.exists(candidato)) {
                return candidato;
            }
            actual = actual.getParent();
        }
        // Fallback: crea DB/pedidos.csv en el CWD
        return Paths.get("DB", "pedidos.csv");
    }

    private void cargarPedidos() {
        pedidos.clear();
        siguienteId = 1;
        if (!Files.exists(archivoPedidos)) {
            return;
        }
        try {
            List<String> lineas = Files.readAllLines(archivoPedidos, StandardCharsets.UTF_8);
            for (String linea : lineas) {
                if (linea.isBlank() || linea.startsWith("id,")) {
                    continue;
                }
                Pedido p = parsearLinea(linea);
                if (p != null) {
                    pedidos.put(p.getId(), p);
                    if (p.getId() >= siguienteId) {
                        siguienteId = p.getId() + 1;
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer pedidos.csv", e);
        }
    }

    private void anexarPedidoCsv(Pedido pedido) {
        try {
            Files.createDirectories(archivoPedidos.getParent());
            boolean nuevo = !Files.exists(archivoPedidos)
                    || Files.size(archivoPedidos) == 0;
            StringBuilder sb = new StringBuilder();
            if (nuevo) {
                sb.append(CABECERA_CSV).append(System.lineSeparator());
            }
            sb.append(serializarLinea(pedido)).append(System.lineSeparator());
            Files.writeString(archivoPedidos, sb.toString(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo escribir pedidos.csv", e);
        }
    }

    private String serializarLinea(Pedido pedido) {
        StringBuilder productos = new StringBuilder();
        for (int i = 0; i < pedido.getProductos().size(); i++) {
            Producto p = pedido.getProductos().get(i);
            if (i > 0) {
                productos.append('|');
            }
            productos.append(p.getNombre()).append(':')
                    .append(String.format(Locale.US, "%.2f", p.getPrecio())).append(':')
                    .append(p.getCantidad()).append(':')
                    .append(p.getExistencia());
        }
        return String.format(Locale.US, "%d,%s,%s,%.2f,%.2f,%.2f,%.2f,%s",
                pedido.getId(),
                pedido.getCliente(),
                pedido.getEstado(),
                pedido.getSubtotal(),
                pedido.getDescuento(),
                pedido.getImpuestos(),
                pedido.getTotal(),
                productos);
    }

    private Pedido parsearLinea(String linea) {
        String[] campos = linea.split(",", -1);
        if (campos.length < 8) {
            return null;
        }
        try {
            int id = Integer.parseInt(campos[0].trim());
            String cliente = campos[1];
            String estado = campos[2];
            double subtotal = Double.parseDouble(campos[3]);
            double descuento = Double.parseDouble(campos[4]);
            double impuestos = Double.parseDouble(campos[5]);
            double total = Double.parseDouble(campos[6]);

            List<Producto> productos = new ArrayList<>();
            String productosCsv = campos[7];
            if (!productosCsv.isBlank()) {
                for (String item : productosCsv.split("\\|")) {
                    String[] partes = item.split(":", -1);
                    if (partes.length != 4) {
                        continue;
                    }
                    productos.add(new Producto(
                            partes[0],
                            Double.parseDouble(partes[1]),
                            Integer.parseInt(partes[2]),
                            Integer.parseInt(partes[3])));
                }
            }

            Pedido pedido = new Pedido(cliente, productos);
            pedido.asignarId(id);
            pedido.setSubtotal(subtotal);
            pedido.setDescuento(descuento);
            pedido.setImpuestos(impuestos);
            pedido.setTotal(total);
            pedido.setEstado(estado);
            return pedido;
        } catch (RuntimeException e) {
            return null; // linea corrupta: se ignora
        }
    }
}