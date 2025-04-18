// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.cosmos;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CosmosHealthIndicatorTests {

    private static final String MOCK_URL = "https://test.documents.azure.com:443/";
    private static final String DATABASE = "test-database";

    @Test
    void cosmosIsUp() {
        CosmosAsyncClient mockAsyncClient = mock(CosmosAsyncClient.class);
        @SuppressWarnings("unchecked") ResourceResponse<Database> response = mock(ResourceResponse.class);
        CosmosAsyncDatabase databaseResponse = mock(CosmosAsyncDatabase.class);
        CosmosDatabaseResponse cosmosDatabaseResponse = mock(CosmosDatabaseResponse.class);
        when(response.getRequestCharge()).thenReturn(100.0);
        given(mockAsyncClient.getDatabase(anyString())).willReturn(databaseResponse);
        given(databaseResponse.read()).willReturn(Mono.just(cosmosDatabaseResponse));
        given(cosmosDatabaseResponse.getProperties()).willReturn(new CosmosDatabaseProperties("test"));
        CosmosHealthIndicator indicator = new CosmosHealthIndicator(mockAsyncClient, DATABASE, MOCK_URL);
        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void cosmosIsDownWhenReadReturnNull() {
        CosmosAsyncClient mockAsyncClient = mock(CosmosAsyncClient.class);
        @SuppressWarnings("unchecked") ResourceResponse<Database> response = mock(ResourceResponse.class);
        CosmosAsyncDatabase databaseResponse = mock(CosmosAsyncDatabase.class);
        when(response.getRequestCharge()).thenReturn(100.0);
        given(mockAsyncClient.getDatabase(anyString())).willReturn(databaseResponse);
        given(databaseResponse.read()).willReturn(Mono.empty());
        CosmosHealthIndicator indicator = new CosmosHealthIndicator(mockAsyncClient, DATABASE, MOCK_URL);
        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    void cosmosIsDownWhenReadException() {
        CosmosAsyncClient mockAsyncClient = mock(CosmosAsyncClient.class);
        @SuppressWarnings("unchecked") ResourceResponse<Database> response = mock(ResourceResponse.class);
        when(response.getRequestCharge()).thenReturn(100.0);
        given(mockAsyncClient.getDatabase(anyString()))
            .willThrow(new IllegalArgumentException("The gremlins have cut the cable."));
        CosmosHealthIndicator indicator = new CosmosHealthIndicator(mockAsyncClient, DATABASE, MOCK_URL);
        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }
}
