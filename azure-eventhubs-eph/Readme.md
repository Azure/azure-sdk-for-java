#Consuming Events with the Java Event Processor Host for Azure Event Hubs

Event Processor Host is built on top of the Java client for Azure Event Hubs and provides a number of features
not present in that lower layer:

1. Event Processor Host removes the need to write a receive loop. The user simply creates a Java class which
   implements the IEventProcessor interface, and Event Processor Host will call an instance of that class when
   events are available.
2. Event Processor Host removes the need to think about partitions. It creates as many instances of the event
   processor class as are required to receive events from all partitions. Each instance will only ever handle
   events from one partition, further simplifying the processing code.
3. Event Processor Host allows easy load balancing. Utilizing a shared persistent store for leases on partitions
   (by default based on Azure Storage), instances of Event Processor Host receiving from the same consumer group
   of the same Event Hub can be spread across multiple machines and partitions will be distributed across those
   machines as evenly as possible. These instances can be started and stopped at any time, and partitions will be
   redistributed as needed. It is even allowed to have more instances than partitions as a form of hot standby. (Note that
   partition distribution is based solely on the number of partitions per instance, not event flow rate.)
4. Event Processor Host allows the event processor to create a persistent "checkpoint" that describes a position in
   the partition's event stream, and if restarted it automatically begins receiving at the next event after the checkpoint.
   Because checkpointing is usually an expensive operation, it is up to the user's event processor code to create
   them, at whatever interval is suitable for the user's application. For example, an application with relatively
   infrequent messages might checkpoint after processing each one, whereas an application that requires high performance in
   the processing code in order to keep up with event flow might checkpoint once every hundred messages, or once
   per second, etc.

##Getting Started

This library will be available from the Maven Central Repository and the other usual places. For now, it's just code in the
main branch.

##Using Event Processor Host

###Step 1: Implement IEventProcessor

There are four methods which need to be implemented: onOpen, onClose, onError, and onEvents.
onOpen and onClose are called when an event processor instance is created and shut down, respectively, and are intended for setup
and cleanup. For example, in onOpen the user might open a database connection, and then close it in onClose. onError is called when
an error tied to the partition, such as a receiver failure, has occurred. Recovering from the error, if possible, is up to
Event Processor Host; the call to onError is primarily informational. If it is not possible to recover from the error and the event
processor instance must be shut down, onClose will be called to allow graceful cleanup.

The onEvents method is where the real work of processing
events occurs: whenever additional events become available for the partition, this method will be called with a batch of events.
The maximum number of events in a batch can be controlled by an option when the event processor class is registered, described below,
and defaults to 10; the actual number of events in a particular batch will vary between 1 and the specified maximum. onEvents may also
be called with null on receive timeout, if an option is set when the event processor class is registered, but by default will not.

Any particular instance of the event processor is permanently associated with a partition. For convenience, a PartitionContext object
is provided to every call, but the partition id will never change from call to call.

PartitionContext also provides the means to create a checkpoint for the partition. The code snippet below checkpoints after
processing every event, for the purpose of providing an example. Because checkpointing is usually an expensive operation, this
pattern is not appropriate for every application.

    ``` Java
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
        public void onEvents(PartitionContext context, Iterable<EventData> event) throws Exception
        {
            System.out.println("SAMPLE: Partition " + context.getPartitionId() + " got message batch");
            for (EventData data : messages)
            {
                // Do something useful with the event here.

                context.checkpoint(data);
            }
        }
    }
    ```

###Step 2: Implement the General Error Notification Handler

