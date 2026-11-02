package Vista;

import Modelo.Pedido;
import java.util.Locale;

public class PedidoVistaResumida extends PedidoVista {

    @Override
    public void mostrarPedido(Pedido pedido) {
        System.out.println("Pedido: " + pedido.getId());
        System.out.printf(Locale.US, "Total: $%.2f%n", pedido.getTotal());
        System.out.println("Estado: " + pedido.getEstado());
    }
}