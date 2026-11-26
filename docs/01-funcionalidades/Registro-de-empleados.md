# Registro de empleados

## Objetivo principal

Dar de alta a un empleado con su nombre, edad y salario, sin que el usuario tenga que asignarle un identificador.

## Funcionalidades implementadas

1. **Abrir un formulario de alta.** El botón **Agregar empleado** muestra los campos vacíos. **Cancelar** cierra el formulario sin enviar los datos.
2. **Validar los datos.** El nombre es obligatorio y admite hasta 100 caracteres; la edad debe ser un entero mayor que cero y el salario no puede ser negativo. El formulario limita el salario a 99 999 999,99 y acepta incrementos de 0,01. El servicio vuelve a comprobar el nombre, la edad positiva y el salario no negativo.
3. **Guardar y mostrar el resultado.** La base de datos asigna el ID. Después de guardar, la pantalla vuelve a cargar el listado y navega a la última página si la recarga tiene éxito. Se muestra la confirmación cuando se logra recargar la lista.
4. **Informar errores.** Si el servicio rechaza los datos por una regla de validación, devuelve `400`. Si falla el guardado, el formulario conserva los datos ingresados para que el usuario pueda corregirlos o volver a intentar.
