# General Overview of Microsoft Azure Event Processor Host for Java

Event Processor Host is built on top of the Microsoft Azure Event Hubs Client for Java and provides a number of features
not present in that lower layer:

1. Event Processor Host removes the need to write a receive loop. You simply create a Java class which
   implements the IEventProcessor interface, and Event Processor Host will call an instance of that class when
   events are available.
2. Event Processor Host removes the need to think about partitions. By default, it creates one instance of the event
   processor class for each partition. Each instance will only ever handle
   events from one partition, further simplifying the processing code. If you need a different pattern, you can
   replace the event processor factory and generate and dispense event processor instances in any way you like.
3. Event Processor Host allows easy load balancing. Utilizing a shared persistent store for leases on partitions
   (by default based on Azure Storage), instances of Event Processor Host receiving from the same consumer group
   of the same Event Hub can be spread across multiple machines and partitions will be distributed across those
   machines as evenly as possible. These instances can be started and stopped at any time, and partitions will be
   redistributed as needed. It is even allowed to have more instances than partitions as a form of hot standby. (Note that
   partition distribution is based solely on the number of partitions per instance, not event flow rate or any other metric.)
4. Event Processor Host allows the event processor to create a persistent "checkpoint" that describes a position in
   the partition's event stream, and if restarted it automatically begins receiving at the next event after the checkpoint.
   Because checkpointing is usually an expensive operation, it is up to your IEventProcessor implementation to create
   them, at whatever interval is suitable for your application. For example, an application with relatively
   infrequent messages might checkpoint after processing each one, whereas an application that requires high performance in
   the processing code in order to keep up with event flow might checkpoint once every hundred messages, or once
   per second.

## Using Event Processor Host

### Step 1: Implement IEventProcessor

There are four methods which need to be implemented: onOpen, onClose, onError, and onEvents.
onOpen and onClose are called when an event processor instance is created and shut down, respectively, and are intended for setup
and cleanup. For example, an onOpen implementation might open a database connection, and then close it in onClose. onError is called when
an error tied to the partition, such as a receiver failure, has occurred. Recovering from the error, if possible, is up to
Event Processor Host; the call to onError is informational. If it is not possible to recover from the error and the event
processor instance must be shut down, onClose will be called to allow graceful cleanup.

The onEvents method is where the real work of processing
events occurs: whenever additional events become available for the partition, this method will be called with a batch of events.
The maximum number of events in a batch can be controlled by an option when the event processor class is registered, described below,
and defaults to 10; the actual number of events in a particular batch will vary between 1 and the specified maximum. onEvents may also
be called with an empty iterable on receive timeout, if an option is set when the event processor class is registered, but by default will not.
Note that if onEvents throws an exception out to the calling code before processing all events in the iterable, it loses the opportunity to
process the remaining events. We strongly recommend having a try-catch inside the loop which iterates over the events.

By default, any particular instance of the event processor is permanently associated with a partition. A PartitionContext
object is provided to every call, but the partition id will never change from call to call. If you are using a non-default event processor
factory to implement a different pattern, such as one where an event processor instance can handle events from multiple partitions,
then the PartitionContext becomes more meaningful.

PartitionContext also provides the means to create a checkpoint for the partition. The code snippet below checkpoints after
processing every event, for the purpose of providing an example. Because checkpointing is usually an expensive operation, this
pattern is not appropriate for every application.

```Java
class EventProcessor implements IEventProcessor
{
    @Override
    public void onOpen(PartitionContext context) throws Exception
    {
  	    System.out.println("Partition " + context.getPartitionId() + " is opening");
    }

	@Override
    public void onClose(PartitionContext context, CloseReason reason) throws Exception
    {
        System.out.println("Partition " + context.getPartitionId() + " is closing for reason " + reason.toString());
    }
	
	@Override
	public void onError(PartitionContext context, Throwable error)
	{
        System.out.println("Partition " + context.getPartitionId() + " got error " + error.toString());
	}

	@Override
    public void onEvents(PartitionContext context, Iterable<? extends EventData> events) throws Exception
    {
        System.out.println("SAMPLE: Partition " + context.getPartitionId() + " got message batch");
        for (EventData data : events)
        {
            try
            {
                //
                // Do something useful with the event here.
                //

                // Checkpointing is asynchronous. The only way to determine success or failure is to
                // eventually wait for completion of the CompletableFuture. Doing an immediate get() is not
                // the best for performance, but it makes a simple example. Because the get() can throw,
                // it is inside the per-message try/catch.
                context.checkpoint(data).get();
            }
            catch (Exception e) // Replace with specific exceptions to catch.
            {
                // Handle the message-specific issue, or at least swallow the exception so the
                // loop can go on to process the next event. Throwing out of onEvents results in
                // skipping the entire rest of the batch.
            }
        }
    }
}
```

### Step 2: Implement the General Error Notification Handler

This is a class which implements Consumer<ExceptionReceivedEventArgs>. There is just one required method, accept, which will be
called with an argument of type ExceptionReceivedEventArgs if an error occurs which is not tied to any particular partition, or
sometimes if the error came from the event processor for that partition and therefore the state of the event processor is suspect. The
ExceptionReceivedEventArgs contains information specifying the instance of EventProcessorHost where the error occurred, the
exception, and the action being performed at the time of the error. To install this handler, an object of this class is passed
as an option when the event processor class is registered. Recovering from the error, if possible, is up to Event Processor Host; this
notification is informational.

```Java
class ErrorNotificationHandler implements Consumer<ExceptionReceivedEventArgs>
{
    @Override
    public void accept(ExceptionReceivedEventArgs t)
    {
        // Handle the notification here
    }
}
```

