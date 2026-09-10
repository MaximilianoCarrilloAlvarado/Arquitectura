/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ada5_tuberiasyfiltros;

/**
 *
 * @author ojeda
 */
public class validarDatosPedido implements Filtro{

    @Override
    public Pedido procesarPedido(Pedido pedido) {
        if (pedido.getCliente() == null || pedido.getCliente().isBlank()) {
            throw new Exception("El cliente esta vacio, ingrese uno");
        }

        if (pedido.getProductos() == null || pedido.getProductos().isEmpty()) {
            throw new Exception(
                    "La lista de productos esta vacia, pida algo para procesar");
        }

        for (productoPedido producto : pedido.getProductos()) {

            if (producto.getArticulo() == null 
                    || producto.getArticulo().isBlank()
                    || producto.getCantidadSolicitada() <= 0
                    || producto.getPrecio() < 0) {

                throw new Exception(
                    "El producto esta falto de un dato, verifique toda la lista de productos"
                );
            }
        }

        if (pedido.getSubtotal() < 0
                || pedido.getDescuento() < 0
                || pedido.getTotal() < 0
                || pedido.getImpuestos() < 0) {

            throw new Exception("Valor negativo o invalido detectado");
        }
        return pedido;
    }
    
}

