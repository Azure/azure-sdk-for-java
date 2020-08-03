// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.http;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.rx.TestSuiteBase;
import io.netty.handler.timeout.ReadTimeoutException;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.ConnectionObserver;
import reactor.netty.http.client.HttpClientState;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;

import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;

public class ReactorNettyClientTest extends TestSuiteBase {
    private CosmosAsyncClient gatewayClient;
    private RxDocumentClientImpl rxDocumentClient;
    private ReactorNettyClient reactorNettyClient;
    private reactor.netty.http.client.HttpClient httpClient;
    private HttpClientConfig httpClientConfig;
    private CosmosAsyncDatabase cosmosAsyncDatabase;

    @BeforeClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        assertThat(this.gatewayClient).isNull();
        GatewayConnectionConfig connectionConfig = new GatewayConnectionConfig();
        gatewayClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .gatewayMode(connectionConfig)
            .buildAsyncClient();
        cosmosAsyncDatabase = getSharedCosmosDatabase(gatewayClient);
        rxDocumentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(gatewayClient);
        reactorNettyClient = (ReactorNettyClient) ReflectionUtils.getHttpClient(rxDocumentClient);
        httpClient = (reactor.netty.http.client.HttpClient) FieldUtils.readField(reactorNettyClient,
            "httpClient",
            true);
        httpClientConfig = (HttpClientConfig) FieldUtils.readField(reactorNettyClient,
            "httpClientConfig",
            true);
    }

    @Test(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void httpClientRequestTimeout() throws Exception {
        reactor.netty.http.client.HttpClient spyClient = Mockito.spy(httpClient);
        FieldUtils.writeField(reactorNettyClient, "httpClient", spyClient, true);

        cosmosAsyncDatabase.read().block(); //warming up the connection chanel

        // lowering down the timeout for testing various scenarios.
        httpClientConfig = httpClientConfig.withRequestTimeout(Duration.ofMillis(1000));
        FieldUtils.writeField(reactorNettyClient, "httpClientConfig", httpClientConfig, true);

        TestConnectionObserver testConnectionObserver = new TestConnectionObserver();
        Mockito.doReturn(spyClient.observe(testConnectionObserver)).when(spyClient).observe(Mockito.any(ConnectionObserver.class));

        Mono<CosmosDatabaseResponse> response = cosmosAsyncDatabase.read();
        testConnectionObserver.sleepOnConnectedOrAcquired = 1100; // adding sleep > timeout on connected/acquired,
        // verifying failure
        StepVerifier.create(response)
            .expectSubscription()
            .consumeErrorWith(throwable -> {
                assertThat(throwable).isInstanceOf(CosmosException.class);
                assertThat(throwable.getCause()).isInstanceOf(ReadTimeoutException.class);
            })
            .verify();

        testConnectionObserver.sleepOnConnectedOrAcquired = 500; // adding sleep < timeout on connected/acquired,
        // verifying success
        StepVerifier.create(response)
            .expectSubscription()
            .consumeNextWith(databaseResponse -> {
                assertThat(databaseResponse.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.OK);
            })
            .verifyComplete();

        testConnectionObserver.sleepOnConnectedOrAcquired = 0;
        testConnectionObserver.sleepOnConfigured = 1100; //adding sleep > timeout on configures, verifying failure
        StepVerifier.create(response)
            .expectSubscription()
            .consumeErrorWith(throwable -> {
                assertThat(throwable).isInstanceOf(CosmosException.class);
                assertThat(throwable.getCause()).isInstanceOf(ReadTimeoutException.class);
            })
            .verify();

        testConnectionObserver.sleepOnConfigured = 500; //adding sleep < timeout on configures, verifying success
        StepVerifier.create(response)
            .expectSubscription()
            .consumeNextWith(databaseResponse -> {
                assertThat(databaseResponse.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.OK);
            })
            .verifyComplete();

        testConnectionObserver.sleepOnConfigured = 0;
        testConnectionObserver.sleepOnRequestSent = 1100; //adding sleep > timeout on requestSent, verifying failure
        StepVerifier.create(response)
            .expectSubscription()
            .consumeErrorWith(throwable -> {
                assertThat(throwable).isInstanceOf(CosmosException.class);
                assertThat(throwable.getCause()).isInstanceOf(ReadTimeoutException.class);
            })
            .verify();

        testConnectionObserver.sleepOnRequestSent = 500; //adding sleep < timeout on configures, verifying success
        StepVerifier.create(response)
            .expectSubscription()
            .consumeNextWith(databaseResponse -> {
                assertThat(databaseResponse.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.OK);
            })
            .verifyComplete();


        testConnectionObserver.sleepOnRequestSent = 0;
        testConnectionObserver.sleepOnResponseReceived = 1100; //adding sleep > timeout on responseReceived,
        // verifying failure
        StepVerifier.create(response)
            .expectSubscription()
            .consumeErrorWith(throwable -> {
                assertThat(throwable).isInstanceOf(CosmosException.class);
                assertThat(throwable.getCause()).isInstanceOf(ReadTimeoutException.class);
            })
            .verify();

        testConnectionObserver.sleepOnResponseReceived = 500; //adding sleep < timeout on responseReceived
        // verifying success
        StepVerifier.create(response)
            .expectSubscription()
            .consumeNextWith(databaseResponse -> {
                assertThat(databaseResponse.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.OK);
            })
            .verifyComplete();
    }

    public class TestConnectionObserver implements ConnectionObserver {
        int sleepOnConnectedOrAcquired;
        int sleepOnConfigured;
        int sleepOnRequestSent;
        int sleepOnResponseReceived;

        @Override
        public void onStateChange(Connection connection, State state) {
            try {
                if (state.equals(HttpClientState.CONNECTED) || state.equals(HttpClientState.ACQUIRED)) {
                    sleep(sleepOnConnectedOrAcquired);
                } else if (state.equals(HttpClientState.CONFIGURED)) {
                    sleep(sleepOnConfigured);
                } else if (state.equals(HttpClientState.REQUEST_SENT)) {
                    sleep(sleepOnRequestSent);
                } else if (state.equals(HttpClientState.RESPONSE_RECEIVED)) {
                    sleep(sleepOnResponseReceived);
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}

