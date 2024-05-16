// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventprocessorhost.EventProcessorHost.EventProcessorHostBuilder.AuthStep;
import com.microsoft.azure.eventprocessorhost.EventProcessorHost.EventProcessorHostBuilder.OptionalStep;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestBase {
    static final int SKIP_COUNT_CHECK = -3; // expectedEvents could be anything, don't check it at all
    static final int NO_CHECKS = -2; // do no checks at all, used for tests which are expected fail in startup
    static final int ANY_NONZERO_COUNT = -1; // if expectedEvents is -1, just check for > 0

    static boolean logInfo = false;
    static boolean logConsole = false;
    static final Logger TRACE_LOGGER = LoggerFactory.getLogger("servicebus.test-eph.trace");

    @Rule
    public final TestName name = new TestName();

    @BeforeClass
    public static void allTestStart() {
        String env = System.getenv("VERBOSELOG");
        if (env != null) {
            TestBase.logInfo = true;
            if (env.compareTo("CONSOLE") == 0) {
                TestBase.logConsole = true;
            }
        }
    }

    @AfterClass
    public static void allTestFinish() {
    }

    static void logError(String message) {
        if (TestBase.logConsole) {
            System.err.println("TEST ERROR: " + message);
        } else {
            TestBase.TRACE_LOGGER.error(message);
        }
    }

    static void logInfo(String message) {
        if (TestBase.logInfo) {
            if (TestBase.logConsole) {
                System.err.println("TEST INFO: " + message);
            } else {
                TestBase.TRACE_LOGGER.info(message);
            }
        }
    }

    void skipIfAutomated() {
        Assume.assumeTrue(System.getenv("VERBOSELOG") != null);
    }

    @Before
    public void logCaseStart() {
        String usemsg = "CASE START: " + this.name.getMethodName();
        if (TestBase.logConsole) {
            System.err.println(usemsg);
        } else {
            TestBase.TRACE_LOGGER.info(usemsg);
        }
    }

    @After
    public void logCaseEnd() {
        String usemsg = "CASE END: " + this.name.getMethodName();
        if (TestBase.logConsole) {
            System.err.println(usemsg);
        } else {
            TestBase.TRACE_LOGGER.info(usemsg);
        }
    }

    PerTestSettings testSetup(PerTestSettings settings) throws Exception {
        String effectiveHostName = settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.HOST_OVERRIDE)
            ? settings.inoutEPHConstructorArgs.getHostName()
            : settings.getDefaultHostName() + "-1";

        settings.outUtils = new RealEventHubUtilities();
        boolean skipIfNoEventHubConnectionString = !settings.inEventHubDoesNotExist || settings.inSkipIfNoEventHubConnectionString;
        if (settings.inHasSenders) {
            settings.outPartitionIds = settings.outUtils.setup(skipIfNoEventHubConnectionString, settings.inEventHubDoesNotExist ? 8 : RealEventHubUtilities.QUERY_ENTITY_FOR_PARTITIONS);
        } else {
            settings.outPartitionIds = settings.outUtils.setupWithoutSenders(skipIfNoEventHubConnectionString, settings.inEventHubDoesNotExist ? 8 : RealEventHubUtilities.QUERY_ENTITY_FOR_PARTITIONS);
        }
        ConnectionStringBuilder environmentCSB = settings.outUtils.getConnectionString(skipIfNoEventHubConnectionString);

        String effectiveEntityPath = settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.EH_PATH_OVERRIDE)
                ? settings.inoutEPHConstructorArgs.getEHPath()
                : environmentCSB.getEventHubName();

        String effectiveConsumerGroup = settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.CONSUMER_GROUP_OVERRIDE)
                ? settings.inoutEPHConstructorArgs.getConsumerGroupName()
                : EventHubClient.DEFAULT_CONSUMER_GROUP_NAME;

        String effectiveConnectionString = environmentCSB.toString();
        if (settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.EH_PATH_REPLACE_IN_CONNECTION)
            || settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.EH_CONNECTION_REMOVE_PATH)) {
            ConnectionStringBuilder replacedCSB = new ConnectionStringBuilder()
                    .setEndpoint(environmentCSB.getEndpoint())
                    .setEventHubName(
                            settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.EH_CONNECTION_REMOVE_PATH)
                                ? ""
                                : settings.inoutEPHConstructorArgs.getEHPath()
                    )
                    .setSasKeyName(environmentCSB.getSasKeyName())
                    .setSasKey(environmentCSB.getSasKey());
            replacedCSB.setOperationTimeout(environmentCSB.getOperationTimeout());
            effectiveConnectionString = replacedCSB.toString();
        }
        if (settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.EH_CONNECTION_OVERRIDE)) {
            effectiveConnectionString = settings.inoutEPHConstructorArgs.getEHConnection();
        }

        ScheduledExecutorService effectiveExecutor = settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.EXECUTOR_OVERRIDE)
                ? settings.inoutEPHConstructorArgs.getExecutor()
                : null;

        if (settings.inTelltaleOnTimeout) {
            settings.outTelltale = "";
        } else {
            settings.outTelltale = settings.getDefaultHostName() + "-telltale-" + EventProcessorHost.safeCreateUUID();
        }
        settings.outGeneralErrorHandler = new PrefabGeneralErrorHandler();
        settings.outProcessorFactory = new PrefabProcessorFactory(settings.outTelltale, settings.inDoCheckpoint, false, false);

        settings.inOptions.setExceptionNotification(settings.outGeneralErrorHandler);

        if (settings.inoutEPHConstructorArgs.useExplicitManagers()) {
            ICheckpointManager effectiveCheckpointManager = settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.CHECKPOINT_MANAGER_OVERRIDE)
                    ? settings.inoutEPHConstructorArgs.getCheckpointMananger()
                    : new BogusCheckpointMananger();
            ILeaseManager effectiveLeaseManager = settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.LEASE_MANAGER_OVERRIDE)
                    ? settings.inoutEPHConstructorArgs.getLeaseManager()
                    : new BogusLeaseManager();

            settings.outHost = EventProcessorHost.EventProcessorHostBuilder.newBuilder(effectiveHostName, effectiveConsumerGroup)
                .useUserCheckpointAndLeaseManagers(effectiveCheckpointManager, effectiveLeaseManager)
                .useEventHubConnectionString(effectiveConnectionString, effectiveEntityPath)
                .setExecutor(effectiveExecutor).build();
        } else {
            String effectiveStorageConnectionString = settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.STORAGE_CONNECTION_OVERRIDE)
                    ? settings.inoutEPHConstructorArgs.getStorageConnection()
                    : TestUtilities.getStorageConnectionString();

            String effectiveStorageContainerName = settings.getDefaultHostName().toLowerCase() + "-" + EventProcessorHost.safeCreateUUID();
            if (settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.STORAGE_CONTAINER_OVERRIDE)) {
                effectiveStorageContainerName = settings.inoutEPHConstructorArgs.getStorageContainerName();
                if (effectiveStorageContainerName != null) {
                    effectiveStorageContainerName = effectiveStorageContainerName.toLowerCase();
                }
            } else {
                settings.inoutEPHConstructorArgs.setDefaultStorageContainerName(effectiveStorageContainerName);
            }

            String effectiveBlobPrefix = settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.STORAGE_BLOB_PREFIX_OVERRIDE)
                    ? settings.inoutEPHConstructorArgs.getStorageBlobPrefix()
                    : null;

            AuthStep intermediate = EventProcessorHost.EventProcessorHostBuilder.newBuilder(effectiveHostName, effectiveConsumerGroup)
                    .useAzureStorageCheckpointLeaseManager(effectiveStorageConnectionString, effectiveStorageContainerName, effectiveBlobPrefix);
            OptionalStep almostDone = null;
            if (settings.inoutEPHConstructorArgs.isFlagSet(PerTestSettings.EPHConstructorArgs.AUTH_CALLBACK)) {
                ConnectionStringBuilder csb = new ConnectionStringBuilder(effectiveConnectionString);
                almostDone = intermediate.useAADAuthentication(csb.getEndpoint(), effectiveEntityPath)
                        .useAuthenticationCallback(settings.inoutEPHConstructorArgs.getAuthCallback(), settings.inoutEPHConstructorArgs.getAuthAuthority());
            } else {
                almostDone = intermediate.useEventHubConnectionString(effectiveConnectionString, effectiveEntityPath);
            }
            settings.outHost = almostDone.setExecutor(effectiveExecutor).build();
        }

        if (!settings.inEventHubDoesNotExist) {
            settings.outHost.registerEventProcessorFactory(settings.outProcessorFactory, settings.inOptions).get();
        }

        return settings;
    }

    void waitForTelltale(PerTestSettings settings) throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            if (settings.outProcessorFactory.getAnyTelltaleFound()) {
                TestBase.logInfo("Telltale found\n");
                break;
            }
            Thread.sleep(5000);
        }
    }

    void waitForTelltale(PerTestSettings settings, String partitionId) throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            if (settings.outProcessorFactory.getTelltaleFound(partitionId)) {
                TestBase.logInfo("Telltale " + partitionId + " found\n");
                break;
            }
            Thread.sleep(5000);
        }
    }

    void testFinish(PerTestSettings settings, int expectedEvents) throws InterruptedException, ExecutionException, EventHubException {
        if ((settings.outHost != null) && !settings.inEventHubDoesNotExist) {
            settings.outHost.unregisterEventProcessor().get();
            TestBase.logInfo("Host unregistered");
        }

        if (expectedEvents != NO_CHECKS) {
            TestBase.logInfo("Events received: " + settings.outProcessorFactory.getEventsReceivedCount() + "\n");
            if (expectedEvents == ANY_NONZERO_COUNT) {
                assertTrue("no events received", settings.outProcessorFactory.getEventsReceivedCount() > 0);
            } else if (expectedEvents != SKIP_COUNT_CHECK) {
                assertEquals("wrong number of events received", expectedEvents, settings.outProcessorFactory.getEventsReceivedCount());
            }

            assertTrue("telltale event was not found", settings.outProcessorFactory.getAnyTelltaleFound());
            assertEquals("partition errors seen", 0, settings.outProcessorFactory.getErrors().size());
            assertEquals("general errors seen", 0, settings.outGeneralErrorHandler.getErrors().size());
            for (String err : settings.outProcessorFactory.getErrors()) {
                logError(err);
            }
            for (String err : settings.outGeneralErrorHandler.getErrors()) {
                logError(err);
            }
        }

        settings.outUtils.shutdown();
    }

    class BogusCheckpointMananger implements ICheckpointManager {
        @Override
        public CompletableFuture<Boolean> checkpointStoreExists() {
            return CompletableFuture.completedFuture(true);
        }

        @Override
        public CompletableFuture<Void> createCheckpointStoreIfNotExists() {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Void> deleteCheckpointStore() {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Checkpoint> getCheckpoint(String partitionId) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Void> createAllCheckpointsIfNotExists(List<String> partitionIds) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Void> updateCheckpoint(CompleteLease lease, Checkpoint checkpoint) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Void> deleteCheckpoint(String partitionId) {
            return CompletableFuture.completedFuture(null);
        }
    }

    class BogusLeaseManager implements ILeaseManager {
        @Override
        public int getLeaseDurationInMilliseconds() {
            return 0;
        }

        @Override
        public CompletableFuture<Boolean> leaseStoreExists() {
            return CompletableFuture.completedFuture(true);
        }

        @Override
        public CompletableFuture<Void> createLeaseStoreIfNotExists() {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Void> deleteLeaseStore() {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<CompleteLease> getLease(String partitionId) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<List<BaseLease>> getAllLeases() {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Void> createAllLeasesIfNotExists(List<String> partitionIds) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Void> deleteLease(CompleteLease lease) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Boolean> acquireLease(CompleteLease lease) {
            return CompletableFuture.completedFuture(true);
        }

        @Override
        public CompletableFuture<Boolean> renewLease(CompleteLease lease) {
            return CompletableFuture.completedFuture(true);
        }

        @Override
        public CompletableFuture<Void> releaseLease(CompleteLease lease) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Boolean> updateLease(CompleteLease lease) {
            return CompletableFuture.completedFuture(true);
        }
    }
}
