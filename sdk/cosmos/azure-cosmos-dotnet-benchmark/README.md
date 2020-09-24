# Port of the Microsoft Azure Cosmos DB .NET Benchmark tool for Java

## Sample tool usage
```
java -jar ./azure-cosmos-dotnet-benchmark-4.0.1-beta.1-jar-with-dependencies.jar -e {ACCOUNT_ENDPOINT} -k {ACCOUNT_KEY}
```

Dry run targeting emulator will look like below
```
java -jar ./azure-cosmos-dotnet-benchmark-4.0.1-beta.1-jar-with-dependencies.jar -e "https://localhost:8081" -k "C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw=="
```

Run targeting a production account
```
export OSSProjectRef=True
java -jar ./azure-cosmos-dotnet-benchmark-4.0.1-beta.1-jar-with-dependencies.jar -e {ACCOUNT_ENDPOINT} -k {ACCOUNT_KEY}
```


For PerfRuns with reports (INTERNAL)
```
export OSSProjectRef=True
export ACCOUNT_ENDPOINT=<ENDPOINT
export ACCOUNT_KEY=<KEY>
export RESULTS_PK="runs-summary" #For test runs use different one
export PL=300

java -jar ./azure-cosmos-dotnet-benchmark-4.0.1-beta.1-jar-with-dependencies.jar -e $ACCOUNT_ENDPOINT -k $ACCOUNT_KEY --publishResults --resultsPartitionKeyValue $RESULTS_PK -commitId $(git log -1 | head -n 1 | cut -d ' ' -f 2) --commitDate $(git log -1 --date=format:'%Y-%m-%d %H:%M:%S' | grep Date | cut -f 2- -d ':' | sed 's/^[ \t]*//;s/[ \t]*$//' | cut -f 1 -d ' ') --commitTime $(git log -1 --date=format:'%Y-%m-%d %H:%M:%S' | grep Date | cut -f 2- -d ':' | sed 's/^[ \t]*//;s/[ \t]*$//' | cut -f 2 -d ' ') --branchName $(git rev-parse --abbrev-ref HEAD)  --database testdb --container testcol --partitionKeyPath /id -n 500000 -w Insert -pl $PL 
```



## Usage
```
>java -jar ./azure-cosmos-dotnet-benchmark-4.0.1-beta.1-jar-with-dependencies.jar
CosmosBenchmark 1.0.0
Copyright (C) 2019 CosmosBenchmark

  -e                     Required. Cosmos account end point

  -k                     Required. Cosmos account master key

  --database             Database to use

  --container            Collection to use

  -t                     Collection throughput use

  -n                     Number of documents to insert

  --partitionKeyPath     Container partition key path

  -pl                   Degree of parallism

  --itemTemplateFile     Item template

  --help                 Display this help screen.

  --version              Display version information.
```

## Running on Azure

If you quickly get results, you can use our [guide to leverage Azure Container Instances](./AzureContainerInstances/README.md) to execute the benchmarks in any number of Azure regions with very little setup required.