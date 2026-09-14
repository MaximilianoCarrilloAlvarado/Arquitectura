/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ada5_tuberiasyfiltros;

/**
 *
 * @author ojeda
 */
public class CalcularSubtotal implements Filtro {

    @Override
    public Pedido procesarPedido(Pedido pedido) {
        
        double subtotal = 0;

        for (productoPedido productoActual : pedido.getProductos()) {
            subtotal += productoActual.getPrecio()
                    * productoActual.getCantidadSolicitada();
        }

        pedido.setSubtotal(subtotal);

        return pedido;
    }
    
}
