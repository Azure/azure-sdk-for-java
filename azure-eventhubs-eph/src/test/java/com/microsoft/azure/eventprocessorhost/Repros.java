package com.microsoft.azure.eventprocessorhost;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.eventhubs.*;
import org.junit.Test;

public class Repros extends TestBase
{
	/*
	 * This class exists to preserve repro code for specific problems we have had to debug/fix. The
	 * cases here are not really useful/usable as general-purpose tests (most of them run infinitely,
	 * for example), so they are not marked as JUnit cases by default.
	 */

	/*
	 * Two instances of EventProcessorHost with the same host name is not a valid configuration.
	 * Since lease ownership is determined by host name, they will both believe that they own all
	 * the partitions and constantly be recreating receivers and knocking the other one off. Don't
	 * do this. This repro exists because another test case did this scenario by accident and saw
	 * massive memory leaks, so I recreated it deliberately to find out what was going on.
	 */
	//@Test
	public void conflictingHosts() throws Exception
	{
		System.out.println("conflictingHosts starting");
		
		RealEventHubUtilities utils = new RealEventHubUtilities();
		utils.setup(RealEventHubUtilities.QUERY_ENTITY_FOR_PARTITIONS);
		
		String telltale = "conflictingHosts-telltale-" + EventProcessorHost.safeCreateUUID();
		String conflictingName = "conflictingHosts-NOTSAFE";
		String storageName = conflictingName.toLowerCase() + EventProcessorHost.safeCreateUUID();
		PrefabEventProcessor.CheckpointChoices doCheckpointing = PrefabEventProcessor.CheckpointChoices.CKP_NONE;
		boolean doMarker = false;
		
		PrefabGeneralErrorHandler general1 = new PrefabGeneralErrorHandler();
		PrefabProcessorFactory factory1 = new PrefabProcessorFactory(telltale, doCheckpointing, doMarker);
		EventProcessorHost host1 = new EventProcessorHost(conflictingName, utils.getConnectionString().getEventHubName(),
				utils.getConsumerGroup(), utils.getConnectionString().toString(),
				TestUtilities.getStorageConnectionString(), storageName);
		EventProcessorOptions options1 = EventProcessorOptions.getDefaultOptions();
		options1.setExceptionNotification(general1);
		
		PrefabGeneralErrorHandler general2 = new PrefabGeneralErrorHandler();
		PrefabProcessorFactory factory2 = new PrefabProcessorFactory(telltale, doCheckpointing, doMarker);
		EventProcessorHost host2 = new EventProcessorHost(conflictingName, utils.getConnectionString().getEventHubName(),
				utils.getConsumerGroup(), utils.getConnectionString().toString(),
				TestUtilities.getStorageConnectionString(), storageName);
		EventProcessorOptions options2 = EventProcessorOptions.getDefaultOptions();
		options2.setExceptionNotification(general2);

		host1.registerEventProcessorFactory(factory1, options1);
		host2.registerEventProcessorFactory(factory2, options2);
		
		int i = 0;
		while (true)
		{
			utils.sendToAny("conflict-" + i++, 10);
			System.out.println("\n." + factory1.getEventsReceivedCount() + "." + factory2.getEventsReceivedCount() + ":" +
					((ThreadPoolExecutor)host1.getHostContext().getExecutor()).getPoolSize() + "." +
					((ThreadPoolExecutor)host2.getHostContext().getExecutor()).getPoolSize() + ":" +
					Thread.activeCount());
			Thread.sleep(100);
		}
	}

	@Test
	public void infiniteReceive() throws Exception
	{
		System.out.println("infiniteReceive starting");
		
		RealEventHubUtilities utils = new RealEventHubUtilities();
		utils.setupWithoutSenders(RealEventHubUtilities.QUERY_ENTITY_FOR_PARTITIONS);
		
		PrefabGeneralErrorHandler genErr = new PrefabGeneralErrorHandler();
		PrefabProcessorFactory factory = new PrefabProcessorFactory("never match", PrefabEventProcessor.CheckpointChoices.CKP_NONE, true, false);
		InMemoryCheckpointManager checkpointer = new InMemoryCheckpointManager();
		InMemoryLeaseManager leaser = new InMemoryLeaseManager();
		EventProcessorHost host = new EventProcessorHost("infiniteReceive-1", utils.getConnectionString().getEventHubName(),
				utils.getConsumerGroup(), utils.getConnectionString().toString(),
				checkpointer, leaser);
		checkpointer.initialize(host.getHostContext());
		leaser.initialize(host.getHostContext());
		
		EventProcessorOptions opts = EventProcessorOptions.getDefaultOptions();
		opts.setExceptionNotification(genErr);
		host.registerEventProcessorFactory(factory, opts).get();
		
		while (System.in.available() == 0)
		{
			System.out.println("STANDING BY AT " + Thread.activeCount());
			Thread.sleep(10000);
		}
		while (System.in.available() > 0)
		{
			System.in.read();
		}
		while (System.in.available() == 0)
		{
			System.out.println("STANDING BY AT " + Thread.activeCount());
			Thread.sleep(1000);
		}

		host.unregisterEventProcessor();
	}

