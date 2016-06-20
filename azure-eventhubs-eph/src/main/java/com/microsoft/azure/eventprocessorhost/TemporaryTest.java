/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import java.util.UUID;
import java.util.function.Consumer;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.servicebus.ConnectionStringBuilder;

public class TemporaryTest
{
    public static void main(String args[])
    {
    	final int hostCount = 1;
    	int runCase = 3;
    	final boolean useInMemory = false;
    	final boolean useEH = true;

    	final String ehConsumerGroup = "$Default";
    	final String ehNamespace = "";
    	final String ehEventhub = "";
    	final String ehKeyname = "";
    	final String ehKey = "";
    	final String storageConnectionString = "this is not a valid storage connection string";
    	final String storageContainerName = "tt-" + UUID.randomUUID().toString();
    	
    	if (runCase == 1)
    	{
    		ILeaseManager leaseMgr = null;
    		ICheckpointManager checkpointMgr = null;
    		if (useInMemory)
    		{
    			leaseMgr = new InMemoryLeaseManager();
    			checkpointMgr = new InMemoryCheckpointManager();
    		}
    		else
    		{
    			AzureStorageCheckpointLeaseManager azMgr = new AzureStorageCheckpointLeaseManager(storageConnectionString);
    			leaseMgr = azMgr;
    			checkpointMgr = azMgr;
    		}
	    	EventProcessorHost blah = new EventProcessorHost(EventProcessorHost.createHostName(null), "fakeEventHub", "$Default",
	    			"fakeEventHubConnectionString", checkpointMgr, leaseMgr);
	    	try
	    	{
	    		if (useInMemory)
	    		{
	    			((InMemoryLeaseManager)leaseMgr).initialize(blah);
	    			((InMemoryCheckpointManager)checkpointMgr).initialize(blah);
	    		}
	    		else
	    		{
	    			((AzureStorageCheckpointLeaseManager)leaseMgr).initialize(blah);
	    		}
			}
	    	catch (Exception e)
	    	{
	    		System.out.println("Initialize failed " + e.toString());
	    		e.printStackTrace();
			}

	    	final int partitionCount = 8;
			basicLeaseManagerTest(leaseMgr, partitionCount, useInMemory);
    	}
    	else if (runCase == 2)
    	{
    		ILeaseManager leaseMgr1 = null;
    		ILeaseManager leaseMgr2 = null;
    		ICheckpointManager checkMgr1 = null;
    		ICheckpointManager checkMgr2 = null;
    		if (useInMemory)
    		{
    			leaseMgr1 = new InMemoryLeaseManager();
    			checkMgr1 = new InMemoryCheckpointManager();
    			leaseMgr2 = new InMemoryLeaseManager();
    			checkMgr2 = new InMemoryCheckpointManager();
    		}
    		else
    		{
    			AzureStorageCheckpointLeaseManager azMgr1 = new AzureStorageCheckpointLeaseManager(storageConnectionString);
    			leaseMgr1 = azMgr1;
    			checkMgr1 = azMgr1;
		    	AzureStorageCheckpointLeaseManager azMgr2 = new AzureStorageCheckpointLeaseManager(storageConnectionString);
		    	leaseMgr2 = azMgr2;
		    	checkMgr2 = azMgr2;
    		}
	    	EventProcessorHost blah1 = new EventProcessorHost(EventProcessorHost.createHostName(null), "fakeEventhub", "$Default",
	    			"fakeEventhubConnectionString", checkMgr1, leaseMgr1);
	    	EventProcessorHost blah2 = new EventProcessorHost(EventProcessorHost.createHostName(null), "fakeEventhub", "$Default",
	    			"fakeEventhubConnectionString", checkMgr2, leaseMgr2);
	    	try
	    	{
	    		if (useInMemory)
	    		{
	    			((InMemoryLeaseManager)leaseMgr1).initialize(blah1);
	    			((InMemoryCheckpointManager)checkMgr1).initialize(blah1);
	    			((InMemoryLeaseManager)leaseMgr2).initialize(blah2);
	    			((InMemoryCheckpointManager)checkMgr2).initialize(blah2);
	    		}
	    		else
	    		{
	    			((AzureStorageCheckpointLeaseManager)leaseMgr1).initialize(blah1);
	    			((AzureStorageCheckpointLeaseManager)leaseMgr2).initialize(blah2);
	    		}
			}
	    	catch (Exception e)
	    	{
	    		System.out.println("Initialize failed " + e.toString());
	    		e.printStackTrace();
			}
	    	
	    	stealLeaseTest(leaseMgr1, checkMgr1, leaseMgr2, checkMgr2, useInMemory);
    	}
    	else if (runCase == 3)
    	{
    		EventProcessorHost[] hosts = new EventProcessorHost[hostCount];
    		for (int i = 0; i < hostCount; i++)
    		{
    			ConnectionStringBuilder ehConnStr = new ConnectionStringBuilder(ehNamespace, ehEventhub, ehKeyname, ehKey);
        		if (useInMemory)
        		{
        			InMemoryLeaseManager leaseMgr = new InMemoryLeaseManager();
        			InMemoryCheckpointManager checkMgr = new InMemoryCheckpointManager();
        			hosts[i] = new EventProcessorHost(EventProcessorHost.createHostName(null), ehEventhub, ehConsumerGroup, ehConnStr.toString(), checkMgr, leaseMgr);
        			leaseMgr.initialize(hosts[i]);
        			checkMgr.initialize(hosts[i]);
        		}
        		else
        		{
        			hosts[i] = new EventProcessorHost(EventProcessorHost.createHostName(null), ehEventhub, ehConsumerGroup, ehConnStr.toString(),
        					storageConnectionString, storageContainerName); 
        		}
    			if (!useEH)
    			{
    				//hosts[i].setPumpClass(SyntheticPump.class); // See note in SyntheticPump.produceMessages() -- doesn't work right now
    			}
    		}
    		processMessages(hosts);
    	}
    	
        System.out.println("End of sample");
    }
    
