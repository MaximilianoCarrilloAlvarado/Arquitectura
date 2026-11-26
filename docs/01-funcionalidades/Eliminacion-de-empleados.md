# Eliminación de empleados

## Objetivo principal

Retirar de la base de datos uno o varios registros, con una confirmación previa para evitar borrados accidentales.

## Funcionalidades implementadas

1. **Eliminar un empleado.** El botón **Eliminar** de cada fila solicita confirmación indicando el nombre. Si el usuario cancela, no se envía la solicitud. Si el ID ya no existe, la API devuelve `404`.
2. **Eliminar varios empleados en una sola acción (funcionalidad adicional).** El usuario puede marcar registros individualmente o seleccionar todas las filas de la página visible. La selección se conserva al navegar entre páginas. **Eliminar seleccionados** se habilita al marcar al menos un registro y pide confirmación indicando cuántos se eliminarán.
3. **Actualizar el listado tras el borrado.** Después de una operación exitosa, la pantalla actualiza el listado y ajusta la página actual si la última página quedó vacía. Si falla el borrado, se muestra un mensaje de error; si falla únicamente la actualización de la lista, se informa el problema de carga.
