package Modelo;

public class Producto {

    private final String nombre;
    private final double precio;
    private final int cantidad;
    private final int existencia;

    public Producto(String nombre, double precio, int cantidad, int existencia) {
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
        this.existencia = existencia;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public int getCantidad() {
        return cantidad;
    }

    public int getExistencia() {
        return existencia;
    }

    public double calcularImporte() {
        return precio * cantidad;
    }
}