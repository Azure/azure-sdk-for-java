# Azure File Datalake Storage Stress Tests

Represents stress tests for Azure Storage File Datalake client library.

## Running tests in stress infra

Check out [Azure SDK Stress Test Wiki][azure_sdk_stress_test] for general information about stress tests.

### Prerequisites

- [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Maven][maven]
- [Docker][docker]
- [Kubectl][kubectl]
- [Helm][helm]
- [Azure CLI][azure_cli]
- [Powershell 7.0+][powershell]

### Deploy Stress Test

cd into `azure-sdk-for-java` root folder and run command to deploy the package to cluster:

```shell
.\eng\common\scripts\stress-testing\deploy-stress-tests.ps1 -SearchDirectory .\sdk\storage
``` 

### Check Status

Only the most frequently used commands are listed below. See [Deploying A Stress Test][deploy_stress_test] for more details.

List deployed packages:

```shell
helm list -n <stress test namespace>
```

the namespace usually matches your username. 

Get stress test pods and status:

```shell
kubectl get pods -n <stress test namespace>
```

Get stress test pod logs:

```shell
kubectl logs -n <stress test namespace> <stress test pod name>
# Note that we may define multiple containers (for example, `fault-injector` and `main`)
kubectl logs -n <stress test namespace> <stress test pod name> -c <container name>
```

If stress test pod is in `Error` status, check logs from init container:

```shell
kubectl logs -n <stress test namespace> <stress test pod name> -c init-azure-deployer
```

If above command output is empty, there may have been startup failures:

```shell
kubectl describe pod -n <stress test namespace> <stress test pod name>
```

Stop and remove deployed package:

```shell
helm uninstall <stress test name> -n <stress test namespace>
```

### Fault injection

## Running tests locally

You can also run stress tests locally with or without fault-injection.
To run test locally:
1. Build `azure-storage-file-datalake-stress` jar file. For example, you can do it with:
   ```powershell
   cd .\sdk\storage
   mvn clean install -pl .\azure-storage-stress\,.\azure-storage-file-datalake-stress
   ``` 
   As a result, you should have fat jar in `azure-storage-file-datalake-stress\target\azure-storage-file-datalake-stress-1.0.0-beta.1-jar-with-dependencies.jar`
2. Configure environment:
   - Set `STORAGE_CONNECTION_STRING`environment variable (connection string to storage account). You may also provide it with `--cs` command line option
   - Configure Application Insights:
     - Set `APPLICATIONINSIGHTS_CONNECTION_STRING` (connection string to application insights). You may also provide it in any other
       way mentioned in the [Enable Application Insights steps][enable_application_insights]
     - Set `APPLICATIONINSIGHTS_ROLE_NAME` to `storage-{ random string }` (for example, `storage-foobar`).
3. Run the test, for example, with:
   ```powershell
   java -javaagent:path\to\applicationinsights-agent.jar `
     -jar .\azure-storage-file-datalake-stress\target\azure-storage-file-datalake-stress-1.0.0-beta.1-jar-with-dependencies.jar `
     downloadtofile --duration 10 --size 1024 --parallel 2 --warmup 0
   ```

### Running locally with fault-injection

If you want to run test with fault-injection locally, you'd need to run fault-injector first:

1. Install [HTTP fault-injector][http-fault-injector] tool:
   ```powershell
   dotnet tool install azure.sdk.tools.httpfaultinjector --global --prerelease --add-source https://pkgs.dev.azure.com/azure-sdk/public/_packaging/azure-sdk-for-net/nuget/v3/index.json
   ```
   It uses .NET 6, so you might need to install it first.

   You might also need to trust development certificates:
   ```powershell
   dotnet dev-certs https --trust
   ```
2. Run fault-injector:
   ```powershell
    http-fault-injector   
   ```
3. Then you can run stress test with `--faults` option:
   ```powershell
   java -javaagent:path\to\applicationinsights-agent.jar `
     -jar .\azure-storage-file-datalake-stress\target\azure-storage-file-datalake-stress-1.0.0-beta.1-jar-with-dependencies.jar `
     downloadtofile --duration 10 --size 1024 --parallel 2 --warmup 0 --faults
   ```

### Checking test results

To get a super-quick idea about test results, look for a log record that looks like this:

```log
14:49:05.219 [main] INFO  c.a.s.blob.stress.DownloadToFile - {"az.sdk.message":"test finished","scenarioName":"com.azure.storage.blob.stress.DownloadToFile",
  "succeeded":468,"failed":0}
```

After the stress test is deployed on the cluster, we can monitor the progress on the Application Insights resource inside the stress test resource group.
There are several dashboards within the stress test resource group that we can use to monitor the AKS pod and stress test status.

#### Stress Test Dashboard

General-purpose stress test dashboard is available at https://aka.ms/azsdk/stress/dashboard. It shows:
- Pod status events
- CPU and memory utilization of the stress test pods
- Container logs and events

Stress test dashboard does not know about local stress test runs.

#### Application Insights

Application Insights agent brings rich monitoring experience including:
- resource utilization metrics (CPU, memory, GC, threads, etc.)
- live metrics, performance overview, etc
- distributed tracing and dependency calls (HTTP, Azure SDK calls)
- exceptions and logs
- profiling in production

