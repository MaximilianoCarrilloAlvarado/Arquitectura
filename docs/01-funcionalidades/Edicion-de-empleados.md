# Edición de empleados

## Objetivo principal

Actualizar el nombre, la edad o el salario de un empleado existente sin crear otro registro ni modificar su identificador.

## Funcionalidades implementadas

1. **Cargar los datos actuales en el formulario.** El botón **Editar** de cada fila abre el formulario con el nombre, la edad y el salario de ese empleado. El título muestra el ID que se está editando.
2. **Validar y guardar los cambios.** El formulario y el servicio aplican las mismas reglas de [registro](Registro-de-empleados.md). Al guardar, se actualiza el registro correspondiente y se recarga el listado. **Cancelar** cierra el formulario sin guardar.
3. **Manejar registros inexistentes.** Si los datos son válidos pero el ID ya no existe, el servicio lanza `EmpleadoNotFoundException`, la API devuelve `404` y la pantalla muestra el error de guardado.
