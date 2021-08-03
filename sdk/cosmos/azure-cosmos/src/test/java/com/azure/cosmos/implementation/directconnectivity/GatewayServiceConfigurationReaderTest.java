// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.AsyncDocumentClient.Builder;
import com.azure.cosmos.implementation.BaseAuthorizationTokenProvider;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.DatabaseAccountManagerInternal;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.LifeCycleUtils;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.TestSuiteBase;
import com.azure.cosmos.implementation.http.HttpClient;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.net.URI;

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
        assertThat(serviceConfigurationReader.getDefaultConsistencyLevel()).isNotNull();
        assertThat(serviceConfigurationReader.getQueryEngineConfiguration()).isNotNull();
        assertThat(serviceConfigurationReader.getSystemReplicationPolicy()).isNotNull();
        assertThat(serviceConfigurationReader.getSystemReplicationPolicy()).isNotNull();
    }

    @Test(groups = "simple")
    public void configurationPropertyReads() throws Exception {
        DatabaseAccountManagerInternal databaseAccountManagerInternal = Mockito.mock(DatabaseAccountManagerInternal.class);
        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(ArgumentMatchers.any())).thenReturn(Flux.just(new DatabaseAccount(GlobalEndPointManagerTest.dbAccountJson1)));
        Mockito.when(databaseAccountManagerInternal.getServiceEndpoint()).thenReturn(new URI(TestConfigurations.HOST));
        GlobalEndpointManager globalEndpointManager = new GlobalEndpointManager(databaseAccountManagerInternal,
            new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig()), new Configs());
        ReflectionUtils.setBackgroundRefreshLocationTimeIntervalInMS(globalEndpointManager, 1000);
        globalEndpointManager.init();

        GatewayServiceConfigurationReader configurationReader = new GatewayServiceConfigurationReader(globalEndpointManager);
        assertThat(configurationReader.getDefaultConsistencyLevel()).isEqualTo(ConsistencyLevel.SESSION);
        assertThat((boolean) configurationReader.getQueryEngineConfiguration().get("enableSpatialIndexing")).isTrue();
        assertThat(configurationReader.getSystemReplicationPolicy().getMaxReplicaSetSize()).isEqualTo(4);
        assertThat(configurationReader.getUserReplicationPolicy().getMaxReplicaSetSize()).isEqualTo(4);

        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(ArgumentMatchers.any())).thenReturn(Flux.just(new DatabaseAccount(GlobalEndPointManagerTest.dbAccountJson2)));
        Thread.sleep(2000);
        assertThat(configurationReader.getDefaultConsistencyLevel()).isEqualTo(ConsistencyLevel.EVENTUAL);
        assertThat((boolean) configurationReader.getQueryEngineConfiguration().get("enableSpatialIndexing")).isFalse();
        assertThat(configurationReader.getSystemReplicationPolicy().getMaxReplicaSetSize()).isEqualTo(5);
        assertThat(configurationReader.getUserReplicationPolicy().getMaxReplicaSetSize()).isEqualTo(5);

        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(ArgumentMatchers.any())).thenReturn(Flux.just(new DatabaseAccount(GlobalEndPointManagerTest.dbAccountJson3)));
        Thread.sleep(2000);
        assertThat(configurationReader.getDefaultConsistencyLevel()).isEqualTo(ConsistencyLevel.SESSION);
        assertThat((boolean) configurationReader.getQueryEngineConfiguration().get("enableSpatialIndexing")).isTrue();
        assertThat(configurationReader.getSystemReplicationPolicy().getMaxReplicaSetSize()).isEqualTo(4);
        assertThat(configurationReader.getUserReplicationPolicy().getMaxReplicaSetSize()).isEqualTo(4);

        //Testing scenario of scheduled cache refresh with error
        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(ArgumentMatchers.any())).thenReturn(Flux.error(BridgeInternal.createCosmosException(HttpConstants.StatusCodes.FORBIDDEN)));
        Thread.sleep(2000);
        assertThat(configurationReader.getDefaultConsistencyLevel()).isEqualTo(ConsistencyLevel.SESSION);
        assertThat((boolean) configurationReader.getQueryEngineConfiguration().get("enableSpatialIndexing")).isTrue();
        assertThat(configurationReader.getSystemReplicationPolicy().getMaxReplicaSetSize()).isEqualTo(4);
        assertThat(configurationReader.getUserReplicationPolicy().getMaxReplicaSetSize()).isEqualTo(4);
        LifeCycleUtils.closeQuietly(globalEndpointManager);
    }
}
