// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosDatabaseAccountResponseTest extends TestSuiteBase {
    private CosmosClient client;
    private CosmosAsyncClient asyncClient;

    @Factory(dataProvider = "clientBuilders")
    public CosmosDatabaseAccountResponseTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"fast"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosDatabaseAccountTest() {
        assertThat(this.client).isNull();
        assertThat(this.asyncClient).isNull();

        this.client = getClientBuilder().buildClient();
        this.asyncClient = getClientBuilder().buildAsyncClient();
    }

    @AfterClass(groups = {"fast"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        assertThat(this.asyncClient).isNotNull();

        this.client.close();
        this.asyncClient.close();
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void readCosmosDatabaseAccountWithClient() {
        CosmosDatabaseAccountResponse latestDatabaseAccountResponse = client.readDatabaseAccount(false);

        assertThat(latestDatabaseAccountResponse).isNotNull();
        validateDatabaseAccountResponse(latestDatabaseAccountResponse);

        CosmosDatabaseAccountResponse cachedResponse = client.readDatabaseAccount(true);

        assertThat(cachedResponse).isNotNull();
        validateDatabaseAccountResponse(cachedResponse);

        validateEquality(latestDatabaseAccountResponse, cachedResponse);
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void readCosmosDatabaseAccountWithAsyncClient() {
        CosmosDatabaseAccountResponse latestDatabaseAccountResponse = asyncClient.readDatabaseAccount(false).block();

        assertThat(latestDatabaseAccountResponse).isNotNull();
        validateDatabaseAccountResponse(latestDatabaseAccountResponse);

        CosmosDatabaseAccountResponse cachedResponse = asyncClient.readDatabaseAccount(true).block();

        assertThat(cachedResponse).isNotNull();
        validateDatabaseAccountResponse(cachedResponse);

        validateEquality(latestDatabaseAccountResponse, cachedResponse);
    }

    private void validateDatabaseAccountResponse(CosmosDatabaseAccountResponse response) {
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getId()).isNotEmpty();
        assertThat(response.getReadRegions()).isNotEmpty();
        assertThat(response.getWriteRegions()).isNotEmpty();
        assertThat(response.isMultiWriteAccount()).isNotNull();
        assertThat(response.getAccountLevelConsistency()).isNotNull();
    }

    private void validateEquality(CosmosDatabaseAccountResponse response1, CosmosDatabaseAccountResponse response2) {
        assertThat(response1.getId()).isEqualTo(response2.getId());
        assertThat(response1.getReadRegions()).isEqualTo(response2.getReadRegions());
        assertThat(response1.getWriteRegions()).isEqualTo(response2.getWriteRegions());
        assertThat(response1.isMultiWriteAccount()).isEqualTo(response2.isMultiWriteAccount());
        assertThat(response1.getAccountLevelConsistency()).isEqualTo(response2.getAccountLevelConsistency());
    }
}