    private static void stealLeaseTest(ILeaseManager leaseMgr1, ICheckpointManager checkMgr1, ILeaseManager leaseMgr2, ICheckpointManager checkMgr2, boolean useInMemory)
    {
    	try
    	{
	    	System.out.println("Lease store may not exist");
			Boolean boolret = leaseMgr1.leaseStoreExists().get();
			System.out.println("leaseStoreExists() returned " + boolret);
			
			System.out.println("Create lease store if not exists");
			boolret = leaseMgr1.createLeaseStoreIfNotExists().get();
			System.out.println("createLeaseStoreIfNotExists() returned " + boolret);
	
	    	System.out.println("Lease store should exist now");
			boolret = leaseMgr1.leaseStoreExists().get();
			System.out.println("leaseStoreExists() returned " + boolret);

			if (useInMemory)
			{
		    	System.out.println("Checkpoint store may not exist");
				boolret = checkMgr1.checkpointStoreExists().get();
				System.out.println("checkpointStoreExists() returned " + boolret);
				
				System.out.println("Create checkpoint store if not exists");
				boolret = checkMgr1.createCheckpointStoreIfNotExists().get();
				System.out.println("createCheckpointStoreIfNotExists() returned " + boolret);
		
		    	System.out.println("Checkpoint store should exist now");
				boolret = checkMgr1.checkpointStoreExists().get();
				System.out.println("checkpointStoreExists() returned " + boolret);
			}
			
			System.out.print("Mgr1 making sure lease for 0 exists... ");
			Lease mgr1Lease = leaseMgr1.createLeaseIfNotExists("0").get();
			System.out.println("OK");
			
			if (useInMemory)
			{
				System.out.print("Mgr1 making sure checkpoint for 0 exists... ");
				checkMgr1.createCheckpointIfNotExists("0").get();
				System.out.println("OK");
			}
			
			System.out.print("Mgr2 get lease... ");
			Lease mgr2Lease = leaseMgr2.getLease("0").get();
			System.out.println("OK");

			System.out.print("Mgr1 acquiring lease... ");
			boolret = leaseMgr1.acquireLease(mgr1Lease).get();
			System.out.println(boolret);
			System.out.println("Lease token is " + mgr1Lease.getToken());
			
			System.out.println("Waiting for lease on 0 to expire.");
			int x = 1;
			while (!mgr1Lease.isExpired())
			{
				Thread.sleep(5000);
				System.out.println("Still waiting for lease on 0 to expire: " + (5 * x++));
			}
			System.out.println("Expired!");

			System.out.print("Mgr2 acquiring lease... ");
			boolret = leaseMgr2.acquireLease(mgr2Lease).get();
			System.out.println(boolret);
			System.out.println("Lease token is " + mgr2Lease.getToken());
			
			System.out.print("Mgr1 tries to renew lease... ");
			boolret = leaseMgr1.renewLease(mgr1Lease).get();
			System.out.println(boolret);
			
			System.out.print("Mgr1 gets current lease data in order to steal it... ");
			mgr1Lease = leaseMgr1.getLease(mgr1Lease.getPartitionId()).get();
			System.out.println("OK");
			
			System.out.print("Mgr1 tries to steal lease... ");
			boolret = leaseMgr1.acquireLease(mgr1Lease).get();
			System.out.println(boolret);
			System.out.println("Lease token is " + mgr1Lease.getToken());
			
			Checkpoint check1 = checkMgr1.getCheckpoint("0").get();
			System.out.println("Checkpoint currently at offset: " + check1.getOffset() + " seqNo: " + check1.getSequenceNumber());
			check1.setOffset(((Integer)(Integer.parseInt(check1.getOffset()) + 500)).toString());
			check1.setSequenceNumber(check1.getSequenceNumber() + 5);
			System.out.println("Checkpoint changed to offset: " + check1.getOffset() + " seqNo: " + check1.getSequenceNumber());
			System.out.print("Mgr1 checkpointing... ");
			checkMgr1.updateCheckpoint(check1).get();
			System.out.println("done");
			
			System.out.print("Mgr2 gets current lease data in order to steal it... ");
			mgr2Lease = leaseMgr2.getLease("0").get();
			System.out.println("OK");
			
			System.out.print("Mgr2 tries to steal lease... ");
			boolret = leaseMgr2.acquireLease(mgr2Lease).get();
			System.out.println(boolret);
			System.out.println("Lease token is " + mgr2Lease.getToken());
			Checkpoint check2 = checkMgr2.getCheckpoint("0").get();
			System.out.println("Got checkpoint of offset: " + check2.getOffset() + " seqNo: " + check2.getSequenceNumber());
			
			System.out.print("Mgr2 releasing lease... ");
			boolret = leaseMgr2.releaseLease(mgr2Lease).get();
			System.out.println(boolret);

			System.out.print("Mgr1 releasing lease... ");
			boolret = leaseMgr2.releaseLease(mgr1Lease).get();
			System.out.println(boolret);

			check2 = checkMgr2.getCheckpoint("0").get();
			System.out.println("Got checkpoint of offset: " + check2.getOffset() + " seqNo: " + check2.getSequenceNumber());
    	}
    	catch (Exception e)
    	{
        	System.out.println("Sample caught " + e.toString());
        	StackTraceElement[] stack = e.getStackTrace();
        	for (int i = 0; i < stack.length; i++)
        	{
        		System.out.println(stack[i].toString());
        	}
    	}
    }
    
