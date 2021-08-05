# Azure CosmosDB client library for Java

# Port of the Microsoft Azure Cosmos DB .NET Benchmark tool for Java

## Getting started 

### Build the benchmarking tool

```bash
git clone https://github.com/Azure/azure-sdk-for-java.git
cd azure-sdk-for-java
cd sdk/cosmos/
mvn clean package -f pom.xml -DskipTests -Dgpg.skip -Ppackage-assembly
```

and then the package will be generated.

## Examples
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

  -pl                    Degree of parallism
  
  -w                     Workload name

  --itemTemplateFile     Item template

  --help                 Display this help screen.

  --version              Display version information.
```

## Key concepts
n/a

## Troubleshooting
n/a

## Next steps
n/a

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a
[Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights
to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate
the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to
do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq]
or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-cosmos/src
[cosmos_introduction]: https://docs.microsoft.com/azure/cosmos-db/
[api_documentation]: https://azuresdkdocs.blob.core.windows.net/$web/java/azure-cosmos/latest/index.html
[cosmos_docs]: https://docs.microsoft.com/azure/cosmos-db/introduction
[jdk]: https://docs.microsoft.com/java/azure/java-supported-jdk-runtime?view=azure-java-stable
[maven]: https://maven.apache.org/
[cosmos_maven]: https://search.maven.org/artifact/com.azure/azure-cosmos
[cosmos_maven_svg]: https://img.shields.io/maven-central/v/com.azure/azure-cosmos.svg
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[azure_subscription]: https://azure.microsoft.com/free/
[samples]: https://github.com/Azure-Samples/azure-cosmos-java-sql-api-samples
[samples_readme]: https://github.com/Azure-Samples/azure-cosmos-java-sql-api-samples/blob/master/README.md
[troubleshooting]: https://docs.microsoft.com/azure/cosmos-db/troubleshoot-java-sdk-v4-sql
[perf_guide]: https://docs.microsoft.com/azure/cosmos-db/performance-tips-java-sdk-v4-sql?tabs=api-async
[sql_api_query]: https://docs.microsoft.com/azure/cosmos-db/sql-api-sql-query
[getting_started]: https://github.com/Azure-Samples/azure-cosmos-java-getting-started
[quickstart]: https://docs.microsoft.com/azure/cosmos-db/create-sql-api-java?tabs=sync
[project_reactor_schedulers]: https://projectreactor.io/docs/core/release/api/reactor/core/scheduler/Schedulers.html

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcosmos%2Fazure-cosmos%2FREADME.png)
