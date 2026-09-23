/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ada6_capas.persistencia;

import ada6_capas.modelo.Pedido;
import ada6_capas.modelo.Producto;
import java.util.List;

/**
 *
 * @author ojeda
 */
public interface PedidoRepository {

    void actualizarExistencias(Pedido pedido);
    
    Producto buscarPorNombre(String nombre);
    
    int guardar(Pedido pedido);

    Pedido buscarPorId(int id);

    List<Pedido> listarPedidos();
    
    List<Producto> listarProductos();
}
