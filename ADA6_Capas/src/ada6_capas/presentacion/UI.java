/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ada6_capas.presentacion;

import ada6_capas.persistencia.PedidoRepository;
import ada6_capas.persistencia.PedidoRepositoryArchivo;
import ada6_capas.servicio.PedidoService;

/**
 *
 * @author ojeda
 */
public class UI {
     public static void main(String[] args) {

        PedidoRepository pedidoRepository = new PedidoRepositoryArchivo();

        PedidoService servicio =  new PedidoService( pedidoRepository  );

        PedidoPresentacion ui = new PedidoPresentacion(servicio);

        ui.iniciar();
    }
}
