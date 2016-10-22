package com.microsoft.azure.eventprocessorhost;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.junit.AfterClass;

import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.servicebus.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.ServiceBusException;

public class TestBase
{
	PerTestSettings testSetup(PerTestSettings settings) throws Exception
	{
		TestUtilities.log(settings.getTestName() + " starting\n");
		
		String effectiveHostName = settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.HOST_OVERRIDE) ?
				settings.inoutEPHConstructorArgs.getHostName() : settings.getTestName() + "-1";

		settings.outUtils = new RealEventHubUtilities();
		settings.outPartitionIds = settings.outUtils.setup(settings.inEntityDoesNotExist ? 8 : RealEventHubUtilities.QUERY_ENTITY_FOR_PARTITIONS);
		ConnectionStringBuilder environmentCSB = settings.outUtils.getConnectionString();

		String effectiveEntityPath = settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.EH_PATH_OVERRIDE) ?
				settings.inoutEPHConstructorArgs.getEHPath() : environmentCSB.getEntityPath();
				
		String effectiveConsumerGroup = settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.CONSUMER_GROUP_OVERRIDE) ?
				settings.inoutEPHConstructorArgs.getConsumerGroupName() : EventHubClient.DEFAULT_CONSUMER_GROUP_NAME; 

		String effectiveConnectionString = environmentCSB.toString();
		if (settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.EH_PATH_REPLACE_IN_CONNECTION) ||
				settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.EH_CONNECTION_REMOVE_PATH))
		{
			ConnectionStringBuilder replacedCSB = new ConnectionStringBuilder(environmentCSB.getEndpoint(),
					settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.EH_CONNECTION_REMOVE_PATH) ? "" : settings.inoutEPHConstructorArgs.getEHPath(), 
					environmentCSB.getSasKeyName(), environmentCSB.getSasKey());
			replacedCSB.setOperationTimeout(environmentCSB.getOperationTimeout());
			replacedCSB.setRetryPolicy(environmentCSB.getRetryPolicy());
			effectiveConnectionString = replacedCSB.toString();
		}
		if (settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.EH_CONNECTION_OVERRIDE))
		{
			effectiveConnectionString = settings.inoutEPHConstructorArgs.getEHConnection();
		}
		
		ExecutorService effectiveExecutor = settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.EXECUTOR_OVERRIDE) ?
				settings.inoutEPHConstructorArgs.getExecutor() : null;
		
		settings.outTelltale = settings.getTestName() + "-telltale-" + EventProcessorHost.safeCreateUUID();
		settings.outGeneralErrorHandler = new PrefabGeneralErrorHandler();
		settings.outProcessorFactory = new PrefabProcessorFactory(settings.outTelltale, settings.inDoCheckpoint, true, true);
		
		settings.inOptions.setExceptionNotification(settings.outGeneralErrorHandler);
		
		if (settings.inoutEPHConstructorArgs.useExplicitManagers())
		{
			ICheckpointManager effectiveCheckpointMananger = settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.CHECKPOINT_MANAGER_OVERRIDE) ?
					settings.inoutEPHConstructorArgs.getCheckpointMananger() : new BogusCheckpointMananger();
			ILeaseManager effectiveLeaseManager = settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.LEASE_MANAGER_OVERRIDE) ?
					settings.inoutEPHConstructorArgs.getLeaseManager() : new BogusLeaseManager();
					
			settings.outHost = new EventProcessorHost(effectiveHostName, effectiveEntityPath, effectiveConsumerGroup, effectiveConnectionString,
					effectiveCheckpointMananger, effectiveLeaseManager, effectiveExecutor);
		}
		else
		{
			String effectiveStorageConnectionString = settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.STORAGE_CONNECTION_OVERRIDE) ?
					settings.inoutEPHConstructorArgs.getStorageConnection() : TestUtilities.getStorageConnectionString(); 
					
			String effectiveStorageContainerName = settings.getTestName().toLowerCase() + "-" + EventProcessorHost.safeCreateUUID();
			if (settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.STORAGE_CONTAINER_OVERRIDE))
			{
				effectiveStorageContainerName = settings.inoutEPHConstructorArgs.getStorageContainerName();
			}
			else
			{
				settings.inoutEPHConstructorArgs.setDefaultStorageContainerName(effectiveStorageContainerName);
			}
			
			String effectiveBlobPrefix = settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.STORAGE_BLOB_PREFIX_OVERRIDE) ?
					settings.inoutEPHConstructorArgs.getStorageBlobPrefix() : null;
					
			settings.outHost = new EventProcessorHost(effectiveHostName, effectiveEntityPath, effectiveConsumerGroup, effectiveConnectionString,
					effectiveStorageConnectionString, effectiveStorageContainerName, effectiveBlobPrefix, effectiveExecutor);
		}
		
		settings.outHost.registerEventProcessorFactory(settings.outProcessorFactory, settings.inOptions).get();
		
		return settings;
	}
	
	void waitForTelltale(PerTestSettings settings) throws InterruptedException
	{
		for (int i = 0; i < 100; i++)
		{
			if (settings.outProcessorFactory.getAnyTelltaleFound())
			{
				TestUtilities.log("Telltale found\n");
				break;
			}
			Thread.sleep(5000);
		}
	}
	
	void waitForTelltale(PerTestSettings settings, String partitionId) throws InterruptedException
	{
		for (int i = 0; i < 100; i++)
		{
			if (settings.outProcessorFactory.getTelltaleFound(partitionId))
			{
				TestUtilities.log("Telltale " + partitionId + " found\n");
				break;
			}
			Thread.sleep(5000);
		}
	}

	
	final static int NO_CHECKS = -2; // do no checks at all, used for tests which are expected fail in startup
	final static int ANY_NONZERO_COUNT = -1; // if expectedMessages is -1, just check for > 0
	void testFinish(PerTestSettings settings, int expectedMessages) throws InterruptedException, ExecutionException, ServiceBusException
	{
		if (settings.outHost != null)
		{
			settings.outHost.unregisterEventProcessor();
		}
		
		if (expectedMessages != NO_CHECKS)
		{
			TestUtilities.log("Events received: " + settings.outProcessorFactory.getEventsReceivedCount() + "\n");
			if (expectedMessages == ANY_NONZERO_COUNT)
			{
				assertTrue("no messages received", settings.outProcessorFactory.getEventsReceivedCount() > 0);
			}
			else
			{
				assertEquals("wrong number of messages received", expectedMessages, settings.outProcessorFactory.getEventsReceivedCount());
			}
			
			assertTrue("telltale message was not found", settings.outProcessorFactory.getAnyTelltaleFound());
			assertEquals("partition errors seen", 0, settings.outProcessorFactory.getErrors().size());
			assertEquals("general errors seen", 0, settings.outGeneralErrorHandler.getErrors().size());
			for (String err : settings.outProcessorFactory.getErrors())
			{
				TestUtilities.log(err);
			}
			for (String err : settings.outGeneralErrorHandler.getErrors())
			{
				TestUtilities.log(err);
			}
		}
		
		settings.outUtils.shutdown();
		
		TestUtilities.log(settings.getTestName() + " ended");
	}
	
	@AfterClass
	public static void allTestFinish()
	{
		try
		{
			EventProcessorHost.forceExecutorShutdown(20);
		}
		catch (InterruptedException e)
		{
			TestUtilities.log("forceExecutorShutdown threw " + e.toString() + "\n");
		}
	}
	
	class BogusCheckpointMananger implements ICheckpointManager
	{
		@Override
		public Future<Boolean> checkpointStoreExists()
		{
			return null;
		}

		@Override
		public Future<Boolean> createCheckpointStoreIfNotExists()
		{
			return null;
		}

		@Override
		public Future<Boolean> deleteCheckpointStore()
		{
			return null;
		}

		@Override
		public Future<Checkpoint> getCheckpoint(String partitionId)
		{
			return null;
		}

		@Override
		public Future<Checkpoint> createCheckpointIfNotExists(String partitionId)
		{
			return null;
		}

		@Override
		public Future<Void> updateCheckpoint(Checkpoint checkpoint)
		{
			return null;
		}

		@Override
		public Future<Void> deleteCheckpoint(String partitionId)
		{
			return null;
		}
	}
	
	class BogusLeaseManager implements ILeaseManager
	{
		@Override
		public int getLeaseRenewIntervalInMilliseconds()
		{
			return 0;
		}

		@Override
		public int getLeaseDurationInMilliseconds()
		{
			return 0;
		}

		@Override
		public Future<Boolean> leaseStoreExists()
		{
			return null;
		}

		@Override
		public Future<Boolean> createLeaseStoreIfNotExists()
		{
			return null;
		}

		@Override
		public Future<Boolean> deleteLeaseStore()
		{
			return null;
		}

		@Override
		public Future<Lease> getLease(String partitionId)
		{
			return null;
		}

		@Override
		public Iterable<Future<Lease>> getAllLeases() throws Exception
		{
			return null;
		}

		@Override
		public Future<Lease> createLeaseIfNotExists(String partitionId)
		{
			return null;
		}

		@Override
		public Future<Void> deleteLease(Lease lease)
		{
			return null;
		}

		@Override
		public Future<Boolean> acquireLease(Lease lease)
		{
			return null;
		}

		@Override
		public Future<Boolean> renewLease(Lease lease)
		{
			return null;
		}

		@Override
		public Future<Boolean> releaseLease(Lease lease)
		{
			return null;
		}

		@Override
		public Future<Boolean> updateLease(Lease lease)
		{
			return null;
		}
	}
}
