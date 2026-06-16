# Azure Event Hubs Stress test client library for Java

Represents stress tests for Event Hubs client.

## Getting started

The stress tests for the Event Hubs client are developed from [azure-sdk-chaos][azure_sdk_chaos].

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
2. Execute: `.\sdk\eventhubs\azure-messaging-eventhubs-stress\New-StressTestRun.ps1 -Namespace <stress test namespace>`
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

See [Layout][stress_test_layout] section for details. 

Below is the current structure of project:
```
.
├── src/                         # Test code
├── templates/                   # A directory of helm templates that will generate Kubernetes manifest files.
├── Chart.yaml                   # A YAML file containing information about the helm chart and its dependencies
├── scenarios-matrix.yaml        # A YAML file containing configuration and custom values for stress test(s)
├── Dockerfile                   # A Dockerfile for building the stress test image
├── stress-test-resources.bicep  # An Azure Bicep for deploying stress test azure resources
├── pom.xml
└── README.md
```

## Examples

### Configuring EventSender

Change `SEND_TIMES` and `SEND_EVENTS` to the number of events you want to send.

### Configuring EventProcessor for different receiver scenarios

Modify [`job.yaml`][job_yaml] to run stress test in different scenarios.

<details>
<summary>Scenario: Receiving events without checkpointing</summary>

```yaml
    - name: receiver
      image: {{ .Values.image }}
      imagePullPolicy: Always
      command: ['sh', '-c']
      args:
          - |
              set -a &&
              source $ENV_FILE &&
              java -javaagent:BOOT-INF/classes/applicationinsights-agent-3.4.1.jar \
              "org.springframework.boot.loader.JarLauncher" \
              --TEST_CLASS=EventProcessor --ENABLE_CHECKPOINT=false
      {{- include "stress-test-addons.container-env" . | nindent 6 }}
```
</details>

<details>
<summary>Scenario: Receiving and forwarding events to another Event Hub</summary>

The EventProcessor receives events, checkpoints them, and then publishes them to another Event Hub, `eventHubBeta`.

```yaml
    - name: receiver
      image: {{ .Values.image }}
      imagePullPolicy: Always
      command: ['sh', '-c']
      args:
        - |
          set -a &&
          source $ENV_FILE &&
          java -javaagent:BOOT-INF/classes/applicationinsights-agent-3.4.1.jar \
          "org.springframework.boot.loader.JarLauncher" \
          --TEST_CLASS=EventForwarder --FORWARD_EVENT_HUB_NAME=eventHubBeta
      {{- include "stress-test-addons.container-env" . | nindent 6 }}
```
</details>
<details>
<summary>Scenario: Receiving from multiple EventProcessors</summary>

```yaml
    - name: receiver1
      image: {{ .Values.image }}
      imagePullPolicy: Always
      command: ['sh', '-c']
      args:
        - |
          set -a &&
          source $ENV_FILE &&
          java -javaagent:BOOT-INF/classes/applicationinsights-agent-3.4.1.jar \
          "org.springframework.boot.loader.JarLauncher" \
          --TEST_CLASS=EventProcessor
      {{- include "stress-test-addons.container-env" . | nindent 6 }}
    - name: receiver2
      image: {{ .Values.image }}
      imagePullPolicy: Always
      command: ['sh', '-c']
      args:
        - |
          set -a &&
          source $ENV_FILE &&
          java -javaagent:BOOT-INF/classes/applicationinsights-agent-3.4.1.jar \
          "org.springframework.boot.loader.JarLauncher" \
          --TEST_CLASS=EventProcessor
      {{- include "stress-test-addons.container-env" . | nindent 6 }}
    - name: receiver3
      image: {{ .Values.image }}
      imagePullPolicy: Always
      command: ['sh', '-c']
      args:
        - |
          set -a &&
          source $ENV_FILE &&
          java -javaagent:BOOT-INF/classes/applicationinsights-agent-3.4.1.jar \
          "org.springframework.boot.loader.JarLauncher" \
          --TEST_CLASS=EventProcessor
      {{- include "stress-test-addons.container-env" . | nindent 6 }}
    - name: receiver4
      image: {{ .Values.image }}
      imagePullPolicy: Always
      command: ['sh', '-c']
      args:
        - |
          set -a &&
          source $ENV_FILE &&
          java -javaagent:BOOT-INF/classes/applicationinsights-agent-3.4.1.jar \
          "org.springframework.boot.loader.JarLauncher" \
          --TEST_CLASS=EventProcessor
      {{- include "stress-test-addons.container-env" . | nindent 6 }}
```
</details>

### Add New Test Scenario

1. Add a new test class under the [`scenarios`][scenarios_folder] folder.
1. Extend [`EventHubsScenario`][EventHubsScenario] and implement test logic in `run()` method.
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
[azure_core_metrics_opentelemetry]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/core/azure-core-metrics-opentelemetry
[logback_xml]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs-stress/src/main/resources/logback.xml
[stress_test_file_share]: https://github.com/Azure/azure-sdk-tools/blob/main/tools/stress-cluster/chaos/README.md#stress-test-file-share
[enable_application_insights]: https://learn.microsoft.com/azure/azure-monitor/app/java-in-process-agent#enable-azure-monitor-application-insights
[deploy_stress_test]: https://github.com/Azure/azure-sdk-tools/blob/main/tools/stress-cluster/chaos/README.md#deploying-a-stress-test
[config_faults]: https://github.com/Azure/azure-sdk-tools/blob/main/tools/stress-cluster/chaos/README.md#configuring-faults
[stress_test_layout]: https://github.com/Azure/azure-sdk-tools/blob/main/tools/stress-cluster/chaos/README.md#layout
[spring_configuration]: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config
[ScenarioOptions]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs-stress/src/main/java/com/azure/messaging/eventhubs/stress/util/ScenarioOptions.java
[scenarios_folder]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventhubs/azure-messaging-eventhubs-stress/src/main/java/com/azure/messaging/eventhubs/stress/scenarios
[EventHubsScenario]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs-stress/src/main/java/com/azure/messaging/eventhubs/stress/scenarios/EventHubsScenario.java
[job_yaml]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs-stress/templates/job.yaml