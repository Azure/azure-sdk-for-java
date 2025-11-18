# Azure Blob Storage Stress Tests

Represents stress tests for Azure Storage Blob client library.

## Running tests in stress infra

Check out [Azure SDK Stress Test Wiki][azure_sdk_stress_test] for general information about stress tests.

Check out [Java Stress Testing Documentation](https://msazure.visualstudio.com/One/_wiki/wikis/One.wiki/697419/Java-Stress-Testing-Documentation) for specific information about Java stress tests.


### Prerequisites

- [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Maven][maven]
- [Docker][docker]
- [Kubectl][kubectl]
- [Helm][helm]
- [Azure CLI][azure_cli]
- [Powershell 7.0+][powershell]

### Deploy Stress Test

cd into `azure-sdk-for-java` root folder and run command to deploy all packages to the cluster:

```shell
.\eng\common\scripts\stress-testing\deploy-stress-tests.ps1 -SearchDirectory .\sdk\storage
``` 

Run the following command, if you only want to deploy this package to the cluster:

```shell
.\eng\common\scripts\stress-testing\deploy-stress-tests.ps1 -SearchDirectory .\sdk\storage\azure-storage-blob-stress
```

### Check Status

Only the most frequently used commands are listed below. See [Deploying A Stress Test][deploy_stress_test] for more details.

List deployed packages:

```shell
helm list -n <stress test namespace>
```

The namespace usually matches your username and `java` for auto-deployed tests.

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

For details on using the HTTP fault injector and how it is wired into the Java stress tests, see the [Java Stress Testing documentation](https://msazure.visualstudio.com/One/_wiki/wikis/One.wiki/697419/Java-Stress-Testing-Documentation) and the [HTTP fault-injector documentation][http-fault-injector].

## Running tests locally

You can also run stress tests locally with or without fault-injection.
To run test locally from the command line, refer to the Java stress testing documentation:
- [Java Stress Testing Documentation](https://msazure.visualstudio.com/One/_wiki/wikis/One.wiki/697419/Java-Stress-Testing-Documentation)

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
     -jar .\azure-storage-blob-stress\target\azure-storage-blob-stress-1.0.0-beta.1-jar-with-dependencies.jar `
     downloadtofile --duration 10 --size 1024 --parallel 2 --warmup 0 --faults
   ```

### Checking test results

To get a super-quick idea about test results, look for a log record that looks like this:

```log
14:49:05.219 [main] INFO  c.a.s.blob.stress.DownloadToFile - {"az.sdk.message":"test finished","scenarioName":"com.azure.storage.blob.stress.DownloadToFile",
  "succeeded":468,"failed":0}
```

After the stress test is deployed on the cluster, we can monitor the progress in the Log Analytics Workspace resource inside the stress test resource group.
There are several dashboards within the stress test resource group that we can use to monitor the AKS pod and stress test status.

#### Stress Test Dashboard

General-purpose stress test dashboard is available at [Stress Test Dashboard][stress-test-dashboard]. It shows:

- Stress test pods per namespace
- Successful runs
- Duration
- Container status
- The ability to query details on each of the pods

Stress test dashboard does not know about local stress test runs.

##### Azure Monitor

Storage stress test workbook is available [here][azure-monitor] and allows to pick a specific run and see its summary:
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

Here are a few things that clearly indicate an issue:
- Content mismatch - they can be detected in following ways: 
  - workbook shows that some operations has failed with `content mismatch` status
  - you see error logs like `{"az.sdk.message":"mismatched crc"...}`. Such logs have some additional context to investigate (content length, first 1024 bytes of it).
- Throughput is noticeably lower than for previous runs with the similar parameters. This would normally correlate with latency being higher than usual. 
- CPU/memory consumption is noticeably higher than for previous runs with the similar parameters

## Key concepts

### Project Structure

See [Layout][stress_test_layout] section for details as described by the Azure SDK team.

To understand the project structure, you may also refer to the [Java Stress Testing Documentation](https://msazure.visualstudio.com/One/_wiki/wikis/One.wiki/697419/Java-Stress-Testing-Documentation?anchor=how-it-works).

<!-- links -->
[azure_sdk_stress_test]: https://aka.ms/azsdk/stress
[jdk_link]: https://learn.microsoft.com/java/azure/jdk/?view=azure-java-stable
[maven]: https://maven.apache.org/
[docker]: https://docs.docker.com/get-docker/
[kubectl]: https://kubernetes.io/docs/tasks/tools/#kubectl
[helm]: https://helm.sh/docs/intro/install/
[azure_cli]: https://learn.microsoft.com/cli/azure/install-azure-cli
[powershell]: https://learn.microsoft.com/powershell/scripting/install/installing-powershell?view=powershell-7
[enable_application_insights]: https://learn.microsoft.com/azure/azure-monitor/app/opentelemetry-enable?tabs=java#enable-azure-monitor-application-insights#enable-azure-monitor-application-insights
[logback_xml]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus-stress/src/main/resources/logback.xml
[deploy_stress_test]: https://github.com/Azure/azure-sdk-tools/blob/main/tools/stress-cluster/chaos/README.md#deploying-a-stress-test
[stress_test_layout]: https://github.com/Azure/azure-sdk-tools/blob/main/tools/stress-cluster/chaos/README.md#layout
[http-fault-injector]: https://github.com/Azure/azure-sdk-tools/tree/main/tools/http-fault-injector
[telemetry-helper]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-stress/src/main/java/com/azure/storage/stress/TelemetryHelper.java
[blob-scenario-base]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-blob-stress/src/main/java/com/azure/storage/blob/stress/BlobScenarioBase.java
[application-insights-logging]: https://learn.microsoft.com/azure/azure-monitor/app/java-standalone-config#autocollected-logging
[logging-azure-sdk]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-in-Azure-SDK
[stress-test-dashboard]: https://portal.azure.com/#@TME01.onmicrosoft.com/resource/subscriptions/4d042dc6-fe17-4698-a23f-ec6a8d1e98f4/resourceGroups/rg-stress-cluster-storage/providers/microsoft.insights/workbooks/01d047ea-1c0d-4463-98fa-4f465596401e/workbook
[azure-monitor]: https://portal.azure.com/#view/AppInsightsExtension/WorkbookViewerBlade/ComponentId/azure%20monitor/ConfigurationId/%2Fsubscriptions%2F4d042dc6-fe17-4698-a23f-ec6a8d1e98f4%2FresourceGroups%2Frg-stress-cluster-storage%2Fproviders%2FMicrosoft.Insights%2Fworkbooks%2F1acdb3e6-3135-5dff-b808-85def43fb868/WorkbookTemplateName/Azure%20SDK%20Stress%20Testing%20-%20storage/NotebookParams~/%7B%22TimeRange%22%3A%7B%22durationMs%22%3A604800000%7D%2C%22NamespaceParameter%22%3A%5B%22java%22%5D%2C%22PodUidParameter%22%3A%5B%2267d0dcea-82f1-4992-bc3d-b7ee199aaca3%22%5D%7D
