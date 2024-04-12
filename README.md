# POD TPE1 - GRUPO 8 - Mostradores de Check-in

---
Implementación de un **sistema remoto *thread-safe*** para la **asignación de mostradores de check-in de un aeropuerto**
permitiendo notificar a las aerolíneas y ofreciendo reportes de los check-ins realizados hasta el momento.

## Instrucciones de Compilación

___
Para comilar el proyecto, se deben ejecutar los siguientes comandos en la carpeta raíz del proyecto:
````bash
chmod +x compile.sh
./compile.sh
````
Este script se encargara de compilar el proyecto con `maven`, generando los archivos `.tar.gz` en el directorio temprario `tmp`.


## Ejecución del Servidor

---
Para ejecutar el servidor, se deben ejecutar los siguientes comandos en la raíz del proyecto:
````bash
cd ./tmp/tpe1-g8-server-2024.1Q/
./run-server
````
El servidor corre en el puerto `50051`

## Ejecución del Cliente de Administración

---
Para ejecutar el cliente de Administración, se demen ejecutar los siguientes comandos en la raíz del proyecto:
````bash
cd ./tmp/tpe1-g8-client-2024.1Q/
./adminClient -DserverAddress=xx.xx.xx.xx:50051 -Daction=actionName [ -Dsector=sectorName | -Dcounters=counterCount | -DinPath=manifestPath ]
````