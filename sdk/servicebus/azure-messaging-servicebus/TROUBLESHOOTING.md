# Troubleshooting Service Bus issues

This troubleshooting guide covers failure investigation techniques, common errors for the credential types in the Azure Service Bus Java client library, and mitigation steps to resolve these errors.

## Table of contents
- [Troubleshooting Service Bus issues](#troubleshooting-service-bus-issues)
  - [Table of contents](#table-of-contents)
  - [Implicit prefetch issue in ServiceBusReceiverClient](#implicit-prefetch-issue-in-servicebusreceiverclient)
  - [Troubleshoot ServiceBusProcessorClient issues](#troubleshoot-servicebusprocessorclient-issues)
    - [Client hangs or stalls with a high prefetch and maxConcurrentCall value](#client-hangs-or-stalls-with-a-high-prefetch-and-maxconcurrentcall-value)
      - [Credit calculation issue](#credit-calculation-issue)
  - [Autocomplete issue](#autocomplete-issue)
  - [Migrate from legacy to new client library](#migrate-from-legacy-to-new-client-library)
  - [Enable and configure logging](#enable-and-configure-logging)
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

The contents have moved to: https://learn.microsoft.com/azure/developer/java/sdk/troubleshooting-messaging-service-bus-overview

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

[MigrationGuide]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/migration-guide.md
[SyncReceiveAndPrefetch]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/servicebus/azure-messaging-servicebus/docs/SyncReceiveAndPrefetch.md
[Samples]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/servicebus/azure-messaging-servicebus/src/samples
[SUPPORT]: https://github.com/Azure/azure-sdk-for-java/blob/main/SUPPORT.md

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fservicebus%2Fazure-messaging-servicebus%2FTROUBLESHOOTING.png)
