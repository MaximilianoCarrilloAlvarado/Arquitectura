package Modelo;

public class ProductoInventario {

    private final String nombre;
    private final double precio;
    private int existencia;

    public ProductoInventario(String nombre, double precio, int existencia) {
        this.nombre = nombre;
        this.precio = precio;
        this.existencia = existencia;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public int getExistencia() {
        return existencia;
    }

    public void reducirExistencia(int cantidad) {
        existencia -= cantidad;
    }
}