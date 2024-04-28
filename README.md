# POD TPE1 - GRUPO 8 - Mostradores de Check-in

Implementación de un **sistema remoto *thread-safe*** para la **asignación de mostradores de check-in de un aeropuerto**
permitiendo notificar a las aerolíneas y ofreciendo reportes de los check-ins realizados hasta el momento.

## Instrucciones de Compilación

Para comilar el proyecto, se deben ejecutar los siguientes comandos en la carpeta raíz del proyecto:
````bash
chmod +x compile.sh
./compile.sh
````
Este script se encargara de compilar el proyecto con `maven`, generando los archivos `.tar.gz` en el directorio temprario `tmp`.

Asegurarse de tener el JDK de Java 21 instalado.

## Ejecución del Servidor

Para ejecutar el servidor, se deben ejecutar los siguientes comandos en la raíz del proyecto:
````bash
cd ./tmp/tpe1-g8-server-2024.1Q/
./run-server.sh
````
El servidor corre en el puerto `50051`

## Ejecución del Cliente de Administración

Para ejecutar el cliente de Administración, se deben ejecutar los siguientes comandos en la raíz del proyecto:
````bash
cd ./tmp/tpe1-g8-client-2024.1Q/
./adminClient.sh -DserverAddress=xx.xx.xx.xx:50051 -Daction=actionName [ -Dsector=sectorName | -Dcounters=counterCount | -DinPath=manifestPath ]
````

## Ejecución del Cliente de Reserva de Mostradores

Para ejecutar el cliente de Reserva de Mostradores, se deben ejecutar los siguientes comandos en la raíz del proyecto:
````bash
cd ./tmp/tpe1-g8-client-2024.1Q/
./counterClient -DserverAddress=xx.xx.xx.xx:50051 -Daction=actionName [ -Dsector=sectorName | -DcounterFrom=fromVal | -DcounterTo=toVal | -Dflights=flights | -Dairline=airlineName | -DcounterCount=countVal ]
````

## Ejecución del Cliente de Check-in de Pasajeros

Para ejecutar el cliente de Check-in de Pasajeros, se deben ejecutar los siguientes comandos en la raíz del proyecto:
````bash
cd ./tmp/tpe1-g8-client-2024.1Q/
./passengerClient -DserverAddress=xx.xx.xx.xx:50051 -Daction=actionName [ -Dbooking=booking | -Dsector=sectorName | -Dcounter=counterNumber ]
````

## Ejecución del Cliente de Notificaciones de Aerolínea

Para ejecutar el cliente de Notificaciones de Aerolínea, se deben ejecutar los siguientes comandos en la raíz del proyecto:
````bash
cd ./tmp/tpe1-g8-client-2024.1Q/
./notifyClient -DserverAddress=xx.xx.xx.xx:50051 -Daction=actionName -Dairline=airlineName
````

## Ejecución del Cliente de Consulta de Mostradores

Para ejecutar el cliente de Notificaciones de Aerolínea, se deben ejecutar los siguientes comandos en la raíz del proyecto:
````bash
cd ./tmp/tpe1-g8-client-2024.1Q/
./queryClient -DserverAddress=xx.xx.xx.xx:50051 -Daction=actionName [ -Dsector=sectorName | -Dairline=airlineName | -Dcounter=counterVal ]
````

## Integrantes
| Nombre                          | Legajo |
|---------------------------------|--------|
| Arnott, Ian James               | 61267  |
| Itcovici, David                 | 61466  |
| Perri, Lucas David              | 62746  |
| Rivas Berancourt, Santiago Luis | 61007  |