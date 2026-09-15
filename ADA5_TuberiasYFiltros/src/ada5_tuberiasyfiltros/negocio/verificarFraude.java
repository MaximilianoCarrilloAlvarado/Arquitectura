/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ada5_tuberiasyfiltros.negocio;

import ada5_tuberiasyfiltros.dominio.Pedido;

/**
 *
 * @author ojeda
 */
public class verificarFraude implements Filtro{

    @Override
    public Pedido procesarPedido(Pedido pedido) {
        if(pedido.getSubtotal() > 5000){
            pedido.setEstado("En revision");
            throw new Exception("El pedido supera un subtotal de 5000, sera marcado para revision");           
        }
        return pedido;
    }
}