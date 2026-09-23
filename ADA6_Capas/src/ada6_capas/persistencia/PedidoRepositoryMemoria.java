/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ada6_capas.persistencia;

import ada6_capas.modelo.Pedido;
import ada6_capas.modelo.Producto;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ojeda
 */
public class PedidoRepositoryMemoria implements PedidoRepository{
    private Map<Integer,Pedido> listaPedidos = new HashMap<>();
    
     List<Producto> productos = new ArrayList<>(List.of(
    new Producto("Laptop", 8, 18500, 0),
    new Producto("Mouse", 25, 450, 0),
    new Producto("Teclado", 14, 850, 0),
    new Producto("Monitor", 11, 4200, 0),
    new Producto("Audifonos", 18, 1250, 0),
    new Producto("Webcam", 9, 980, 0),
    new Producto("Impresora", 6, 3500, 0),
    new Producto("Tablet", 12, 6200, 0),
    new Producto("DiscoSSD", 20, 1800, 0),
    new Producto("MemoriaRAM", 16, 1450, 0),
    new Producto("Bocinas", 13, 1100, 0),
    new Producto("Microfono", 7, 2300, 0),
    new Producto("Router", 22, 1350, 0),
    new Producto("CamaraWeb", 15, 750, 0),
    new Producto("SillaOficina", 5, 4800, 0)
));
     
    private int siguienteId = 1;

    @Override
    public int guardar(Pedido pedido) {
       int id = siguienteId++;
       
       pedido.setId(id);
       listaPedidos.put(id, pedido);
       
       return id;
    }
    

    @Override
    public Pedido buscarPorId(int id) {
         return listaPedidos.get(id);
  }

    @Override
    public List<Pedido> listarPedidos() {
        return new ArrayList(listaPedidos.values());
    }

    @Override
    public List<Producto> listarProductos() {
        return productos;
    }

    @Override
    public Producto buscarPorNombre(String nombre) {
        for(Producto producto : productos){
               if(producto.getNombreProducto().equalsIgnoreCase(nombre)){
                   return producto;
               }
           }
        return null;    
    }
}
