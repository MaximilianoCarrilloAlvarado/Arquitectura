package Controlador;

import Modelo.InventarioModelo;
import Modelo.Pedido;
import Modelo.PedidoModelo;
import Vista.PedidoVista;

public class PedidoControlador {

    private final PedidoModelo modelo;
    private final PedidoVista vista;

    public PedidoControlador(PedidoModelo modelo, PedidoVista vista) {
        this.modelo = modelo;
        this.vista = vista;
    }

    public void registrarPedido() {
        try {
            Pedido resultado = modelo.registrarPedido(vista.capturarPedido());
            vista.mostrarResultado(resultado);
        } catch (IllegalArgumentException e) {
            vista.mostrarError(e.getMessage());
        }
    }

    public void consultarPedido(int id) {
        Pedido pedido = modelo.consultarPedido(id);
        if (pedido == null) {
            vista.mostrarError("Pedido no encontrado");
        } else {
            vista.mostrarPedido(pedido);
        }
    }

    public void listarPedidos() {
        vista.mostrarLista(modelo.listarPedidos());
    }

    public void mostrarInventario(InventarioModelo inventario) {
        vista.mostrarInventario(inventario.listarDisponibles());
    }

    public void ejecutarMenu() {
        boolean continuar = true;
        while (continuar) {
            try {
                vista.mostrarMenu();
                switch (vista.capturarOpcion()) {
                    case 1:
                        registrarPedido();
                        break;
                    case 2:
                        consultarPedido(vista.capturarId());
                        break;
                    case 3:
                        listarPedidos();
                        break;
                    case 4:
                        mostrarInventario(modelo.getInventario());
                        break;
                    case 0:
                        continuar = false;
                        break;
                    default:
                        vista.mostrarError("Opción no válida");
                }
            } catch (IllegalArgumentException e) {
                vista.mostrarError(e.getMessage());
            }
        }
    }
}