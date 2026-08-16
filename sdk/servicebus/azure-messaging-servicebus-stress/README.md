# Azure Service Bus Stress test client library for Java

Contains stress tests for the Service Bus client.

## Getting started

The stress tests for the Service Bus client are developed from [azure-sdk-chaos][azure_sdk_chaos].

To learn how to develop a stress test project, first review the [Azure SDK Stress Test Wiki][azure_sdk_stress_test].

### Prerequisites

- [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Maven][maven]
- [Docker][docker]
- [Kubectl][kubectl]
- [Helm][helm]
- [Azure CLI][azure_cli]
- [Powershell 7.0+][powershell]

### Deploying stress tests

1. In a terminal, navigate to the `azure-sdk-for-java` root folder
2. Execute: `.\sdk\servicebus\azure-messaging-servicebus-stress\New-StressTestRun.ps1 -Namespace <stress test namespace>`

> NOTE: The default environment for stress test deployments is "storage". This may change depending on EngSys resources.

### Monitoring and validating stress tests

#### Azure portal

1. Navigate to: https://ms.portal.azure.com/#settings/directory
2. Select the TME directory
3. Choose the resource group
4. Open the Azure Workbook named "Azure SDK Stress Testing - storage"
5. Filter the "Namespace" by `<stress test namespace>` used to deploy the tests

Validate the events in the dashboard and examine "Container Logs" at the bottom of the dashboard.

#### Command-line

See [Deploying A Stress Test][deploy_stress_test] for more commands.

| Command | Description | 
|--|--|
| `helm list -n <stress test namespace>` | List deployed packages |
| `kubectl get pods -n <stress test namespace>` | Get stress test pods and status |
| `kubectl logs -n <stress test namespace> <stress test pod name>` | Get stress test pod logs |
| `kubectl logs -n <stress test namespace> <stress test pod name> -c <container name>`| Get logs for a specific container (ex. "sender" and "receiver") |
| `helm uninstall <stress test name> -n <stress test namespace>` | Stop and remove deployed package |

### Additional Monitoring

After deployment, monitor telemetry and SDK metrics in the stress test resource group's Application Insights. SDK metrics are available through the
[Azure OpenTelemetry Metrics plugin][azure_core_metrics_opentelemetry] dependency.

Use the dashboards in the stress test resource group to monitor AKS pods and stress test status.

For local runs, follow the [steps][enable_application_insights] to enable the Java agent for Application Insights, and start tests with the required JVM parameters.

### Logging

We use [logback.xml][logback_xml] to configure logging. By default, stress tests running on the cluster output
`INFO` level logs to the file share. The container console only saves `WARN` and `ERROR` level logs.

Follow the steps in [Stress Test File Share][stress_test_file_share] to find the file share logs.

### Configure Faults

See [Config Faults][config_faults] section for details.

## Key concepts

### Project Structure

See the [Layout][stress_test_layout] section for details.

Below is the current project structure:
```
.
├── src/                           # Test code
├── templates/                     # A directory of helm templates that will generate Kubernetes manifest files.
├── Chart.yaml                     # A YAML file containing information about the helm chart and its dependencies
├── scenarios-matrix.yaml          # A YAML file containing configuration and custom values for stress test(s)
├── Dockerfile                     # A Dockerfile for building the stress test image
├── stress-test-resources.bicep    # An Azure Bicep file for deploying stress test Azure resources
├── pom.xml
└── README.md
```

## Examples

### Add New Test Scenario

1. Add a new test class under the [`scenarios`][scenarios_folder] folder.
1. Extend [`ServiceBusScenario`][ServiceBusScenario] and implement test logic in `run()` method.
1. Configure new class as bean and use class name as its bean name.
1. Update `args` field in [`templates/job.yaml`][job_yaml] to execute the new test class.
1. Build out jar package and redeploy to cluster.

### Add New Scenario Option

We use [Spring][spring_configuration] to inject environment variables or
command-line arguments as scenario options. You can add a new scenario option in [ScenarioOptions][ScenarioOptions] with the format below:

```java
@Value("NEW_OPTION: default value")
private Type newOption;

public Type getNewOption() {
    return newOption;
}
```

It is recommended to provide a default value for the new option, as it will not have any impact
on the existing job configuration.

## Troubleshooting

### ERROR: AADSTS500571: The guest user account is disabled.

Verify that your account has access to the TME subscription.

### Stress test pod has `Error` status

There could have been an error while deploying the pod, such as logging into Azure or setting environment variables. Investigate the `init-azure-deployer` logs via Azure Workbook or the command line. On the command line:
1. Execute `kubectl logs -n <stress test namespace> <stress test pod name> -c init-azure-deployer` to explore logs.
2. If the logs are empty, there could have been startup failures. Execute `kubectl describe pod -n <stress test namespace> <stress test pod name>` to view additional pod details.

### 0/x nodes are available: x node(s) didn't match Pod's node affinity/selector.

The nodes probably do not have enough resources for the containers. A node pool of `Standard_D8ads_v6` had to be created to run the stress tests.

## Next steps

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- links -->
[azure_sdk_chaos]: https://github.com/Azure/azure-sdk-tools/blob/main/tools/stress-cluster/chaos/README.md
[azure_sdk_stress_test]: https://aka.ms/azsdk/stress
[jdk_link]: https://learn.microsoft.com/java/azure/jdk/?view=azure-java-stable
[maven]: https://maven.apache.org/
[docker]: https://docs.docker.com/get-docker/
[kubectl]: https://kubernetes.io/docs/tasks/tools/#kubectl
[helm]: https://helm.sh/docs/intro/install/
[azure_cli]: https://learn.microsoft.com/cli/azure/install-azure-cli
[powershell]: https://learn.microsoft.com/powershell/scripting/install/installing-powershell?view=powershell-7
[enable_application_insights]: https://learn.microsoft.com/azure/azure-monitor/app/java-in-process-agent#enable-azure-monitor-application-insights
[azure_core_metrics_opentelemetry]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/core/azure-core-metrics-opentelemetry
[logback_xml]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus-stress/src/main/resources/logback.xml
[stress_test_file_share]: https://github.com/Azure/azure-sdk-tools/blob/main/tools/stress-cluster/chaos/README.md#stress-test-file-share
[deploy_stress_test]: https://github.com/Azure/azure-sdk-tools/blob/main/tools/stress-cluster/chaos/README.md#deploying-a-stress-test
[config_faults]: https://github.com/Azure/azure-sdk-tools/blob/main/tools/stress-cluster/chaos/README.md#configuring-faults
[stress_test_layout]: https://github.com/Azure/azure-sdk-tools/blob/main/tools/stress-cluster/chaos/README.md#layout
[spring_configuration]: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config
[ScenarioOptions]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus-stress/src/main/java/com/azure/messaging/servicebus/stress/util/ScenarioOptions.java
[scenarios_folder]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/servicebus/azure-messaging-servicebus-stress/src/main/java/com/azure/messaging/servicebus/stress/scenarios
[ServiceBusScenario]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus-stress/src/main/java/com/azure/messaging/servicebus/stress/scenarios/ServiceBusScenario.java
[job_yaml]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus-stress/templates/job.yaml