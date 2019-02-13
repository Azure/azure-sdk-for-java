# Benchmark tool

##Build the benchmarking tool

```bash
git clone https://github.com/Azure/azure-cosmosdb-java.git
cd azure-cosmosdb-java

mvn clean package -DskipTests
```

and then the package will be generated. 

##Run the WriteLatency workload

```bash
java -jar azure-cosmosdb-benchmark-2.4.1-SNAPSHOT-jar-with-dependencies.jar \
 -serviceEndpoint $endpoint -masterKey $masterkey \
 -databaseId $dbname -collectionId $colname \
 -consistencyLevel Eventual -concurrency 10 -numberOfOperations 1000000 \
 -operation WriteLatency -connectionMode Direct
```

You can provide ``--help`` to the tool to see the list of other work loads (read, etc) and other options. 
