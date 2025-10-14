# Troubleshoot Event Hubs issues

The troubleshooting guide has moved to: https://learn.microsoft.com/azure/developer/java/sdk/troubleshooting-messaging-event-hubs-overview

## Get additional help

Additional information on ways to reach out for support can be found in the [SUPPORT.md][SUPPORT] at the repo's root.

### Filing GitHub issues

When filing GitHub issues, the following details are requested:

* Event Hub environment
  * How many partitions?
* EventProcessorClient environment
  * What is the machine(s) specs processing your Event Hub?
  * How many instances are running?
  * What is the max heap set (i.e., Xmx)?
* What is the average size of each EventData?
* What is the traffic pattern like in your Event Hub?  (i.e. # messages/minute and if the EventProcessorClient is always busy or has slow traffic periods.)
* Repro code and steps
  * This is important as we often cannot reproduce the issue in our environment.
* Logs.  We need DEBUG logs, but if that is not possible, INFO at least.  Error and warning level logs do not provide enough information.  The period of at least +/- 10 minutes from when the issue occurred.

<!-- repo links -->
[SUPPORT]: https://github.com/Azure/azure-sdk-for-java/blob/main/SUPPORT.md


