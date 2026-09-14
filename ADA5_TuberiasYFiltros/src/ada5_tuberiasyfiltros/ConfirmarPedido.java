package ada5_tuberiasyfiltros;

public class ConfirmarPedido implements Filtro {

    @Override
    public Pedido procesarPedido(Pedido pedido) {
    validarImportes(pedido);

        double total = pedido.getSubtotal()
                - pedido.getDescuento()
                + pedido.getImpuestos();

        pedido.setTotal(total);
        pedido.setEstado("Confirmado");
        return pedido;
    }

    private void validarImportes(Pedido pedido) {
        if (pedido == null) {
            throw new Exception("No se puede confirmar un pedido nulo");
        }

        if (Double.isNaN(pedido.getSubtotal()) || Double.isInfinite(pedido.getSubtotal()) || pedido.getSubtotal() < 0) {
            throw new Exception("El subtotal no es valido");
        }

        if (Double.isNaN(pedido.getDescuento()) || Double.isInfinite(pedido.getDescuento()) || pedido.getDescuento() < 0 || pedido.getDescuento() > pedido.getSubtotal()) {
            throw new Exception("El descuento no es valido");
        }

        if (Double.isNaN(pedido.getImpuestos()) || Double.isInfinite(pedido.getImpuestos()) || pedido.getImpuestos() < 0) {
            throw new Exception("Los impuestos no son validos");
        }
    }
}
