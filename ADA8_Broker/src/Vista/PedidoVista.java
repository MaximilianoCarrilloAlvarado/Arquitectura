package Vista;

import Controlador.PedidoControlador;
import Modelo.InventarioModelo;
import Modelo.Pedido;
import Modelo.PedidoModelo;
import Modelo.Producto;
import Modelo.ProductoInventario;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class PedidoVista {

    private final Scanner scanner;

    public PedidoVista() {
        this(new Scanner(System.in));
    }

    public PedidoVista(Scanner scanner) {
        this.scanner = scanner;
    }

    public static void main(String[] args) {
        InventarioModelo inventario = new InventarioModelo();
        inventario.reiniciar();
        PedidoModelo modelo = new PedidoModelo(inventario);
        modelo.inicializarPedidos();
        PedidoVista vista = new PedidoVista();
        PedidoControlador controlador = new PedidoControlador(modelo, vista);

        if (args.length > 0 && args[0].equalsIgnoreCase("--demo")) {
            ejecutarDemostracion(modelo, vista, controlador);
        } else {
            controlador.ejecutarMenu();
        }
    }

    private static void ejecutarDemostracion(
            PedidoModelo modelo, PedidoVista vista, PedidoControlador controlador) {
        System.out.println("=== EVIDENCIA 1: pedido con descuento ===");
        registrarDirectamente(modelo, vista, new Pedido("Ana", java.util.Arrays.asList(
                new Producto("Laptop", 1200, 1, 5))));

        System.out.println("\n=== EVIDENCIA 2: pedido sin descuento ===");
        registrarDirectamente(modelo, vista, new Pedido("Luis", java.util.Arrays.asList(
                new Producto("Cuaderno", 80, 2, 10))));

        System.out.println("\n=== EVIDENCIA 3: pedido inválido ===");
        registrarDirectamente(modelo, vista, new Pedido("", java.util.Arrays.asList(
                new Producto("Pluma", 15, 3, 2))));

        System.out.println("\n=== EVIDENCIA 4: consultar pedido existente ===");
        controlador.consultarPedido(5);

        System.out.println("\n=== EVIDENCIA 5: consultar pedido inexistente ===");
        controlador.consultarPedido(99);

        System.out.println("\n=== EVIDENCIA 6: el mismo Modelo con vista resumida ===");
        PedidoControlador controladorResumido = new PedidoControlador(
                modelo, new PedidoVistaResumida());
        controladorResumido.consultarPedido(5);
    }

    private static void registrarDirectamente(
            PedidoModelo modelo, PedidoVista vista, Pedido pedido) {
        try {
            vista.mostrarResultado(modelo.registrarPedido(pedido));
        } catch (IllegalArgumentException e) {
            vista.mostrarError(e.getMessage());
        }
    }

    public void mostrarMenu() {
        System.out.println("\n=== GESTIÓN DE PEDIDOS MVC ===");
        System.out.println("1. Registrar pedido");
        System.out.println("2. Consultar pedido");
        System.out.println("3. Listar pedidos");
        System.out.println("4. Ver inventario disponible");
        System.out.println("0. Salir");
        System.out.print("Seleccione una opción: ");
    }

    public int capturarOpcion() {
        return leerEntero();
    }

    public int capturarId() {
        System.out.print("Identificador del pedido: ");
        return leerEntero();
    }

    public Pedido capturarPedido() {
        System.out.print("Cliente: ");
        String cliente = scanner.nextLine();
        System.out.print("¿Cuántos productos desea agregar? ");
        int cantidadProductos = leerEntero();
        List<Producto> productos = new ArrayList<>();

        for (int i = 1; i <= cantidadProductos; i++) {
            System.out.println("Producto " + i + ":");
            System.out.print("  Nombre: ");
            String nombre = scanner.nextLine();
            System.out.print("  Cantidad: ");
            int cantidad = leerEntero();
            productos.add(new Producto(nombre, 0, cantidad, 0));
        }
        return new Pedido(cliente, productos);
    }

    public void mostrarInventario(List<ProductoInventario> inventario) {
        System.out.println("\n=== INVENTARIO DISPONIBLE ===");
        for (ProductoInventario producto : inventario) {
            System.out.printf(Locale.US, "- %s | Precio: $%.2f | Existencia: %d%n",
                    producto.getNombre(), producto.getPrecio(), producto.getExistencia());
        }
    }

    public void mostrarResultado(Pedido pedido) {
        System.out.println("\nPedido registrado correctamente.");
        mostrarPedido(pedido);
    }

    public void mostrarPedido(Pedido pedido) {
        System.out.printf(Locale.US, "Pedido: %d%n", pedido.getId());
        System.out.println("Cliente: " + pedido.getCliente());
        for (Producto producto : pedido.getProductos()) {
            System.out.printf(Locale.US, "- %s x%d: $%.2f%n",
                    producto.getNombre(), producto.getCantidad(), producto.calcularImporte());
        }
        System.out.printf(Locale.US, "Subtotal: $%.2f%n", pedido.getSubtotal());
        System.out.printf(Locale.US, "Descuento: $%.2f%n", pedido.getDescuento());
        System.out.printf(Locale.US, "Impuestos: $%.2f%n", pedido.getImpuestos());
        System.out.printf(Locale.US, "Total: $%.2f%n", pedido.getTotal());
        System.out.println("Estado: " + pedido.getEstado());
    }

    public void mostrarLista(List<Pedido> pedidos) {
        if (pedidos.isEmpty()) {
            System.out.println("No hay pedidos registrados.");
            return;
        }
        System.out.println("\n=== PEDIDOS REGISTRADOS ===");
        for (Pedido pedido : pedidos) {
            mostrarPedido(pedido);
            System.out.println();
        }
    }

    public void mostrarError(String mensaje) {
        System.out.println("[ERROR] " + mensaje);
    }

    protected int leerEntero() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Debe escribir un número entero válido");
        }
    }

    protected double leerDouble() {
        try {
            return Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Debe escribir un precio válido");
        }
    }
}