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
    private Producto productoInventario;

    public productoPedido(){

    }
    
    public productoPedido(String articulo, double precio, int cantidadSolicitada, int existenciaDisponibles){
            this.articulo = articulo;
            this.precio = precio;
            this.cantidadSolicitada = cantidadSolicitada;
            this.existenciaDisponibles = existenciaDisponibles;
    }

        public productoPedido(Producto productoInventario, int cantidadSolicitada) {
                this(productoInventario.getNombreProducto(),
                                productoInventario.getPrecio(),
                                cantidadSolicitada,
                                productoInventario.getExistencias());
                this.productoInventario = productoInventario;
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
        this.existenciaDisponibles = existenciaDisponible;
    }

    public Producto getProductoInventario() {
        return productoInventario;
    }
}
