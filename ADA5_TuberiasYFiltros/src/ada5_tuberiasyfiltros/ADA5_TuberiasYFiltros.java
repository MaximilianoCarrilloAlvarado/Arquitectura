/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package ada5_tuberiasyfiltros;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author ojeda
 */
public class ADA5_TuberiasYFiltros {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
       LeerInventario lector = new LeerInventario();
       List<Producto> inventario = new ArrayList();
       List<productoPedido> productosPedidos;
       Scanner scanner = new Scanner(System.in);
        
        try {
            inventario = lector.leerArchivo();
        } catch (FileNotFoundException e) {
            System.out.println("No se encontró el archivo inventario.txt");
            return;
        }

        List<Filtro> pipeline = List.of(
            new ValidarDatosPedido(),
            new ComprobarDisponibilidad(),
            new CalcularSubtotal(),
            new verificarFraude(),
            new AplicarDescuento(),
            new CalcularImpuestos(),
            new ConfirmarPedido()
        );
        
        boolean continuar = true;
       while (continuar) {

            Pedido pedido = new Pedido();
            List<productoPedido> productosPedido = new ArrayList<>();

            pedido.setProductos(productosPedido);

            // -------------------------
            // DATOS DEL CLIENTE
            // -------------------------

            System.out.print("\nIngrese el nombre del cliente: ");
            String cliente = scanner.nextLine();

            pedido.setCliente(cliente);

            boolean pedidoTerminado = false;

            // -------------------------
            // MENÚ DEL PEDIDO
            // -------------------------

            while (!pedidoTerminado) {

                System.out.println("\n===== MENU DEL PEDIDO =====");
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

                        for (int i = 0; i < inventario.size(); i++) {

                            Producto producto = inventario.get(i);

                            System.out.println(
                                    (i + 1) + ". "
                                    + producto.getNombreProducto()
                                    + " | Existencia: "
                                    + producto.getExistencias()
                                    + " | Precio: $"
                                    + producto.getPrecio()
                            );
                        }

                        break;

                    case 2:

                        System.out.print("Ingrese el número del producto: ");
                        int numeroProducto = scanner.nextInt();
                        scanner.nextLine();

                        if (numeroProducto < 1
                                || numeroProducto > inventario.size()) {

                            System.out.println("Producto no válido.");
                            break;
                        }

                        Producto productoSeleccionado =
                                inventario.get(numeroProducto - 1);

                        System.out.print("Ingrese la cantidad: ");
                        int cantidad = scanner.nextInt();
                        scanner.nextLine();

                        productoPedido nuevoProducto = new productoPedido(
                            productoSeleccionado,
                            cantidad
                        );

                        productosPedido.add(nuevoProducto);

                        System.out.println("Producto agregado al pedido.");

                        break;

                    case 3:

                        System.out.println("\n===== PEDIDO ACTUAL =====");
                        System.out.println("Cliente: " + pedido.getCliente());

                        for (productoPedido producto : productosPedido) {

                            System.out.println(
                                    producto.getArticulo()
                                    + " x "
                                    + producto.getCantidadSolicitada()
                                    + " | $"
                                    + producto.getPrecio()
                            );
                        }

                        break;

                    case 4:

                        try {

                            for (Filtro filtro : pipeline) {
                                pedido = filtro.procesarPedido(pedido);
                            }

                            System.out.println("\n===== PEDIDO PROCESADO =====");
                            System.out.println("Cliente: " + pedido.getCliente());
                            System.out.println("Subtotal: $" + pedido.getSubtotal());
                            System.out.println("Descuento: $" + pedido.getDescuento());
                            System.out.println("Impuestos: $" + pedido.getImpuestos());
                            System.out.println("Total: $" + pedido.getTotal());
                            System.out.println("Estado: " + pedido.getEstado());

                            pedidoTerminado = true;

                        } catch (RuntimeException e) {

                            System.out.println(
                                    "\nError al procesar pedido: "
                                    + e.getMessage()
                            );

                            pedidoTerminado = true;
                        }

                        break;

                    case 5:

                        System.out.println("\nPedido cancelado.");
                        pedidoTerminado = true;

                        break;

                    default:

                        System.out.println("Opción no válida.");
                }
            }

            // -------------------------
            // ¿OTRO PEDIDO?
            // -------------------------

            System.out.print("\n¿Desea realizar otro pedido? (s/n): ");
            String respuesta = scanner.nextLine();

            if (!respuesta.equalsIgnoreCase("s")) {
                continuar = false;
            }
        }

        System.out.println("\nPrograma finalizado.");
        scanner.close();
    }
        
    
}
