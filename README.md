<p align="center">
  <img src="service-bus.png" alt="Microsoft Azure Relay" width="100"/>
</p>

# Microsoft Azure Service Bus Client for Java

**Please be aware that this library is currently in active development, and is not intended for production**

This is the next generation Service Bus Java client library that focuses on Queues & Topics. If you are looking for Event Hubs, follow the below link:
* [Event Hubs](https://github.com/azure/azure-event-hubs-java)

Azure Service Bus Messaging is an asynchronous messaging cloud platform that enables you to send messages between decoupled systems. Microsoft offers this feature as a service, which means that you do not need to host any of your own hardware in order to use it.

Refer to [azure.com](https://azure.microsoft.com/services/service-bus/) to learn more about Service Bus. 

## How to provide feedback

See our [Contribution Guidelines](./.github/CONTRIBUTING.md).

## Road map

- [x] Sprint 1: **Complete**

  All runtime operations for queues (not topics / subscriptions)
    * Send
    * Receive/Peeklock (without receive by sequence number)
    * Abandon
    * Deadletter
    * Defer
  
- [ ] Sprint 2: **Batch operations complete**
  * RenewLock (Request/Response)
  * ~~Batch operation  - Explicit batching only~~
  * Runtime operation only
  * Linux testing setup/investigation

- [ ] Sprint 3: **Topic/subscription support complete**
  * ~~Add topic/subscription support~~
  * Session support
    * Accept session
    * Session Receive/ReceiveBatch
	
- [ ] Sprint 4: **Retry policy complete**
  * ~~Retry policy~~
  * Receive by sequence number

- [ ] Sprint 5:
  * Add major error conditions (ex. preventing all operations that are not supported, for Ex Transaction scenarios, etc)
  * Request/Response features:
      * Add/Remove Rule
      * Browse messages and sessions
  * Scheduled messages specific API (Scheduling of messages can be done today through the queue/topic client, but this item is to add specific API's for scheduled messages)
  * OnMessage/OnSession handlers
