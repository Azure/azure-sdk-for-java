// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.rx;

import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.CosmosGlobalSecondaryIndex;
import com.azure.cosmos.models.CosmosGlobalSecondaryIndexDefinition;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GlobalSecondaryIndexTest {

    protected static final int TIMEOUT = 30000;

    private final ObjectMapper simpleObjectMapper = Utils.getSimpleObjectMapper();

    private static final ImplementationBridgeHelpers
        .CosmosContainerPropertiesHelper
        .CosmosContainerPropertiesAccessor containerPropertiesAccessor =
        ImplementationBridgeHelpers
            .CosmosContainerPropertiesHelper
            .getCosmosContainerPropertiesAccessor();

    /** Helper: create CosmosContainerProperties from a raw JSON string (simulates server response). */
    private CosmosContainerProperties fromJson(String json) {
        return containerPropertiesAccessor.create(new DocumentCollection(json));
    }

    // -------------------------------------------------------------------------
    // CosmosGlobalSecondaryIndexDefinition – getter/setter tests
    // -------------------------------------------------------------------------

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void containerProperties_setAndGetGlobalSecondaryIndexDefinition() {
        CosmosContainerProperties containerProperties =
            new CosmosContainerProperties("testContainer", "/pk");

        CosmosGlobalSecondaryIndexDefinition definition = new CosmosGlobalSecondaryIndexDefinition()
            .setSourceContainerId("gsi-src")
            .setDefinition("SELECT c.customerId, c.emailAddress FROM c");

        containerProperties.setCosmosGlobalSecondaryIndexDefinition(definition);

        CosmosGlobalSecondaryIndexDefinition retrieved = containerProperties.getCosmosGlobalSecondaryIndexDefinition();
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getSourceContainerId()).isEqualTo("gsi-src");
        assertThat(retrieved.getDefinition()).isEqualTo("SELECT c.customerId, c.emailAddress FROM c");
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void containerProperties_setGlobalSecondaryIndexDefinition_nullThrows() {
        CosmosContainerProperties containerProperties =
            new CosmosContainerProperties("testContainer", "/pk");

        assertThatThrownBy(() -> containerProperties.setCosmosGlobalSecondaryIndexDefinition(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("cosmosGlobalSecondaryIndexDefinition cannot be null");
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void containerProperties_setGlobalSecondaryIndexDefinition_returnsThis() {
        CosmosContainerProperties containerProperties =
            new CosmosContainerProperties("testContainer", "/pk");

        CosmosGlobalSecondaryIndexDefinition definition = new CosmosGlobalSecondaryIndexDefinition()
            .setSourceContainerId("gsi-src");

        assertThat(containerProperties.setCosmosGlobalSecondaryIndexDefinition(definition))
            .isSameAs(containerProperties);
    }

    // -------------------------------------------------------------------------
    // Serialization / Deserialization round-trip
    // -------------------------------------------------------------------------

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void globalSecondaryIndexDefinition_serializesToJson() throws Exception {
        CosmosContainerProperties containerProperties =
            new CosmosContainerProperties("testContainer", "/pk");

        CosmosGlobalSecondaryIndexDefinition definition = new CosmosGlobalSecondaryIndexDefinition()
            .setSourceContainerId("gsi-src")
            .setDefinition("SELECT c.customerId, c.emailAddress FROM c");

        // Simulate the RID resolution that CosmosAsyncDatabase performs during createContainer
        ModelBridgeInternal.setGlobalSecondaryIndexDefinitionSourceCollectionRid(definition, "TughAMEOdUI=");

        containerProperties.setCosmosGlobalSecondaryIndexDefinition(definition);

        // Serialize via DocumentCollection.toJson() which calls populatePropertyBag()
        String json = ModelBridgeInternal.getResource(containerProperties).toJson();

        ObjectNode jsonNode = (ObjectNode) simpleObjectMapper.readTree(json);
        ObjectNode gsiDefNode = (ObjectNode) jsonNode.get("materializedViewDefinition");

        assertThat(gsiDefNode).isNotNull();
        assertThat(gsiDefNode.get("sourceCollectionRid").asText()).isEqualTo("TughAMEOdUI=");
        assertThat(gsiDefNode.get("definition").asText())
            .isEqualTo("SELECT c.customerId, c.emailAddress FROM c");
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void globalSecondaryIndexDefinition_deserializesFromJson() {
        String json = "{"
            + "\"id\":\"testContainer\","
            + "\"partitionKey\":{\"paths\":[\"/pk\"],\"kind\":\"Hash\"},"
            + "\"materializedViewDefinition\":{"
            + "\"sourceCollectionId\":\"gsi-src\","
            + "\"sourceCollectionRid\":\"TughAMEOdUI=\","
            + "\"definition\":\"SELECT c.customerId, c.emailAddress FROM c\""
            + "}"
            + "}";

        CosmosContainerProperties containerProperties = fromJson(json);

        CosmosGlobalSecondaryIndexDefinition definition = containerProperties.getCosmosGlobalSecondaryIndexDefinition();
        assertThat(definition).isNotNull();
        assertThat(definition.getSourceContainerId()).isEqualTo("gsi-src");
        assertThat(definition.getDefinition()).isEqualTo("SELECT c.customerId, c.emailAddress FROM c");
        assertThat(definition.getSourceContainerRid()).isEqualTo("TughAMEOdUI=");
        assertThat(definition.getStatus()).isNull();
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void globalSecondaryIndexDefinition_deserializesStatusFromJson() {
        String json = "{"
            + "\"id\":\"testContainer\","
            + "\"partitionKey\":{\"paths\":[\"/pk\"],\"kind\":\"Hash\"},"
            + "\"materializedViewDefinition\":{"
            + "\"sourceCollectionRid\":\"TughAMEOdUI=\","
            + "\"definition\":\"SELECT c.customerId, c.emailAddress FROM c\","
            + "\"status\":\"Initialized\""
            + "}"
            + "}";

        CosmosContainerProperties containerProperties = fromJson(json);

        CosmosGlobalSecondaryIndexDefinition definition = containerProperties.getCosmosGlobalSecondaryIndexDefinition();
        assertThat(definition).isNotNull();
        assertThat(definition.getSourceContainerRid()).isEqualTo("TughAMEOdUI=");
        assertThat(definition.getStatus()).isEqualTo("Initialized");
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void GlobalSecondaryIndexDefinition_fullRoundTrip() throws Exception {
        // Set on container properties using the new public API
        CosmosContainerProperties original = new CosmosContainerProperties("testContainer", "/pk");
        CosmosGlobalSecondaryIndexDefinition definition = new CosmosGlobalSecondaryIndexDefinition()
            .setSourceContainerId("gsi-src")
            .setDefinition("SELECT c.customerId, c.emailAddress FROM c");

        // Simulate the RID resolution that CosmosAsyncDatabase performs during createContainer
        ModelBridgeInternal.setGlobalSecondaryIndexDefinitionSourceCollectionRid(definition, "TughAMEOdUI=");

        original.setCosmosGlobalSecondaryIndexDefinition(definition);

        // Serialize via DocumentCollection.toJson()
        String json = ModelBridgeInternal.getResource(original).toJson();

        // Deserialize back using the same path as server responses
        CosmosContainerProperties deserialized = fromJson(json);

        CosmosGlobalSecondaryIndexDefinition deserializedDef = deserialized.getCosmosGlobalSecondaryIndexDefinition();
        assertThat(deserializedDef).isNotNull();
        assertThat(deserializedDef.getSourceContainerId()).isEqualTo("gsi-src");
        assertThat(deserializedDef.getDefinition()).isEqualTo("SELECT c.customerId, c.emailAddress FROM c");

        // Verify the RID round-tripped correctly through the wire format
        ObjectNode jsonNode = (ObjectNode) simpleObjectMapper.readTree(json);
        ObjectNode gsiDefNode = (ObjectNode) jsonNode.get("materializedViewDefinition");
        assertThat(gsiDefNode.get("sourceCollectionRid").asText()).isEqualTo("TughAMEOdUI=");
    }


    // -------------------------------------------------------------------------
    // getGlobalSecondaryIndexes – read-only list from server response
    // -------------------------------------------------------------------------

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void getGlobalSecondaryIndexes_returnsEmptyListWhenAbsent() {
        CosmosContainerProperties containerProperties =
            new CosmosContainerProperties("testContainer", "/pk");

        List<CosmosGlobalSecondaryIndex> gsiList = containerProperties.getGlobalSecondaryIndexes();
        assertThat(gsiList).isNotNull();
        assertThat(gsiList).isEmpty();
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void getGlobalSecondaryIndexes_deserializesFromServerResponse() {
        String json = "{"
            + "\"id\":\"src-container\","
            + "\"partitionKey\":{\"paths\":[\"/pk\"],\"kind\":\"Hash\"},"
            + "\"materializedViews\":["
            + "{\"id\":\"gsi_testcontainer1\",\"_rid\":\"TughAMEOdUI=\"},"
            + "{\"id\":\"gsi_testcontainer2\",\"_rid\":\"AbcdEFGhIJk=\"}"
            + "]"
            + "}";

        CosmosContainerProperties containerProperties = fromJson(json);

        List<CosmosGlobalSecondaryIndex> gsiList = containerProperties.getGlobalSecondaryIndexes();
        assertThat(gsiList).isNotNull();
        assertThat(gsiList).hasSize(2);

        assertThat(gsiList.get(0).getId()).isEqualTo("gsi_testcontainer1");
        assertThat(gsiList.get(0).getResourceId()).isEqualTo("TughAMEOdUI=");

        assertThat(gsiList.get(1).getId()).isEqualTo("gsi_testcontainer2");
        assertThat(gsiList.get(1).getResourceId()).isEqualTo("AbcdEFGhIJk=");
    }

}
