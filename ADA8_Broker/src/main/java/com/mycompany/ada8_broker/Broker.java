/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.ada8_broker;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
/**
 *
 * @author ojeda
 */
public class Broker {
    
    public static void main(String[] args)throws IOException {
        
        if(args.length != 1){
            System.out.println("Uso: java broker.Broker <puerto>");
            return;
        }
        int puerto = Integer.parseInt(args[0]);
        
        try(ServerSocket servidor = new ServerSocket(puerto)){
            System.out.println("Broker esta escuchando en el puerto: " + puerto);
           
            RegistroServicios registro = new RegistroServicios();            
            while(true){
                Socket conexion = servidor.accept();
                
                 Thread hilo = new Thread(
                    new ManejadorClientes(conexion,registro)
                );
                
                hilo.start();
            }
            
        }
    }
}
