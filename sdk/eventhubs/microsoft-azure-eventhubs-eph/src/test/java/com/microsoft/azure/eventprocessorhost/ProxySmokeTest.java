// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

import com.microsoft.azure.eventhubs.ProxyConfiguration;
import com.microsoft.azure.eventhubs.TransportType;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ProxySmokeTest extends TestBase {
    private final Set<String> containerNames = new HashSet<>();

    private static final int PARTITION_COUNT = 8;

    private ILeaseManager[] leaseManagers;
    private ICheckpointManager[] checkpointManagers;
    private EventProcessorHost[] hosts;
    private ProxyConfiguration proxyConfiguration;
    private String azureStorageConnectionString;

    @Before
    public void setup() {
        proxyConfiguration = TestUtilities.getProxyConfiguration();
        azureStorageConnectionString = TestUtilities.getStorageConnectionString();

        Assume.assumeTrue("Cannot test with proxy. Environment variable not set.",
            proxyConfiguration != null);
        Assume.assumeTrue("Cannot test with Azure storage. Environment variable not set.",
            azureStorageConnectionString != null);
    }

    @After
    public void teardown() {
        containerNames.clear();
    }

    @Test
    public void receiveMessages() throws Exception {
        PerTestSettings settings = new PerTestSettings("receiveMessages");

        final PerTestSettings.EPHConstructorArgs ephConstructorArgs = settings.inoutEPHConstructorArgs;
        ephConstructorArgs.setProxyConfiguration(proxyConfiguration);
        ephConstructorArgs.setTransportType(TransportType.AMQP_WEB_SOCKETS);

        settings = testSetup(settings);

        settings.outUtils.sendToAny(settings.outTelltale);
        waitForTelltale(settings);

        testFinish(settings, SmokeTest.ANY_NONZERO_COUNT);
    }

    /**
     * Generates a container name and adds it to the set of container names we are keeping track of.
     */
    private String generateContainerName() {
        final String containerName = "proxy-smoke-test-" + UUID.randomUUID().toString();

        containerNames.add(containerName);
        return containerName;
    }
}
