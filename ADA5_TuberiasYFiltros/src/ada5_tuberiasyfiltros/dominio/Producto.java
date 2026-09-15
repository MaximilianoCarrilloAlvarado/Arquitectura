/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ada5_tuberiasyfiltros.dominio;

/**
 *
 * @author ojeda
 */
public class Producto{
    private String nombreProducto;
    private int existencias;
    private double precio;
    
    public Producto(String nombreProducto, int existencias, double precio){
        this.nombreProducto = nombreProducto;
        this.existencias = existencias;
        this.precio = precio;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public int getExistencias() {
        return existencias;
    }

    public double getPrecio() {
        return precio;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public void setExistencias(int existencias) {
        this.existencias = existencias;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }
    
    
}
