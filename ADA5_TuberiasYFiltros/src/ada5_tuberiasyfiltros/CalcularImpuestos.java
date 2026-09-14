package ada5_tuberiasyfiltros;

public class CalcularImpuestos implements Filtro {
    private static final double TASA_IMPUESTO = 0.16; // No sé si volverlo una variable o dejarlo así

    @Override
    public Pedido procesarPedido(Pedido pedido) {
        double importeGravable = pedido.getSubtotal() - pedido.getDescuento();
        pedido.setImpuestos(importeGravable * TASA_IMPUESTO);
        return pedido;
    }
}
