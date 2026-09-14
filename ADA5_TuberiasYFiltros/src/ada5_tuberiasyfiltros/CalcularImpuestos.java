/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ada5_tuberiasyfiltros;

/**
 *
 * @author ojeda
 */
public class CalcularImpuestos implements Filtro {
    private static final double TASA_IMPUESTO = 0.16;

    @Override
    public Pedido procesarPedido(Pedido pedido) {
        double importeGravable = pedido.getSubtotal() - pedido.getDescuento();
        pedido.setImpuestos(importeGravable * TASA_IMPUESTO);
        pedido.setTotal(pedido.getSubtotal() + pedido.getImpuestos());
        
        return pedido;
    }
}