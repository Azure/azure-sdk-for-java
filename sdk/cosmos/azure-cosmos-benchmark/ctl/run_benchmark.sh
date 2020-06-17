#!/bin/bash

service_endpoint=$Endpoint
master_key=$Key

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
consistency_level=Eventual
else
consistency_level=$ConsistencyLevel
fi

if [ -z "$NumberOfOperations" ]
then
number_of_operations=-1
else
number_of_operations=$NumberOfOperations
fi

if [ -z "$MaxRunningTimeDuration" ]
then
max_running_time_duration=PT10H
else
max_running_time_duration=$MaxRunningTimeDuration
fi

connection_mode=Direct
number_of_precreated_documents=1000
gateway_connection_poolsize=5

protocol=Tcp

col_name="testCol"
db_name="testdb"

log_filename="/tmp/javactl.log"

echo "log file name is $log_filename"

echo "serviceEndpoint $service_endpoint, colNmae $col_name operation: $operation, consistencyLevel: $consistency_level, connectionMode: $connection_mode, protocol: $protocol, concurrency: $concurrency" > $log_filename
start=`test "x$1" == x && date +%s`
jar_file=./azure-cosmos-benchmark-jar-with-dependencies.jar

jvm_opt=""
#jvm_opt='-Dazure.cosmos.rntbd.threadcount=16 -Dazure.cosmos.directTcp.defaultOptions={"maxRequestsPerChannel":10}'
additional_benchmark_options="" 
additional_benchmark_options="-documentDataFieldSize 10 -documentDataFieldCount 10" 
additional_benchmark_options="$additional_benchmark_options -maxConnectionPoolSize $gateway_connection_poolsize"

if [ -z "$GraphiteEndpoint" ]
then
java -Xmx2g -Xms2g  $jvm_opt -Dcosmos.directModeProtocol=$protocol -Dazure.cosmos.directModeProtocol=$protocol -jar "$jar_file" -serviceEndpoint "$service_endpoint" -masterKey "$master_key" -databaseId "$db_name"  -collectionId "$col_name" -consistencyLevel $consistency_level -concurrency $concurrency -numberOfOperations $number_of_operations -operation $operation -connectionMode $connection_mode -maxRunningTimeDuration $max_running_time_duration -numberOfPreCreatedDocuments $number_of_precreated_documents $additional_benchmark_options 2>&1 | tee -a "$log_filename"
else
java -Xmx2g -Xms2g  $jvm_opt -Dcosmos.directModeProtocol=$protocol -Dazure.cosmos.directModeProtocol=$protocol -jar "$jar_file" -serviceEndpoint "$service_endpoint" -masterKey "$master_key" -databaseId "$db_name"  -collectionId "$col_name" -consistencyLevel $consistency_level -concurrency $concurrency -numberOfOperations $number_of_operations -operation $operation -connectionMode $connection_mode -maxRunningTimeDuration $max_running_time_duration -graphiteEndpoint $GraphiteEndpoint -numberOfPreCreatedDocuments $number_of_precreated_documents $additional_benchmark_options 2>&1 | tee -a "$log_filename"
fi

end=`test "x$1" == x && date +%s`
total_time=`expr $end - $start`

echo "It took $total_time seconds to insert $numberOfOperations documents." 
