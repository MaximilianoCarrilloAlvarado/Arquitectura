package Broker;

import Modelo.InventarioModelo;
import Modelo.PedidoModelo;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Servidor {

    public static void main(String[] args) throws IOException {
        if (args.length < 2 || args.length > 3) {
            System.out.println("Uso: java Broker.Servidor <puerto> <ipBroker:puertoBroker> [ipPublicada]");
            return;
        }

        int puertoServidor = Integer.parseInt(args[0]);
        String[] datosBroker = args[1].split(":", -1);
        if (datosBroker.length != 2) {
            System.out.println("Direccion del Broker invalida.");
            return;
        }

        String ipBroker = datosBroker[0];
        int puertoBroker = Integer.parseInt(datosBroker[1]);
        String ipPublicada = args.length == 3
                ? args[2]
                : java.net.InetAddress.getLocalHost().getHostAddress();

        System.out.println("=== SERVIDOR DE PEDIDOS ===");
        System.out.println("Puerto de escucha: " + puertoServidor);
        System.out.println("Broker: " + ipBroker + ":" + puertoBroker);
        System.out.println("IP publicada: " + ipPublicada);
        System.out.println("Registrando servicios en el Broker...");

        PedidoModelo modelo = new PedidoModelo(new InventarioModelo());
        registrarServicios(ipBroker, puertoBroker, ipPublicada, puertoServidor);

        System.out.println("Servicios registrados correctamente.");

        ExecutorService hilos = Executors.newCachedThreadPool();
        try (ServerSocket servidor = new ServerSocket(puertoServidor)) {
            System.out.println("Servidor escuchando en el puerto " + puertoServidor);
            System.out.println("Esperando solicitudes del Broker...");
            while (true) {
                Socket conexion = servidor.accept();
                System.out.println("Conexion entrante desde "
                        + conexion.getInetAddress().getHostAddress()
                        + ":" + conexion.getPort());
                hilos.submit(new ManejadorSolicitudServidor(conexion, modelo));
            }
        } finally {
            hilos.shutdown();
        }
    }

    private static void registrarServicios(String ipBroker, int puertoBroker,
            String ipPublicada, int puertoServidor) throws IOException {
        String[] servicios = {"registrarPedido", "consultarPedido", "listarPedidos"};
        for (String servicio : servicios) {
            ManejadorSolicitudServidor.enviarRegistro(
                    ipBroker, puertoBroker, servicio, ipPublicada, puertoServidor);
            System.out.println("  [OK] Servicio registrado: " + servicio);
        }
    }
}