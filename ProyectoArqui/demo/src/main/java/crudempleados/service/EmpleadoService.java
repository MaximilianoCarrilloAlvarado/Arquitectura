package crudempleados.service;

import crudempleados.model.Empleado;
import crudempleados.repository.EmpleadoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;

    public EmpleadoService(EmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    public List<Empleado> listarEmpleados() {
        return empleadoRepository.findAll();
    }

    public Empleado buscarEmpleado(Integer idEmpleado) {
        return empleadoRepository.findById(idEmpleado)
                .orElseThrow(() -> new EmpleadoNotFoundException(idEmpleado));
    }

    public Empleado crearEmpleado(Empleado empleado) {
        validarEmpleado(empleado);
        empleado.setIdEmpleado(null);
        return empleadoRepository.save(empleado);
    }

    public Empleado actualizarEmpleado(Integer idEmpleado, Empleado datosActualizados) {
        validarEmpleado(datosActualizados);

        Empleado empleado = buscarEmpleado(idEmpleado);
        empleado.setNombre(datosActualizados.getNombre());
        empleado.setEdad(datosActualizados.getEdad());
        empleado.setSalario(datosActualizados.getSalario());

        return empleadoRepository.save(empleado);
    }

    public void eliminarEmpleado(Integer idEmpleado) {
        Empleado empleado = buscarEmpleado(idEmpleado);
        empleadoRepository.delete(empleado);
    }

    public void eliminarEmpleados(List<Integer> idsEmpleados) {
    if (idsEmpleados == null || idsEmpleados.isEmpty()) {
        throw new IllegalArgumentException(
            "Debe indicar al menos un empleado."
        );
    }

    empleadoRepository.deleteAllById(idsEmpleados);
}
    private void validarEmpleado(Empleado empleado) {
        if (empleado == null) {
            throw new IllegalArgumentException("Los datos del empleado son obligatorios.");
        }
        if (empleado.getNombre() == null || empleado.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del empleado es obligatorio.");
        }
        if (empleado.getNombre().length() > 100) {
            throw new IllegalArgumentException("El nombre no puede superar los 100 caracteres.");
        }
        if (empleado.getEdad() == null || empleado.getEdad() <= 0) {
            throw new IllegalArgumentException("La edad debe ser un valor mayor o igual a cero.");
        }
        if (empleado.getSalario() == null || empleado.getSalario().signum() < 0) {
            throw new IllegalArgumentException("El salario debe ser un valor mayor o igual a cero.");
        }
    }
}
