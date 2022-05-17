// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestBase;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;

public abstract class TableServiceClientTestBase extends TestBase {
    protected static final HttpClient DEFAULT_HTTP_CLIENT = HttpClient.createDefault();

    protected HttpPipelinePolicy recordPolicy;
    protected HttpClient playbackClient;

    protected TableServiceClientBuilder getClientBuilder(String connectionString) {
        final TableServiceClientBuilder tableServiceClientBuilder = new TableServiceClientBuilder()
            .connectionString(connectionString);

        return configureTestClientBuilder(tableServiceClientBuilder);
    }

    protected TableServiceClientBuilder getClientBuilder(String endpoint, TokenCredential tokenCredential,
                                                         boolean enableTenantDiscovery) {
        final TableServiceClientBuilder tableServiceClientBuilder = new TableServiceClientBuilder()
            .credential(tokenCredential)
            .endpoint(endpoint);

        if (enableTenantDiscovery) {
            tableServiceClientBuilder.enableTenantDiscovery();
        }

        return configureTestClientBuilder(tableServiceClientBuilder);
    }

    private TableServiceClientBuilder configureTestClientBuilder(TableServiceClientBuilder tableServiceClientBuilder) {
        tableServiceClientBuilder
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        if (interceptorManager.isPlaybackMode()) {
            playbackClient = interceptorManager.getPlaybackClient();

            tableServiceClientBuilder.httpClient(playbackClient);
        } else {
            tableServiceClientBuilder.httpClient(DEFAULT_HTTP_CLIENT);

            if (!interceptorManager.isLiveMode()) {
                recordPolicy = interceptorManager.getRecordPolicy();

                tableServiceClientBuilder.addPolicy(recordPolicy);
            }
        }

        return tableServiceClientBuilder;
    }

    @Test
    public abstract void serviceCreateTable();

    @Test
    public abstract void serviceCreateTableWithResponse();

    @Test
    public abstract void serviceCreateTableFailsIfExists();

    @Test
    public abstract void serviceCreateTableIfNotExists();

    @Test
    public abstract void serviceCreateTableIfNotExistsSucceedsIfExists();

    @Test
    public abstract void serviceCreateTableIfNotExistsWithResponse();

    @Test
    public abstract void serviceCreateTableIfNotExistsWithResponseSucceedsIfExists();

    @Test
    public abstract void serviceDeleteTable();

    @Test
    public abstract void serviceDeleteNonExistingTable();

    @Test
    public abstract void serviceDeleteTableWithResponse();

    @Test
    public abstract void serviceDeleteNonExistingTableWithResponse();

    @Test
    public abstract void serviceListTables();

    @Test
    public abstract void serviceListTablesWithFilter();

    @Test
    public abstract void serviceListTablesWithTop();

    @Test
    public abstract void serviceGetTableClient();

    @Test
    public abstract void generateAccountSasTokenWithMinimumParameters();

    @Test
    public abstract void generateAccountSasTokenWithAllParameters();

    @Test
    public abstract void canUseSasTokenToCreateValidTableClient();

    @Test
    public abstract void setGetProperties();

    @Test
    public abstract void getStatistics() throws URISyntaxException;
}
