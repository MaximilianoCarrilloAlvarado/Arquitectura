package Broker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Broker {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Uso: java Broker.Broker <puerto>");
            return;
        }

        int puerto = Integer.parseInt(args[0]);
        RegistroServicios registro = new RegistroServicios();

        try (ServerSocket servidor = new ServerSocket(puerto)) {
            System.out.println("Broker escuchando en el puerto " + puerto);
            while (true) {
                Socket conexion = servidor.accept();
                Thread hilo = new Thread(new ManejadorClientes(conexion, registro));
                hilo.start();
            }
        }
    }
}
