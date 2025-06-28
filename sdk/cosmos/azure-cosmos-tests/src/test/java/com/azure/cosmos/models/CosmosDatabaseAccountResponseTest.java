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
        CosmosDatabaseAccountResponse response = client.readDatabaseAccount(false);
        validateDatabaseAccountResponse(response);
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void readCosmosDatabaseAccountWithAsyncClient() {
        CosmosDatabaseAccountResponse response = asyncClient.readDatabaseAccount(false).block();
        validateDatabaseAccountResponse(response);
    }

    private void validateDatabaseAccountResponse(CosmosDatabaseAccountResponse response) {
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getReadRegions()).isNotEmpty();
        assertThat(response.getWriteRegions()).isNotEmpty();
        assertThat(response.isMultiWriteAccount()).isNotNull();
        assertThat(response.getAccountLevelConsistency()).isNotNull();
    }
}
