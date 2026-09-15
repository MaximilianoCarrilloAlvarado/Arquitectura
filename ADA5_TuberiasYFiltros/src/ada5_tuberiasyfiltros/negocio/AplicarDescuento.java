/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ada5_tuberiasyfiltros.negocio;

import ada5_tuberiasyfiltros.dominio.Pedido;

/** Filtro que guarda el importe del descuento sin modificar el subtotal. */
public class AplicarDescuento implements Filtro {
    private final double montoMinimo = 1500;
    private final double porcentaje = 10;


    @Override
    public Pedido procesarPedido(Pedido pedido) {
        
        double subtotal = pedido.getSubtotal();
        double descuento = 0;

        if (subtotal >= montoMinimo) {
            descuento = subtotal * (porcentaje / 100.0);
        }

        pedido.setDescuento(descuento);

        return pedido;
    }
}
