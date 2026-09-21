package ada6_capas;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PedidoPresentacion {

    private PedidoService servicio;
    private Scanner scanner;

    public PedidoPresentacion(PedidoService servicio) {
        this.servicio = servicio;
        this.scanner = new Scanner(System.in);
    }

    public void iniciar() {

        boolean continuar = true;

        while (continuar) {

            System.out.println("\n===== MENÚ PRINCIPAL =====");
            System.out.println("1. Registrar pedido");
            System.out.println("2. Consultar pedido por ID");
            System.out.println("3. Listar pedidos");
            System.out.println("4. Salir");
            System.out.print("Seleccione una opción: ");

            int opc = scanner.nextInt();
            scanner.nextLine();

            switch (opc) {

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
                    continuar = false;
                    System.out.println("\nSaliendo...");
                    break;

                default:
                    System.out.println("\nOpción no válida.");
                    break;
            }
        }

        System.out.println("\nPrograma finalizado.");
        scanner.close();
    }

    private void registrarPedido() {

        Pedido pedido = new Pedido();

        List<Producto> productosPedido = new ArrayList<>();
        pedido.setProductos(productosPedido);

        System.out.print("\nIngrese el nombre del cliente: ");
        String cliente = scanner.nextLine();

        pedido.setCliente(cliente);

        boolean pedidoTerminado = false;

        while (!pedidoTerminado) {

            System.out.println("\n===== MENÚ DEL PEDIDO =====");
            System.out.println("1. Mostrar productos");
            System.out.println("2. Agregar producto");
            System.out.println("3. Ver pedido");
            System.out.println("4. Confirmar pedido");
            System.out.println("5. Cancelar pedido");
            System.out.print("Seleccione una opción: ");

            int opcion = scanner.nextInt();
            scanner.nextLine();

            switch (opcion) {

                case 1:

                    System.out.println("\n===== INVENTARIO =====");

                    List<Producto> inventario = servicio.listarLosProductos();

                    for (Producto producto : inventario) {
                        System.out.println(producto);
                    }

                    break;

                case 2:

                    System.out.print("\nIngrese el nombre del producto: ");
                    String nombreProducto = scanner.nextLine();

                    System.out.print("Ingrese la cantidad: ");
                    int cantidad = scanner.nextInt();
                    scanner.nextLine();

                    try {

                        servicio.agregarProducto(
                                nombreProducto,
                                pedido,
                                cantidad
                        );

                        System.out.println(
                                "Producto agregado al pedido."
                        );

                    } catch (RuntimeException e) {

                        System.out.println(
                                "Error: " + e.getMessage()
                        );
                    }

                    break;

                case 3:

                    System.out.println("\n===== PEDIDO ACTUAL =====");
                    System.out.println(
                            "Cliente: " + pedido.getCliente()
                    );

                    for (Producto producto : productosPedido) {
                        System.out.println(producto);
                    }

                    break;

                case 4:

                    try {

                        servicio.registrar(pedido);

                        System.out.println(
                                "\nPedido registrado correctamente."
                        );

                        System.out.println(pedido);

                        pedidoTerminado = true;

                    } catch (RuntimeException e) {

                        System.out.println(
                                "\nError al procesar pedido: "
                                + e.getMessage()
                        );
                    }

                    break;

                case 5:

                    System.out.println("\nPedido cancelado.");

                    pedidoTerminado = true;

                    break;

                default:

                    System.out.println(
                            "\nOpción no válida."
                    );

                    break;
            }
        }
    }

    private void consultarPedido() {

        System.out.print("\nIngrese el ID del pedido: ");

        int id = scanner.nextInt();
        scanner.nextLine();

        try {

            Pedido pedido = servicio.buscarPedido(id);

            System.out.println("\n===== PEDIDO ENCONTRADO =====");
            System.out.println(pedido);

        } catch (RuntimeException e) {

            System.out.println(
                    "Error: " + e.getMessage()
            );
        }
    }

    private void listarPedidos() {

        System.out.println("\n===== PEDIDOS REGISTRADOS =====");

        List<Pedido> pedidos = servicio.listarPedidosHechos();

        if (pedidos.isEmpty()) {

            System.out.println("No hay pedidos registrados.");

        } else {

            for (Pedido pedido : pedidos) {
                System.out.println(pedido);
            }
        }
    }
}