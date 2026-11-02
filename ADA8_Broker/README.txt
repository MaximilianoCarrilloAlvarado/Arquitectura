ADA8_BROKER
============

DESCRIPCION
-----------
Este proyecto integra el sistema MVC de pedidos con una arquitectura Broker.

Flujo de una solicitud:

    Cliente -> Broker -> Servidor de pedidos -> Modelo MVC
                                      |
                                      v
                                  Inventario

El Broker registra y localiza estos servicios:

    registrarPedido
    consultarPedido
    listarPedidos

REQUISITOS
----------
- Java 17 o superior
- Maven
- Ejecutar los comandos desde la carpeta ADA8_Broker

COMPILACION
-----------
Abrir PowerShell y ejecutar:

    cd "C:\Users\IGNITER\Documents\GitHub\Arquitectura\ADA8_Broker"
    mvn package

Si la compilacion es correcta, Maven mostrara BUILD SUCCESS.

INICIAR EL BROKER
-----------------
Abrir una segunda terminal y ejecutar:

    cd "C:\Users\IGNITER\Documents\GitHub\Arquitectura\ADA8_Broker"
    $gson = "$env:USERPROFILE\.m2\repository\com\google\code\gson\gson\2.11.0\gson-2.11.0.jar"
    $cp = "target\classes;$gson"
    java -cp $cp Broker.Broker 19090

El Broker debe mostrar:

    Broker escuchando en el puerto 19090

INICIAR EL SERVIDOR DE PEDIDOS
------------------------------
Abrir una tercera terminal y ejecutar:

    cd "C:\Users\IGNITER\Documents\GitHub\Arquitectura\ADA8_Broker"
    $gson = "$env:USERPROFILE\.m2\repository\com\google\code\gson\gson\2.11.0\gson-2.11.0.jar"
    $cp = "target\classes;$gson"
    java -cp $cp Broker.Servidor 19091 127.0.0.1:19090

El Servidor debe mostrar:

    Servicio registrado: registrarPedido
    Servicio registrado: consultarPedido
    Servicio registrado: listarPedidos
    Servidor escuchando en el puerto 19091

Esto confirma que el Servidor se conecto al Broker y registro sus servicios.

PROBAR EL BROKER
----------------
Abrir una cuarta terminal. La siguiente funcion envia una solicitud JSON al
Broker:

   cd "C:\Users\IGNITER\Documents\GitHub\Arquitectura\ADA8_Broker"
   function Send-Json($port, $json) {
       $tcp = New-Object System.Net.Sockets.TcpClient
       $tcp.Connect("127.0.0.1", $port)
       $stream = $tcp.GetStream()
       $writer = New-Object System.IO.StreamWriter($stream, [System.Text.Encoding]::UTF8)
       $reader = New-Object System.IO.StreamReader($stream, [System.Text.Encoding]::UTF8)
        $writer.AutoFlush = $true
        $writer.WriteLine($json)
        $respuesta = $reader.ReadLine()
        $reader.Dispose()
        $writer.Dispose()
        $tcp.Dispose()
        return $respuesta
    }

LISTAR PEDIDOS
--------------
Ejecutar:

    Send-Json 19090 '{"servicio":"ejecutar","parametros":{"servicio":"listarPedidos","argumentos":{}}}'

Una respuesta correcta contiene:

    "estado":"OK"

REGISTRAR UN PEDIDO
-------------------
Ejecutar:

    Send-Json 19090 '{"servicio":"ejecutar","parametros":{"servicio":"registrarPedido","argumentos":{"cliente":"Ana","productos":[{"nombre":"Cuaderno","cantidad":2}]}}}'

La respuesta correcta incluye el pedido, precio, subtotal, impuestos y total.
El inventario se descuenta despues de registrar el pedido.

CONSULTAR UN PEDIDO
-------------------
Despues de registrar un pedido, usar su identificador. Por ejemplo, si el ID
devuelto fue 1:

    Send-Json 19090 '{"servicio":"ejecutar","parametros":{"servicio":"consultarPedido","argumentos":{"id":1}}}'

DETENER LOS PROCESOS
--------------------
En cada terminal donde se este ejecutando un proceso, presionar:

    Ctrl+C

NOTAS
-----
- El Broker escucha en el puerto 19090.
- El Servidor de pedidos escucha en el puerto 19091.
- Los puertos pueden cambiarse, pero deben coincidir en los comandos.
- El servidor usa el inventario ubicado en src\DB\inventario.txt.
- El Broker no llama automaticamente a otros Brokers. Esa funcion seria una
  ampliacion futura mediante federacion o encadenamiento de Brokers.
