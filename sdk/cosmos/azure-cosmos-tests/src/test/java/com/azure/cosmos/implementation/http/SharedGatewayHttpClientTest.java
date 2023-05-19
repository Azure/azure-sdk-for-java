// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.http;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.LifeCycleUtils;
import org.testng.annotations.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SharedGatewayHttpClientTest {
    @Test(groups = { "unit" })
    public void createTwoClient_SharedReference() {
        HttpClient httpClient1 = null;
        HttpClient httpClient2 = null;
        try {
            httpClient1 = SharedGatewayHttpClient.getOrCreateInstance(new HttpClientConfig(new Configs()), new DiagnosticsClientContext.DiagnosticsClientConfig());
            httpClient2 = SharedGatewayHttpClient.getOrCreateInstance(new HttpClientConfig(new Configs()), new DiagnosticsClientContext.DiagnosticsClientConfig());

            assertThat(httpClient2).isSameAs(httpClient1);
            assertThat(((SharedGatewayHttpClient) httpClient1).getReferenceCounter()).isEqualTo(2);
        } finally {
            LifeCycleUtils.closeQuietly(httpClient1);
            LifeCycleUtils.closeQuietly(httpClient2);
        }
    }

    @Test(groups = { "unit" })
    public void createTwoHttpsClient_SharedReference() {
        HttpClient httpClient1 = null;
        HttpClient httpClient2 = null;
        try {
            httpClient1 = SharedGatewayHttpClient.getOrCreateInstance(new HttpClientConfig(new Configs()), new DiagnosticsClientContext.DiagnosticsClientConfig());
            httpClient2 = SharedGatewayHttpClient.getOrCreateInstance(new HttpClientConfig(new Configs()), new DiagnosticsClientContext.DiagnosticsClientConfig());

            assertThat(httpClient2).isSameAs(httpClient1);
            assertThat(((SharedGatewayHttpClient) httpClient1).getReferenceCounter()).isEqualTo(2);
        } finally {
            LifeCycleUtils.closeQuietly(httpClient1);
            LifeCycleUtils.closeQuietly(httpClient2);
        }
    }

    @Test(groups = { "unit" })
    public void createTwoClient_CloseOne_CreateAnotherClient_SharedReference() throws Exception {
        HttpClient httpClient1 = null;
        HttpClient httpClient2 = null;
        HttpClient httpClient3 = null;

        try {
            httpClient1 = SharedGatewayHttpClient.getOrCreateInstance(new HttpClientConfig(new Configs()), new DiagnosticsClientContext.DiagnosticsClientConfig());
            httpClient2 = SharedGatewayHttpClient.getOrCreateInstance(new HttpClientConfig(new Configs()), new DiagnosticsClientContext.DiagnosticsClientConfig());

            httpClient2.shutdown();
            assertThat(((SharedGatewayHttpClient) httpClient1).getReferenceCounter()).isEqualTo(1);

            httpClient3 = SharedGatewayHttpClient.getOrCreateInstance(new HttpClientConfig(new Configs()), new DiagnosticsClientContext.DiagnosticsClientConfig());
            assertThat(httpClient3).isSameAs(httpClient1);
            assertThat(((SharedGatewayHttpClient) httpClient1).getReferenceCounter()).isEqualTo(2);
        } finally {
            LifeCycleUtils.closeQuietly(httpClient1);
            LifeCycleUtils.closeQuietly(httpClient3);
        }
    }

    @Test(groups = { "unit" })
    public void createTwoClient_CloseBoth_ReCreateClient_NewReference() throws Exception {
        HttpClient httpClient1 = null;
        HttpClient httpClient2 = null;
        HttpClient httpClient3 = null;

        try {
            httpClient1 = SharedGatewayHttpClient.getOrCreateInstance(new HttpClientConfig(new Configs()), new DiagnosticsClientContext.DiagnosticsClientConfig());
            httpClient2 = SharedGatewayHttpClient.getOrCreateInstance(new HttpClientConfig(new Configs()), new DiagnosticsClientContext.DiagnosticsClientConfig());
            httpClient1.shutdown();
            httpClient2.shutdown();
            assertThat(((SharedGatewayHttpClient) httpClient1).getReferenceCounter()).isEqualTo(0);

            httpClient3 = SharedGatewayHttpClient.getOrCreateInstance(new HttpClientConfig(new Configs()), new DiagnosticsClientContext.DiagnosticsClientConfig());
            assertThat(httpClient3).isNotSameAs(httpClient1);
            assertThat(((SharedGatewayHttpClient) httpClient3).getReferenceCounter()).isEqualTo(1);
        } finally {
            LifeCycleUtils.closeQuietly(httpClient3);
        }
    }
}
