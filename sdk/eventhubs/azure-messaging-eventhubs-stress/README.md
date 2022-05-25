# Azure Event Hubs Stress test client library for Java

Represents stress tests for Event Hubs client.

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Maven][maven]
- [Docker][docker]
- [Kubectl][kubectl]
- [Helm][helm]
- [Azure CLI][azure_cli]
- [Powershell 7.0+][powershell]

Make sure you have permission to access subscription `Azure SDK Developer Playground` and resource group `rg-stress-cluster-test`.

```shell
# Authenticate to Azure
az login

# Download the kubeconfig for the cluster (creates a 'context' named 'stress-test')
az aks get-credentials --subscription "Azure SDK Developer Playground" -g rg-stress-cluster-test -n stress-test
```

### Deploy to cluster

Build out the jar package:

```shell
cd <current project path>
mvn clean install
```

Update the `namespace` field in `Chart.yaml` file. It is suggested to use your own alias to avoid conflict.

```yaml
name: <stress test name>
...
annotations:
  namespace: <stress test namespace>
```

Note: the default value of stress test namespace is `java-eh`. You can keep it when no other stress test are running.

Download project [azure-sdk-tools][azure_sdk_tools] for the deployment to stress test cluster.

Keep in current project path, run below command:

```shell
<root path>\azure-sdk-tools\eng\common\scripts\stress-testing\deploy-stress-tests.ps1 -PushImage -Login -Namespace <stress test namespace>
```

### Validate status

Get pods and jobs:

```shell
helm list -n <stress test namespace>
kubectl get pods -n <stress test namespace>
kubectl get jobs -n <stress test namespace>
```

List stress test pods:

```shell
kubectl get pods -n <stress test namespace> -l release=<stress test name>
```

Get logs from the init-azure-deployer init container, if deploying resources. Omit `-c init-azure-deployer` to get main container logs.

```shell
kubectl logs -n <stress test namespace> <stress test pod name> -c init-azure-deployer
```

If above command output is empty, there may have been startup failures:

```shell
kubectl describe pod -n <stress test namespace> <stress test pod name>
```

Get stress test logs:

```shell
kubectl logs -n <stress test namespace> <stress test pod name>
# Note that we may define multiple containers (for example, sender and receiver)
kubectl logs -n <stress test namespace> <stress test pod name> -c <container name>
```

Stop and remove deployed package:

```shell
helm uninstall <stress test name> -n <stress test namespace>
```

### Configure Faults

