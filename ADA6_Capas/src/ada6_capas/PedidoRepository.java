/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ada6_capas;

import java.util.List;

/**
 *
 * @author ojeda
 */
public interface PedidoRepository {

    Producto buscarPorNombre(String nombre);
    
    int guardar(Pedido pedido);

    Pedido buscarPorId(int id);

    List<Pedido> listarPedidos();
    
    List<Producto> listarProductos();
}