This is a class which implements Consumer<ExceptionReceivedEventArgs>. There is just one required method, accept, which will be
called with an argument of type ExceptionReceivedEventArgs if an error occurs which is not tied to any particular partition. The
ExceptionReceivedEventArgs contains information specifying the instance of EventProcessorHost where the error occurred, the
exception, and the action being performed at the time of the error. To install this handler, an object of this class is passed
as an option when the event processor class is registered. Recovering from the error, if possible, is up to Event Processor Host; this
notification is primarily informational.

    ``` Java
    class ErrorNotificationHandler implements Consumer<ExceptionReceivedEventArgs>
    {
        @Override
        public void accept(ExceptionReceivedEventArgs t)
        {
            // Handle the notification here
        }
    }
    ```

###Step 3: Instantiate EventProcessorHost

In order to do this, the user will first need to build a connection string for the Event Hub. This may be conveniently done using
the ConnectionStringBuilder class provided by the Java client for Azure Event Hubs.

The EventProcessorHost class itself has four constructors. All of them require the path to the Event Hub, the name of the consumer
group to receive from, and the connection string for the Event Hub. The most basic constructor also requires an Azure Storage
connection string for a storage account that the built-in partition lease and checkpoint managers will use to persist these
artifacts. The next simplest constructor allows the caller to control the host name of the EventProcessorHost instance, which is
a string that uniquely identifies the instance among all EventProcessorHosts receiving from the same Event Hub and consumer group.
The EventProcessorHost.createHostName method appends a UUID to a user-supplied string, providing a convenient way to guarantee
uniqueness. The third constructor allows the caller to also control the name of the Azure Storage container used by the
partition lease and checkpoint managers, which is otherwise assembled by combining the Event Hub and consumer group names. The
final constructor is the most advanced, allowing the user to replace the lease and checkpoint managers with user implementations
of ILeaseManager and ICheckpointManager (for example, to use Zookeeper instead of Azure Storage), and hence is lacking the
Azure Storage-related arguments.

    ``` Java
    ConnectionStringBuilder eventHubConnectionString = new ConnectionStringBuilder(namespaceName, eventHubName, sasKeyName, sasKey);
    EventProcessorHost host = new EventProcessorHost(eventHubName, consumerGroupName, eventHubConnectionString.toString(), storageConnectionString);
    ```

###Step 4: Register the Event Processor Implementation to Start Processing Events

Instantiate an object of class EventProcessorOptions and call the setExceptionNotification method with an object of the class
implemented in step 2. This is also the time to modify the maximum event batch size (setMaxBatchSize), or set other options
such as the receive timeout duration or prefetch count.

To start processing events, call registerEventProcessor with the options object and the .class of the IEventProcessor implementation
from step 1. This call returns a Future which will complete when initialization is finished and event pumping is about to begin.
Waiting for the Future to complete (by calling get) is important because initialization failures are detected by catching
ExecutionException from the get call. The actual failure is available as the inner exception on the ExecutionException.

    ``` Java
    EventProcessorOptions options = new EventProcessorOptions();
    options.setExceptionNotification(new ErrorNotificationHandler());
    try
    {
        host.registerEventProcessor(EventProcessor.class, options).get();
    }
    catch (Exception e)
    {
        System.out.print("Failure while registering: ");
        if (e instanceof ExecutionException)
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

###Step 5: Graceful Shutdown

When the time comes to shut down the instance of EventProcessorHost, call the unregisterEventProcessor method.

    ``` Java
    host.unregisterEventProcessor();
    ```

If the entire process is shutting down and will never need a new instance of EventProcessorHost, then it is also time to shut
down EventProcessorHost's internal thread pool. (This can also be done automatically, but the automatic option should only be
used if there is no possibility of creating a new EventProcessorHost after all existing instances have been shut down. The
manual method is safer and hence is the default.) The EventProcessorHost.forceExecutorShutdown method takes one argument, the
number of seconds to wait for all threads in the pool to exit. After calling unregisterEventProcessor on all instances of
EventProcessorHost, all threads in the pool should have exited anyway, so the timeout can be relatively short. The 120 seconds
shown here is very, very conservative.

    ``` Java
    EventProcessorHost.forceExecutorShutdown(120);
    ```