[Chaos Mesh](https://chaos-mesh.org/) is used to configure faults against test jobs. There are two ways for the configuration, which are via the UI or via kubernetes manifests.

#### Chaos Dashboard

Make sure you can access the chaos dashboard by running the below command, and navigating to localhost:2333 in your browser.

```shell
kubectl port-forward -n stress-infra svc/chaos-dashboard 2333:2333
```
From dashboard, click `New experiment` and choose your parameters to submit a fault experiment.

#### Chaos Manifest

See [Chaos manifest](https://github.com/Azure/azure-sdk-tools/blob/main/tools/stress-cluster/chaos/README.md#chaos-manifest) for details.


### Configure Monitor

Download [application insights java agent][java_agent_jar]. 

Place the `applicationinsights-agent-3.2.11.jar` under `src\main\resources` folder.

Add JVM parameter `-javaagent:src\main\resources\applicationinsights-agent-3.2.11.jar` for java command in `job.yaml`.
```yaml
java -javaagent:BOOT-INF/classes/applicationinsights-agent-3.2.11.jar \
"org.springframework.boot.loader.JarLauncher" \
```


Redeploy job to cluster and monitor application insights under resource group `rg-stress-cluster-test`.


## Key concepts

The stress tests for event hubs client is developed from [azure-sdk-chaos][azure_sdk_chaos].

### Project Structure

```
.
├── src/                         # Test code
├── templates/                   # A directory of helm templates that will generate Kubernetes manifest files.
├── Chart.yaml                   # A YAML file containing information about the helm chart and its dependencies
├── Dockerfile                   # A Dockerfile for building the stress test image
├── stress-test-resouce.bicep    # An Azure Bicep for deploying stress test azure resources
├── values.yaml                  # Any default helm template values for this chart, e.g. a `scenarios` list
├── pom.xml
└── README.md
```

## Examples

### Update SendEvents

Change `sendTimes` and `eventNumber` to the number of events you want to send.

### Run ProcessEventsWithOptions

Modify receiver command in `job.yaml` to run stress test in different scenarios:

Scenario 1: receiver does not checkpoint and not write to another new event hub.

```yaml
    - name: receiver
      image: {{ .Values.image }}
      imagePullPolicy: Always
      command: ['sh', '-c']
      args:
          - |
              set -a &&
              source $ENV_FILE &&
              java -javaagent:BOOT-INF/classes/applicationinsights-agent-3.2.11.jar \
              "org.springframework.boot.loader.JarLauncher" \
              --TEST_CLASS=ProcessEventsWithOptions --UPDATE_CHECKPOINT=NO --NEED_SEND_EVENT_HUB=NO
      {{- include "stress-test-addons.container-env" . | nindent 6 }}
```
Scenario 2: receiver does checkpoint and not write to another new event hub.

```yaml
    - name: receiver
      image: {{ .Values.image }}
      imagePullPolicy: Always
      command: ['sh', '-c']
      args:
        - |
          set -a &&
          source $ENV_FILE &&
          java -javaagent:BOOT-INF/classes/applicationinsights-agent-3.2.11.jar \
          "org.springframework.boot.loader.JarLauncher" \
          --TEST_CLASS=ProcessEventsWithOptions --UPDATE_CHECKPOINT=YES --NEED_SEND_EVENT_HUB=NO
      {{- include "stress-test-addons.container-env" . | nindent 6 }}
```

Scenario 3: receiver does checkpoint and write to another event hub.

```yaml
    - name: receiver
      image: {{ .Values.image }}
      imagePullPolicy: Always
      command: ['sh', '-c']
      args:
        - |
          set -a &&
          source $ENV_FILE &&
          java -javaagent:BOOT-INF/classes/applicationinsights-agent-3.2.11.jar \
          "org.springframework.boot.loader.JarLauncher" \
          --TEST_CLASS=ProcessEventsWithOptions --UPDATE_CHECKPOINT=YES --NEED_SEND_EVENT_HUB=YES
      {{- include "stress-test-addons.container-env" . | nindent 6 }}
```

Scenario 4: Four receiver does checkpoint and not write to another new event hub.

```yaml
    - name: receiver1
      image: {{ .Values.image }}
      imagePullPolicy: Always
      command: ['sh', '-c']
      args:
        - |
          set -a &&
          source $ENV_FILE &&
          java -javaagent:BOOT-INF/classes/applicationinsights-agent-3.2.11.jar \
          "org.springframework.boot.loader.JarLauncher" \
          --TEST_CLASS=ProcessEventsWithOptions --UPDATE_CHECKPOINT=YES --NEED_SEND_EVENT_HUB=NO
      {{- include "stress-test-addons.container-env" . | nindent 6 }}
    - name: receiver2
      image: {{ .Values.image }}
      imagePullPolicy: Always
      command: ['sh', '-c']
      args:
        - |
          set -a &&
          source $ENV_FILE &&
          java -javaagent:BOOT-INF/classes/applicationinsights-agent-3.2.11.jar \
          "org.springframework.boot.loader.JarLauncher" \
          --TEST_CLASS=ProcessEventsWithOptions --UPDATE_CHECKPOINT=YES --NEED_SEND_EVENT_HUB=NO
      {{- include "stress-test-addons.container-env" . | nindent 6 }}
    - name: receiver3
      image: {{ .Values.image }}
      imagePullPolicy: Always
      command: ['sh', '-c']
      args:
        - |
          set -a &&
          source $ENV_FILE &&
          java -javaagent:BOOT-INF/classes/applicationinsights-agent-3.2.11.jar \
          "org.springframework.boot.loader.JarLauncher" \
          --TEST_CLASS=ProcessEventsWithOptions --UPDATE_CHECKPOINT=YES --NEED_SEND_EVENT_HUB=NO
      {{- include "stress-test-addons.container-env" . | nindent 6 }}
    - name: receiver4
      image: {{ .Values.image }}
      imagePullPolicy: Always
      command: ['sh', '-c']
      args:
        - |
          set -a &&
          source $ENV_FILE &&
          java -javaagent:BOOT-INF/classes/applicationinsights-agent-3.2.11.jar \
          "org.springframework.boot.loader.JarLauncher" \
          --TEST_CLASS=ProcessEventsWithOptions --UPDATE_CHECKPOINT=YES --NEED_SEND_EVENT_HUB=NO
      {{- include "stress-test-addons.container-env" . | nindent 6 }}
```

### Add New Test Scenario

Add a new test class under `\scenarios`.

Extend `EventHubsScenarios` and implement test logic in `run()` method.

Configure new class as bean and use class name as its bean name.

Update `args` field in `job.yaml` to execute the new test class.

Build out jar package and redeploy to cluster.


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
[event_hubs_create]: https://docs.microsoft.com/azure/event-hubs/event-hubs-create
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[maven]: https://maven.apache.org/
[docker]: https://docs.docker.com/get-docker/
[kubectl]: https://kubernetes.io/docs/tasks/tools/#kubectl
[helm]: https://helm.sh/docs/intro/install/
[azure_cli]: https://docs.microsoft.com/cli/azure/install-azure-cli
[powershell]: https://docs.microsoft.com/powershell/scripting/install/installing-powershell?view=powershell-7 
[azure_sdk_tools]: https://github.com/Azure/azure-sdk-tools
[azure_sdk_chaos]: https://github.com/Azure/azure-sdk-tools/blob/main/tools/stress-cluster/chaos/README.md
[java_agent_jar]: https://docs.microsoft.com/azure/azure-monitor/app/java-in-process-agent
