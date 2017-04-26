<p align="center">
  <img src="service-bus.png" alt="Microsoft Azure Relay" width="100"/>
</p>

# Microsoft Azure Service Bus Client for Java

**Please be aware that this library is currently in active development, and is not intended for production**

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.microsoft.azure/azure-servicebus/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.microsoft.azure/azure-servicebus)

This is the next generation Service Bus Java client library that focuses on Queues & Topics. If you are looking for Event Hubs, follow this [link](https://github.com/azure/azure-event-hubs-java).

Azure Service Bus is an asynchronous messaging cloud platform that enables you to send messages between decoupled systems. Microsoft offers this feature as a service, which means that you do not need to host any of your own hardware in order to use it.

Refer to [azure.com](https://azure.microsoft.com/services/service-bus/) to learn more about Service Bus. 

## How to provide feedback

See our [Contribution Guidelines](./.github/CONTRIBUTING.md).

## FAQ

### Where can I find examples that use this library?

To get started *sending* messages to Service Bus refer to [Get started sending to Service Bus queues](https://github.com/Azure/azure-service-bus/blob/master/samples/Java/src/main/java/com/microsoft/azure/servicebus/samples/SendSample.java).

To get started *receiving* messages with Service Bus refer to [Get started receiving from Service Bus queues](https://github.com/Azure/azure-service-bus/blob/master/samples/Java/src/main/java/com/microsoft/azure/servicebus/samples/ReceiveSample.java).  

### Can I manage Service Bus entities with this library?

The standard way to manage Azure resources is by using [Azure Resource Manager](https://docs.microsoft.com/en-us/azure/azure-resource-manager/resource-group-overview). In order to use functionality that previously existed in the azure-servicebus Java library, there will be a new Java specific library before this library becomes generally available. This will enable use cases that dynamically create/read/update/delete resources, and will be similar to the currently available [.NET management library](https://www.nuget.org/packages/Microsoft.WindowsAzure.Management.ServiceBus/).