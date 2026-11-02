/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.ada8_broker;

/**
 *
 * @author ojeda
 */
import Modelo.InventarioModelo;
import Modelo.PedidoModelo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Servidor {

    public static void main(String[] args) throws Exception {
        if (args.length < 2 || args.length > 3) {
            System.out.println(
                "Uso: java Servidor <puerto> <ipBroker:puertoBroker> [ipPublicada]");
            return;
        }

        int puertoServidor = Integer.parseInt(args[0]);

        String[] broker = args[1].split(":");
        if (broker.length != 2) {
            System.out.println("Dirección del Broker inválida.");
            return;
        }

        String ipBroker = broker[0];
        int puertoBroker = Integer.parseInt(broker[1]);

        // Para pruebas locales, la IP publicada predeterminada es localhost.
        // En otra computadora, indica la IP LAN de este servidor.
        String ipPublicada = args.length == 3
                ? args[2] : "127.0.0.1";

        // Reutilizamos el modelo existente de ADA7.
        InventarioModelo inventario = new InventarioModelo();
        PedidoModelo modelo = new PedidoModelo(inventario);

        // Registrar cada servicio en el Broker antes de atender solicitudes.
        registrarServicio(ipBroker, puertoBroker,
                "registrarPedido", ipPublicada, puertoServidor);

        registrarServicio(ipBroker, puertoBroker,
                "consultarPedido", ipPublicada, puertoServidor);

        registrarServicio(ipBroker, puertoBroker,
                "listarPedidos", ipPublicada, puertoServidor);

        ExecutorService hilos = Executors.newCachedThreadPool();

        try (ServerSocket servidor = new ServerSocket(puertoServidor)) {
            System.out.println(
                "Servidor escuchando en el puerto " + puertoServidor);

            while (true) {
                Socket conexion = servidor.accept();
                hilos.submit(new ManejadorSolicitudServidor(conexion, modelo));
            }
        } finally {
            hilos.shutdown();
        }
    }

    private static void registrarServicio(
            String ipBroker,
            int puertoBroker,
            String nombreServicio,
            String ipPublicada,
            int puertoServidor) throws IOException {

        ManejadorSolicitudServidor.enviarRegistro(
                ipBroker, puertoBroker,
                nombreServicio, ipPublicada, puertoServidor);

        System.out.println("Servicio registrado: " + nombreServicio);
    }
}