Application Insights is useful to:
- monitor and compare throughput and latency across runs
- investigate issues and find bottlenecks

Application Insights is available for local runs (as long as you provide `-javaagent` option and make sure connection string is configured).

##### Stress test workbook

Storage stress test workbook is available [here][storage-workbook] and allows to pick a specific run and see it's summary:
1. Key test parameters
2. Throughput and latency charts
3. Failed operations including their status and fault injected (if any)
4. CPU and memory usage
5. Errors/warnings and exceptions in logs

The workbook relies on tests to emit:
- certain logs in a specific format produced by [TelemetryHelper][telemetry-helper] class
- certain spans produced in [BlobScenarioBase][blob-scenario-base] class.
- report cloud role name that follows `storage-{runId}` pattern. 
  Make sure to set `APPLICATIONINSIGHTS_ROLE_NAME` environment variable accordingly to make sure run appears on the dashboard. 

_Note: some failures are expected and there is no clear 'success' criteria for the stress test_

Here are a few things that clearly indicate an issue:
- Content mismatch - they can be detected in following ways: 
  - workbook shows that some operations has failed with `content mismatch` status
  - you see error logs like `{"az.sdk.message":"mismatched crc"...}`. Such logs have some additional context to investigate (content length, first 1024 bytes of it).
- Throughput is noticeably lower than for previous runs with the similar parameters. This would normally correlate with latency being higher than usual. 
- CPU/memory consumption is noticeably higher than for previous runs with the similar parameters

### Logging

We use [logback.xml][logback_xml] to configure the logging. By default, the stress test run on cluster will output
`INFO` level log which you may adjust based on your needs. 

The [storage workbook](#stress-test-workbook) needs `com.azure.storage.file-datalake.stress` and `com.azure.storage.stress` to stay at the `INFO` level.

You may also control the verbosity of logs that go to Application Insights - see [Application Insights logging configuration][application-insights-logging] for more details.

See also [Logging in Azure SDK][logging-azure-sdk] for more details.

## Key concepts

### Project Structure

See [Layout][stress_test_layout] section for details.

Below is the current structure of project:
```
.
├── src/                           # Test code
├── templates/                     # A directory of helm templates that will generate Kubernetes manifest files.
├── Chart.yaml                     # A YAML file containing information about the helm chart and its dependencies
├── scenarios-matrix.yaml          # A YAML file containing configuration and custom values for stress test(s)
├── Dockerfile                     # A Dockerfile for building the stress test image
├── stress-test-resources.bicep    # An Azure Bicep for deploying stress test azure resources
├── pom.xml
└── README.md
```

<!-- links -->
[azure_sdk_stress_test]: https://aka.ms/azsdk/stress
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[maven]: https://maven.apache.org/
[docker]: https://docs.docker.com/get-docker/
[kubectl]: https://kubernetes.io/docs/tasks/tools/#kubectl
[helm]: https://helm.sh/docs/intro/install/
[azure_cli]: https://docs.microsoft.com/cli/azure/install-azure-cli
[powershell]: https://docs.microsoft.com/powershell/scripting/install/installing-powershell?view=powershell-7
[enable_application_insights]: https://learn.microsoft.com/azure/azure-monitor/app/opentelemetry-enable?tabs=java#enable-azure-monitor-application-insights#enable-azure-monitor-application-insights
[logback_xml]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus-stress/src/main/resources/logback.xml
[deploy_stress_test]: https://github.com/Azure/azure-sdk-tools/blob/main/tools/stress-cluster/chaos/README.md#deploying-a-stress-test
[stress_test_layout]: https://github.com/Azure/azure-sdk-tools/blob/main/tools/stress-cluster/chaos/README.md#layout
[http-fault-injector]: https://github.com/Azure/azure-sdk-tools/tree/main/tools/http-fault-injector
[telemtery-helper]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-stress/src/main/java/com/azure/storage/stress/TelemetryHelper.java
[datalake-scenario-base]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-file-datalake-stress/src/main/java/com/azure/storage/file/datalake/stress/DataLakeScenarioBase.java
[storage-workbook]: https://ms.portal.azure.com/#blade/AppInsightsExtension/UsageNotebookBlade/ComponentId/%2Fsubscriptions%2Ffaa080af-c1d8-40ad-9cce-e1a450ca5b57%2FresourceGroups%2Frg-stress-cluster-pg%2Fproviders%2FMicrosoft.Insights%2Fcomponents%2Fstress-pg-ai-s7b6dif73rup6/ConfigurationId/%2Fsubscriptions%2Ffaa080af-c1d8-40ad-9cce-e1a450ca5b57%2Fresourcegroups%2Frg-stress-cluster-pg%2Fproviders%2Fmicrosoft.insights%2Fworkbooks%2Fa6fc3414-4c15-4651-8517-6f74cbe0d0fe/Type/workbook/WorkbookTemplateName/Storage%20stress%20tests
[application-insights-logging]: https://learn.microsoft.com/azure/azure-monitor/app/java-standalone-config#autocollected-logging
[logging-azure-sdk]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-in-Azure-SDK
