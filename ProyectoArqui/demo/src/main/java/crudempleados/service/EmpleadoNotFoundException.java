package crudempleados.service;

public class EmpleadoNotFoundException extends RuntimeException {

    public EmpleadoNotFoundException(Integer idEmpleado) {
        super("No se encontró el empleado con id " + idEmpleado + ".");
    }
}
