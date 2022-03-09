#!/bin/bash

############################################################################
## The parameters supported
##  - ctl_endpoint (required)
##  - ctl_key (required)
##  - ctl_database (optional: default testdb)
##  - ctl_collection (optional: default testCol)
##  - ctl_manage_database (optional: default false)
##  - ctl_throughput (optional: default 100,000)
##  - ctl_concurrency (optional: default 50)
##  - ctl_consistency_level (optional: default Session)
##  - ctl_number_of_precreated_documents (optional: default 100,000)
##  - ctl_bulk_load_batch_size (optional: default 200,000)
##  - ctl_number_of_operations (optional: default 1,000,000)
##  - ctl_max_running_time_duration (optional: default 10 minutes)
##  - ctl_printing_interval (optional: default 15 seconds)
##  - ctl_heap_size (optional: default 8g)
##  - ctl_environment (optional: default Daily)

service_endpoint=$ctl_endpoint
master_key=$ctl_key
operation=LinkedInCtlWorkload
number_Of_collection=1
connection_mode=Direct
protocol=Tcp

if [ -z "$ctl_database" ]
then
database_name="linkedInTestDB"
else
database_name=$ctl_database
fi

if [ -z "$ctl_collection" ]
then
collection_name="linkedInTestCollection"
else
collection_name=$ctl_collection
fi

if [ -z "$ctl_throughput" ]
then
throughput=100000
else
throughput=$ctl_throughput
fi

if [ -z "$ctl_concurrency" ]
then
concurrency=50
else
concurrency=$ctl_concurrency
fi

if [ -z "$ctl_consistency_level" ]
then
consistency_level=Session
else
consistency_level=$ctl_consistency_level
fi

if [ -z "$ctl_number_of_precreated_documents" ]
then
number_of_precreated_documents=1000000
else
number_of_precreated_documents=$ctl_number_of_precreated_documents
fi

if [ -z "$ctl_bulk_load_batch_size" ]
then
bulk_load_batch_size=200000
else
bulk_load_batch_size=$ctl_bulk_load_batch_size
fi

if [ -z "$ctl_number_of_operations" ]
then
number_of_operations=-1
else
number_of_operations=$ctl_number_of_operations
fi

if [ -z "$ctl_max_running_time_duration" ]
then
max_running_time_duration=PT24H
else
max_running_time_duration=$ctl_max_running_time_duration
fi

if [ -z "$ctl_printing_interval" ]
then
printing_interval=15
else
printing_interval=$ctl_printing_interval
fi

if [ -z "$ctl_heap_size" ]
then
heap_size=8g
else
heap_size=$ctl_heap_size
fi

if [ -z "$ctl_environment" ]
then
environment=Daily
else
environment=$ctl_environment
fi

if [ -z "$ctl_test_scenario" ]
then
test_scenario=GET
else
test_scenario=$ctl_test_scenario
fi

log_filename="/tmp/javactl.log"
echo "log file name is $log_filename"

pwd
ls -lart

echo "serviceEndpoint $service_endpoint, collectionName $collection_name operation: $operation, consistencyLevel: $consistency_level, connectionMode: $connection_mode, protocol: $protocol, concurrency: $concurrency" > $log_filename
start=`date +%s`
jar_file=./azure-cosmos-benchmark-jar-with-dependencies.jar

jvm_opt=""

if [ -z "$ctl_graphite_endpoint" ]
then
    if [ -z "$ctl_manage_database" ]
    then
        java -Xmx$heap_size -Xms$heap_size  $jvm_opt -Dcosmos.directModeProtocol=$protocol -Dazure.cosmos.directModeProtocol=$protocol -jar "$jar_file" -serviceEndpoint "$service_endpoint" -masterKey "$master_key" -databaseId "$database_name"  -collectionId "$collection_name" -numberOfCollectionForCtl "$number_Of_collection" -throughput $throughput  -consistencyLevel $consistency_level -concurrency $concurrency -numberOfOperations $number_of_operations -operation $operation -connectionMode $connection_mode -maxRunningTimeDuration $max_running_time_duration -numberOfPreCreatedDocuments $number_of_precreated_documents -bulkloadBatchSize $bulk_load_batch_size -printingInterval $printing_interval -environment $environment -testScenario $test_scenario 2>&1 | tee -a "$log_filename"
    else
        java -Xmx$heap_size -Xms$heap_size  $jvm_opt -Dcosmos.directModeProtocol=$protocol -Dazure.cosmos.directModeProtocol=$protocol -jar "$jar_file" -serviceEndpoint "$service_endpoint" -masterKey "$master_key" -databaseId "$database_name"  -collectionId "$collection_name" -numberOfCollectionForCtl "$number_Of_collection" -throughput $throughput  -consistencyLevel $consistency_level -concurrency $concurrency -numberOfOperations $number_of_operations -operation $operation -connectionMode $connection_mode -maxRunningTimeDuration $max_running_time_duration -numberOfPreCreatedDocuments $number_of_precreated_documents -bulkloadBatchSize $bulk_load_batch_size -printingInterval $printing_interval -manageDatabase -environment $environment -testScenario $test_scenario 2>&1 | tee -a "$log_filename"
    fi
else
    if [ -z "$ctl_manage_database" ]
    then
        java -Xmx$heap_size -Xms$heap_size  $jvm_opt -Dcosmos.directModeProtocol=$protocol -Dazure.cosmos.directModeProtocol=$protocol -jar "$jar_file" -serviceEndpoint "$service_endpoint" -masterKey "$master_key" -databaseId "$database_name"  -collectionId "$collection_name" -numberOfCollectionForCtl "$number_Of_collection" -throughput $throughput  -consistencyLevel $consistency_level -concurrency $concurrency -numberOfOperations $number_of_operations -operation $operation -connectionMode $connection_mode -maxRunningTimeDuration $max_running_time_duration -graphiteEndpoint $ctl_graphite_endpoint -numberOfPreCreatedDocuments $number_of_precreated_documents -bulkloadBatchSize $bulk_load_batch_size -printingInterval $printing_interval -environment $environment -testScenario $test_scenario 2>&1 | tee -a "$log_filename"
    else
        java -Xmx$heap_size -Xms$heap_size  $jvm_opt -Dcosmos.directModeProtocol=$protocol -Dazure.cosmos.directModeProtocol=$protocol -jar "$jar_file" -serviceEndpoint "$service_endpoint" -masterKey "$master_key" -databaseId "$database_name"  -collectionId "$collection_name" -numberOfCollectionForCtl "$number_Of_collection" -throughput $throughput  -consistencyLevel $consistency_level -concurrency $concurrency -numberOfOperations $number_of_operations -operation $operation -connectionMode $connection_mode -maxRunningTimeDuration $max_running_time_duration -graphiteEndpoint $ctl_graphite_endpoint -numberOfPreCreatedDocuments $number_of_precreated_documents -bulkloadBatchSize $bulk_load_batch_size -printingInterval $printing_interval -manageDatabase -environment $environment -testScenario $test_scenario 2>&1 | tee -a "$log_filename"
    fi
fi

end=`date +%s`
total_time=`expr $end - $start`

echo "It took $total_time seconds to test $number_of_operations documents."
