#!/bin/bash


sh manifest_generate.sh

$TERMINAL -e sh -c 'cd tmp/tpe1-g8-server-2024.1Q && ./run-server.sh' &

echo waiting for server
sleep 1

cd tmp/tpe1-g8-client-2024.1Q


rm ../../query1.txt
sh queryClient.sh -DserverAddress=127.0.0.0:50051 -Daction=queryCounters -DoutPath=../../query1.txt -Dsector=C

cat ../../query1.txt

sh adminClient.sh -DserverAddress=127.0.0.0:50051 -Daction=addSector -Dsector=A
sh adminClient.sh -DserverAddress=127.0.0.0:50051 -Daction=addSector -Dsector=B
sh adminClient.sh -DserverAddress=127.0.0.0:50051 -Daction=addSector -Dsector=C

sh adminClient.sh -DserverAddress=127.0.0.0:50051 -Daction=addCounters -Dsector=A -Dcounters=1



rm ../../query1.txt
sh queryClient.sh -DserverAddress=127.0.0.0:50051 -Daction=queryCounters -DoutPath=../../query1.txt -Dsector=G

cat ../../query1.txt

rm ../../query1.txt
sh queryClient.sh -DserverAddress=127.0.0.0:50051 -Daction=queryCounters -DoutPath=../../query1.txt -Dsector=C

cat ../../query1.txt


sh adminClient.sh -DserverAddress=127.0.0.0:50051 -Daction=addCounters -Dsector=C -Dcounters=3
sh adminClient.sh -DserverAddress=127.0.0.0:50051 -Daction=addCounters -Dsector=B -Dcounters=2
sh adminClient.sh -DserverAddress=127.0.0.0:50051 -Daction=addCounters -Dsector=C -Dcounters=2

sh adminClient.sh -DserverAddress=127.0.0.0:50051 -Daction=manifest -DinPath=../../manifest.csv


sh counterClient.sh -DserverAddress=127.0.0.0:50051 -Daction=assignCounters -Dsector=C -Dflights='AA123|AA124|AA125' -Dairline=AmericanAirlines -DcounterCount=2
sh counterClient.sh -DserverAddress=127.0.0.0:50051 -Daction=listCounters -Dsector=C -DcounterFrom=2 -DcounterTo=8

sh passengerClient.sh -DserverAddress=127.0.0.0:50051 -Daction=passengerStatus -Dbooking=XYZ234
sh passengerClient.sh -DserverAddress=127.0.0.0:50051 -Daction=passengerStatus -Dbooking=XYZ235
sh passengerClient.sh -DserverAddress=127.0.0.0:50051 -Daction=passengerStatus -Dbooking=XYZ236

sh passengerClient.sh -DserverAddress=127.0.0.0:50051 -Daction=passengerCheckin -Dbooking=XYZ234 -Dsector=C -Dcounter=2
sh passengerClient.sh -DserverAddress=127.0.0.0:50051 -Daction=passengerCheckin -Dbooking=XYZ235 -Dsector=C -Dcounter=2
sh passengerClient.sh -DserverAddress=127.0.0.0:50051 -Daction=passengerCheckin -Dbooking=XYZ236 -Dsector=C -Dcounter=2

sh passengerClient.sh -DserverAddress=127.0.0.0:50051 -Daction=passengerStatus -Dbooking=XYZ234
sh passengerClient.sh -DserverAddress=127.0.0.0:50051 -Daction=passengerStatus -Dbooking=XYZ235
sh passengerClient.sh -DserverAddress=127.0.0.0:50051 -Daction=passengerStatus -Dbooking=XYZ236

rm ../..query1.txt
sh queryClient.sh -DserverAddress=127.0.0.0:50051 -Daction=queryCounters -DoutPath=../../query1.txt -Dsector=C

cat ../../query1.txt

sh counterClient.sh -DserverAddress=127.0.0.0:50051 -Daction=checkinCounters -Dsector=C -DcounterFrom=2 -Dairline=AmericanAirlines
sh counterClient.sh -DserverAddress=127.0.0.0:50051 -Daction=checkinCounters -Dsector=C -DcounterFrom=2 -Dairline=AmericanAirlines

rm ../..query1.txt
sh queryClient.sh -DserverAddress=127.0.0.0:50051 -Daction=queryCounters -DoutPath=../../query1.txt -Dsector=C

cat ../../query1.txt

# sh counterClient.sh -DserverAddress=127.0.0.0:50051 -Daction=assignCounters -Dsector=C -Dflights='AA125' -Dairline=AmericanAirlines -DcounterCount=2

sh counterClient.sh -DserverAddress=127.0.0.0:50051 -Daction=freeCounters -Dsector=C -DcounterFrom=2 -Dairline=AmericanAirlines

sh counterClient.sh -DserverAddress=127.0.0.0:50051 -Daction=listCounters -Dsector=C -DcounterFrom=2 -DcounterTo=4
sh counterClient.sh -DserverAddress=127.0.0.0:50051 -Daction=listCounters -Dsector=C -DcounterFrom=1 -DcounterTo=1


