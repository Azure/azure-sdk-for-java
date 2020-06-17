#!/bin/bash

serviceEndpoint=$Endpoint
masterKey=$Key

if [ -z "$Operation" ]
then
operation=ReadLatency
else
operation=$Operation
fi

if [ -z "$Concurrency" ]
then
concurrency=50
else
concurrency=$Concurrency
fi

if [ -z "$ConsistencyLevel" ]
then
consistencyLevel=Eventual
else
consistencyLevel=$ConsistencyLevel
fi

if [ -z "$NumberOfOperations" ]
then
numberOfOperations=-1
else
numberOfOperations=$NumberOfOperations
fi

if [ -z "$MaxRunningTimeDuration" ]
then
maxRunningTimeDuration=PT10H
else
maxRunningTimeDuration=$MaxRunningTimeDuration
fi

connectionMode=Direct
numberOfPreCreatedDocuments=1000
gatewayConnectionPoolSize=5

protocol=Tcp

colName="testCol"
dbName="testdb"

logFileName="/tmp/javactl.log"

echo "log file name is $logFileName"

echo "serviceEndpoint $serviceEndpoint, colNmae $colName operation: $operation, consistencyLevel: $consistencyLevel, connectionMode: $connectionMode, protocol: $protocol, concurrency: $concurrency" > $logFileName
start=`test "x$1" == x && date +%s`
jar_file=./azure-cosmos-benchmark-jar-with-dependencies.jar

JVM_OPT=""
#JVM_OPT='-Dazure.cosmos.rntbd.threadcount=16 -Dazure.cosmos.directTcp.defaultOptions={"maxRequestsPerChannel":10}'
additionalBenchmarkOptions="" 
additionalBenchmarkOptions="-documentDataFieldSize 10 -documentDataFieldCount 10" 
additionalBenchmarkOptions="$additionalBenchmarkOptions -maxConnectionPoolSize $gatewayConnectionPoolSize"

if [ -z "$GraphiteEndpoint" ]
then
java -Xmx2g -Xms2g  $JVM_OPT -Dcosmos.directModeProtocol=$protocol -Dazure.cosmos.directModeProtocol=$protocol -jar "$jar_file" -serviceEndpoint "$serviceEndpoint" -masterKey "$masterKey" -databaseId "$dbName"  -collectionId "$colName" -consistencyLevel $consistencyLevel -concurrency $concurrency -numberOfOperations $numberOfOperations -operation $operation -connectionMode $connectionMode -maxRunningTimeDuration $maxRunningTimeDuration -numberOfPreCreatedDocuments $numberOfPreCreatedDocuments $additionalBenchmarkOptions 2>&1 | tee -a "$logFileName"
else
java -Xmx2g -Xms2g  $JVM_OPT -Dcosmos.directModeProtocol=$protocol -Dazure.cosmos.directModeProtocol=$protocol -jar "$jar_file" -serviceEndpoint "$serviceEndpoint" -masterKey "$masterKey" -databaseId "$dbName"  -collectionId "$colName" -consistencyLevel $consistencyLevel -concurrency $concurrency -numberOfOperations $numberOfOperations -operation $operation -connectionMode $connectionMode -maxRunningTimeDuration $maxRunningTimeDuration -graphiteEndpoint $GraphiteEndpoint -numberOfPreCreatedDocuments $numberOfPreCreatedDocuments $additionalBenchmarkOptions 2>&1 | tee -a "$logFileName"
fi

end=`test "x$1" == x && date +%s`
total_time=`expr $end - $start`

echo "It took $total_time seconds to insert $numberOfOperations documents." 
