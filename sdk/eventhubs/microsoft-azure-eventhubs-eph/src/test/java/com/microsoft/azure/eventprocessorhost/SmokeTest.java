// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ClientSecret;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.azure.eventhubs.AzureActiveDirectoryTokenProvider;
import com.microsoft.azure.eventhubs.EventPosition;

import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;


public class SmokeTest extends TestBase {
    @Test
    public void sendRecv1MsgTest() throws Exception {
        PerTestSettings settings = new PerTestSettings("SendRecv1Msg");
        settings = testSetup(settings);

        settings.outUtils.sendToAny(settings.outTelltale);
        waitForTelltale(settings);

        testFinish(settings, SmokeTest.ANY_NONZERO_COUNT);
    }
    
    /**
     * This JUnit test case is all commented out by default because it can only be run with special setup. 
     * It extracts the namespace (endpoint) and event hub name from the connection string in the environment variable
     * which all test cases use, but it assumes that the namespace (or event hub) has been set up with special permissions.
     * Within the AAD directory indicated by "authority", there is a registered application with id "clientId" and a secret
     * "clientSecret". This application has been granted the "Azure Event Hubs Data Owner" role on the namespace or
     * event hub.
     */
    //@Test
    public void sendRecv1MsgAADTest() throws Exception {
        PerTestSettings settings = new PerTestSettings("SendRecv1MsgAAD");
        AzureActiveDirectoryTokenProvider.AuthenticationCallback authCallback = new MsalAuthCallback();
        String authAuthority = "https://login.windows.net/replaceWithTenantIdGuid";
        settings.inoutEPHConstructorArgs.setAuthCallback(authCallback, authAuthority);
        settings = testSetup(settings);

        settings.outUtils.sendToAny(settings.outTelltale);
        waitForTelltale(settings);

        testFinish(settings, SmokeTest.ANY_NONZERO_COUNT);
    }
    
    private class MsalAuthCallback implements AzureActiveDirectoryTokenProvider.AuthenticationCallback {
        private final String clientId = "replaceWithClientIdGuid";
        private final String clientSecret = "replaceWithClientSecret";
        
        @Override
        public CompletableFuture<String> acquireToken(String audience, String authority, Object state) {
            try {
                ConfidentialClientApplication app = ConfidentialClientApplication.builder(this.clientId, new ClientSecret(this.clientSecret))
                        .authority(authority)
                        .build();
                
                ClientCredentialParameters parameters = ClientCredentialParameters.builder(Collections.singleton(audience + ".default")).build();
    
                IAuthenticationResult result = app.acquireToken(parameters).get();
    
                return CompletableFuture.completedFuture(result.accessToken());
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }
    }

    @Test
    public void receiverRuntimeMetricsTest() throws Exception {
        PerTestSettings settings = new PerTestSettings("ReceiverRuntimeMetrics");
        settings.inOptions.setReceiverRuntimeMetricEnabled(true);
        settings = testSetup(settings);

        settings.outUtils.sendToAny(settings.outTelltale);
        waitForTelltale(settings);

        // correctness of runtimeInfo is already tested in javaclient - this is only testing for presence of non-default value
        Assert.assertTrue(settings.outProcessorFactory.getOnEventsContext().getRuntimeInformation() != null);
        Assert.assertTrue(settings.outProcessorFactory.getOnEventsContext().getRuntimeInformation().getLastEnqueuedSequenceNumber() > 0);

        testFinish(settings, SmokeTest.ANY_NONZERO_COUNT);
    }

    @Test
    public void receiveFromNowTest() throws Exception {
        // Doing two iterations with the same "now" requires storing the "now" value instead of
        // using the current time when the initial offset provider is executed. It also requires
        // that the "now" be before the first send.
        final Instant storedNow = Instant.now();

        // Do the first iteration.
        PerTestSettings firstSettings = receiveFromNowIteration(storedNow, 1, 1, null);

        // Do a second iteration with the same "now". Because the first iteration does not checkpoint,
        // it should receive the telltale from the first iteration AND the telltale from this iteration.
        // The purpose of running a second iteration is to look for bugs that occur when leases have been
        // created and persisted but checkpoints have not, so it is vital that the second iteration uses the
        // same storage container.
        receiveFromNowIteration(storedNow, 2, 2, firstSettings.inoutEPHConstructorArgs.getStorageContainerName());
    }

