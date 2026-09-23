/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ada6_capas.servicio;

import ada6_capas.modelo.Exception;
import ada6_capas.modelo.Pedido;
import ada6_capas.modelo.Producto;
import ada6_capas.persistencia.PedidoRepository;
import java.util.List;

/**
 *
 * @author ojeda
 */
public class PedidoService {
    private PedidoRepository repositorio;
    
    public PedidoService(PedidoRepository repositorio) {
    this.repositorio = repositorio;
}
    
    public List<Pedido> listarPedidosHechos(){
        return repositorio.listarPedidos();
    }
    
    public Pedido buscarPedido(int id){  
        Pedido pedido = repositorio.buscarPorId(id);

    if (pedido == null) {
        throw new IllegalArgumentException("No existe un pedido con ese ID.");
    }

    return pedido;
    }
    
    public List<Producto> listarLosProductos(){
        return repositorio.listarProductos();
    }
    
    public void agregarProducto(String nombre, Pedido pedido, int cantidad){
        Producto producto = repositorio.buscarPorNombre(nombre);

    if (producto == null) {
        throw new IllegalArgumentException("Producto no encontrado");
    }

    if (cantidad <= 0) {
        throw new IllegalArgumentException("Cantidad inválida");
    }

    if (cantidad > producto.getExistencias()) {
        throw new IllegalArgumentException("No hay suficiente existencia");
    }

    Producto productoAñadido = new Producto(
    producto.getNombreProducto(),
    producto.getExistencias(),
    producto.getPrecio(),
    cantidad
    );
    
    pedido.getProductos().add(productoAñadido);
    }
    
    public Pedido registrar (Pedido pedido){
        
        validarDatosPedido(pedido);
        ComprobarDisponibilidad(pedido);
        calcularSubtotal(pedido);  
        AplicarDescuento(pedido);
        calcularImpuestos(pedido);
         
        for(Producto producto : pedido.getProductos()){
            producto.setExistencias(producto.getExistencias() - producto.getCantidad());
        }
        
        pedido.setEstado("PROCESADO");
        repositorio.guardar(pedido);
        
        return pedido;
    }
    
    public Pedido validarDatosPedido(Pedido pedido){
        if (pedido == null) {
        throw new IllegalArgumentException("El pedido no puede ser nulo");
        }
        
         if (pedido.getCliente() == null || pedido.getCliente().isBlank()) {
            throw new Exception("El cliente esta vacio, ingrese uno");
        }

        if (pedido.getProductos() == null || pedido.getProductos().isEmpty()) {
            throw new Exception(
                    "La lista de productos esta vacia, pida algo para procesar");
        }

        for (Producto producto : pedido.getProductos()) {

            if (producto.getNombreProducto() == null 
                    || producto.getNombreProducto().isBlank()
                    || producto.getCantidad() <= 0
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
    
    public Pedido ComprobarDisponibilidad(Pedido pedido){
        if (pedido == null || pedido.getProductos() == null
                || pedido.getProductos().isEmpty()) {

            throw new Exception(
                    "El pedido esta vacio."
            );
        }

        for (Producto productoActual : pedido.getProductos()) {

            if (productoActual.getCantidad()
                    > productoActual.getExistencias()) {

                throw new Exception(
                        "No hay suficiente existencia disponible para: "
                        + productoActual.getNombreProducto()
                );
            }
        }

        return pedido;
    }
    
    public Pedido calcularImpuestos (Pedido pedido){
        double TASA_IMPUESTO = 0.16;

        double importeGravable = pedido.getSubtotal() - pedido.getDescuento();
        pedido.setImpuestos(importeGravable * TASA_IMPUESTO);
        pedido.setTotal(pedido.getSubtotal() -pedido.getDescuento() + pedido.getImpuestos());
        
        return pedido;
    }
    
    public Pedido AplicarDescuento(Pedido pedido){
     double montoMinimo = 1000;
     double porcentaje = 10;
        
        double subtotal = pedido.getSubtotal();
        double descuento = 0;

        if (subtotal >= montoMinimo) {
            descuento = subtotal * (porcentaje / 100.0);
        }

        pedido.setDescuento(descuento);

        return pedido;
    }
    
    
    public Pedido calcularSubtotal(Pedido pedido){
        double subtotal = 0;

        for (Producto productoActual : pedido.getProductos()) {
            subtotal += productoActual.getPrecio()
                    * productoActual.getCantidad();
        }

        pedido.setSubtotal(subtotal);
        
        return pedido;
    }
    
}
