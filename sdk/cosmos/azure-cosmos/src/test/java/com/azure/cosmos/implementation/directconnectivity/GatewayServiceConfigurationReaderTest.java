// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConnectionPolicy;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosKeyCredential;
import com.azure.cosmos.DatabaseAccount;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.AsyncDocumentClient.Builder;
import com.azure.cosmos.implementation.BaseAuthorizationTokenProvider;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.DatabaseAccountManagerInternal;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.SpyClientUnderTestFactory;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.TestSuiteBase;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.reactivex.subscribers.TestSubscriber;
import org.apache.commons.io.IOUtils;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class GatewayServiceConfigurationReaderTest extends TestSuiteBase {

    private static final int TIMEOUT = 8000;
    private HttpClient mockHttpClient;
    private BaseAuthorizationTokenProvider baseAuthorizationTokenProvider;
    private ConnectionPolicy connectionPolicy;
    private GatewayServiceConfigurationReader mockGatewayServiceConfigurationReader;
    private GatewayServiceConfigurationReader gatewayServiceConfigurationReader;
    private AsyncDocumentClient client;
    private String databaseAccountJson;
    private DatabaseAccount expectedDatabaseAccount;

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
        assertThat(serviceConfigurationReader.getDefaultConsistencyLevel()).isNotNull();
        assertThat(serviceConfigurationReader.getQueryEngineConfiguration()).isNotNull();
        assertThat(serviceConfigurationReader.getSystemReplicationPolicy()).isNotNull();
        assertThat(serviceConfigurationReader.getSystemReplicationPolicy()).isNotNull();
    }

    @Test(groups = "simple")
    public void configurationPropertyReads() throws Exception {
        DatabaseAccountManagerInternal databaseAccountManagerInternal = Mockito.mock(DatabaseAccountManagerInternal.class);
        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(Matchers.any())).thenReturn(Flux.just(new DatabaseAccount(GlobalEndPointManagerTest.dbAccountJson1)));
        Mockito.when(databaseAccountManagerInternal.getServiceEndpoint()).thenReturn(new URI(TestConfigurations.HOST));
        GlobalEndpointManager globalEndpointManager = new GlobalEndpointManager(databaseAccountManagerInternal, new ConnectionPolicy(), new Configs());
        ReflectionUtils.setBackgroundRefreshLocationTimeIntervalInMS(globalEndpointManager, 1000);
        globalEndpointManager.init();

        GatewayServiceConfigurationReader configurationReader = new GatewayServiceConfigurationReader(new URI(TestConfigurations.HOST), globalEndpointManager);
        assertThat(configurationReader.getDefaultConsistencyLevel()).isEqualTo(ConsistencyLevel.SESSION);
        assertThat((boolean) configurationReader.getQueryEngineConfiguration().get("enableSpatialIndexing")).isTrue();
        assertThat(configurationReader.getSystemReplicationPolicy().getMaxReplicaSetSize()).isEqualTo(4);
        assertThat(configurationReader.getUserReplicationPolicy().getMaxReplicaSetSize()).isEqualTo(4);

        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(Matchers.any())).thenReturn(Flux.just(new DatabaseAccount(GlobalEndPointManagerTest.dbAccountJson2)));
        Thread.sleep(2000);
        assertThat(configurationReader.getDefaultConsistencyLevel()).isEqualTo(ConsistencyLevel.EVENTUAL);
        assertThat((boolean) configurationReader.getQueryEngineConfiguration().get("enableSpatialIndexing")).isFalse();
        assertThat(configurationReader.getSystemReplicationPolicy().getMaxReplicaSetSize()).isEqualTo(5);
        assertThat(configurationReader.getUserReplicationPolicy().getMaxReplicaSetSize()).isEqualTo(5);

        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(Matchers.any())).thenReturn(Flux.just(new DatabaseAccount(GlobalEndPointManagerTest.dbAccountJson3)));
        Thread.sleep(2000);
        assertThat(configurationReader.getDefaultConsistencyLevel()).isEqualTo(ConsistencyLevel.SESSION);
        assertThat((boolean) configurationReader.getQueryEngineConfiguration().get("enableSpatialIndexing")).isTrue();
        assertThat(configurationReader.getSystemReplicationPolicy().getMaxReplicaSetSize()).isEqualTo(4);
        assertThat(configurationReader.getUserReplicationPolicy().getMaxReplicaSetSize()).isEqualTo(4);

        //Testing scenario of scheduled cache refresh with error
        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(Matchers.any())).thenReturn(Flux.error(BridgeInternal.createCosmosClientException(HttpConstants.StatusCodes.FORBIDDEN)));
        Thread.sleep(2000);
        assertThat(configurationReader.getDefaultConsistencyLevel()).isEqualTo(ConsistencyLevel.SESSION);
        assertThat((boolean) configurationReader.getQueryEngineConfiguration().get("enableSpatialIndexing")).isTrue();
        assertThat(configurationReader.getSystemReplicationPolicy().getMaxReplicaSetSize()).isEqualTo(4);
        assertThat(configurationReader.getUserReplicationPolicy().getMaxReplicaSetSize()).isEqualTo(4);
    }

    @Test(groups = "simple")
    public void configurationPropertyReadsViaCache() throws Exception {
        DatabaseAccountManagerInternal databaseAccountManagerInternal = Mockito.mock(DatabaseAccountManagerInternal.class);
        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(Matchers.any())).thenReturn(Flux.just(new DatabaseAccount(GlobalEndPointManagerTest.dbAccountJson1)));
        Mockito.when(databaseAccountManagerInternal.getServiceEndpoint()).thenReturn(new URI(TestConfigurations.HOST));
        GlobalEndpointManager globalEndpointManager = new GlobalEndpointManager(databaseAccountManagerInternal, new ConnectionPolicy(), new Configs());
        ReflectionUtils.setBackgroundRefreshLocationTimeIntervalInMS(globalEndpointManager, 1000);
        globalEndpointManager.init();

        assertThat(BridgeInternal.getConsistencyPolicy(globalEndpointManager.getLatestDatabaseAccount()).getDefaultConsistencyLevel()).isEqualTo(ConsistencyLevel.SESSION);
        assertThat((boolean) BridgeInternal.getQueryEngineConfiuration(globalEndpointManager.getLatestDatabaseAccount()).get("enableSpatialIndexing")).isTrue();
        assertThat(BridgeInternal.getSystemReplicationPolicy(globalEndpointManager.getLatestDatabaseAccount()).getMaxReplicaSetSize()).isEqualTo(4);
        assertThat(BridgeInternal.getReplicationPolicy(globalEndpointManager.getLatestDatabaseAccount()).getMaxReplicaSetSize()).isEqualTo(4);

        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(Matchers.any())).thenReturn(Flux.just(new DatabaseAccount(GlobalEndPointManagerTest.dbAccountJson2)));
        Thread.sleep(2000);
        assertThat(BridgeInternal.getConsistencyPolicy(globalEndpointManager.getLatestDatabaseAccount()).getDefaultConsistencyLevel()).isEqualTo(ConsistencyLevel.EVENTUAL);
        assertThat((boolean) BridgeInternal.getQueryEngineConfiuration(globalEndpointManager.getLatestDatabaseAccount()).get("enableSpatialIndexing")).isFalse();
        assertThat(BridgeInternal.getSystemReplicationPolicy(globalEndpointManager.getLatestDatabaseAccount()).getMaxReplicaSetSize()).isEqualTo(5);
        assertThat(BridgeInternal.getReplicationPolicy(globalEndpointManager.getLatestDatabaseAccount()).getMaxReplicaSetSize()).isEqualTo(5);

        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(Matchers.any())).thenReturn(Flux.just(new DatabaseAccount(GlobalEndPointManagerTest.dbAccountJson3)));
        Thread.sleep(2000);
        assertThat(BridgeInternal.getConsistencyPolicy(globalEndpointManager.getLatestDatabaseAccount()).getDefaultConsistencyLevel()).isEqualTo(ConsistencyLevel.SESSION);
        assertThat((boolean) BridgeInternal.getQueryEngineConfiuration(globalEndpointManager.getLatestDatabaseAccount()).get("enableSpatialIndexing")).isTrue();
        assertThat(BridgeInternal.getSystemReplicationPolicy(globalEndpointManager.getLatestDatabaseAccount()).getMaxReplicaSetSize()).isEqualTo(4);
        assertThat(BridgeInternal.getReplicationPolicy(globalEndpointManager.getLatestDatabaseAccount()).getMaxReplicaSetSize()).isEqualTo(4);

        //Testing scenario of scheduled cache refresh with error
        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(Matchers.any())).thenReturn(Flux.error(BridgeInternal.createCosmosClientException(HttpConstants.StatusCodes.FORBIDDEN)));
        Thread.sleep(2000);
        assertThat(BridgeInternal.getConsistencyPolicy(globalEndpointManager.getLatestDatabaseAccount()).getDefaultConsistencyLevel()).isEqualTo(ConsistencyLevel.SESSION);
        assertThat((boolean) BridgeInternal.getQueryEngineConfiuration(globalEndpointManager.getLatestDatabaseAccount()).get("enableSpatialIndexing")).isTrue();
        assertThat(BridgeInternal.getSystemReplicationPolicy(globalEndpointManager.getLatestDatabaseAccount()).getMaxReplicaSetSize()).isEqualTo(4);
        assertThat(BridgeInternal.getReplicationPolicy(globalEndpointManager.getLatestDatabaseAccount()).getMaxReplicaSetSize()).isEqualTo(4);
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