	/*
	 * The memory leak mentioned in the previous case turned out to be a thread leak. This case was created to see if
	 * the thread leak was related to EPH or was in the underlying client. At first we believed that the leak was due
	 * to creating a new epoch receiver that was kicking the old receiver off. Then we believed that it was about epoch
	 * receivers. Then we finally determined that a thread was leaked every time a receiver was closed, with no special
	 * sauce required.
	 */
	@Test
	public void rawEpochStealing() throws Exception
	{
		RealEventHubUtilities utils = new RealEventHubUtilities();
		utils.setup(RealEventHubUtilities.QUERY_ENTITY_FOR_PARTITIONS);

		int clientSerialNumber = 0;
		while (true)
		{
			Thread[] blah = new Thread[Thread.activeCount() + 10];
			int actual = Thread.enumerate(blah); 
			if (actual >= blah.length)
			{
				System.out.println("Lost some threads");
			}
			int parkedCount = 0;
			String selectingList = "";
			boolean display = true;
			for (int i = 0; i < actual; i++)
			{
				display = true;
				StackTraceElement[] bloo = blah[i].getStackTrace();
				String show = "nostack";
				if (bloo.length > 0)
				{
					show = bloo[0].getClassName() + "." + bloo[0].getMethodName();
					if (show.compareTo("sun.misc.Unsafe.park") == 0)
					{
						parkedCount++;
						display = false;
					}
					else if (show.compareTo("sun.nio.ch.WindowsSelectorImpl$SubSelector.poll0") == 0)
					{
						selectingList += (" " + blah[i].getId());
						display = false;
					}
				}
				if (display)
				{
					System.out.print(" " + blah[i].getId() + ":" + show);
				}
			}
			System.out.println("\nParked: " + parkedCount + "  SELECTING: " + selectingList);
			
			System.out.println("Client " + clientSerialNumber + " starting");
			EventHubClient client = EventHubClient.createFromConnectionStringSync(utils.getConnectionString().toString(), TestUtilities.EXECUTOR_SERVICE);
			PartitionReceiver receiver = client.createReceiver(utils.getConsumerGroup(), "0", EventPosition.fromStartOfStream()).get();
					//client.createEpochReceiver(utils.getConsumerGroup(), "0", PartitionReceiver.START_OF_STREAM, 1).get();

			boolean useReceiveHandler = false;
			
			if (useReceiveHandler)
			{
				Blah b = new Blah(clientSerialNumber++, receiver, client);
				receiver.setReceiveHandler(b).get();
				// wait for messages to start flowing
				b.waitForReceivedMessages().get();
			}
			else
			{
				receiver.receiveSync(1);
				System.out.println("Received a message");
			}
			
			// Enable these lines to avoid overlap
			try
			{
				System.out.println("Non-overlap close of PartitionReceiver");
				if (useReceiveHandler)
				{
					receiver.setReceiveHandler(null).get();
				}
				receiver.close().get();
			}
			catch (InterruptedException | ExecutionException e)
			{
				System.out.println("Client " + clientSerialNumber + " failed while closing PartitionReceiver: " + e.toString());
			}
			try
			{
				System.out.println("Non-overlap close of EventHubClient");
				client.close().get();
			}
			catch (InterruptedException | ExecutionException e)
			{
				System.out.println("Client " + clientSerialNumber + " failed while closing EventHubClient: " + e.toString());
			}
			System.out.println("Client " + clientSerialNumber + " closed");
			// Enable these lines to avoid overlap

			System.out.println("Threads: " + Thread.activeCount());
		}
	}
	
	private class Blah extends PartitionReceiveHandler
	{
		private int clientSerialNumber;
		private PartitionReceiver receiver;
		private EventHubClient client;
		private CompletableFuture<Void> receivedMessages = null;
		private boolean firstEvents = true;
		
		protected Blah(int clientSerialNumber, PartitionReceiver receiver, EventHubClient client)
		{
			super(300);
			this.clientSerialNumber = clientSerialNumber;
			this.receiver = receiver;
			this.client = client;
		}
		
		CompletableFuture<Void> waitForReceivedMessages()
		{
			this.receivedMessages = new CompletableFuture<Void>();
			return this.receivedMessages;
		}

		@Override
		public void onReceive(Iterable<EventData> events)
		{
			if (this.firstEvents)
			{
				System.out.println("Client " + this.clientSerialNumber + " got events");
				this.receivedMessages.complete(null);
				this.firstEvents = false;
			}
		}

		@Override
		public void onError(Throwable error)
		{
			System.out.println("Client " + this.clientSerialNumber + " got " + error.toString());
			try
			{
				this.receiver.close().get();
			}
			catch (InterruptedException | ExecutionException e)
			{
				System.out.println("Client " + this.clientSerialNumber + " failed while closing PartitionReceiver: " + e.toString());
			}
			try
			{
				this.client.close().get();
			}
			catch (InterruptedException | ExecutionException e)
			{
				System.out.println("Client " + this.clientSerialNumber + " failed while closing EventHubClient: " + e.toString());
			}
			System.out.println("Client " + this.clientSerialNumber + " closed");
		}
	}
}
