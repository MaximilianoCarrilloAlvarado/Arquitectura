/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ada5_tuberiasyfiltros.negocio;

import ada5_tuberiasyfiltros.dominio.Pedido;
import ada5_tuberiasyfiltros.dominio.Producto;
import ada5_tuberiasyfiltros.dominio.productoPedido;

/**
 *
 * @author ojeda
 */
public class ConfirmarPedido implements Filtro{

    @Override
    public Pedido procesarPedido(Pedido pedido) {
         if (pedido == null) {
            throw new Exception(
                    "No se puede confirmar un pedido nulo");
        }

        if (Double.isNaN(pedido.getSubtotal())
                || Double.isInfinite(pedido.getSubtotal())
                || pedido.getSubtotal() < 0) {

            throw new Exception(
                    "El subtotal no es valido");
        }

        if (Double.isNaN(pedido.getDescuento())
                || Double.isInfinite(pedido.getDescuento())
                || pedido.getDescuento() < 0
                || pedido.getDescuento() > pedido.getSubtotal()) {

            throw new Exception(
                    "El descuento no es valido");
        }

        if (Double.isNaN(pedido.getImpuestos())
                || Double.isInfinite(pedido.getImpuestos())
                || pedido.getImpuestos() < 0) {

            throw new Exception(
                    "Los impuestos no son validos");
        }

        double total = pedido.getSubtotal()
                - pedido.getDescuento()
                + pedido.getImpuestos();

        for (productoPedido productoPedido : pedido.getProductos()) {
            Producto productoInventario = productoPedido.getProductoInventario();

            if (productoInventario != null) {
                productoInventario.setExistencias(
                        productoInventario.getExistencias()
                        - productoPedido.getCantidadSolicitada()
                );
            }
        }

        pedido.setTotal(total);
        pedido.setEstado("Confirmado");

        return pedido;
    }
    
}
