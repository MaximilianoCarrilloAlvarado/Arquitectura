/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ada6_capas.persistencia;

import ada6_capas.modelo.Pedido;
import ada6_capas.modelo.Producto;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author ojeda
 */
public class PedidoRepositoryArchivo implements PedidoRepository{
    
    private int siguienteId = 1;
    private Map<Integer,Pedido> listaPedidos = new HashMap<>();
    private List<Producto> inventario = new ArrayList<>();
    
     public PedidoRepositoryArchivo() {
        try {
            inventario = leerArchivo();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(
                "No se encontró Inventario.txt", e
            );
        }
    }
    
    public List<Producto> leerArchivo() throws FileNotFoundException {

    File inventarioArchivo = new File("inventarioB.txt");
    if (!inventarioArchivo.isFile()) {
        inventarioArchivo = new File("ADA6_Capas", "inventarioB.txt");
    }
    Scanner scanner = new Scanner(inventarioArchivo);

    while (scanner.hasNextLine()) {

        String linea = scanner.nextLine().trim();
        String[] datos = linea.split(",");

        String articulo = datos[0].replace("\uFEFF", "").trim();
        int existencia = Integer.parseInt(datos[1].trim());
        double precio = Double.parseDouble(datos[2].trim());
        int cantidad = Integer.parseInt(datos[3].trim());

        Producto producto = new Producto(articulo, existencia, precio,cantidad);

        inventario.add(producto);
    }

    scanner.close();

    return inventario;
}
    
    @Override
    public Producto buscarPorNombre(String nombre) {
        for(Producto producto : inventario){
               if(producto.getNombreProducto().equalsIgnoreCase(nombre)){
                   return producto;
               }
           }
        return null;    
    }

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
        return inventario;
    }

    @Override
    public void actualizarExistencias(Pedido pedido) {
          for (Producto productoPedido : pedido.getProductos()) {

        Producto productoInventario =
                buscarPorNombre(productoPedido.getNombreProducto());

        productoInventario.setExistencias(
                productoInventario.getExistencias()
                - productoPedido.getCantidad()
        );
    }
    }
}
