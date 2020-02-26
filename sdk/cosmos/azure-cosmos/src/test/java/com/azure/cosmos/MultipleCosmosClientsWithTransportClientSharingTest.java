/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.CosmosItemProperties;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.LifeCycleUtils;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.SharedTransportClient;
import com.azure.cosmos.implementation.directconnectivity.TransportClient;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.SharedGatewayHttpClient;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class MultipleCosmosClientsWithTransportClientSharingTest extends TestSuiteBase {

    private CosmosClient client;
    private CosmosContainer container1;
    private CosmosClient client1;
    private CosmosClient client2;
    private CosmosContainer container2;

    @Factory(dataProvider = "clientBuildersWithDirectSession")
    public MultipleCosmosClientsWithTransportClientSharingTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"simple"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosItemTest() {
        assertThat(this.client).isNull();
        this.client = clientBuilder().buildClient();
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client.asyncClient());
        container1 = client.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());

        client1 = copyCosmosClientBuilder(clientBuilder()).setConnectionReuseAcrossClientsEnabled(true).buildClient();
        client2 = copyCosmosClientBuilder(clientBuilder()).setConnectionReuseAcrossClientsEnabled(true).buildClient();

        container1 = client1.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());
        container2 = client1.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());
    }

    @AfterClass(groups = {"simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        LifeCycleUtils.closeQuietly(client);
        LifeCycleUtils.closeQuietly(client1);
        LifeCycleUtils.closeQuietly(client2);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createItem() {
        CosmosItemProperties properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<CosmosItemProperties> itemResponse = container1.createItem(properties);
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        validateItemResponse(properties, itemResponse);

        properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<CosmosItemProperties> itemResponse1 = container1.createItem(properties, new CosmosItemRequestOptions());
        validateItemResponse(properties, itemResponse1);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void createItem_alreadyExists() {
        CosmosItemProperties properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<CosmosItemProperties> itemResponse = container1.createItem(properties);
        validateItemResponse(properties, itemResponse);

        properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<CosmosItemProperties> itemResponse1 = container1.createItem(properties, new CosmosItemRequestOptions());
        validateItemResponse(properties, itemResponse1);

        // Test for conflict
        try {
            container1.createItem(properties, new CosmosItemRequestOptions());
        } catch (Exception e) {
            assertThat(e).isInstanceOf(CosmosClientException.class);
            assertThat(((CosmosClientException) e).getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CONFLICT);
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readItem() {
        CosmosItemProperties properties1 = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<CosmosItemProperties> itemResponse1 = container1.createItem(properties1);
        CosmosItemResponse<CosmosItemProperties> readResponse1 = container1.readItem(properties1.getId(),
                                                                                    new PartitionKey(properties1.get("mypk")),
                                                                                    new CosmosItemRequestOptions(),
                                                                                    CosmosItemProperties.class);
        validateItemResponse(properties1, readResponse1);

        CosmosItemProperties properties2 = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<CosmosItemProperties> itemResponse2 = container2.createItem(properties2);
        CosmosItemResponse<CosmosItemProperties> readResponse2 = container2.readItem(properties2.getId(),
            new PartitionKey(properties2.get("mypk")),
            new CosmosItemRequestOptions(),
            CosmosItemProperties.class);
        validateItemResponse(properties2, readResponse2);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void replaceItem() throws Exception{
        CosmosItemProperties properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<CosmosItemProperties> itemResponse = container1.createItem(properties);

        validateItemResponse(properties, itemResponse);
        String newPropValue = UUID.randomUUID().toString();
        BridgeInternal.setProperty(properties, "newProp", newPropValue);
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        options.setPartitionKey(new PartitionKey(properties.get("mypk")));
        // replace document
        CosmosItemResponse<CosmosItemProperties> replace = container1.replaceItem(properties,
                                                              properties.getId(),
                                                              new PartitionKey(properties.get("mypk")),
                                                              options);
        assertThat(replace.getProperties().get("newProp")).isEqualTo(newPropValue);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void deleteItem() {
        CosmosItemProperties properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<CosmosItemProperties> itemResponse = container1.createItem(properties);
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();

        CosmosItemResponse<CosmosItemProperties> deleteResponse = container1.deleteItem(properties.getId(),
                                                                    new PartitionKey(properties.get("mypk")),
                                                                    options);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(204);
    }


    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readAllItems() throws Exception{
        CosmosItemProperties properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<CosmosItemProperties> itemResponse = container1.createItem(properties);

        FeedOptions feedOptions = new FeedOptions();

        CosmosPagedIterable<CosmosItemProperties> feedResponseIterator3 =
                container1.readAllItems(feedOptions, CosmosItemProperties.class);
        assertThat(feedResponseIterator3.iterator().hasNext()).isTrue();
    }


    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryItems() throws Exception{
        CosmosItemProperties properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<CosmosItemProperties> itemResponse = container1.createItem(properties);

        String query = String.format("SELECT * from c where c.id = '%s'", properties.getId());
        FeedOptions feedOptions = new FeedOptions();

        CosmosPagedIterable<CosmosItemProperties> feedResponseIterator1 =
                container1.queryItems(query, feedOptions, CosmosItemProperties.class);
        // Very basic validation
        assertThat(feedResponseIterator1.iterator().hasNext()).isTrue();

        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        CosmosPagedIterable<CosmosItemProperties> feedResponseIterator3 =
                container1.queryItems(querySpec, feedOptions, CosmosItemProperties.class);
        assertThat(feedResponseIterator3.iterator().hasNext()).isTrue();
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryItemsWithContinuationTokenAndPageSize() throws Exception{
        List<String> actualIds = new ArrayList<>();
        CosmosItemProperties properties = getDocumentDefinition(UUID.randomUUID().toString());
        container1.createItem(properties);
        actualIds.add(properties.getId());
        properties = getDocumentDefinition(UUID.randomUUID().toString());
        container1.createItem(properties);
        actualIds.add(properties.getId());
        properties = getDocumentDefinition(UUID.randomUUID().toString());
        container1.createItem(properties);
        actualIds.add(properties.getId());


        String query = String.format("SELECT * from c where c.id in ('%s', '%s', '%s')", actualIds.get(0), actualIds.get(1), actualIds.get(2));
        FeedOptions feedOptions = new FeedOptions();
        String continuationToken = null;
        int pageSize = 1;

        int initialDocumentCount = 3;
        int finalDocumentCount = 0;

        CosmosPagedIterable<CosmosItemProperties> feedResponseIterator1 =
            container1.queryItems(query, feedOptions, CosmosItemProperties.class);

        do {
            Iterable<FeedResponse<CosmosItemProperties>> feedResponseIterable =
                feedResponseIterator1.iterableByPage(continuationToken, pageSize);
            for (FeedResponse<CosmosItemProperties> fr : feedResponseIterable) {
                int resultSize = fr.getResults().size();
                assertThat(resultSize).isEqualTo(pageSize);
                finalDocumentCount += fr.getResults().size();
                continuationToken = fr.getContinuationToken();
            }
        } while(continuationToken != null);

        assertThat(finalDocumentCount).isEqualTo(initialDocumentCount);

    }


    private CosmosItemProperties getDocumentDefinition(String documentId) {
        final String uuid = UUID.randomUUID().toString();
        final CosmosItemProperties properties =
            new CosmosItemProperties(String.format("{ "
                                                       + "\"id\": \"%s\", "
                                                       + "\"mypk\": \"%s\", "
                                                       + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                                                       + "}"
                , documentId, uuid));
        return properties;
    }

    private void validateItemResponse(CosmosItemProperties containerProperties,
                                      CosmosItemResponse<CosmosItemProperties> createResponse) {
        // Basic validation
        assertThat(createResponse.getProperties().getId()).isNotNull();
        assertThat(createResponse.getProperties().getId())
            .as("check Resource Id")
            .isEqualTo(containerProperties.getId());
    }


    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void transportClientReferenceValidation() {
        if (!ifDirectMode()) {
            throw new SkipException("not direct mode");
        }

        TransportClient transportClient = ReflectionUtils.getTransportClient(client.asyncClient());
        assertThat(transportClient).isNotInstanceOf(SharedTransportClient.class);

        TransportClient transportClient1 = ReflectionUtils.getTransportClient(client1.asyncClient());
        assertThat(transportClient1).isInstanceOf(SharedTransportClient.class);

        TransportClient transportClient2 = ReflectionUtils.getTransportClient(client2);
        assertThat(transportClient2).isSameAs(transportClient1);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void gatewayHttpClientReferenceValidation() {
        HttpClient httpClient = ReflectionUtils.getGatewayHttpClient(client);
        assertThat(httpClient).isNotInstanceOf(SharedGatewayHttpClient.class);

        HttpClient httpClient1 = ReflectionUtils.getGatewayHttpClient(client1);
        assertThat(httpClient1).isInstanceOf(SharedGatewayHttpClient.class);

        HttpClient httpClient2 = ReflectionUtils.getGatewayHttpClient(client2);
        assertThat(httpClient2).isSameAs(httpClient1);
    }

    private boolean ifDirectMode() {
        return (clientBuilder().getConnectionPolicy().getConnectionMode() == ConnectionMode.DIRECT);
    }
}