### Step 3: Instantiate EventProcessorHost

You will first need to build a connection string for the Event Hub. This may be conveniently done using
the ConnectionStringBuilder class provided by the Java client for Azure Event Hubs. Make sure the sasKey has listen permission.

The EventProcessorHost class itself has multiple constructors. All of them require a name for the host instance,
the path to the Event Hub, the name of the consumer
group to receive from, and the connection string for the Event Hub. The most basic constructor also requires an Azure Storage
connection string for a storage account that the built-in partition lease and checkpoint managers will use to persist these
artifacts, and the name of a container to use or create in that storage account. Other constructors add more options. The
most advanced constructor allows the user to replace the Azure Storage-based lease and checkpoint managers with user implementations
of ILeaseManager and ICheckpointManager (for example, to use Zookeeper instead of Azure Storage).

```Java
final String namespaceName = "---ServiceBusNamespaceName---";
final String eventHubName = "---EventHubName---";
final String sasKeyName = "---SharedAccessSignatureKeyName---";
final String sasKey = "---SharedAccessSignatureKey---";
ConnectionStringBuilder eventHubConnectionString = new ConnectionStringBuilder(namespaceName, eventHubName, sasKeyName, sasKey);

final String hostname = EventProcessorHost.createHostName("examplehost"); // createHostName adds a UUID to make a unique host name
final String consumerGroupName = EventHubClient.DEFAULT\_CONSUMER\_GROUP_NAME; // or any consumer group you have created
final String storageConnectionString = "---YouCanGetTheConnectionStringForAStorageAccountFromPortal---";
final String storageContainerName = "---StorageContainerName---";
EventProcessorHost host = new EventProcessorHost(hostname, eventHubName, consumerGroupName, eventHubConnectionString.toString(), storageConnectionString, storageContainerName);
```

### Step 4: Register the Event Processor Implementation to Start Processing Events

Instantiate an object of class EventProcessorOptions and call the setExceptionNotification method with an object of the class
implemented in step 2. This is also the time to modify the maximum event batch size (setMaxBatchSize) if you wish, or set other options
such as the receive timeout duration or prefetch count.

To start processing events, call registerEventProcessor with the options object and the .class of the IEventProcessor implementation
from step 1. This call returns a CompletableFuture which will complete when initialization is finished and event pumping is about to begin.
Waiting for the CompletableFuture to complete (by calling get) is important because initialization failures are detected by catching
exceptions from the get call. Many exceptions will be wrapped within a CompletionException.

The code shown here uses the default event processor factory, which will generate and dispense a new instance of the event processor class
for every partition. To use a different pattern, you would need to implement IEventProcessorFactory and pass an instance of the
implementation to EventProcessorHost.registerEventProcessorFactory. 

``` Java
EventProcessorOptions options = EventProcessorOptions.getDefaultOptions();
options.setExceptionNotification(new ErrorNotificationHandler());
try
{
    host.registerEventProcessor(EventProcessor.class, options).get();
}
catch (Exception e)
{
    System.out.print("Failure while registering: ");
    if (e instanceof CompletionException)
    {
        Throwable inner = e.getCause();
        System.out.println(inner.toString());
    }
    else
    {
        System.out.println(e.toString());
    }
}
```

### Step 5: Graceful Shutdown

When the time comes to shut down the instance of EventProcessorHost, call the unregisterEventProcessor method. This also
returns a CompletableFuture, which will complete when the event processor host has finished shutting down.

``` Java
CompletableFuture<Void> hostShutdown = host.unregisterEventProcessor();

// Do some other shutdown tasks here.

try
{
	hostShutdown.get();
}
catch (Exception e)
{
    System.out.print("Failure while shutting down: ");
    if (e instanceof CompletionException)
    {
        Throwable inner = e.getCause();
        System.out.println(inner.toString());
    }
    else
    {
        System.out.println(e.toString());
    }
}
```

## Threading Notes

Calls to the IEventProcessor methods onOpen, onEvents, and onClose are serialized for a given partition. There is no guarantee that
calls to these methods will be on any particular thread, but there will only be one call to any of these methods at a time. The onError
method does not share this guarantee. In particular, if onEvents throws an exception up to the caller, then onError will be called with
that exception. Technically onError is not running at the same time as onEvents, since onEvents has terminated by throwing, but shared data
may be in an unexpected state.

When using the default event processor factory, there is one IEventProcessor instance per partition, and each instance is permanently tied
to one partition. Under these conditions, an IEventProcessor instance is effectively single-threaded, except for onError. A user-supplied
event processor factory can implement any pattern, such as creating only one IEventProcessor instance and dispensing that instance for use
by every partition. In that example, onEvents will not receive multiple calls for a given partition at the same time, but it can be called
on multiple threads for different partitions.

## Running Tests

Event Processor Host comes with a suite of JUnit-based tests. To run these tests, you will need an event hub and an Azure Storage account.
You can create both through the Azure Portal at [portal.azure.com](http://portal.azure.com/). Once you have done that, get the
connection strings for both and place them in environment variables:

* `EVENT_HUB_CONNECTION_STRING` is the event hub connection string. The connection string needs to include a SAS rule which has send and listen permissions.
* `EPHTESTSTORAGE` is the storage account connection string.

Under src/test/java, the general test cases are in files named *Test. If you have made modifications to the code, these are the
cases to run in order to detect major breakage. There are also some test cases in Repros.java, but those are not suitable for
general use. That file preserves repro code from times when we had to mount a major investigation to get to the
bottom of a problem.

## Tracing

Event Processor Host can trace its execution for debugging and problem diagnosis, using the well-known SLF4J library. 
