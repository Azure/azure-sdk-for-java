# Azure Service Bus Stress test client library for Java

Represents stress tests for Service Bus client.

## Getting started

The stress tests for service bus client is developed from [azure-sdk-chaos][azure_sdk_chaos].

To know how to develop a stress test project, you should first go through the [Azure SDK Stress Test Wiki][azure_sdk_stress_test].

### Prerequisites

- [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Maven][maven]
- [Docker][docker]
- [Kubectl][kubectl]
- [Helm][helm]
- [Azure CLI][azure_cli]
- [Powershell 7.0+][powershell]

### Deploy Stress Test

Cd into `azure-sdk-for-java` root folder and run command to deploy the package to cluster:

```shell
.\eng\common\scripts\stress-testing\deploy-stress-tests.ps1 -SearchDirectory .\sdk\servicebus\
``` 

### Validate Status

Only the most frequently used commands are listed below. See [Deploying A Stress Test][deploy_stress_test] for more details.

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
# Note that we may define multiple containers (for example, sender and receiver)
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

### Monitoring

After the stress test is deployed on the cluster, we can monitor the telemetry data on the application insights which is 
inside the stress test resource group.

The SDK metrics can also be monitored on application insights as we have imported 
[Azure OpenTelemetry Metrics plugin][azure_core_metrics_opentelemetry] as project dependency.

There are several dashboards within the stress test resource group that we can use to monitor the AKS pod and stress test status.

If you want do the local test and enable application insights, you can follow the [steps][enable_application_insights] to set the java agent.
Make sure you have added the JVM parameters when you start the test.


### Logging

We use [logback.xml][logback_xml] to configure the logging. By default, the stress test run on cluster will output 
`INFO` level log to the file share. The container console only save the `WARN` and `ERROR` level log.

Follow the steps in [Stress Test File Share][stress_test_file_share] to find the file share logs.

### Configure Faults

See [Config Faults][config_faults] section for details.

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

### Cluster Namespace 
The cluster namespace is defined in `Chart.yaml`. The default value we set is `java-sb`.

```yaml
name: <stress test name>
...
annotations:
  namespace: <stress test namespace>
```

For local deployment with script, if the namespace option is not specified, the value will be overridden by the shell username.

```shell
..\..\..\eng\common\scripts\stress-testing\deploy-stress-tests.ps1 -Namespace <stress test namespace>
```

## Examples

### Test Queue/Topic

You can switch to test Queue or Topic by providing the program argument `--SERVICEBUS_ENTITY_TYPE=QUEUE/TOPIC`. 

If you haven't provided, the default value we are using is `QUEUE`.

### Add New Test Scenario

Add a new test class under `\scenarios`.

Extend `ServiceBusScenario` and implement test logic in `run()` method.

Configure new class as bean and use class name as its bean name.

Update `args` field in `job.yaml` to execute the new test class.

Build out jar package and redeploy to cluster.

### Add New Scenario Option

We use [Spring][spring_configuration] to inject environment variable or
command line arguments as the scenario options.

You can add new scenario option in [ScenarioOptions][ScenarioOptions] with below format:

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
[enable_application_insights]: https://learn.microsoft.com/azure/azure-monitor/app/java-in-process-agent#enable-azure-monitor-application-insights
[azure_core_metrics_opentelemetry]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/core/azure-core-metrics-opentelemetry
[logback_xml]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus-stress/src/main/resources/logback.xml
[stress_test_file_share]: https://github.com/Azure/azure-sdk-tools/blob/main/tools/stress-cluster/chaos/README.md#stress-test-file-share
[deploy_stress_test]: https://github.com/Azure/azure-sdk-tools/blob/main/tools/stress-cluster/chaos/README.md#deploying-a-stress-test
[config_faults]: https://github.com/Azure/azure-sdk-tools/blob/main/tools/stress-cluster/chaos/README.md#configuring-faults
[stress_test_layout]: https://github.com/Azure/azure-sdk-tools/blob/main/tools/stress-cluster/chaos/README.md#layout
[spring_configuration]: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config
[ScenarioOptions]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus-stress/src/main/java/com/azure/messaging/servicebus/stress/util/ScenarioOptions.java