    private static void processMessages(EventProcessorHost[] hosts)
    {
    	int hostCount = hosts.length;
    	
    	for (int i = 0; i < hostCount; i++)
    	{
    		System.out.println("Registering host " + i + " named " + hosts[i].getHostName());
    		EventProcessorOptions options = EventProcessorOptions.getDefaultOptions();
    		options.setExceptionNotification(new GeneralErrorHandler());
    		try 
    		{
				hosts[i].registerEventProcessor(EventProcessor.class, options).get();
			}
    		catch (Exception e)
    		{
            	System.out.println("Sample caught from register " + e.toString());
            	StackTraceElement[] stack = e.getStackTrace();
            	for (int j = 0; j < stack.length; j++)
            	{
            		System.out.println(stack[j].toString());
            	}
            	return;
			}
    		try
    		{
    			Thread.sleep(3000);
    		}
    		catch (InterruptedException e1)
    		{
    			// Watch me not care
    		}
    	}

        System.out.println("Press enter to stop");
        try
        {
            System.in.read();
            for (int i = 0; i < hostCount; i++)
            {
	            System.out.println("Calling unregister " + i);
	            hosts[i].unregisterEventProcessor();
	            System.out.println("Completed");
            }
            EventProcessorHost.forceExecutorShutdown(120);
        }
        catch(Exception e)
        {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }
    
    private static void basicLeaseManagerTest(ILeaseManager mgr, int partitionCount, boolean useInMemory)
    {
    	try
    	{
        	System.out.println("Store may not exist");
			Boolean boolret = mgr.leaseStoreExists().get();
			System.out.println("getStoreExists() returned " + boolret);
			
			System.out.println("Create store if not exists");
			boolret = mgr.createLeaseStoreIfNotExists().get();
			System.out.println("createStoreIfNotExists() returned " + boolret);

        	System.out.println("Store should exist now");
			boolret = mgr.leaseStoreExists().get();
			System.out.println("getStoreExists() returned " + boolret);
			
			Lease[] leases = new Lease[partitionCount];
			for (Integer i = 0; i < partitionCount; i++)
			{
				System.out.print("Creating lease for partition " + i + "... ");
				Lease createdLease = mgr.createLeaseIfNotExists(i.toString()).get();
				leases[i] = createdLease;
				System.out.println("OK");
			}
			
			for (int i = 0; i < partitionCount; i++)
			{
				if (!useInMemory)
				{
					System.out.println("Partition " + i + " state before: " + ((AzureBlobLease)leases[i]).getStateDebug());
				}
				System.out.print("Acquiring lease for partition " + i + "... ");
				boolret = mgr.acquireLease(leases[i]).get();
				System.out.println(boolret.toString());
				if (!useInMemory)
				{
					System.out.println("Partition " + i + " state after: " + ((AzureBlobLease)leases[i]).getStateDebug());
				}
			}
			
			System.out.print("Sleeping... ");
			Thread.sleep(5000);
			System.out.println("done");
			
			for (int i = 0; i < partitionCount; i++)
			{
				if (!useInMemory)
				{
					System.out.println("Partition " + i + " state before: " + ((AzureBlobLease)leases[i]).getStateDebug());
				}
				System.out.print("Renewing lease for partition " + i + "... ");
				boolret = mgr.renewLease(leases[i]).get();
				System.out.println(boolret.toString());
				if (!useInMemory)
				{
					System.out.println("Partition " + i + " state after: " + ((AzureBlobLease)leases[i]).getStateDebug());
				}
			}
			
			System.out.println("Waiting for lease on 0 to expire.");
			int x = 1;
			while (!leases[0].isExpired())
			{
				Thread.sleep(5000);
				System.out.println("Still waiting for lease on 0 to expire: " + (5 * x++));
				for (int i = 1; i < partitionCount; i++)
				{
					System.out.print("   Renewing lease for partition " + i + "... ");
					boolret = mgr.renewLease(leases[i]).get();
					System.out.println(boolret.toString());
				}
			}
			System.out.println("Expired!");
			
			for (int i = 0; i < partitionCount; i++)
			{
				if (!useInMemory)
				{
					System.out.println("Partition " + i + " state before: " + ((AzureBlobLease)leases[i]).getStateDebug());
				}
				System.out.print("Releasing lease for partition " + i + "... ");
				boolret = mgr.releaseLease(leases[i]).get();
				System.out.println(boolret.toString());
				if (!useInMemory)
				{
					System.out.println("Partition " + i + " state after: " + ((AzureBlobLease)leases[i]).getStateDebug());
				}
			}
    	}
    	catch (Exception e)
    	{
        	System.out.println("Sample caught " + e.toString());
        	StackTraceElement[] stack = e.getStackTrace();
        	for (int i = 0; i < stack.length; i++)
        	{
        		System.out.println(stack[i].toString());
        	}
		}
    }
    
    /*
    public static class SyntheticPump extends PartitionPump
    {
    	Future<Void> producer = null;
    	boolean keepGoing = true;

		@Override
		public void specializedStartPump()
		{
			this.producer = EventProcessorHost.getExecutorService().submit(() -> produceMessages());
		}

		@Override
		public void specializedShutdown(CloseReason reason)
		{
			this.keepGoing = false;
			try
			{
				this.producer.get();
			}
			catch (InterruptedException | ExecutionException e)
			{
				System.out.println("SyntheticPump shutdown failure" + e.toString());
				e.printStackTrace();
			}
		}
		
		private Void produceMessages()
		{
			ArrayList<EventData> events = new ArrayList<EventData>();
			int eventNumber = 0;
			
			while (this.keepGoing)
			{
				events.clear();
				String eventBody = "Event " + eventNumber + " on partition " + this.lease.getPartitionId();
				eventNumber++;
				EventData event = new EventData(eventBody.getBytes());
				// Need a way to set the offset and sequenceNumber on event! Normally they only exist on received instances.
				// Testing checkpointing won't work without them.
				//event.fakeReceivedMessage(Integer.toString(eventNumber * 75), eventNumber);
				events.add(event);
				onEvents(events);
				
				try
				{
					Thread.sleep(3000);
				}
				catch (InterruptedException e)
				{
					// Watch me not care
				}
			}
			
			return null;
		}
    }
    */

    public static class EventProcessor implements IEventProcessor
    {
    	private int checkpointBatchingCount = 0;
    	
    	@Override
        public void onOpen(PartitionContext context) throws Exception
        {
            String hostname = context.getLease().getOwner();
        	System.out.println("SAMPLE: Partition " + context.getPartitionId() + " is opening for host " + hostname.substring(hostname.length() - 4));
        }

    	@Override
        public void onClose(PartitionContext context, CloseReason reason) throws Exception
        {
            String hostname = context.getLease().getOwner();
            System.out.println("SAMPLE: Partition " + context.getPartitionId() + " is closing for reason " + reason.toString() + " for host " + hostname.substring(hostname.length() - 4));
        }

    	@Override
        public void onEvents(PartitionContext context, Iterable<EventData> messages) throws Exception
        {
            String hostname = context.getLease().getOwner();
            hostname = hostname.substring(hostname.length() - 4);
            System.out.println("SAMPLE: Partition " + context.getPartitionId() + " got message batch for host " + hostname);
            int messageCount = 0;
            for (EventData data : messages)
            {
                System.out.println("SAMPLE (" + hostname + "," + context.getPartitionId() + "," + data.getSystemProperties().getOffset() + "," +
                		data.getSystemProperties().getSequenceNumber() + "): " + new String(data.getBody(), "UTF8"));
                messageCount++;
                this.checkpointBatchingCount++;
                if ((checkpointBatchingCount % 5) == 0)
                {
                	System.out.println("SAMPLE: Partition " + context.getPartitionId() + " checkpointing at " +
               			data.getSystemProperties().getOffset() + "," + data.getSystemProperties().getSequenceNumber());
                	context.checkpoint(data);
                }
            }
            System.out.println("SAMPLE: Partition " + context.getPartitionId() + " batch size was " + messageCount + " for host " + hostname);
        }
    	
    	@Override
    	public void onError(PartitionContext context, Throwable error)
    	{
    		System.out.println("SAMPLE: Partition " + context.getPartitionId() + " onError: " + error.toString());
    	}
    }
    
    public static class GeneralErrorHandler implements Consumer<ExceptionReceivedEventArgs>
    {
		@Override
		public void accept(ExceptionReceivedEventArgs t)
		{
			System.out.println("SAMPLE ERROR HANDLER for host " + t.getHostname() + ": " + t.getAction() + ": " + t.getException().toString());
		}
    }
}
