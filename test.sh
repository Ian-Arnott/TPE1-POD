#!/bin/bash

sh manifest_generate.sh

$TERMINAL -e sh -c 'cd tmp/tpe1-g8-server-2024.1Q && ./run-server.sh' &

echo waiting for server
sleep 1

cd tmp/tpe1-g8-client-2024.1Q

sh adminClient.sh -DserverAddress=127.0.0.0:50051 -Daction=addSector -Dsector=A
sh adminClient.sh -DserverAddress=127.0.0.0:50051 -Daction=addSector -Dsector=B
sh adminClient.sh -DserverAddress=127.0.0.0:50051 -Daction=addSector -Dsector=C

sh adminClient.sh -DserverAddress=127.0.0.0:50051 -Daction=addCounters -Dsector=A -Dcounters=1
sh adminClient.sh -DserverAddress=127.0.0.0:50051 -Daction=addCounters -Dsector=C -Dcounters=3
sh adminClient.sh -DserverAddress=127.0.0.0:50051 -Daction=addCounters -Dsector=B -Dcounters=3

sh adminClient.sh -DserverAddress=127.0.0.0:50051 -Daction=manifest -DinPath=../../manifest.csv
sh adminClient.sh -DserverAddress=127.0.0.0:50051 -Daction=manifest -DinPath=../../manifest.csv


sh counterClient.sh -DserverAddress=127.0.0.0:50051 -Daction=assignCounters -Dsector=C -Dflights='AA123|AA124' -Dairline=AmericanAirlines -DcounterCount=3
sh counterClient.sh -DserverAddress=127.0.0.0:50051 -Daction=listCounters -Dsector=C -DcounterFrom=2 -DcounterTo=4


# sh counterClient.sh -DserverAddress=127.0.0.0:50051 -Daction=assignCounters -Dsector=C -Dflights='AA125' -Dairline=AmericanAirlines -DcounterCount=2


