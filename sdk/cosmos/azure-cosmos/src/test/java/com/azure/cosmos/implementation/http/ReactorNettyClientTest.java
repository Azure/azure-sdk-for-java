// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.http;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.LifeCycleUtils;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.rx.TestSuiteBase;
import io.netty.handler.timeout.ReadTimeoutException;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.ConnectionObserver;
import reactor.netty.http.client.HttpClientState;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class ReactorNettyClientTest extends TestSuiteBase {
    private RxDocumentClientImpl rxDocumentClient;
    private ReactorNettyClient reactorNettyClient;
    private reactor.netty.http.client.HttpClient httpClient;
    private HttpClientConfig httpClientConfig;
    private CosmosAsyncClient directClient;
    private CosmosAsyncClient gatewayClient;
    private CosmosAsyncDatabase cosmosAsyncDatabase;
    private CosmosAsyncContainer cosmosAsyncContainer;

    @BeforeClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        gatewayClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .gatewayMode()
            .buildAsyncClient();

        directClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .directMode()
            .buildAsyncClient();
    }

    @DataProvider(name = "cosmosClients")
    public Object[][] cosmosClients() {
        return new Object[][]{
            {gatewayClient}, {directClient}};
    }

    @Test(groups = {"emulator"}, dataProvider = "cosmosClients", timeOut = SETUP_TIMEOUT)
    public void httpClientRequestTimeout(CosmosAsyncClient cosmosAsyncClient) throws Exception {
        cosmosAsyncDatabase = getSharedCosmosDatabase(cosmosAsyncClient);
        cosmosAsyncContainer = getSharedMultiPartitionCosmosContainer(cosmosAsyncClient);
        rxDocumentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(cosmosAsyncClient);
        reactorNettyClient = (ReactorNettyClient) ReflectionUtils.getHttpClient(rxDocumentClient);
        httpClient = (reactor.netty.http.client.HttpClient) FieldUtils.readField(reactorNettyClient,
            "httpClient",
            true);
        httpClientConfig = (HttpClientConfig) FieldUtils.readField(reactorNettyClient,
            "httpClientConfig",
            true);

        reactor.netty.http.client.HttpClient spyClient = Mockito.spy(httpClient);
        FieldUtils.writeField(reactorNettyClient, "httpClient", spyClient, true);

        // lowering down the timeout for testing various scenarios.
        httpClientConfig = httpClientConfig.withRequestTimeout(Duration.ofMillis(1000));
        FieldUtils.writeField(reactorNettyClient, "httpClientConfig", httpClientConfig, true);

        //Attaching test observer to create timeouts
        TestConnectionObserver testConnectionObserver = new TestConnectionObserver();
        Mockito.doReturn(spyClient.observe(testConnectionObserver)).when(spyClient).observe(Mockito.any(ConnectionObserver.class));

        Mono<CosmosDatabaseResponse> databaseResponse = cosmosAsyncDatabase.read();

        // adding sleep > timeout on requestConfigured verifying failure
        testConnectionObserver.sleepOnConfigured = 1100;
        StepVerifier.create(databaseResponse)
            .expectSubscription()
            .consumeErrorWith(throwable -> {
                assertThat(throwable).isInstanceOf(CosmosException.class);
                assertThat(throwable.getCause()).isInstanceOf(ReadTimeoutException.class);
            })
            .verify();

        // adding sleep < timeout on requestConfigured verifying success
        testConnectionObserver.sleepOnConfigured = 500;
        StepVerifier.create(databaseResponse)
            .expectSubscription()
            .consumeNextWith(response -> {
                assertThat(response.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.OK);
            })
            .verifyComplete();


        Mono<CosmosContainerResponse> containerResponse = cosmosAsyncContainer.read();
        testConnectionObserver.sleepOnConfigured = 0;
        // adding sleep > timeout on requestPrepared verifying failure
        testConnectionObserver.sleepOnRequestPrepared = 1100;
        StepVerifier.create(containerResponse)
            .expectSubscription()
            .consumeErrorWith(throwable -> {
                assertThat(throwable).isInstanceOf(CosmosException.class);
                assertThat(throwable.getCause()).isInstanceOf(ReadTimeoutException.class);
            })
            .verify();

        // adding sleep < timeout on requestPrepared verifying success
        testConnectionObserver.sleepOnRequestPrepared = 500;
        StepVerifier.create(containerResponse)
            .expectSubscription()
            .consumeNextWith(response -> {
                assertThat(response.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.OK);
            })
            .verifyComplete();
    }

    @AfterClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void afterClass() {
        LifeCycleUtils.closeQuietly(this.gatewayClient);
        LifeCycleUtils.closeQuietly(this.directClient);
    }

    public class TestConnectionObserver implements ConnectionObserver {
        int sleepOnConfigured;
        int sleepOnRequestPrepared;

        @Override
        public void onStateChange(Connection connection, State state) {
            try {
                if (state.equals(HttpClientState.CONFIGURED)) {
                    Thread.sleep(sleepOnConfigured);
                } else if (state.equals(HttpClientState.REQUEST_PREPARED)) {
                    Thread.sleep(sleepOnRequestPrepared);
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}

