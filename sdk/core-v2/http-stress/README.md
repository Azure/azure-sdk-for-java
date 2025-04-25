# Stress tests for Azure Core v2 HTTP client library for Java

This package contains the stress test project for Azure Core v2 HTTP stack. It demonstrates how to create and run stress tests for the Azure SDK for Java core HTTP pipeline and client infrastructure.

## Getting started

Check out [Azure SDK Stress Test Wiki][azure_sdk_stress_test] for general information about stress tests.

### Prerequisites

- [Java Development Kit (JDK)][jdk_link], version 11 or later.
- [Maven][maven]
- [Docker][docker]
- [Kubectl][kubectl]
- [Helm][helm]
- [Azure CLI][azure_cli]
- [Powershell 7.0+][powershell]

### Deploy Stress Test

Change directory to the Azure SDK for Java root and deploy the package to your cluster:

```shell
./eng/common/scripts/stress-testing/deploy-stress-tests.ps1 -MatrixSelection all -SearchDirectory ./sdk/core-v2
```

### Check Status

See [Deploying A Stress Test][deploy_stress_test] for more details.

List deployed packages:

```shell
helm list -n <stress test namespace>
```

Get stress test pods and status:

```shell
kubectl get pods -n <stress test namespace>
```

Get stress test pod logs:

```shell
kubectl logs -n <stress test namespace> <stress test pod name>
```

Stop and remove deployed package:

```shell
helm uninstall <stress test name> -n <stress test namespace>
```

### Other useful commands

Execute commands in the container:

```shell
kubectl exec --stdin --tty -n <stress test namespace> <pod name> -c <container name> -- /bin/bash
```

## Key concepts

### Project Structure

```
.
├── src/                           # Test code
├── templates/                     # Helm templates for Kubernetes manifests
├── workbooks/                     # Azure Monitor workbooks for analyzing stress test results
├── Chart.yaml                     # Helm chart metadata
├── scenarios-matrix.yaml          # Configuration for stress test scenarios
├── Dockerfile                     # Dockerfile for building the stress test image
├── stress-test-resources.bicep    # Azure Bicep for deploying stress test resources
├── pom.xml
└── README.md
```

### How to create your own tests

Start with [Azure SDK stress Wiki](https://aka.ms/azsdk/stress) to learn about stress tests.

1. Copy `src/main/java/com/azure/core/http/stress` to your service folder.
2. Update the code:
   - Update `pom.xml` to change artifact name and add dependencies on your service.
   - Implement your first stress test instead of `HttpGet` and update `StressOptions` for your test parameters.

Now you can run stress tests locally. Remaining steps are required to run tests on a stress cluster.

3. Update `dockerfiles` to build your service artifacts and dependencies.
4. Describe Azure resources in `stress-test-resources.bicep`.
5. Update `Chart.yaml` and `templates/job.yaml` for your service.
6. Define scenarios and parameters in `scenarios-matrix.yaml`.

Now you're ready to run tests with `./eng/common/scripts/stress-testing/deploy-stress-tests.ps1 -SearchDirectory ./sdk/core-v2`.

### Checking test results

See [Stress Test Dashboard](https://aka.ms/azsdk/stress/dashboard) and [Application Insights](https://learn.microsoft.com/azure/azure-monitor/app/opentelemetry-enable?tabs=java#install-the-client-library) for monitoring and telemetry.

### Logging and Metrics

- Logging is configured via `logback.xml`.
- Metrics are collected using OpenTelemetry and exported to Application Insights.
- The main metric is `test.run.duration` (seconds), implemented in `com.azure.core.http.stress.util.TelemetryHelper`.

### Example: Running a Stress Test

```java readme-sample-runStressTest
public class RunStressTest {
    public static void main(String[] args) {
        com.azure.core.http.stress.App.main(args);
    }
}
```

## Writing useful tests

Stress tests are intended to detect reliability and resiliency issues, such as retry policy bugs, resource leaks, and performance bottlenecks. For fault injection, see [Chaos mesh](https://github.com/Azure/azure-sdk-tools/blob/main/tools/stress-cluster/chaos/README.md#chaos-manifest).

<!-- links -->
[azure_sdk_stress_test]: https://aka.ms/azsdk/stress
[jdk_link]: https://learn.microsoft.com/java/azure/jdk/?view=azure-java-stable
[maven]: https://maven.apache.org/
[docker]: https://docs.docker.com/get-docker/
[kubectl]: https://kubernetes.io/docs/tasks/tools/#kubectl
[helm]: https://helm.sh/docs/intro/install/
[azure_cli]: https://learn.microsoft.com/cli/azure/install-azure-cli
[powershell]: https://learn.microsoft.com/powershell/scripting/install/installing-powershell?view=powershell-7
[deploy_stress_test]: https://github.com/Azure/azure-sdk-tools/blob/main/tools/stress-cluster/chaos/README.md#deploying-a-stress-test
