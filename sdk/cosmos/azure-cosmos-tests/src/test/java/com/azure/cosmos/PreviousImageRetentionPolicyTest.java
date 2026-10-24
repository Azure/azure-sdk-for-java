// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.CosmosChangeFeedPreviousImageRetentionMode;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PreviousImageRetentionPolicyTest {
    private static final String POLICY_NAME = "previousImageRetentionPolicy";
    private static final String FEATURE_PATH = "/supportedFeatures/allVersionsAndDeletes";
    private static final String SERVICE_POLICY = "{\"supportedFeatures\":{"
        + "\"allVersionsAndDeletes\":{\"mode\":1,\"includedPaths\":[\"/nested\",\"/version\"],\"futureSetting\":{\"value\":true}},"
        + "\"globalSecondaryIndex\":{\"mode\":1,\"includedPaths\":[\"/id\"]},"
        + "\"containerCopy\":{\"mode\":2},"
        + "\"embeddingGeneratorService\":{\"mode\":3},"
        + "\"futureFeature\":{\"mode\":7,\"setting\":\"preserved\"}},\"futurePolicySetting\":42}";

    @DataProvider(name = "modes")
    public Object[][] modes() {
        return new Object[][] {
            { CosmosChangeFeedPreviousImageRetentionMode.DISABLED, 0 },
            { CosmosChangeFeedPreviousImageRetentionMode.ENABLED_FOR_REPLACE_OPERATIONS, 1 },
            { CosmosChangeFeedPreviousImageRetentionMode.ENABLED_FOR_DELETE_OPERATIONS, 2 },
            { CosmosChangeFeedPreviousImageRetentionMode.ENABLED_FOR_ALL_OPERATIONS, 3 }
        };
    }

    @Test(groups = "unit", dataProvider = "modes")
    public void modeUsesNumericNestedWireFormat(CosmosChangeFeedPreviousImageRetentionMode mode, int wireValue)
        throws JsonProcessingException {
        CosmosContainerProperties properties = new CosmosContainerProperties("container", "/pk");
        assertThat(properties.setChangeFeedPreviousImageRetentionMode(mode)).isSameAs(properties);
        assertThat(properties.getChangeFeedPreviousImageRetentionMode()).isEqualTo(mode);

        DocumentCollection collection = ModelBridgeInternal.getV2Collection(properties);
        JsonNode serialized = Utils.getSimpleObjectMapper().readTree(collection.toJson());
        assertThat(serialized.get(POLICY_NAME)).isEqualTo(Utils.getSimpleObjectMapper().readTree(
            "{\"supportedFeatures\":{\"allVersionsAndDeletes\":{\"mode\":" + wireValue + "}}}"));
        assertThat(serialized.get(POLICY_NAME).at(FEATURE_PATH + "/mode").isInt()).isTrue();
        assertThat(serialized.has("changeFeedPolicy")).isFalse();

        CosmosContainerProperties roundTripped = fromCollection(new DocumentCollection(collection.toJson()));
        assertThat(roundTripped.getChangeFeedPreviousImageRetentionMode()).isEqualTo(mode);
    }

    @Test(groups = "unit", dataProvider = "modes")
    public void readsServiceModeAndPreservesOtherPolicyFields(CosmosChangeFeedPreviousImageRetentionMode mode, int wireValue)
        throws JsonProcessingException {
        ObjectNode expected = (ObjectNode) Utils.getSimpleObjectMapper().readTree(SERVICE_POLICY);
        ((ObjectNode) expected.at(FEATURE_PATH)).put("mode", wireValue);
        CosmosContainerProperties properties = fromPolicy(expected.toString());

        assertThat(properties.getChangeFeedPreviousImageRetentionMode()).isEqualTo(mode);
        assertThat(serializedPolicy(properties)).isEqualTo(expected);
        assertThat(properties.getChangeFeedPolicy().getRetentionDurationForAllVersionsAndDeletesPolicy())
            .isEqualTo(Duration.ofMinutes(5));
        assertThat(properties.getDefaultTimeToLiveInSeconds()).isEqualTo(-1);
    }

    @Test(groups = "unit", dataProvider = "modes")
    public void changesOnlyModeInServicePopulatedPolicy(CosmosChangeFeedPreviousImageRetentionMode mode, int wireValue)
        throws JsonProcessingException {
        CosmosContainerProperties properties = fromPolicy(SERVICE_POLICY);
        ObjectNode before = ModelBridgeInternal.getV2Collection(properties).getPropertyBag().deepCopy();
        ObjectNode expected = before.deepCopy();
        ((ObjectNode) expected.get(POLICY_NAME).at(FEATURE_PATH)).put("mode", wireValue);

        properties.setChangeFeedPreviousImageRetentionMode(mode);
        DocumentCollection after = ModelBridgeInternal.getV2Collection(properties);
        after.populatePropertyBag();
        assertThat(after.getPropertyBag()).isEqualTo(expected);
        assertThat(before.get(POLICY_NAME)).isEqualTo(Utils.getSimpleObjectMapper().readTree(SERVICE_POLICY));

        DocumentCollection restored = DocumentCollection.fromSerializableObjectNode(after.toSerializableObjectNode());
        assertThat(restored.getPropertyBag()).isEqualTo(expected);
        assertThat(fromCollection(restored).getChangeFeedPreviousImageRetentionMode()).isEqualTo(mode);
    }

    @Test(groups = "unit")
    public void unsetModeDoesNotAddPolicyOrChangeSerialization() {
        CosmosContainerProperties properties = new CosmosContainerProperties("container", "/pk");
        String before = ModelBridgeInternal.getV2Collection(properties).toJson();
        assertThat(properties.getChangeFeedPreviousImageRetentionMode()).isNull();
        DocumentCollection collection = ModelBridgeInternal.getV2Collection(properties);
        collection.populatePropertyBag();

        assertThat(collection.toJson()).isEqualTo(before);
        assertThat(collection.has(POLICY_NAME)).isFalse();
        CosmosContainerProperties clone = fromCollection(collection);
        assertThat(clone.getChangeFeedPreviousImageRetentionMode()).isNull();
        assertThat(ModelBridgeInternal.getV2Collection(clone).toJson()).isEqualTo(before);
    }

    @DataProvider(name = "policiesWithoutMode")
    public Object[][] policiesWithoutMode() {
        return new Object[][] {
            { "{}" },
            { "{\"supportedFeatures\":{}}" },
            { "{\"supportedFeatures\":{\"containerCopy\":{\"mode\":2}}}" },
            { "{\"supportedFeatures\":{\"allVersionsAndDeletes\":{\"includedPaths\":[\"/\"],\"future\":true}}}" }
        };
    }

    @Test(groups = "unit", dataProvider = "policiesWithoutMode")
    public void readingUnspecifiedFeaturePreservesServicePolicy(String policy) throws JsonProcessingException {
        CosmosContainerProperties properties = fromPolicy(policy);
        assertThat(properties.getChangeFeedPreviousImageRetentionMode()).isNull();
        assertThat(serializedPolicy(properties)).isEqualTo(Utils.getSimpleObjectMapper().readTree(policy));
    }

    @Test(groups = "unit")
    public void settingModeAddsOnlyMissingFeature() throws JsonProcessingException {
        CosmosContainerProperties properties = fromPolicy("{\"supportedFeatures\":{\"containerCopy\":{\"mode\":2}}}");
        properties.setChangeFeedPreviousImageRetentionMode(CosmosChangeFeedPreviousImageRetentionMode.DISABLED);
        assertThat(serializedPolicy(properties)).isEqualTo(Utils.getSimpleObjectMapper().readTree(
            "{\"supportedFeatures\":{\"containerCopy\":{\"mode\":2},\"allVersionsAndDeletes\":{\"mode\":0}}}"));
        assertThat(properties.getChangeFeedPreviousImageRetentionMode()).isEqualTo(CosmosChangeFeedPreviousImageRetentionMode.DISABLED);
    }

    @DataProvider(name = "unsupportedModes")
    public Object[][] unsupportedModes() {
        return new Object[][] {
            { "-1" }, { "4" }, { "2147483647" }, { "2147483648" },
            { "\"3\"" }, { "3.5" }, { "true" }, { "null" }
        };
    }

    @Test(groups = "unit", dataProvider = "unsupportedModes")
    public void unsupportedModeIsPreservedUntilTypedAccess(String wireValue) throws JsonProcessingException {
        String policy = "{\"supportedFeatures\":{\"allVersionsAndDeletes\":{\"mode\":" + wireValue + "}}}";
        CosmosContainerProperties properties = fromPolicy(policy);
        DocumentCollection collection = ModelBridgeInternal.getV2Collection(properties);
        collection.populatePropertyBag();
        DocumentCollection cached = DocumentCollection.fromSerializableObjectNode(collection.toSerializableObjectNode());
        CosmosContainerProperties clone = fromCollection(cached);
        clone.setDefaultTimeToLiveInSeconds(60);

        assertThat(serializedPolicy(clone)).isEqualTo(Utils.getSimpleObjectMapper().readTree(policy));
        assertThatThrownBy(clone::getChangeFeedPreviousImageRetentionMode)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("previous image retention mode");
        assertThat(serializedPolicy(clone)).isEqualTo(Utils.getSimpleObjectMapper().readTree(policy));
        assertThat(ModelBridgeInternal.getV2Collection(clone).getDefaultTimeToLive()).isEqualTo(60);
    }

    @Test(groups = "unit")
    public void nullModeIsRejectedWithoutChangingPolicy() {
        CosmosContainerProperties unset = new CosmosContainerProperties("container", "/pk");
        assertThatThrownBy(() -> unset.setChangeFeedPreviousImageRetentionMode(null))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("cannot be null");
        assertThat(ModelBridgeInternal.getV2Collection(unset).has(POLICY_NAME)).isFalse();

        CosmosContainerProperties configured = fromPolicy(SERVICE_POLICY);
        JsonNode before = serializedPolicy(configured).deepCopy();
        assertThatThrownBy(() -> configured.setChangeFeedPreviousImageRetentionMode(null))
            .isInstanceOf(IllegalArgumentException.class);
        assertThat(serializedPolicy(configured)).isEqualTo(before);
    }

    @Test(groups = "unit")
    public void modeChangesDoNotMutateClonedCollections() throws JsonProcessingException {
        CosmosContainerProperties properties = fromPolicy(SERVICE_POLICY);
        DocumentCollection collection = ModelBridgeInternal.getV2Collection(properties);
        CosmosContainerProperties clone = fromCollection(collection);
        properties.setChangeFeedPreviousImageRetentionMode(CosmosChangeFeedPreviousImageRetentionMode.DISABLED);
        clone.setChangeFeedPreviousImageRetentionMode(CosmosChangeFeedPreviousImageRetentionMode.ENABLED_FOR_ALL_OPERATIONS);

        assertThat(collection.getObject(POLICY_NAME)).isEqualTo(Utils.getSimpleObjectMapper().readTree(SERVICE_POLICY));
        assertThat(properties.getChangeFeedPreviousImageRetentionMode()).isEqualTo(CosmosChangeFeedPreviousImageRetentionMode.DISABLED);
        assertThat(clone.getChangeFeedPreviousImageRetentionMode()).isEqualTo(
            CosmosChangeFeedPreviousImageRetentionMode.ENABLED_FOR_ALL_OPERATIONS);
    }

    @Test(groups = "unit")
    public void repeatedUpdatesKeepDisabledExplicitAndPreserveUnspecifiedPaths() throws JsonProcessingException {
        CosmosContainerProperties properties = new CosmosContainerProperties("container", "/pk");
        for (CosmosChangeFeedPreviousImageRetentionMode mode : new CosmosChangeFeedPreviousImageRetentionMode[] {
            CosmosChangeFeedPreviousImageRetentionMode.ENABLED_FOR_DELETE_OPERATIONS,
            CosmosChangeFeedPreviousImageRetentionMode.ENABLED_FOR_ALL_OPERATIONS,
            CosmosChangeFeedPreviousImageRetentionMode.DISABLED }) {
            properties.setChangeFeedPreviousImageRetentionMode(mode);
            properties = fromCollection(ModelBridgeInternal.getV2Collection(properties));
            assertThat(properties.getChangeFeedPreviousImageRetentionMode()).isEqualTo(mode);
            assertThat(serializedPolicy(properties).at(FEATURE_PATH).has("includedPaths")).isFalse();
        }
        assertThat(serializedPolicy(properties)).isEqualTo(Utils.getSimpleObjectMapper().readTree(
            "{\"supportedFeatures\":{\"allVersionsAndDeletes\":{\"mode\":0}}}"));
    }

    @Test(groups = "unit")
    public void internalSetterRejectsUnknownModeWithoutChangingPolicy() {
        DocumentCollection collection = ModelBridgeInternal.getV2Collection(fromPolicy(SERVICE_POLICY));
        String before = collection.toJson();
        assertThatThrownBy(() -> collection.setChangeFeedPreviousImageRetentionMode(-1))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> collection.setChangeFeedPreviousImageRetentionMode(4))
            .isInstanceOf(IllegalArgumentException.class);
        assertThat(collection.toJson()).isEqualTo(before);
    }

    private static CosmosContainerProperties fromPolicy(String policy) {
        return fromCollection(new DocumentCollection("{\"id\":\"container\","
            + "\"partitionKey\":{\"paths\":[\"/pk\"],\"kind\":\"Hash\"},\"defaultTtl\":-1,"
            + "\"changeFeedPolicy\":{\"retentionDuration\":5},\"" + POLICY_NAME + "\":" + policy + "}"));
    }

    private static CosmosContainerProperties fromCollection(DocumentCollection collection) {
        return ModelBridgeInternal.getCosmosContainerPropertiesFromV2Results(Collections.singletonList(collection)).get(0);
    }

    private static JsonNode serializedPolicy(CosmosContainerProperties properties) {
        return ModelBridgeInternal.getV2Collection(properties).getObject(POLICY_NAME);
    }
}
