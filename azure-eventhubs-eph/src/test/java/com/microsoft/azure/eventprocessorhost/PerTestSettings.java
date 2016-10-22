package com.microsoft.azure.eventprocessorhost;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

public class PerTestSettings
{
	// Properties which are inputs to test setup. Constructor sets up defaults, except for testName.
	private String inTestName;
	EventProcessorOptions inOptions; // can be null
	boolean inDoCheckpoint;
	boolean inEntityDoesNotExist; // Prevents test code from doing certain checks that would fail on nonexistence before reaching product code.
	
	PerTestSettings(String testName)
	{
		this.inTestName = testName;
		this.inOptions = EventProcessorOptions.getDefaultOptions();
		this.inDoCheckpoint = false;
		this.inEntityDoesNotExist = false;
		
		this.inoutEPHConstructorArgs = new EPHConstructorArgs();
	}
	
	String getTestName() { return this.inTestName; }

	// In-out properties: may be set before test setup and then changed by setup.
	final EPHConstructorArgs inoutEPHConstructorArgs;
	
	// Output properties: any value set before test setup is ignored. The real value is
	// established during test setup.
	RealEventHubUtilities outUtils;
	String outTelltale;
	ArrayList<String> outPartitionIds;
	PrefabGeneralErrorHandler outGeneralErrorHandler;
	PrefabProcessorFactory outProcessorFactory;
	EventProcessorHost outHost;

	
	class EPHConstructorArgs
	{
		static final int HOST_OVERRIDE = 0x0001;
		static final int EH_PATH_OVERRIDE = 0x0002;
		static final int EH_PATH_REPLACE_IN_CONNECTION = 0x0004;
		static final int EH_PATH_OVERRIDE_AND_REPLACE = EH_PATH_OVERRIDE | EH_PATH_REPLACE_IN_CONNECTION;
		static final int CONSUMER_GROUP_OVERRIDE = 0x0008;
		static final int EH_CONNECTION_OVERRIDE = 0x0010;
		static final int EH_CONNECTION_REMOVE_PATH = 0x0020;
		static final int STORAGE_CONNECTION_OVERRIDE = 0x0040;
		static final int STORAGE_CONTAINER_OVERRIDE = 0x0080;
		static final int STORAGE_BLOB_PREFIX_OVERRIDE = 0x0100;
		static final int EXECUTOR_OVERRIDE = 0x0200;
		static final int CHECKPOINT_MANAGER_OVERRIDE = 0x0400;
		static final int LEASE_MANAGER_OVERRIDE = 0x0800;
		static final int EXPLICIT_MANAGER = CHECKPOINT_MANAGER_OVERRIDE | LEASE_MANAGER_OVERRIDE;
		
		private int flags;
		
		private String hostName;
		private String ehPath;
		private String consumerGroupName;
		private String ehConnection;
		private String storageConnection;
		private String storageContainerName;
		private String storageBlobPrefix;
		private ExecutorService executor;
		private ICheckpointManager checkpointManager;
		private ILeaseManager leaseManager;
		
		EPHConstructorArgs()
		{
			this.flags = 0;
			
			this.hostName = null;
			this.ehPath = null;
			this.consumerGroupName = null;
			this.ehConnection = null;
			this.storageConnection = null;
			this.storageContainerName = null;
			this.storageBlobPrefix = null;
			this.executor = null;
			this.checkpointManager = null;
			this.leaseManager = null;
		}
		
		int getFlags() { return this.flags; }
		boolean isFlagSet(int testFlag) { return ((this.flags & testFlag) != 0); }
		
		void setHostName(String hostName)
		{
			this.hostName = hostName;
			this.flags |= HOST_OVERRIDE;
		}
		String getHostName() { return this.hostName; }
		
		void setEHPath(String ehPath, int flags)
		{
			this.ehPath = ehPath;
			this.flags |= (flags & EH_PATH_OVERRIDE_AND_REPLACE);
		}
		String getEHPath() { return this.ehPath; }
		
		void setConsumerGroupName(String consumerGroupName)
		{
			this.consumerGroupName = consumerGroupName;
			this.flags |= CONSUMER_GROUP_OVERRIDE;
		}
		String getConsumerGroupName() { return this.consumerGroupName; }
		
		void setEHConnection(String ehConnection)
		{
			this.ehConnection = ehConnection;
			this.flags |= EH_CONNECTION_OVERRIDE;
		}
		void removePathFromEHConnection() { this.flags |= EH_CONNECTION_REMOVE_PATH; }
		String getEHConnection() { return this.ehConnection; }
		
		void setStorageConnection(String storageConnection)
		{
			this.storageConnection = storageConnection;
			this.flags |= STORAGE_CONNECTION_OVERRIDE;
		}
		String getStorageConnection() { return this.storageConnection; }
		
		void setStorageContainerName(String storageContainerName)
		{
			this.storageContainerName = storageContainerName;
			this.flags |= STORAGE_CONTAINER_OVERRIDE;
		}
		void setDefaultStorageContainerName(String defaultStorageContainerName)
		{
			this.storageContainerName = defaultStorageContainerName;
		}
		String getStorageContainerName() { return this.storageContainerName; }
		
		void setStorageBlobPrefix(String storageBlobPrefix)
		{
			this.storageBlobPrefix = storageBlobPrefix;
			this.flags |= STORAGE_BLOB_PREFIX_OVERRIDE;
		}
		String getStorageBlobPrefix() { return this.storageBlobPrefix; }
		
		void setExecutor(ExecutorService executor)
		{
			this.executor = executor;
			this.flags |= EXECUTOR_OVERRIDE;
		}
		ExecutorService getExecutor() { return this.executor; }
		
		boolean useExplicitManagers() { return ((this.flags & EXPLICIT_MANAGER) != 0); }
		
		void setCheckpointManager(ICheckpointManager checkpointManager)
		{
			this.checkpointManager = checkpointManager;
			this.flags |= CHECKPOINT_MANAGER_OVERRIDE;
		}
		ICheckpointManager getCheckpointMananger() { return this.checkpointManager; }
		
		void setLeaseManager(ILeaseManager leaseManager)
		{
			this.leaseManager = leaseManager;
			this.flags |= LEASE_MANAGER_OVERRIDE;
		}
		ILeaseManager getLeaseManager() { return this.leaseManager; }
	}
}
