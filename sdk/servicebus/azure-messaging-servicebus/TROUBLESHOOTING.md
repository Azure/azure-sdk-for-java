# Troubleshooting Service Bus issues

This troubleshooting guide covers failure investigation techniques, common errors for the credential types in the Azure Service Bus Java client library, and mitigation steps to resolve these errors.

## Table of contents
- [Implicit prefetch issue in ServiceBusReceiverClient](#implicit-prefetch-issue-in-servicebusreceiverclient)
- [Troubleshoot ServiceBusProcessorClient issues](#troubleshoot-servicebusprocessorclient-issues)
  - [Client hangs or stalls with a high prefetch and maxConcurrentCall value](#client-hangs-or-stalls-with-a-high-prefetch-and-maxconcurrentcall-value)
    - [Credit calculation issue](#credit-calculation-issue)
- [Autocomplete issue](#autocomplete-issue)
- [Migrate from legacy to new client library](#migrate-from-legacy-to-new-client-library)
- [Enable and configure logging](#enable-and-configure-logging)
    - [Configuring Log4J 2](#configuring-log4j-2)
    - [Configuring logback](#configuring-logback)
    - [Enable AMQP transport logging](#enable-amqp-transport-logging)
    - [Reduce logging](#reduce-logging)
- [Get additional help](#get-additional-help)
    - [Filing GitHub issues](#filing-github-issues)

## Implicit prefetch issue in ServiceBusReceiverClient
Even after the application disables prefetch in the client builder, the `receiveMessages` API in 
`ServiceBusReceiverClient` can re-enable prefetch implicitly, which may not be obvious.

A detailed write-up on this issue is present in the [SyncReceiveAndPrefetch][SyncReceiveAndPrefetch] document present 
in the docs folder.

## Troubleshoot ServiceBusProcessorClient issues
### Client hangs or stalls with a high prefetch and maxConcurrentCall value
`Update disposition request timed out.` exception is throw and the client stops processing new messages. 
This issue is known to occur when the number of threads in thread-pool is low or equal to the `maxConcurrentCalls` value.

<!-- cSpell:ignore Dreactor -->
Mitigation: 
* add vm option `-Dreactor.schedulers.defaultBoundedElasticSize=<large number greater than concurrency count>`
The default value of this property is `10 * number of CPU cores`. This problem is encountered more 
frequently in an Azure Kubernetes Service (AKS) environment.
* Another setting that helps ease the problem here is disabling prefetch which can be done by setting `prefetchCount(0)` when the client is created.

The reason this occurs is because of thread starvation. When all threads are utilized for message processing, the 
processing thread can't get new threads to run other tasks. And the reason for thread starvation are two-fold. 
One of them is, given that today message settlement acknowledgements and the actual messages share the same
`receiveLinkHandler`'s buffer, the backpressure from the reactor-operators-chain which is supposed only act on the 
messages, ends up making the settlement acknowledgement to stay in the buffer. The other is the problem with credit 
calculation. More information on that is in [Credit calculation issue](#credit-calculation-issue).

#### Credit calculation issue
Currently, the credits that are placed on the link are overestimated as the way we calculated the number of credits 
used is incorrect. Trying to get link Credits from multiple threads to calculate credits fail. This is something we 
are actively working on. This section will be updated in the future when there is a resolution (most likely upgrading 
to the version including the fix).

## Autocomplete issue
The way autocomplete and auto lock renewal works today in the `ServiceBusReceiverAsyncClient` and 
`ServiceBusProcessorClient` is incorrect. It makes an assumption that there are no buffers / prefetch queues on the 
customer side.

This problem also manifests itself due to thread hopping in the reactor operators, which might cause the completion to
occur out of order, resulting in incorrect message state. 

Mitigation: Use `disableAutoComplete()` and `.maxAutoLockRenewalDuration(Duration.ZERO)` to turn off the two features 
in `ServiceBusReceiverAsyncClient`. 
Especially when there is any kind of buffering involved in the message processing code path. After disabling 
AutoComplete, message settlement (completion / abandonment) should be done explicitly from the message processing code.
For more information on how to use this correctly, please refer to the [samples][Samples]

## Migrate from legacy to new client library
The [migration guide][MigrationGuide] includes steps on migrating from the legacy client and migrating legacy 
checkpoints.

## Enable and configure logging
The Azure SDK for Java offers a consistent logging story to help troubleshoot application errors and expedite their 
resolution. The logs produced will capture the flow of an application before reaching the terminal state to help locate 
the root issue.  View the [logging][Logging] wiki for guidance about enabling logging.

In addition to enabling logging, setting the log level to `VERBOSE` or `DEBUG` provides insights into the library's 
state.  Below are sample log4j2 and logback configurations to reduce the excessive messages when verbose logging is 
enabled.

### Configuring Log4J 2
1. Add the dependencies in your pom.xml using ones from the [logging sample pom.xml][LoggingPom] under the "Dependencies required for Log4j2" section.
2. Add [log4j2.xml][log4j2] to your `src/main/resources`.

### Configuring logback
1. Add the dependencies in your pom.xml using ones from the [logging sample pom.xml][LoggingPom] under the "Dependencies required for logback" section.
2. Add [logback.xml][logback] to your `src/main/resources`.

### Enable AMQP transport logging
If enabling client logging is not enough to diagnose your issues.  You can enable logging to a file in the underlying
AMQP library, [Qpid Proton-J][qpid_proton_j_apache].  Qpid Proton-J uses `java.util.logging`. You can enable logging by
creating a configuration file with the contents below.  Or set `proton.trace.level=ALL` and whichever configuration options
you want for the `java.util.logging.Handler` implementation.  The implementation classes and their options can be found in
[Java 8 SDK javadoc][java_8_sdk_javadocs].

To trace the AMQP transport frames, set the environment variable: `PN_TRACE_FRM=1`.

#### Sample "logging.properties" file
The configuration file below logs TRACE level output from proton-j to the file "proton-trace.log".

```
handlers=java.util.logging.FileHandler
.level=OFF
proton.trace.level=ALL
java.util.logging.FileHandler.level=ALL
java.util.logging.FileHandler.pattern=proton-trace.log
java.util.logging.FileHandler.formatter=java.util.logging.SimpleFormatter
java.util.logging.SimpleFormatter.format=[%1$tF %1$tr] %3$s %4$s: %5$s %n
```

### Reduce logging
One way to decrease logging is to change the verbosity.  Another is to add filters that exclude logs from logger names 
packages like `com.azure.messaging.servicebus` or `com.azure.core.amqp`.  Examples of this can be found in the XML files 
in [Configuring Log4J 2](#configuring-log4j-2) and [Configure logback](#configuring-logback).

When submitting a bug, log messages from classes in the following packages are interesting:

* `com.azure.core.amqp.implementation`
* `com.azure.core.amqp.implementation.handler`
    * The exception is that the onDelivery message in ReceiveLinkHandler can be ignored.
* `com.azure.messaging.servicebus.implementation`


## Get additional help
Additional information on ways to reach out for support can be found in the [SUPPORT.md][SUPPORT] at the repo's root.

### Filing GitHub issues
When filing GitHub issues, the following details are requested:

* Service Bus configuration / Namespace environment
    * What tier is the namespace (standard / premium)?
    * What type of messaging entity is being used (queue/topic)? and it's configuration.
    * What is the average size of each Message?
* ServiceBusReceiverClient / ServiceBusProcessorClient environment
    * What is the machine(s) specs where the client is processing messages?
    * How many instances are running?
    * What is the max heap set (i.e., Xmx)?
    * What is the configuration the client is created with?  
      * If applicable, what is the MaxConcurrentCalls value?
      * If applicable, what is the PrefetchCount value?
      * If applicable, is AutoComplete enabled or disabled?
* What is the traffic pattern like?  (i.e. # messages/minute and if the Client is always busy or has slow traffic periods.)
* Repro code and steps
    * This is important as we often cannot reproduce the issue in our environment.
* Logs.  We need DEBUG logs, but if that is not possible, INFO at least.  
Error and warning level logs do not provide enough information. The period of at least +/- 10 minutes from when the issue occurred.


[log4j2]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/servicebus/azure-messaging-servicebus/docs/log4j2.xml
[logback]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/servicebus/azure-messaging-servicebus/docs/logback.xml
[LoggingPom]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/servicebus/azure-messaging-servicebus/docs/pom.xml
[MigrationGuide]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/migration-guide.md
[SyncReceiveAndPrefetch]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/servicebus/azure-messaging-servicebus/docs/SyncReceiveAndPrefetch.md
[Samples]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/servicebus/azure-messaging-servicebus/src/samples
[SUPPORT]: https://github.com/Azure/azure-sdk-for-java/blob/main/SUPPORT.md
[Logging]: https://docs.microsoft.com/azure/developer/java/sdk/logging-overview
[java_8_sdk_javadocs]: https://docs.oracle.com/javase/8/docs/api/java/util/logging/package-summary.html
[qpid_proton_j_apache]: https://qpid.apache.org/proton/
