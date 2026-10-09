// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.rx;

import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.CosmosGlobalSecondaryIndexBuildStatus;
import com.azure.cosmos.models.CosmosGlobalSecondaryIndexDefinition;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.Test;

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

        CosmosGlobalSecondaryIndexDefinition definition =
            new CosmosGlobalSecondaryIndexDefinition("gsi-src", "SELECT c.customerId, c.emailAddress FROM c");

        containerProperties.setGlobalSecondaryIndexDefinition(definition);

        CosmosGlobalSecondaryIndexDefinition retrieved = containerProperties.getGlobalSecondaryIndexDefinition();
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getSourceContainerId()).isEqualTo("gsi-src");
        assertThat(retrieved.getDefinition()).isEqualTo("SELECT c.customerId, c.emailAddress FROM c");
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void containerProperties_setGlobalSecondaryIndexDefinition_nullThrows() {
        CosmosContainerProperties containerProperties =
            new CosmosContainerProperties("testContainer", "/pk");

        assertThatThrownBy(() -> containerProperties.setGlobalSecondaryIndexDefinition(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("cosmosGlobalSecondaryIndexDefinition cannot be null");
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void globalSecondaryIndexDefinition_nullSourceContainerIdThrows() {
        assertThatThrownBy(() -> new CosmosGlobalSecondaryIndexDefinition(null, "SELECT c.id FROM c"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("sourceContainerId");
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void globalSecondaryIndexDefinition_emptySourceContainerIdThrows() {
        assertThatThrownBy(() -> new CosmosGlobalSecondaryIndexDefinition("", "SELECT c.id FROM c"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("sourceContainerId");
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void globalSecondaryIndexDefinition_nullDefinitionThrows() {
        assertThatThrownBy(() -> new CosmosGlobalSecondaryIndexDefinition("gsi-src", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("definition");
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void globalSecondaryIndexDefinition_emptyDefinitionThrows() {
        assertThatThrownBy(() -> new CosmosGlobalSecondaryIndexDefinition("gsi-src", ""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("definition");
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void containerProperties_setGlobalSecondaryIndexDefinition_returnsThis() {
        CosmosContainerProperties containerProperties =
            new CosmosContainerProperties("testContainer", "/pk");

        CosmosGlobalSecondaryIndexDefinition definition =
            new CosmosGlobalSecondaryIndexDefinition("gsi-src", "SELECT c.customerId FROM c");

        assertThat(containerProperties.setGlobalSecondaryIndexDefinition(definition))
            .isSameAs(containerProperties);
    }

    // -------------------------------------------------------------------------
    // Serialization / Deserialization round-trip
    // -------------------------------------------------------------------------

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void globalSecondaryIndexDefinition_serializesToJson() throws Exception {
        CosmosContainerProperties containerProperties =
            new CosmosContainerProperties("testContainer", "/pk");

        CosmosGlobalSecondaryIndexDefinition definition =
            new CosmosGlobalSecondaryIndexDefinition("gsi-src", "SELECT c.customerId, c.emailAddress FROM c");

        // Simulate the RID resolution that CosmosAsyncDatabase performs during createContainer
        ImplementationBridgeHelpers.CosmosGlobalSecondaryIndexDefinitionHelper
            .getCosmosGlobalSecondaryIndexDefinitionAccessor()
            .setSourceCollectionRid(definition, "TughAMEOdUI=");

        containerProperties.setGlobalSecondaryIndexDefinition(definition);

        // Serialize via DocumentCollection.toJson() which calls populatePropertyBag()
        String json = ModelBridgeInternal.getResource(containerProperties).toJson();

        ObjectNode jsonNode = (ObjectNode) simpleObjectMapper.readTree(json);

        // Verify the new wire format property is written
        ObjectNode newFormatNode = (ObjectNode) jsonNode.get("globalSecondaryIndexDefinition");
        assertThat(newFormatNode).isNotNull();
        assertThat(newFormatNode.get("sourceCollectionRid").asText()).isEqualTo("TughAMEOdUI=");
        assertThat(newFormatNode.get("definition").asText())
            .isEqualTo("SELECT c.customerId, c.emailAddress FROM c");

        // Verify the legacy wire format property is also written for backward compatibility
        ObjectNode legacyNode = (ObjectNode) jsonNode.get("materializedViewDefinition");
        assertThat(legacyNode).isNotNull();
        assertThat(legacyNode.get("sourceCollectionRid").asText()).isEqualTo("TughAMEOdUI=");
        assertThat(legacyNode.get("definition").asText())
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

        CosmosGlobalSecondaryIndexDefinition definition = containerProperties.getGlobalSecondaryIndexDefinition();
        assertThat(definition).isNotNull();
        assertThat(definition.getSourceContainerId()).isEqualTo("gsi-src");
        assertThat(definition.getDefinition()).isEqualTo("SELECT c.customerId, c.emailAddress FROM c");
        assertThat(definition.getSourceContainerRid()).isEqualTo("TughAMEOdUI=");
        // No status field in the JSON, so the accessor returns null.
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

        CosmosGlobalSecondaryIndexDefinition definition = containerProperties.getGlobalSecondaryIndexDefinition();
        assertThat(definition).isNotNull();
        assertThat(definition.getSourceContainerRid()).isEqualTo("TughAMEOdUI=");
        // "Initialized" is not a value declared as a constant on this SDK version, but ExpandableStringEnum
        // still echoes the wire value back through toString().
        assertThat(definition.getStatus()).isNotNull();
        assertThat(definition.getStatus().toString()).isEqualTo("Initialized");
    }

    // -------------------------------------------------------------------------
    // Deserialization from new wire format (globalSecondaryIndexDefinition)
    // -------------------------------------------------------------------------

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void globalSecondaryIndexDefinition_deserializesFromNewWireFormat() {
        String json = "{"
            + "\"id\":\"testContainer\","
            + "\"partitionKey\":{\"paths\":[\"/pk\"],\"kind\":\"Hash\"},"
            + "\"globalSecondaryIndexDefinition\":{"
            + "\"sourceCollectionId\":\"gsi-src\","
            + "\"sourceCollectionRid\":\"TughAMEOdUI=\","
            + "\"definition\":\"SELECT c.customerId, c.emailAddress FROM c\","
            + "\"status\":\"Active\""
            + "}"
            + "}";

        CosmosContainerProperties containerProperties = fromJson(json);

        CosmosGlobalSecondaryIndexDefinition definition = containerProperties.getGlobalSecondaryIndexDefinition();
        assertThat(definition).isNotNull();
        assertThat(definition.getSourceContainerId()).isEqualTo("gsi-src");
        assertThat(definition.getSourceContainerRid()).isEqualTo("TughAMEOdUI=");
        assertThat(definition.getDefinition()).isEqualTo("SELECT c.customerId, c.emailAddress FROM c");
        assertThat(definition.getStatus()).isEqualTo(CosmosGlobalSecondaryIndexBuildStatus.ACTIVE);
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void globalSecondaryIndexDefinition_newWireFormatTakesPrecedenceOverLegacy() {
        // When both property names are present, the new wire format should take precedence
        String json = "{"
            + "\"id\":\"testContainer\","
            + "\"partitionKey\":{\"paths\":[\"/pk\"],\"kind\":\"Hash\"},"
            + "\"globalSecondaryIndexDefinition\":{"
            + "\"sourceCollectionId\":\"gsi-new\","
            + "\"sourceCollectionRid\":\"NewRid=\","
            + "\"definition\":\"SELECT c.id FROM c\""
            + "},"
            + "\"materializedViewDefinition\":{"
            + "\"sourceCollectionId\":\"gsi-legacy\","
            + "\"sourceCollectionRid\":\"LegacyRid=\","
            + "\"definition\":\"SELECT c.name FROM c\""
            + "}"
            + "}";

        CosmosContainerProperties containerProperties = fromJson(json);

        CosmosGlobalSecondaryIndexDefinition definition = containerProperties.getGlobalSecondaryIndexDefinition();
        assertThat(definition).isNotNull();
        assertThat(definition.getSourceContainerId())
            .as("New wire format should take precedence when both are present")
            .isEqualTo("gsi-new");
        assertThat(definition.getSourceContainerRid()).isEqualTo("NewRid=");
        assertThat(definition.getDefinition()).isEqualTo("SELECT c.id FROM c");
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void GlobalSecondaryIndexDefinition_fullRoundTrip() throws Exception {
        // Set on container properties using the new public API
        CosmosContainerProperties original = new CosmosContainerProperties("testContainer", "/pk");
        CosmosGlobalSecondaryIndexDefinition definition =
            new CosmosGlobalSecondaryIndexDefinition("gsi-src", "SELECT c.customerId, c.emailAddress FROM c");

        // Simulate the RID resolution that CosmosAsyncDatabase performs during createContainer
        ImplementationBridgeHelpers.CosmosGlobalSecondaryIndexDefinitionHelper
            .getCosmosGlobalSecondaryIndexDefinitionAccessor()
            .setSourceCollectionRid(definition, "TughAMEOdUI=");

        original.setGlobalSecondaryIndexDefinition(definition);

        // Serialize via DocumentCollection.toJson()
        String json = ModelBridgeInternal.getResource(original).toJson();

        // Deserialize back using the same path as server responses
        CosmosContainerProperties deserialized = fromJson(json);

        CosmosGlobalSecondaryIndexDefinition deserializedDef = deserialized.getGlobalSecondaryIndexDefinition();
        assertThat(deserializedDef).isNotNull();
        assertThat(deserializedDef.getSourceContainerId()).isEqualTo("gsi-src");
        assertThat(deserializedDef.getDefinition()).isEqualTo("SELECT c.customerId, c.emailAddress FROM c");

        // Verify the RID round-tripped correctly through the wire format
        ObjectNode jsonNode = (ObjectNode) simpleObjectMapper.readTree(json);
        ObjectNode gsiDefNode = (ObjectNode) jsonNode.get("materializedViewDefinition");
        assertThat(gsiDefNode.get("sourceCollectionRid").asText()).isEqualTo("TughAMEOdUI=");
    }


    // -------------------------------------------------------------------------
    // GSI definition with null sourceContainerId – non-GSI path fallthrough
    // -------------------------------------------------------------------------

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void globalSecondaryIndexDefinition_nullSourceContainerId_fallsThroughToNonGsiPath() {
        // Simulate a deserialized GSI definition where sourceCollectionId is absent.
        // This can happen when a server response includes a materializedViewDefinition
        // node without the sourceCollectionId field. The ObjectNode constructor (used
        // during deserialization) does not enforce the sourceContainerId requirement.
        String json = "{"
            + "\"id\":\"testContainer\","
            + "\"partitionKey\":{\"paths\":[\"/pk\"],\"kind\":\"Hash\"},"
            + "\"materializedViewDefinition\":{"
            + "\"definition\":\"SELECT c.customerId FROM c\""
            + "}"
            + "}";

        CosmosContainerProperties containerProperties = fromJson(json);

        // The GSI definition itself should be non-null (the JSON node exists)
        CosmosGlobalSecondaryIndexDefinition gsiDef = containerProperties.getGlobalSecondaryIndexDefinition();
        assertThat(gsiDef).isNotNull();
        assertThat(gsiDef.getDefinition()).isEqualTo("SELECT c.customerId FROM c");

        // But sourceContainerId should be null
        assertThat(gsiDef.getSourceContainerId())
            .as("sourceContainerId should be null when absent in the JSON")
            .isNull();

        // Verify the condition used in createContainerInternal falls through to
        // the non-GSI path: (gsiDefinition != null && gsiDefinition.getSourceContainerId() != null) == false
        boolean wouldAttemptRidResolution = gsiDef != null && gsiDef.getSourceContainerId() != null;
        assertThat(wouldAttemptRidResolution)
            .as("When sourceContainerId is null, RID resolution should be skipped (non-GSI path)")
            .isFalse();
    }

}