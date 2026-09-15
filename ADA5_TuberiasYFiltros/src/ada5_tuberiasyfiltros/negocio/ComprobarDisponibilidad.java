/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ada5_tuberiasyfiltros.negocio;

import ada5_tuberiasyfiltros.dominio.Pedido;
import ada5_tuberiasyfiltros.dominio.productoPedido;

/**
 *
 * @author ojeda
 */
public class ComprobarDisponibilidad implements Filtro {

    @Override
    public Pedido procesarPedido(Pedido pedido) {
         if (pedido == null || pedido.getProductos() == null
                || pedido.getProductos().isEmpty()) {

            throw new Exception(
                    "El pedido esta vacio."
            );
        }

        for (productoPedido productoActual : pedido.getProductos()) {

            if (productoActual.getCantidadSolicitada()
                    > productoActual.getExistenciaDisponible()) {

                throw new Exception(
                        "No hay suficiente existencia disponible para: "
                        + productoActual.getArticulo()
                );
            }
        }

        return pedido;
    }
}