// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestProxyTestBase;
import org.junit.jupiter.api.Test;

public abstract class TableClientTestBase extends TestProxyTestBase {
    protected static final HttpClient DEFAULT_HTTP_CLIENT = HttpClient.createDefault();
    protected static final boolean IS_COSMOS_TEST = TestUtils.isCosmosTest();

    protected HttpPipelinePolicy recordPolicy;
    protected HttpClient playbackClient;

    protected abstract HttpClient buildAssertingClient(HttpClient httpClient);


    protected TableClientBuilder getClientBuilder(String tableName, boolean enableTenantDiscovery) {
        return TestUtils.isCosmosTest() ? getClientBuilderWithConnectionString(tableName, enableTenantDiscovery)
            : getClientBuilderUsingEntra(tableName, enableTenantDiscovery);
    }

    protected TableClientBuilder getClientBuilderUsingEntra(String tableName, boolean enableTenantDiscovery) {
        final TableClientBuilder tableClientBuilder = new TableClientBuilder()
            .credential(TestUtils.getTestTokenCredential(interceptorManager))
            .endpoint(TestUtils.getEndpoint(interceptorManager.isPlaybackMode()));

        if (enableTenantDiscovery) {
            tableClientBuilder.enableTenantDiscovery();
        }

        return configureTestClientBuilder(tableClientBuilder, tableName);
    }

    protected TableClientBuilder getClientBuilderWithConnectionString(String tableName, boolean enableTenantDiscovery) {
        final TableClientBuilder tableClientBuilder = new TableClientBuilder()
            .connectionString(TestUtils.getConnectionString(interceptorManager.isPlaybackMode()));

        if (enableTenantDiscovery) {
            tableClientBuilder.enableTenantDiscovery();
        }

        return configureTestClientBuilder(tableClientBuilder, tableName);
    }


    private TableClientBuilder configureTestClientBuilder(TableClientBuilder tableClientBuilder, String tableName) {
        tableClientBuilder
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .tableName(tableName);

        if (interceptorManager.isPlaybackMode()) {
            playbackClient = interceptorManager.getPlaybackClient();

            tableClientBuilder.httpClient(buildAssertingClient(playbackClient));
        } else {
            tableClientBuilder.httpClient(buildAssertingClient(DEFAULT_HTTP_CLIENT));

            if (interceptorManager.isRecordMode()) {
                recordPolicy = interceptorManager.getRecordPolicy();
                tableClientBuilder.addPolicy(recordPolicy);
            }
        }
        TestUtils.addTestProxyTestSanitizersAndMatchers(interceptorManager);
        return tableClientBuilder;
    }

    @Test
    public abstract void createTable();

    @Test
    public abstract void createTableWithResponse();

    @Test
    public abstract void createEntity();

    @Test
    public abstract void createEntityWithSingleQuotesInPartitionKey();

    @Test
    public abstract void createEntityWithSingleQuotesInRowKey();

    @Test
    public abstract void createEntityWithResponse();

    @Test
    public abstract void createEntityWithAllSupportedDataTypes();

    // Support for subclassing TableEntity was removed for the time being, although having it back is not 100%
    // discarded. -vicolina
    /*@Test
    public abstract void createEntitySubclass();*/

    @Test
    public abstract void deleteTable();

    @Test
    public abstract void deleteNonExistingTable();

    @Test
    public abstract void deleteTableWithResponse();

    @Test
    public abstract void deleteNonExistingTableWithResponse();

    @Test
    public abstract void deleteEntity();

    @Test
    public abstract void deleteEntityWithSingleQuotesInPartitionKey();

    @Test
    public abstract void deleteEntityWithSingleQuotesInRowKey();

    @Test
    public abstract void deleteNonExistingEntity();

    @Test
    public abstract void deleteEntityWithResponse();

    @Test
    public abstract void deleteNonExistingEntityWithResponse();

    @Test
    public abstract void deleteEntityWithResponseMatchETag();

    @Test
    public abstract void getEntityWithSingleQuotesInPartitionKey();

    @Test
    public abstract void getEntityWithSingleQuotesInRowKey();

    @Test
    public abstract void getEntityWithResponse();

    @Test
    public abstract void getEntityWithResponseWithSelect();

    // Support for subclassing TableEntity was removed for the time being, although having it back is not 100%
    // discarded. -vicolina
    /*@Test
    public abstract void getEntityWithResponseSubclass();*/

    @Test
    public abstract void updateEntityWithSingleQuotesInPartitionKey();

    @Test
    public abstract void updateEntityWithSingleQuotesInRowKey();

    @Test
    public abstract void updateEntityWithResponseReplace();

    @Test
    public abstract void updateEntityWithResponseMerge();

    // Support for subclassing TableEntity was removed for the time being, although having it back is not 100%
    // discarded. -vicolina
    /*@Test
    public abstract void updateEntityWithResponseSubclass();*/

    @Test
    public abstract void listEntities();

    @Test
    public abstract void listEntitiesWithSingleQuotesInPartitionKey();

    @Test
    public abstract void listEntitiesWithSingleQuotesInRowKey();

    @Test
    public abstract void listEntitiesWithFilter();

    @Test
    public abstract void listEntitiesWithSelect();

    @Test
    public abstract void listEntitiesWithTop();

    // Support for subclassing TableEntity was removed for the time being, although having it back is not 100%
    // discarded. -vicolina
    /*@Test
    public abstract void listEntitiesSubclass();*/

    @Test
    public abstract void submitTransaction();

    @Test
    public abstract void submitTransactionAllActions();

    @Test
    public abstract void submitTransactionAllActionsForEntitiesWithSingleQuotesInPartitionKey();

    @Test
    public abstract void submitTransactionAllActionsForEntitiesWithSingleQuotesInRowKey();

    @Test
    public abstract void submitTransactionWithFailingAction();

    @Test
    public abstract void submitTransactionWithSameRowKeys();

    @Test
    public abstract void submitTransactionWithDifferentPartitionKeys();

    @Test
    public abstract void generateSasTokenWithMinimumParameters();

    @Test
    public abstract void generateSasTokenWithAllParameters();

    @Test
    public abstract void canUseSasTokenToCreateValidTableClient();

    @Test
    public abstract void setAndListAccessPolicies();

    @Test
    public abstract void setAndListMultipleAccessPolicies();
}
