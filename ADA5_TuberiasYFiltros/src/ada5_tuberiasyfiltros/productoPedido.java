/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ada5_tuberiasyfiltros;

/**
 *
 * @author ojeda
 */
public class productoPedido {
     private String articulo;
    private double precio;
    private int cantidadSolicitada;
    private int existenciaDisponibles;

    public productoPedido(){

    }

    public String getArticulo() {
        return articulo;
    }

    public void setArticulo(String articulo) {
        this.articulo = articulo;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getCantidadSolicitada() {
        return cantidadSolicitada;
    }

    public void setCantidadSolicitada(int cantidadSolicitada) {
        this.cantidadSolicitada = cantidadSolicitada;
    }

    public int getExistenciaDisponible() {
        return existenciaDisponibles;
    }

    public void setExistenciaDisponible(int existenciaDisponible) {
////        this.existenciaDisponibles = existenciaDisponible;
    }
}
