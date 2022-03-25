# RequestResponseChannel

[`RequestResponseChannel`][RequestResponseChannel] represents a bidirectional link between the message broker and the client. It allows client to send a request to the broker and receive the associated response. 

In AMQP project, there are two channels leverage [`RequestResponseChannel`][RequestResponseChannel] for communication:

- [`ClaimsBasedSecurityChannel`][ClaimsBasedSecurityChannel] create [`RequestResponseChannel`][RequestResponseChannel] to send and receive authorization messages.
-  [`ManagementChannel`][ManagementChannel] create [`RequestResponseChannel`][RequestResponseChannel] to perform management and metadata operations.

## Closing Flow 

When the client is going to close, [`ReactorConnection`][ReactorConnection] will emit the shutdown signal, which can close [`RequestResponseChannel`][RequestResponseChannel]. 

Below diagram shows the closing flow of [`RequestResponseChannel`][RequestResponseChannel]: 

![RequestResponseChannel Closing Flow][Closing Flow]

Notes that:

- Lines in different color represent for different threads.

- Some steps related to the closing of [`ReactorSender`][ReactorSender]/[`ReactorReceiver`][ReactorReceiver]/[`ReactorSession`][ReactorSession] are not shown here. 

<!-- Links -->
[RequestResponseChannel]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-amqp/src/main/java/com/azure/core/amqp/implementation/RequestResponseChannel.java
[ClaimsBasedSecurityChannel]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-amqp/src/main/java/com/azure/core/amqp/implementation/ClaimsBasedSecurityChannel.java
[ManagementChannel]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-amqp/src/main/java/com/azure/core/amqp/implementation/ManagementChannel.java
[ReactorConnection]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-amqp/src/main/java/com/azure/core/amqp/implementation/ReactorConnection.java
[ReactorSender]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-amqp/src/main/java/com/azure/core/amqp/implementation/ReactorSender.java
[ReactorReceiver]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-amqp/src/main/java/com/azure/core/amqp/implementation/ReactorReceiver.java
[ReactorSession]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-amqp/src/main/java/com/azure/core/amqp/implementation/ReactorSession.java
[Closing Flow]: ./request-response-channel-close.jpg