    private PerTestSettings receiveFromNowIteration(final Instant storedNow, int iteration, int expectedEvents, String containerName) throws Exception {
        PerTestSettings settings = new PerTestSettings("receiveFromNow-iter-" + iteration);
        if (containerName != null) {
            settings.inoutEPHConstructorArgs.setStorageContainerName(containerName);
        }
        settings.inOptions.setInitialPositionProvider((partitionId) -> {
            return EventPosition.fromEnqueuedTime(storedNow);
        });
        settings = testSetup(settings);

        settings.outUtils.sendToAny(settings.outTelltale);
        waitForTelltale(settings);

        testFinish(settings, expectedEvents);

        return settings;
    }

    @Test
    public void receiveFromCheckpoint() throws Exception {
        PerTestSettings firstSettings = receiveFromCheckpointIteration(1, SmokeTest.ANY_NONZERO_COUNT, null, PrefabEventProcessor.CheckpointChoices.CKP_EXPLICIT);

        receiveFromCheckpointIteration(2, firstSettings.outPartitionIds.size(), firstSettings.inoutEPHConstructorArgs.getStorageContainerName(),
            firstSettings.inDoCheckpoint);
    }

    @Test
    public void receiveFromCheckpointNoArgs() throws Exception {
        PerTestSettings firstSettings = receiveFromCheckpointIteration(1, SmokeTest.ANY_NONZERO_COUNT, null, PrefabEventProcessor.CheckpointChoices.CKP_NOARGS);

        receiveFromCheckpointIteration(2, firstSettings.outPartitionIds.size(), firstSettings.inoutEPHConstructorArgs.getStorageContainerName(),
            firstSettings.inDoCheckpoint);
    }

    private PerTestSettings receiveFromCheckpointIteration(int iteration, int expectedEvents, String containerName,
                                                           PrefabEventProcessor.CheckpointChoices checkpointCallType) throws Exception {
        String distinguisher = "e";
        if (checkpointCallType == PrefabEventProcessor.CheckpointChoices.CKP_NOARGS) {
            distinguisher = "n";
        }
        PerTestSettings settings = new PerTestSettings("recvFromCkpt-" + iteration + "-" + distinguisher);
        if (containerName != null) {
            settings.inoutEPHConstructorArgs.setStorageContainerName(containerName);
        }
        settings.inDoCheckpoint = checkpointCallType;
        settings = testSetup(settings);

        for (String id : settings.outPartitionIds) {
            settings.outUtils.sendToPartition(id, settings.outTelltale);
            waitForTelltale(settings, id);
        }

        testFinish(settings, expectedEvents);

        return settings;
    }

    @Test
    public void receiveInvokeOnTimeout() throws Exception {
        PerTestSettings settings = new PerTestSettings("receiveInvokeOnTimeout");
        settings.inOptions.setInvokeProcessorAfterReceiveTimeout(true);
        settings.inTelltaleOnTimeout = true;
        settings.inHasSenders = false;
        settings = testSetup(settings);

        waitForTelltale(settings, "0");

        testFinish(settings, SmokeTest.SKIP_COUNT_CHECK);
    }

    @Test
    public void receiveNotInvokeOnTimeout() throws Exception {
        PerTestSettings settings = new PerTestSettings("receiveNotInvokeOnTimeout");
        settings = testSetup(settings);

        // Receive timeout is one minute. If event processor is invoked on timeout, it will
        // record an error that will fail the case on shutdown.
        Thread.sleep(120 * 1000);

        settings.outUtils.sendToAny(settings.outTelltale);
        waitForTelltale(settings);

        testFinish(settings, SmokeTest.ANY_NONZERO_COUNT);
    }

