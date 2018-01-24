package com.microsoft.azure.eventprocessorhost;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.AfterClass;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;

public class TestBase
{
	PerTestSettings testSetup(PerTestSettings settings) throws Exception
	{
		TestUtilities.log(settings.getTestName() + " starting\n");
		
		String effectiveHostName = settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.HOST_OVERRIDE) ?
				settings.inoutEPHConstructorArgs.getHostName() : settings.getTestName() + "-1";

		settings.outUtils = new RealEventHubUtilities();
		if (settings.inHasSenders)
		{
			settings.outPartitionIds = settings.outUtils.setup(settings.inEventHubDoesNotExist ? 8 : RealEventHubUtilities.QUERY_ENTITY_FOR_PARTITIONS);
		}
		else
		{
			settings.outPartitionIds = settings.outUtils.setupWithoutSenders(settings.inEventHubDoesNotExist ? 8 : RealEventHubUtilities.QUERY_ENTITY_FOR_PARTITIONS);
		}
		ConnectionStringBuilder environmentCSB = settings.outUtils.getConnectionString();

		String effectiveEntityPath = settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.EH_PATH_OVERRIDE) ?
				settings.inoutEPHConstructorArgs.getEHPath() : environmentCSB.getEventHubName();
				
		String effectiveConsumerGroup = settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.CONSUMER_GROUP_OVERRIDE) ?
				settings.inoutEPHConstructorArgs.getConsumerGroupName() : EventHubClient.DEFAULT_CONSUMER_GROUP_NAME; 

		String effectiveConnectionString = environmentCSB.toString();
		if (settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.EH_PATH_REPLACE_IN_CONNECTION) ||
				settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.EH_CONNECTION_REMOVE_PATH))
		{
			ConnectionStringBuilder replacedCSB = new ConnectionStringBuilder()
					.setEndpoint(environmentCSB.getEndpoint())
					.setEventHubName(
							settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.EH_CONNECTION_REMOVE_PATH) ?
                                    "" :
                                    settings.inoutEPHConstructorArgs.getEHPath()
					)
					.setSasKeyName(environmentCSB.getSasKeyName())
					.setSasKey(environmentCSB.getSasKey());
			replacedCSB.setOperationTimeout(environmentCSB.getOperationTimeout());
			effectiveConnectionString = replacedCSB.toString();
		}
		if (settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.EH_CONNECTION_OVERRIDE))
		{
			effectiveConnectionString = settings.inoutEPHConstructorArgs.getEHConnection();
		}
		
		ScheduledExecutorService effectiveExecutor = settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.EXECUTOR_OVERRIDE) ?
				settings.inoutEPHConstructorArgs.getExecutor() : null;
		
		if (settings.inTelltaleOnTimeout)
		{
			settings.outTelltale = "";
		}
		else
		{
			settings.outTelltale = settings.getTestName() + "-telltale-" + EventProcessorHost.safeCreateUUID();
		}
		settings.outGeneralErrorHandler = new PrefabGeneralErrorHandler();
		settings.outProcessorFactory = new PrefabProcessorFactory(settings.outTelltale, settings.inDoCheckpoint, false, false);
		
		settings.inOptions.setExceptionNotification(settings.outGeneralErrorHandler);
		
		if (settings.inoutEPHConstructorArgs.useExplicitManagers())
		{
			ICheckpointManager effectiveCheckpointMananger = settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.CHECKPOINT_MANAGER_OVERRIDE) ?
					settings.inoutEPHConstructorArgs.getCheckpointMananger() : new BogusCheckpointMananger();
			ILeaseManager effectiveLeaseManager = settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.LEASE_MANAGER_OVERRIDE) ?
					settings.inoutEPHConstructorArgs.getLeaseManager() : new BogusLeaseManager();
					
			settings.outHost = new EventProcessorHost(effectiveHostName, effectiveEntityPath, effectiveConsumerGroup, effectiveConnectionString,
					effectiveCheckpointMananger, effectiveLeaseManager, effectiveExecutor, null);
		}
		else
		{
			String effectiveStorageConnectionString = settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.STORAGE_CONNECTION_OVERRIDE) ?
					settings.inoutEPHConstructorArgs.getStorageConnection() : TestUtilities.getStorageConnectionString(); 
					
			String effectiveStorageContainerName = settings.getTestName().toLowerCase() + "-" + EventProcessorHost.safeCreateUUID();
			if (settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.STORAGE_CONTAINER_OVERRIDE))
			{
				effectiveStorageContainerName = settings.inoutEPHConstructorArgs.getStorageContainerName();
				if (effectiveStorageContainerName != null)
				{
					effectiveStorageContainerName = effectiveStorageContainerName.toLowerCase();
				}
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

	
	final static int SKIP_COUNT_CHECK = -3; // expectedMessages could be anything, don't check it at all
	final static int NO_CHECKS = -2; // do no checks at all, used for tests which are expected fail in startup
	final static int ANY_NONZERO_COUNT = -1; // if expectedMessages is -1, just check for > 0
	void testFinish(PerTestSettings settings, int expectedMessages) throws InterruptedException, ExecutionException, EventHubException
	{
		if (settings.outHost != null)
		{
			settings.outHost.unregisterEventProcessor().get();
			TestUtilities.log("Host unregistered");
		}
		
		if (expectedMessages != NO_CHECKS)
		{
			TestUtilities.log("Events received: " + settings.outProcessorFactory.getEventsReceivedCount() + "\n");
			if (expectedMessages == ANY_NONZERO_COUNT)
			{
				assertTrue("no messages received", settings.outProcessorFactory.getEventsReceivedCount() > 0);
			}
			else if (expectedMessages != SKIP_COUNT_CHECK)
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
	}
	
	class BogusCheckpointMananger implements ICheckpointManager
	{
		@Override
		public CompletableFuture<Boolean> checkpointStoreExists()
		{
			return CompletableFuture.completedFuture(true);
		}

		@Override
		public CompletableFuture<Void> createCheckpointStoreIfNotExists()
		{
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public CompletableFuture<Boolean> deleteCheckpointStore()
		{
			return CompletableFuture.completedFuture(true);
		}

		@Override
		public CompletableFuture<Checkpoint> getCheckpoint(String partitionId)
		{
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public CompletableFuture<Checkpoint> createCheckpointIfNotExists(String partitionId)
		{
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public CompletableFuture<Void> updateCheckpoint(Lease lease, Checkpoint checkpoint)
		{
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public CompletableFuture<Void> deleteCheckpoint(String partitionId)
		{
			return CompletableFuture.completedFuture(null);
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
		public CompletableFuture<Boolean> leaseStoreExists()
		{
			return CompletableFuture.completedFuture(true);
		}

		@Override
		public CompletableFuture<Void> createLeaseStoreIfNotExists()
		{
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public CompletableFuture<Boolean> deleteLeaseStore()
		{
			return CompletableFuture.completedFuture(true);
		}

		@Override
		public CompletableFuture<List<Lease>> getAllLeases()
		{
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public CompletableFuture<Lease> createLeaseIfNotExists(String partitionId)
		{
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public CompletableFuture<Void> deleteLease(Lease lease)
		{
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public CompletableFuture<Boolean> acquireLease(Lease lease)
		{
			return CompletableFuture.completedFuture(true);
		}

		@Override
		public CompletableFuture<Boolean> renewLease(Lease lease)
		{
			return CompletableFuture.completedFuture(true);
		}

		@Override
		public CompletableFuture<Void> releaseLease(Lease lease)
		{
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public CompletableFuture<Boolean> updateLease(Lease lease)
		{
			return CompletableFuture.completedFuture(true);
		}
	}
}
