/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.ConsoleLoggingRegistryFactory;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.util.CosmosPagedIterable;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientMetricsTest extends TestSuiteBase {

    private CosmosClient client;
    private CosmosContainer container;
    private String databaseId;
    private String containerId;
    private MeterRegistry meterRegistry;

    @Factory(dataProvider = "clientBuildersWithDirectSession")
    public ClientMetricsTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    public void beforeTest() {
        assertThat(this.client).isNull();
        assertThat(this.meterRegistry).isNull();

        this.meterRegistry = ConsoleLoggingRegistryFactory.create(1);
        this.client = getClientBuilder()
            .clientTelemetryConfig().clientMetrics(this.meterRegistry)
            .buildClient();

        if (databaseId == null) {
            CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client.asyncClient());
            this.databaseId = asyncContainer.getDatabase().getId();
            this.containerId = asyncContainer.getId();
        }

        container = client.getDatabase(databaseId).getContainer(containerId);
    }

    public void afterTest() {
        this.container = null;
        CosmosClient clientSnapshot = this.client;
        if (clientSnapshot != null) {
            this.client.close();
        }
        this.client = null;

        MeterRegistry meterRegistrySnapshot = this.meterRegistry;
        if (meterRegistrySnapshot != null) {
            meterRegistrySnapshot.clear();
            meterRegistrySnapshot.close();
        }
        this.meterRegistry = null;
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, singleThreaded = true)
    public void createItem() throws Exception {
        this.beforeTest();
        try {
            InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
            CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties);
            assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
            validateItemResponse(properties, itemResponse);

            properties = getDocumentDefinition(UUID.randomUUID().toString());
            CosmosItemResponse<InternalObjectNode> itemResponse1 = container.createItem(properties, new CosmosItemRequestOptions());
            validateItemResponse(properties, itemResponse1);

            this.validateMetrics();
        } finally {
            this.afterTest();
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, singleThreaded = true)
    public void readItem() throws Exception {
        this.beforeTest();
        try {
            InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
            container.createItem(properties);

            CosmosItemResponse<InternalObjectNode> readResponse1 = container.readItem(properties.getId(),
                new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(properties, "mypk")),
                new CosmosItemRequestOptions(),
                InternalObjectNode.class);
            validateItemResponse(properties, readResponse1);

            this.validateMetrics();
        } finally {
            this.afterTest();
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, singleThreaded = true)
    public void replaceItem() throws Exception {
        this.beforeTest();
        try {
            InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
            CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties);

            validateItemResponse(properties, itemResponse);
            String newPropValue = UUID.randomUUID().toString();
            BridgeInternal.setProperty(properties, "newProp", newPropValue);
            CosmosItemRequestOptions options = new CosmosItemRequestOptions();
            ModelBridgeInternal.setPartitionKey(options, new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(properties, "mypk")));
            // replace document
            CosmosItemResponse<InternalObjectNode> replace = container.replaceItem(properties,
                properties.getId(),
                new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(properties, "mypk")),
                options);
            assertThat(ModelBridgeInternal.getObjectFromJsonSerializable(BridgeInternal.getProperties(replace), "newProp")).isEqualTo(newPropValue);

            this.validateMetrics();
        } finally {
            this.afterTest();
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, singleThreaded = true)
    public void deleteItem() throws Exception {
        this.beforeTest();
        try {
            InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
            container.createItem(properties);
            CosmosItemRequestOptions options = new CosmosItemRequestOptions();

            CosmosItemResponse<?> deleteResponse = container.deleteItem(properties.getId(),
                new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(properties, "mypk")),
                options);
            assertThat(deleteResponse.getStatusCode()).isEqualTo(204);

            this.validateMetrics();
        } finally {
            this.afterTest();
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, singleThreaded = true)
    public void readAllItems() throws Exception {
        this.beforeTest();
        try {
            InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
            container.createItem(properties);

            CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

            CosmosPagedIterable<InternalObjectNode> feedResponseIterator3 =
                container.readAllItems(cosmosQueryRequestOptions, InternalObjectNode.class);
            assertThat(feedResponseIterator3.iterator().hasNext()).isTrue();

            this.validateMetrics();
        } finally {
            this.afterTest();
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, singleThreaded = true)
    public void queryItems() throws Exception {
        this.beforeTest();
        try {
            InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
            container.createItem(properties);

            String query = String.format("SELECT * from c where c.id = '%s'", properties.getId());
            CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

            CosmosPagedIterable<InternalObjectNode> feedResponseIterator1 =
                container.queryItems(query, cosmosQueryRequestOptions, InternalObjectNode.class);

            // Very basic validation
            assertThat(feedResponseIterator1.iterator().hasNext()).isTrue();

            SqlQuerySpec querySpec = new SqlQuerySpec(query);
            CosmosPagedIterable<InternalObjectNode> feedResponseIterator3 =
                container.queryItems(querySpec, cosmosQueryRequestOptions, InternalObjectNode.class);
            assertThat(feedResponseIterator3.iterator().hasNext()).isTrue();

            this.validateMetrics();
        } finally {
            this.afterTest();
        }
    }

    private InternalObjectNode getDocumentDefinition(String documentId) {
        final String uuid = UUID.randomUUID().toString();
        return
            new InternalObjectNode(String.format("{ "
                    + "\"id\": \"%s\", "
                    + "\"mypk\": \"%s\", "
                    + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                    + "}"
                , documentId, uuid));
    }

    private void validateItemResponse(InternalObjectNode containerProperties,
                                      CosmosItemResponse<InternalObjectNode> createResponse) {
        // Basic validation
        assertThat(BridgeInternal.getProperties(createResponse).getId()).isNotNull();
        assertThat(BridgeInternal.getProperties(createResponse).getId())
            .as("check Resource Id")
            .isEqualTo(containerProperties.getId());
    }

    private void validateMetrics() {
        this.assertMetrics("cosmos.client.op.latency", true);
        if (this.client.asyncClient().getConnectionPolicy().getConnectionMode() == ConnectionMode.DIRECT) {
            this.assertMetrics("cosmos.client.req.rntbd", true);
        } else {
            this.assertMetrics("cosmos.client.req.gw", true);
            this.assertMetrics("cosmos.client.req.rntbd", false);
        }
    }

    private void assertMetrics(String prefix, boolean expectedToFind) {
        assertThat(this.meterRegistry).isNotNull();
        assertThat(this.meterRegistry.getMeters()).isNotNull();
        List<Meter> meters = this.meterRegistry.getMeters().stream().collect(Collectors.toList());

        if (expectedToFind) {
            assertThat(meters.size()).isGreaterThan(0);
        }

        List<Meter> meterMatches = meters
            .stream()
            .filter(meter -> meter.getId().getName().startsWith(prefix) &&
                meter.measure().iterator().next().getValue() > 0)
            .collect(Collectors.toList());

        if (expectedToFind) {
            assertThat(meterMatches.size()).isGreaterThan(0);
        } else {
            if (meterMatches.size() > 0) {
                meterMatches.forEach( m ->
                logger.error("Found unexpected meter {}", m.getId().getName()));
            }
            assertThat(meterMatches.size()).isEqualTo(0);
        }
    }
}