    @Test
    public void receiveAllPartitionsTest() throws Exception {
        // Save "now" to avoid race with sender startup.
        final Instant savedNow = Instant.now();

        PerTestSettings settings = new PerTestSettings("receiveAllPartitions");
        settings.inOptions.setInitialPositionProvider((partitionId) -> {
            return EventPosition.fromEnqueuedTime(savedNow);
        });
        settings = testSetup(settings);

        final int maxGeneration = 10;
        for (int generation = 0; generation < maxGeneration; generation++) {
            for (String id : settings.outPartitionIds) {
                settings.outUtils.sendToPartition(id, "receiveAllPartitions-" + id + "-" + generation);
            }
            TestBase.logInfo("Generation " + generation + " sent\n");
        }
        for (String id : settings.outPartitionIds) {
            settings.outUtils.sendToPartition(id, settings.outTelltale);
            TestBase.logInfo("Telltale " + id + " sent\n");
        }
        for (String id : settings.outPartitionIds) {
            waitForTelltale(settings, id);
        }

        testFinish(settings, (settings.outPartitionIds.size() * (maxGeneration + 1))); // +1 for the telltales
    }

    @Test
    public void receiveAllPartitionsWithUserExecutorTest() throws Exception {
        // Save "now" to avoid race with sender startup.
        final Instant savedNow = Instant.now();

        PerTestSettings settings = new PerTestSettings("rcvAllPartsUserExec");
        settings.inOptions.setInitialPositionProvider((partitionId) -> {
            return EventPosition.fromEnqueuedTime(savedNow);
        });
        settings.inoutEPHConstructorArgs.setExecutor(Executors.newScheduledThreadPool(4));
        settings = testSetup(settings);

        final int maxGeneration = 10;
        for (int generation = 0; generation < maxGeneration; generation++) {
            for (String id : settings.outPartitionIds) {
                settings.outUtils.sendToPartition(id, "receiveAllPartitionsWithUserExecutor-" + id + "-" + generation);
            }
            TestBase.logInfo("Generation " + generation + " sent\n");
        }
        for (String id : settings.outPartitionIds) {
            settings.outUtils.sendToPartition(id, settings.outTelltale);
            TestBase.logInfo("Telltale " + id + " sent\n");
        }
        for (String id : settings.outPartitionIds) {
            waitForTelltale(settings, id);
        }

        testFinish(settings, (settings.outPartitionIds.size() * (maxGeneration + 1))); // +1 for the telltales
    }

    @Test
    public void receiveAllPartitionsWithSingleThreadExecutorTest() throws Exception {
        // Save "now" to avoid race with sender startup.
        final Instant savedNow = Instant.now();

        PerTestSettings settings = new PerTestSettings("rcvAllParts1ThrdExec");
        settings.inOptions.setInitialPositionProvider((partitionId) -> {
            return EventPosition.fromEnqueuedTime(savedNow);
        });
        settings.inoutEPHConstructorArgs.setExecutor(Executors.newSingleThreadScheduledExecutor());
        settings = testSetup(settings);

        final int maxGeneration = 10;
        for (int generation = 0; generation < maxGeneration; generation++) {
            for (String id : settings.outPartitionIds) {
                settings.outUtils.sendToPartition(id, "receiveAllPartitionsWithSingleThreadExecutor-" + id + "-" + generation);
            }
            TestBase.logInfo("Generation " + generation + " sent\n");
        }
        for (String id : settings.outPartitionIds) {
            settings.outUtils.sendToPartition(id, settings.outTelltale);
            TestBase.logInfo("Telltale " + id + " sent\n");
        }
        for (String id : settings.outPartitionIds) {
            waitForTelltale(settings, id);
        }

        testFinish(settings, (settings.outPartitionIds.size() * (maxGeneration + 1))); // +1 for the telltales
    }
}
