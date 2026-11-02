package Broker;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RegistroServicios {

    public static class ServicioRegistrado {
        private final int id;
        private final String nombre;
        private final String ip;
        private final int puerto;

        public ServicioRegistrado(int id, String nombre, String ip, int puerto) {
            this.id = id;
            this.nombre = nombre;
            this.ip = ip;
            this.puerto = puerto;
        }

        public int getId() {
            return id;
        }

        public String getNombre() {
            return nombre;
        }

        public String getIp() {
            return ip;
        }

        public int getPuerto() {
            return puerto;
        }
    }

    private final Map<String, ServicioRegistrado> servicios = new LinkedHashMap<>();
    private int siguienteId = 1;

    public synchronized ServicioRegistrado registrar(String nombre, String ip, int puerto) {
        ServicioRegistrado servicio = new ServicioRegistrado(
                siguienteId++, nombre, ip, puerto);
        servicios.put(nombre, servicio);
        return servicio;
    }

    public synchronized ServicioRegistrado buscar(String nombre) {
        return servicios.get(nombre);
    }

    public synchronized List<ServicioRegistrado> listar(String filtro) {
        List<ServicioRegistrado> resultado = new ArrayList<>();
        String filtroNormalizado = filtro == null ? null : filtro.toLowerCase();

        for (ServicioRegistrado servicio : servicios.values()) {
            if (filtroNormalizado == null
                    || servicio.getNombre().toLowerCase().contains(filtroNormalizado)) {
                resultado.add(servicio);
            }
        }
        return resultado;
    }
}
