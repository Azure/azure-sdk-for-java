#!/bin/bash

service_endpoint=$ctl_endpoint
master_key=$ctl_key

if [ -z "$ctl_operation" ]; then
    operation=CtlWorkload
else
    operation=$ctl_operation
fi

if [ -z "$ctl_concurrency" ]; then
    concurrency=50
else
    concurrency=$ctl_concurrency
fi

if [ -z "$ctl_consistency_level" ]; then
    consistency_level=Eventual
else
    consistency_level=$ctl_consistency_level
fi

if [ -z "$ctl_throughput" ]; then
    throughput=100000
else
    throughput=$ctl_throughput
fi

if [ -z "$ctl_read_write_query_pct" ]; then
    read_write_query_pct="90,9,1"
else
    read_write_query_pct=$ctl_read_write_query_pct
fi

if [ -z "$ctl_number_of_operations" ]; then
    number_of_operations=-1
else
    number_of_operations=$ctl_number_of_operations
fi

if [ -z "$ctl_max_running_time_duration" ]; then
    max_running_time_duration=PT10H
else
    max_running_time_duration=$ctl_max_running_time_duration
fi

if [ -z "$ctl_number_Of_collection" ]; then
    number_Of_collection=4
else
    number_Of_collection=$ctl_number_Of_collection
fi

if [ -z "$ctl_diagnostics_threshold_duration" ]; then
    diagnostics_threshold_duration=PT60S
else
    diagnostics_threshold_duration=$ctl_diagnostics_threshold_duration
fi

if [ -z "$ctl_number_of_precreated_documents" ]; then
    number_of_precreated_documents=10000
else
    number_of_precreated_documents=$ctl_number_of_precreated_documents
fi

connection_mode=Direct
gateway_connection_poolsize=5

protocol=Tcp

if [ -z "$ctl_database_name" ]; then
    db_name="testdb"
else
    db_name=$ctl_database_name
fi

if [ -z "$ctl_container_name" ]; then
    col_name="testCol"
else
    col_name=$ctl_container_name
fi

log_filename="/tmp/javactl.log"

echo "log file name is $log_filename"

echo "serviceEndpoint $service_endpoint, colNmae $col_name operation: $operation, consistencyLevel: $consistency_level, connectionMode: $connection_mode, protocol: $protocol, concurrency: $concurrency" >$log_filename
start=$(date +%s)
jar_file=./azure-cosmos-benchmark-jar-with-dependencies.jar

jvm_opt=""
#jvm_opt='-Dazure.cosmos.rntbd.threadcount=16 -Dazure.cosmos.directTcp.defaultOptions={"maxRequestsPerChannel":10}'
additional_benchmark_options=""
additional_benchmark_options="-documentDataFieldSize 10 -documentDataFieldCount 10"
additional_benchmark_options="$additional_benchmark_options -maxConnectionPoolSize $gateway_connection_poolsize"

if [ -z "$ctl_graphite_endpoint" ]; then
    java -Xmx8g -Xms8g $jvm_opt -Dcosmos.directModeProtocol=$protocol -Dazure.cosmos.directModeProtocol=$protocol -DCOSMOS.ENVIRONMENT_NAME=$ctl_env -DCOSMOS.CLIENT_TELEMETRY_ENDPOINT=$ctl_client_telemetry_endpoint -jar "$jar_file" -serviceEndpoint "$service_endpoint" -masterKey "$master_key" -databaseId "$db_name" -collectionId "$col_name" -readWriteQueryPct "$read_write_query_pct" -diagnosticsThresholdDuration "$diagnostics_threshold_duration" -numberOfCollectionForCtl "$number_Of_collection" -throughput $throughput -consistencyLevel $consistency_level -concurrency $concurrency -numberOfOperations $number_of_operations -operation $operation -connectionMode $connection_mode -maxRunningTimeDuration $max_running_time_duration -numberOfPreCreatedDocuments $number_of_precreated_documents -preferredRegionsList "$ctl_preferred_regions" $additional_benchmark_options 2>&1 | tee -a "$log_filename"
else
    java -Xmx8g -Xms8g $jvm_opt -Dcosmos.directModeProtocol=$protocol -Dazure.cosmos.directModeProtocol=$protocol -DCOSMOS.ENVIRONMENT_NAME=$ctl_env -DCOSMOS.CLIENT_TELEMETRY_ENDPOINT=$ctl_client_telemetry_endpoint -jar "$jar_file" -serviceEndpoint "$service_endpoint" -masterKey "$master_key" -databaseId "$db_name" -collectionId "$col_name" -readWriteQueryPct "$read_write_query_pct" -diagnosticsThresholdDuration "$diagnostics_threshold_duration" -numberOfCollectionForCtl "$number_Of_collection" -throughput $throughput -consistencyLevel $consistency_level -concurrency $concurrency -numberOfOperations $number_of_operations -operation $operation -connectionMode $connection_mode -maxRunningTimeDuration $max_running_time_duration -graphiteEndpoint $ctl_graphite_endpoint -numberOfPreCreatedDocuments $number_of_precreated_documents -preferredRegionsList "$ctl_preferred_regions" $ctl_accountNameInGraphiteReporter $additional_benchmark_options 2>&1 | tee -a "$log_filename"
fi

end=$(date +%s)
total_time=$(expr $end - $start)

echo "It took $total_time seconds to insert $numberOfOperations documents."
