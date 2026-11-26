# Consulta de empleados

## Objetivo principal

Permitir que el usuario revise los registros existentes y navegue por el listado sin mostrar todos los empleados a la vez.

## Funcionalidades implementadas

1. **Listar empleados.** Al abrir la pantalla se muestra una tabla con ID, nombre, edad y salario. Si no existen registros, se muestra «No hay empleados registrados».
2. **Consultar un registro por ID.** La API devuelve el empleado solicitado. Si el ID no existe, devuelve `404` con un mensaje. Esta operación está disponible en la API; la pantalla no tiene un campo de búsqueda por ID.
3. **Paginar el listado (funcionalidad adicional).** Se muestran hasta seis empleados por página. Se puede avanzar, retroceder o ir a un número de página. El indicador muestra la página actual, el total de páginas y la cantidad de empleados. El navegador pagina el listado recibido; no solicita cada página al servidor por separado.
4. **Informar fallos de carga.** Si no se puede obtener el listado, se muestra un mensaje de error distinto del aviso de lista vacía.
