package Modelo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PedidoModelo {

    private static final double LIMITE_DESCUENTO = 1000.00;
    private static final double PORCENTAJE_DESCUENTO = 0.10;
    private static final double PORCENTAJE_IMPUESTO = 0.16;

    private final Map<Integer, Pedido> pedidos = new LinkedHashMap<>();
    private final InventarioModelo inventario;
    private int siguienteId = 1;

    public PedidoModelo(InventarioModelo inventario) {
        this.inventario = inventario;
    }

    public PedidoModelo() {
        this(new InventarioModelo());
    }

    public InventarioModelo getInventario() {
        return inventario;
    }

    public void inicializarPedidos() {
        if (!pedidos.isEmpty()) {
            return;
        }
        List<String> clientes = Arrays.asList("Prueba1", "Prueba2");
        for (String cliente : clientes) {
            Pedido pedido = new Pedido(cliente, Arrays.asList(
                    new Producto("Pedido inicial", 100.00, 1, 1)));
            guardarPedido(pedido);
        }
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
            throw new IllegalArgumentException("El cliente no puede estar vacío");
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
}