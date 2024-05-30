# Stress tests for Azure <Name> client library for Java

This package contains template project for stress tests and recommendations on how to create them for your library.

## Getting started

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
./eng/common/scripts/stress-testing/deploy-stress-tests.ps1 -SearchDirectory ./sdk/<your service directory>
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

To get readable metadata for pods and/or containers use 

```shell
kubectl describe pod -n <stress test namespace> <stress test pod name>  -c <container-name>
```

Get stress test pod logs:

```shell
kubectl logs -n <stress test namespace> <stress test pod name>
# Note that we may define multiple containers (for example, `fault-injector` and `main`)
kubectl logs -n <stress test namespace> <stress test pod name> -c <container name>
```

If stress test pod is in `Error` status, check logs from containers:

```shell
kubectl logs -n <stress test namespace> <stress test pod name>
```

You may also get logs for specific containers:

```shell
kubectl logs -n <stress test namespace> <stress test pod name> -c <container-name>
```

Stop and remove deployed package:

```shell
helm uninstall <stress test name> -n <stress test namespace>
```

### Other useful commands

Execute commands in the container:

```shell
kubectl exec --stdin --tty -n <stress test namespace> <pod name> -c <container name> -- /bin/bash
````

### Share data from within the container

Stress containers run with `$DEBUG_SHARE` environment variable set to the location of the shared folder. You can put anything you want to share there and access it - check out https://aka.ms/azsdk/stress/fileshare.

## Key concepts

### Project Structure

See [Layout][stress_test_layout] section for details.

Below is the current structure of project:
```
.
├── src/                           # Test code
├── templates/                     # A directory of helm templates that will generate Kubernetes manifest files.
├── workbooks/                     # A directory of Azure Monitor workbooks for analyzing stress test results.
├── Chart.yaml                     # A YAML file containing information about the helm chart and its dependencies
├── scenarios-matrix.yaml          # A YAML file containing configuration and custom values for stress test(s)
├── Dockerfile                     # A Dockerfile for building the stress test image
├── stress-test-resources.bicep    # An Azure Bicep for deploying stress test azure resources
├── pom.xml
└── README.md
```

### How to create your own tests

Start with [Azure SDK stress Wiki](https://aka.ms/azsdk/stress) to learn about stress tests.

1. Copy `src/main/java/com/azure/sdk/template/azure-template-stress` folder to your service folder.
2. Update the code
  - Update `pom.xml` to change artifact name and add dependencies on your service.
  - Implement your first stress test instead of `HttpGet` and make sure to update `StressTestOptions` to include important parameters for your tests.

Now you can run stress tests locally. Remaining steps are required to run tests on a stress cluster. 
  
3. Update `dockerfiles` to build your service artifacts and any dependencies of current version.
4. Describe Azure resources necessary for your tests in `stress-test-resources.bicep`
5. Update `Chart.yaml`:
   - change chart `name` to include your service name. Please keep `java-` prefix.
   - change `annotations.stressTest` to `true` to enable auto-discovery 
5. Update `templates/job.yaml`
   - remove `server` container as you probably don't need it 
   - replace occurrences of `java-template` to match name in the `Chart.yaml`
   - update test parameters for `test` container, feel free to rename the container as you see fit
6. Define scenarios and parameters in `scenarios-matrix.yaml`

Now you're ready to run tests with `./eng/common/scripts/stress-testing/deploy-stress-tests.ps1 -SearchDirectory ./sdk/<your service directory>`.
See [Deploying A Stress Test][deploy_stress_test] for more details.

Let's see how we can check test results.

### Checking test results

#### Stress Test Dashboard

General-purpose stress test dashboard is available at https://aka.ms/azsdk/stress/dashboard. It shows:
- Pod status events
- CPU and memory utilization of the stress test pods
- Container logs and events

Stress test dashboard does not know about local stress test runs.

#### Application Insights

Stress test template comes with OpenTelemetry and rich monitoring experience including:
- resource utilization metrics (CPU, memory, GC, threads, etc.)
- live metrics, performance overview, etc
- distributed tracing and dependency calls (HTTP, Azure SDK calls)
- exceptions and logs
- profiling in production

The telemetry is sent to Application Insights where it's useful to:
- monitor and compare throughput and latency across runs
- investigate issues and find bottlenecks

Application Insights is available for local runs (as long as you provide `APPLICATIONINSIGHTS_CONNECTION_STRING` environment variable).

You may choose to use [ApplicationInsights Java agent](https://learn.microsoft.com/azure/azure-monitor/app/opentelemetry-enable?tabs=java#install-the-client-library) if
your test throughput (and amount of telemetry it generates) is relatively low.
Since agent does a lot og things, it might create some noise during performance analysis and micro-optimizations.   

### Logging

We use [logback.xml][logback_xml] to configure the logging. By default, the stress test run on cluster will output
`WARN` level log which you may adjust based on your needs.
You may also control the verbosity of logs that go to Application Insights - see [OpenTelemetry logback appender][opentelemetry-logback] for more details.

Since logs are hard to query and are extremely verbose (in case of high-scale stress tests), we're relying on metrics and workbooks for test result analysis.

See also [Logging in Azure SDK][logging-azure-sdk].

### Metrics

While some Azure SDKs provide custom metrics, we're going to collect generic test metrics and build queries/workbooks on top of them,
so it's important to reuse the same metric across different tests whenever possible.

We need just one generic metric for basic analysis - the one that measures duration of one test execution (with additional dimensions).
It's implemented in `com.azure.sdk.template.stress.util.TelemetryHelper` and has the following semantic:
- name: `test.run.duration` - it is used in the stress workbook, so make sure to use the same name when applicable
- unit: seconds
- customDimensions:
    - `error.type` - The low-cardinality type of error describing what happened (eg. exception class name).

The metric should measure exactly one test operation, so we'll be able to derive the key performance indicators from it such as:
- throughput (rate of operations per period of time)
- duration of one operation
- error rate (how frequently errors of different types occur)

Each metric collected with OpenTelemetry (and exported to Application Insights) also has the following dimensions:
- `cloud_RoleName` - in case of stress tests, it matches value of `otel.service.name` property configured in `Chart.yaml` to `{{ .Release.Name }}-{{ .Stress.BaseName }}`.
- `cloud_RoleInstance` - in case of k8s it matches pod name and is auto-detected.

When running multiple test containers, make sure to assign different role instances to them, for example use `{{ .Stress.BaseName }}-consumer` and `{{ .Stress.BaseName }}-producer`. 
This would allow you to distinguish telemetry coming from different containers.  

You would need to adjust the workbook to accommodate those changes.

In addition to `test.run_duration`, we're also collecting:
- [JVM metrics](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/instrumentation/runtime-telemetry/runtime-telemetry-java8/library/README.md) measured by OpenTelemetry:
    - CPU and memory usage
    - GC stats
    - Thread count
    - Class stats
    - See [JVM metrics semantic conventions for the details](https://github.com/open-telemetry/semantic-conventions/blob/main/docs/runtime/jvm-metrics.md)

You can also enable [reactor schedulers metrics](https://github.com/reactor/reactor-core/blob/962aeb77a09088fa2a7bac6d814c2b35220b1d35/docs/modules/ROOT/pages/metrics.adoc) collection by installing `micrometer-core` and
[OpenTelemetry micrometer bridge](https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/instrumentation/micrometer/micrometer-1.5/library).

### Stress test workbook

[Stress test workbook](https://ms.portal.azure.com/#@microsoft.onmicrosoft.com/resource/subscriptions/faa080af-c1d8-40ad-9cce-e1a450ca5b57/resourceGroups/rg-stress-cluster-pg/providers/Microsoft.Insights/components/stress-pg-ai-s7b6dif73rup6/workbooks)
shows a summary of a test run.

First, select a time range and run from the list, then check the report:
- `Test summary` contains key test parameters and key counters (total number of operations, errors, etc.)
- Tst operation success rate, latency and error rate
- CPU and memory utilization, number of threads and time spent in GC
- Warnings, errors, and exceptions in logs. Note logs and traces are sampled (at 1%) rate, so you won't see every error there

Since you're changing the chart name, you would need to update the workbook to use `java-your-service-name` instead of `java-template`.
Then you'd need to create a new workbook for your service, follow
[Azure Monitor workbook documentation](https://learn.microsoft.com/azure/azure-monitor/visualize/workbooks-create-workbook) for more details.
Then you can import json file from `workbooks` folder.

## Writing useful tests

Stress tests are intended to detect reliability and resiliency issues:
- bugs in retry policy
- graceful degradation under high load and transient failures
- memory and connection leaks, thread pool starvation, etc

To explore fault injection options, check out [Chaos mesh](https://github.com/Azure/azure-sdk-tools/blob/main/tools/stress-cluster/chaos/README.md#chaos-manifest) and [Http Fault injector](https://github.com/Azure/azure-sdk-tools/tree/main/tools/http-fault-injector).

> Note: [Azure Chaos Studio](https://azure.microsoft.com/products/chaos-studio) is not currently supported by the stress test infra.

Even without fault injection, by applying maximum load to the service, we can detect memory leaks, extensive allocations,
thread pool issues, or other performance issues in the code. So make sure to configure resource limits and apply the maximum load you can get under them. 

<!-- links -->
[azure_sdk_stress_test]: https://aka.ms/azsdk/stress
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[maven]: https://maven.apache.org/
[docker]: https://docs.docker.com/get-docker/
[kubectl]: https://kubernetes.io/docs/tasks/tools/#kubectl
[helm]: https://helm.sh/docs/intro/install/
[azure_cli]: https://docs.microsoft.com/cli/azure/install-azure-cli
[powershell]: https://docs.microsoft.com/powershell/scripting/install/installing-powershell?view=powershell-7
[enable_application_insights]: https://learn.microsoft.com/en-us/azure/azure-monitor/app/opentelemetry-enable?tabs=java#enable-azure-monitor-application-insights
[logback_xml]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus-stress/src/main/resources/logback.xml
[deploy_stress_test]: https://github.com/Azure/azure-sdk-tools/blob/main/tools/stress-cluster/chaos/README.md#deploying-a-stress-test
[stress_test_layout]: https://github.com/Azure/azure-sdk-tools/blob/main/tools/stress-cluster/chaos/README.md#layout
[opentelemetry-logback]: https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/instrumentation/logback/logback-appender-1.0/library
[logging-azure-sdk]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-in-Azure-SDK
