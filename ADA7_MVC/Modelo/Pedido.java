package Modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Pedido {

    private int id;
    private final String cliente;
    private final List<Producto> productos;
    private double subtotal;
    private double descuento;
    private double impuestos;
    private double total;
    private String estado;

    public Pedido(String cliente, List<Producto> productos) {
        this.cliente = cliente;
        this.productos = new ArrayList<>(productos);
    }

    public int getId() {
        return id;
    }

    public void asignarId(int id) {
        this.id = id;
    }

    public String getCliente() {
        return cliente;
    }

    public List<Producto> getProductos() {
        return Collections.unmodifiableList(productos);
    }

    public void reemplazarProductos(List<Producto> productos) {
        this.productos.clear();
        this.productos.addAll(productos);
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDescuento() {
        return descuento;
    }

    public void setDescuento(double descuento) {
        this.descuento = descuento;
    }

    public double getImpuestos() {
        return impuestos;
    }

    public void setImpuestos(double impuestos) {
        this.impuestos = impuestos;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}