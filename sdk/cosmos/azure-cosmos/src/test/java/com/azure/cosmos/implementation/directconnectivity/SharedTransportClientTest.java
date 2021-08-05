// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.LifeCycleUtils;
import com.azure.cosmos.implementation.UserAgentContainer;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SharedTransportClientTest {
    @Test(groups = { "unit" })
    public void createTwoClient_SharedReference() {
        TransportClient transportClient1 = null;
        TransportClient transportClient2 = null;
        try {
            transportClient1 = SharedTransportClient.getOrCreateInstance(Protocol.TCP, new Configs(), ConnectionPolicy.getDefaultPolicy(), new UserAgentContainer(), new DiagnosticsClientContext.DiagnosticsClientConfig(), null);
            transportClient2 = SharedTransportClient.getOrCreateInstance(Protocol.TCP, new Configs(), ConnectionPolicy.getDefaultPolicy(), new UserAgentContainer(), new DiagnosticsClientContext.DiagnosticsClientConfig(), null);

            assertThat(transportClient2).isSameAs(transportClient1);
            assertThat(((SharedTransportClient) transportClient1).getReferenceCounter()).isEqualTo(2);
        } finally {
            LifeCycleUtils.closeQuietly(transportClient1);
            LifeCycleUtils.closeQuietly(transportClient2);
        }
    }

    @Test(groups = { "unit" })
    public void createTwoHttpsClient_SharedReference() {
        TransportClient transportClient1 = null;
        TransportClient transportClient2 = null;
        try {
            transportClient1 = SharedTransportClient.getOrCreateInstance(Protocol.HTTPS, new Configs(), ConnectionPolicy.getDefaultPolicy(), new UserAgentContainer(), new DiagnosticsClientContext.DiagnosticsClientConfig(), null);
            transportClient2 = SharedTransportClient.getOrCreateInstance(Protocol.HTTPS, new Configs(), ConnectionPolicy.getDefaultPolicy(), new UserAgentContainer(), new DiagnosticsClientContext.DiagnosticsClientConfig(), null);

            assertThat(transportClient2).isSameAs(transportClient1);
            assertThat(((SharedTransportClient) transportClient1).getReferenceCounter()).isEqualTo(2);
        } finally {
            LifeCycleUtils.closeQuietly(transportClient1);
            LifeCycleUtils.closeQuietly(transportClient2);
        }
    }

    @Test(groups = { "unit" })
    public void createTwoClient_CloseOne_CreateAnotherClient_SharedReference() throws Exception {
        TransportClient transportClient1 = null;
        TransportClient transportClient2 = null;
        TransportClient transportClient3 = null;

        try {
            transportClient1 = SharedTransportClient.getOrCreateInstance(Protocol.TCP, new Configs(), ConnectionPolicy.getDefaultPolicy(), new UserAgentContainer(), new DiagnosticsClientContext.DiagnosticsClientConfig(), null);
            transportClient2 = SharedTransportClient.getOrCreateInstance(Protocol.TCP, new Configs(), ConnectionPolicy.getDefaultPolicy(), new UserAgentContainer(), new DiagnosticsClientContext.DiagnosticsClientConfig(), null);
            transportClient2.close();
            assertThat(((SharedTransportClient) transportClient1).getReferenceCounter()).isEqualTo(1);

            transportClient3 = SharedTransportClient.getOrCreateInstance(Protocol.TCP, new Configs(), ConnectionPolicy.getDefaultPolicy(), new UserAgentContainer(), new DiagnosticsClientContext.DiagnosticsClientConfig(), null);
            assertThat(transportClient3).isSameAs(transportClient1);
            assertThat(((SharedTransportClient) transportClient1).getReferenceCounter()).isEqualTo(2);
        } finally {
            LifeCycleUtils.closeQuietly(transportClient1);
            LifeCycleUtils.closeQuietly(transportClient3);
        }
    }

    @Test(groups = { "unit" })
    public void createTwoClient_CloseBoth_ReCreateClient_NewReference() throws Exception {
        TransportClient transportClient1 = null;
        TransportClient transportClient2 = null;
        TransportClient transportClient3 = null;

        try {
            transportClient1 = SharedTransportClient.getOrCreateInstance(Protocol.TCP, new Configs(), ConnectionPolicy.getDefaultPolicy(), new UserAgentContainer(), new DiagnosticsClientContext.DiagnosticsClientConfig(), null);
            transportClient2 = SharedTransportClient.getOrCreateInstance(Protocol.TCP, new Configs(), ConnectionPolicy.getDefaultPolicy(), new UserAgentContainer(), new DiagnosticsClientContext.DiagnosticsClientConfig(), null);
            transportClient1.close();
            transportClient2.close();
            assertThat(((SharedTransportClient) transportClient1).getReferenceCounter()).isEqualTo(0);

            transportClient3 = SharedTransportClient.getOrCreateInstance(Protocol.TCP, new Configs(), ConnectionPolicy.getDefaultPolicy(), new UserAgentContainer(), new DiagnosticsClientContext.DiagnosticsClientConfig(), null);
            assertThat(transportClient3).isNotSameAs(transportClient1);
            assertThat(((SharedTransportClient) transportClient3).getReferenceCounter()).isEqualTo(1);
        } finally {
            LifeCycleUtils.closeQuietly(transportClient3);
        }
    }
}
