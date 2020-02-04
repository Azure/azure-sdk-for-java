// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConnectionPolicy;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.DatabaseAccount;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.AsyncDocumentClient.Builder;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.DatabaseAccountManagerInternal;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.TestSuiteBase;
import io.reactivex.subscribers.TestSubscriber;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class GatewayServiceConfigurationReaderTest extends TestSuiteBase {

    private static final int TIMEOUT = 8000;
    private AsyncDocumentClient client;

    @Factory(dataProvider = "clientBuilders")
    public GatewayServiceConfigurationReaderTest(Builder clientBuilder) {
        super(clientBuilder);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    @Test(groups = "simple")
    public void clientInitialization() throws Exception {
        client = this.clientBuilder().build();
        RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) client;
        GatewayServiceConfigurationReader serviceConfigurationReader = ReflectionUtils.getServiceConfigurationReader(rxDocumentClient);
        GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(serviceConfigurationReader);
        Mono<DatabaseAccount> databaseAccountMono = globalEndpointManager.getDatabaseAccountFromCache(new URI(TestConfigurations.HOST));
        validateSuccess(databaseAccountMono);
        assertThat(serviceConfigurationReader.getDefaultConsistencyLevel().block()).isNotNull();
        assertThat(serviceConfigurationReader.getQueryEngineConfiguration().block()).isNotNull();
        assertThat(serviceConfigurationReader.getSystemReplicationPolicy().block()).isNotNull();
        assertThat(serviceConfigurationReader.getSystemReplicationPolicy().block()).isNotNull();
    }

    @Test(groups = "simple")
    public void configurationPropertyReads() throws Exception {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.setUsingMultipleWriteLocations(true);
        DatabaseAccountManagerInternal databaseAccountManagerInternal = Mockito.mock(DatabaseAccountManagerInternal.class);
        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(Matchers.any())).thenReturn(Flux.just(new DatabaseAccount(GlobalEndPointManagerTest.dbAccountJson1)));
        GlobalEndpointManager globalEndpointManager = new GlobalEndpointManager(databaseAccountManagerInternal, connectionPolicy, new Configs());
        ReflectionUtils.setBackgroundRefreshLocationTimeIntervalInMS(globalEndpointManager, 1000);
        globalEndpointManager.init();

        GatewayServiceConfigurationReader configurationReader = new GatewayServiceConfigurationReader(new URI(TestConfigurations.HOST), globalEndpointManager);
        assertThat(configurationReader.getDefaultConsistencyLevel().block()).isEqualTo(ConsistencyLevel.SESSION);
        assertThat((boolean) configurationReader.getQueryEngineConfiguration().block().get("enableSpatialIndexing")).isTrue();
        assertThat(configurationReader.getSystemReplicationPolicy().block().getMaxReplicaSetSize()).isEqualTo(4);
        assertThat(configurationReader.getSystemReplicationPolicy().block().getMaxReplicaSetSize()).isEqualTo(4);

        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(Matchers.any())).thenReturn(Flux.just(new DatabaseAccount(GlobalEndPointManagerTest.dbAccountJson2)));
        Thread.sleep(2000);
        assertThat(configurationReader.getDefaultConsistencyLevel().block()).isEqualTo(ConsistencyLevel.EVENTUAL);
        assertThat((boolean) configurationReader.getQueryEngineConfiguration().block().get("enableSpatialIndexing")).isFalse();
        assertThat(configurationReader.getSystemReplicationPolicy().block().getMaxReplicaSetSize()).isEqualTo(5);
        assertThat(configurationReader.getSystemReplicationPolicy().block().getMaxReplicaSetSize()).isEqualTo(5);

        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(Matchers.any())).thenReturn(Flux.just(new DatabaseAccount(GlobalEndPointManagerTest.dbAccountJson3)));
        Thread.sleep(2000);
        assertThat(configurationReader.getDefaultConsistencyLevel().block()).isEqualTo(ConsistencyLevel.SESSION);
        assertThat((boolean) configurationReader.getQueryEngineConfiguration().block().get("enableSpatialIndexing")).isTrue();
        assertThat(configurationReader.getSystemReplicationPolicy().block().getMaxReplicaSetSize()).isEqualTo(4);
        assertThat(configurationReader.getSystemReplicationPolicy().block().getMaxReplicaSetSize()).isEqualTo(4);

        //Testing scenario of scheduled cache refresh with error
        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(Matchers.any())).thenThrow(BridgeInternal.createCosmosClientException(HttpConstants.StatusCodes.FORBIDDEN));
        Thread.sleep(2000);
        assertThat(configurationReader.getDefaultConsistencyLevel().block()).isEqualTo(ConsistencyLevel.SESSION);
        assertThat((boolean) configurationReader.getQueryEngineConfiguration().block().get("enableSpatialIndexing")).isTrue();
        assertThat(configurationReader.getSystemReplicationPolicy().block().getMaxReplicaSetSize()).isEqualTo(4);
        assertThat(configurationReader.getSystemReplicationPolicy().block().getMaxReplicaSetSize()).isEqualTo(4);
    }

    public static void validateSuccess(Mono<DatabaseAccount> observable) {
        TestSubscriber<DatabaseAccount> testSubscriber = new TestSubscriber<DatabaseAccount>();

        observable.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(TIMEOUT, TimeUnit.MILLISECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertComplete();
        testSubscriber.assertValueCount(1);
        DatabaseAccount databaseAccount = testSubscriber.values().get(0);
        assertThat(BridgeInternal.getQueryEngineConfiuration(databaseAccount).size() > 0).isTrue();
        assertThat(BridgeInternal.getReplicationPolicy(databaseAccount)).isNotNull();
        assertThat(BridgeInternal.getSystemReplicationPolicy(databaseAccount)).isNotNull();
    }
}
