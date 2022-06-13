# Azure Service Bus Stress test client library for Java

Represents stress tests for Service Bus client.

## Getting started

The stress tests for service bus client is developed from [azure-sdk-chaos][azure_sdk_chaos]. 

Follow the same startup steps in [Event Hubs Stress Test - Getting started][event_hubs_stress_test_start].

## Key concepts

### Project Structure

See [Layout][stress_test_layout] section for details. 

Below is the current structure of project:
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

### Cluster Namespace 

Similar with concepts in [Event Hubs Stress Test - Cluster Namespace][event_hubs_stress_test_namespace].

For Service Bus, the default value we set is `java-sb`.


## Examples

### Test Queue/Topic

You can switch to test Queue or Topic by providing the program argument `--SERVICEBUS_ENTITY_TYPE=QUEUE/TOPIC`. 

If you haven't provided, the default value we are using is `QUEUE`.

### Update MessageSender

Change `SEND_TIMES` and `MESSAGE_NUMBER` to the number of messages you want to send.

### Update MessageReceiverSync

Change `MAX_MESSAGE_COUNT` to the number you want to receive.

### Add New Test Scenario

See steps in [Event Hub Stress Test - Add New Test Scenario][event_hubs_stress_test_add_test].

The difference is that here we extend `ServiceBusScenarios` for new test scenarios.

### Add New Scenario Option

See steps in [Event Hub Stress Test - Add New Scenario Option][event_hubs_stress_test_add_option].

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
[stress_test_layout]: https://github.com/Azure/azure-sdk-tools/blob/main/tools/stress-cluster/chaos/README.md#layout
[event_hubs_stress_test_start]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventhubs/azure-messaging-eventhubs-stress#getting-started
[event_hubs_stress_test_namespace]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventhubs/azure-messaging-eventhubs-stress#cluster-namespace
[event_hubs_stress_test_add_test]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventhubs/azure-messaging-eventhubs-stress#add-new-test-scenario
[event_hubs_stress_test_add_option]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/eventhubs/azure-messaging-eventhubs-stress#add-new-scenario-option
