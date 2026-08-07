// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyBuilder;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKind;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link PartitionKeyHelper#isLastPartitionKeyPathId} and
 * {@link PartitionKeyHelper#ensureIdIsInPartitionKeyInternal} that validate the behaviour of
 * appending the item id to a hierarchical partition key whose last path is "/id".
 */
public class PartitionKeyHelperTest {

    private static PartitionKeyDefinition pkDefinition(PartitionKind kind, String... paths) {
        PartitionKeyDefinition definition = new PartitionKeyDefinition();
        definition.setKind(kind);
        definition.setPaths(Arrays.asList(paths));
        return definition;
    }

    private static PartitionKeyInternal toInternal(PartitionKey partitionKey) {
        return ModelBridgeInternal.getPartitionKeyInternal(partitionKey);
    }

    @Test(groups = "unit")
    public void isLastPartitionKeyPathId_returnsTrueOnlyWhenLastPathIsId() {
        assertThat(PartitionKeyHelper.isLastPartitionKeyPathId(
            pkDefinition(PartitionKind.HASH, "/id"))).isTrue();
        assertThat(PartitionKeyHelper.isLastPartitionKeyPathId(
            pkDefinition(PartitionKind.MULTI_HASH, "/id"))).isTrue();
        assertThat(PartitionKeyHelper.isLastPartitionKeyPathId(
            pkDefinition(PartitionKind.MULTI_HASH, "/ZipCode", "/City", "/id"))).isTrue();

        assertThat(PartitionKeyHelper.isLastPartitionKeyPathId(
            pkDefinition(PartitionKind.HASH, "/pk"))).isFalse();
        assertThat(PartitionKeyHelper.isLastPartitionKeyPathId(
            pkDefinition(PartitionKind.MULTI_HASH, "/ZipCode", "/City"))).isFalse();
        // "/id" must be the LAST path, not just present.
        assertThat(PartitionKeyHelper.isLastPartitionKeyPathId(
            pkDefinition(PartitionKind.MULTI_HASH, "/id", "/City"))).isFalse();
        assertThat(PartitionKeyHelper.isLastPartitionKeyPathId(null)).isFalse();
    }

    @Test(groups = "unit")
    public void ensureId_nonIdLastPath_returnsOriginalPartitionKey() {
        PartitionKeyDefinition definition = pkDefinition(PartitionKind.MULTI_HASH, "/ZipCode", "/City");
        PartitionKeyInternal provided = toInternal(new PartitionKeyBuilder().add("10001").build());

        PartitionKeyInternal result =
            PartitionKeyHelper.ensureIdIsInPartitionKeyInternal(definition, provided, "myId");

        assertThat(result).isSameAs(provided);
    }

    @Test(groups = "unit")
    public void ensureId_prefixPartitionKey_appendsId() {
        PartitionKeyDefinition definition = pkDefinition(PartitionKind.MULTI_HASH, "/ZipCode", "/City", "/id");
        PartitionKeyInternal provided =
            toInternal(new PartitionKeyBuilder().add("10001").add("Seattle").build());

        PartitionKeyInternal result =
            PartitionKeyHelper.ensureIdIsInPartitionKeyInternal(definition, provided, "myId");

        assertThat(result.getComponents()).hasSize(3);
        assertThat(result.toObjectArray()).containsExactly("10001", "Seattle", "myId");
    }

    @Test(groups = "unit")
    public void ensureId_fullySpecifiedPartitionKey_returnsOriginalPartitionKey() {
        PartitionKeyDefinition definition = pkDefinition(PartitionKind.MULTI_HASH, "/ZipCode", "/City", "/id");
        PartitionKeyInternal provided =
            toInternal(new PartitionKeyBuilder().add("10001").add("Seattle").add("myId").build());

        PartitionKeyInternal result =
            PartitionKeyHelper.ensureIdIsInPartitionKeyInternal(definition, provided, "myId");

        assertThat(result).isSameAs(provided);
        assertThat(result.getComponents()).hasSize(3);
    }

    @Test(groups = "unit")
    public void ensureId_nullPartitionKey_buildsPartitionKeyFromId() {
        PartitionKeyDefinition definition = pkDefinition(PartitionKind.MULTI_HASH, "/ZipCode", "/City", "/id");

        PartitionKeyInternal result =
            PartitionKeyHelper.ensureIdIsInPartitionKeyInternal(definition, null, "myId");

        assertThat(result.getComponents()).hasSize(3);
        assertThat(result.toObjectArray()).containsExactly(null, null, "myId");
    }

    @Test(groups = "unit")
    public void ensureId_singleIdPath_nullPartitionKey_buildsPartitionKeyFromId() {
        PartitionKeyDefinition definition = pkDefinition(PartitionKind.HASH, "/id");

        PartitionKeyInternal result =
            PartitionKeyHelper.ensureIdIsInPartitionKeyInternal(definition, null, "myId");

        assertThat(result.getComponents()).hasSize(1);
        assertThat(result.toObjectArray()).containsExactly("myId");
    }

    @Test(groups = "unit")
    public void ensureId_prefixPartitionKey_nullItemId_throws() {
        PartitionKeyDefinition definition = pkDefinition(PartitionKind.MULTI_HASH, "/ZipCode", "/City", "/id");
        PartitionKeyInternal provided =
            toInternal(new PartitionKeyBuilder().add("10001").add("Seattle").build());

        assertThatThrownBy(() ->
            PartitionKeyHelper.ensureIdIsInPartitionKeyInternal(definition, provided, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("itemId needs to be specified");
    }

    @Test(groups = "unit")
    public void ensureId_prefixPartitionKey_emptyItemId_throws() {
        PartitionKeyDefinition definition = pkDefinition(PartitionKind.MULTI_HASH, "/ZipCode", "/City", "/id");
        PartitionKeyInternal provided =
            toInternal(new PartitionKeyBuilder().add("10001").add("Seattle").build());

        assertThatThrownBy(() ->
            PartitionKeyHelper.ensureIdIsInPartitionKeyInternal(definition, provided, ""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("itemId needs to be specified");
    }

    @Test(groups = "unit")
    public void ensureId_wrongComponentCount_returnsOriginalPartitionKey() {
        // definition has 3 paths, but the provided partition key has only 1 component (not pathCount - 1).
        PartitionKeyDefinition definition = pkDefinition(PartitionKind.MULTI_HASH, "/ZipCode", "/City", "/id");
        PartitionKeyInternal provided = toInternal(new PartitionKeyBuilder().add("10001").build());

        PartitionKeyInternal result =
            PartitionKeyHelper.ensureIdIsInPartitionKeyInternal(definition, provided, "myId");

        assertThat(result).isSameAs(provided);
    }

    @Test(groups = "unit")
    public void ensureId_twoLevelPrefix_appendsId() {
        PartitionKeyDefinition definition = pkDefinition(PartitionKind.MULTI_HASH, "/pk", "/id");
        PartitionKeyInternal provided = toInternal(new PartitionKeyBuilder().add("pkValue").build());

        PartitionKeyInternal result =
            PartitionKeyHelper.ensureIdIsInPartitionKeyInternal(definition, provided, "myId");

        assertThat(result.getComponents()).hasSize(2);
        assertThat(result.toObjectArray()).containsExactly("pkValue", "myId");
    }

    @Test(groups = "unit")
    public void ensureIdIsInPartitionKey_partitionKeyOverload_appendsId() {
        PartitionKeyDefinition definition = pkDefinition(PartitionKind.MULTI_HASH, "/pk", "/id");
        PartitionKey provided = new PartitionKeyBuilder().add("pkValue").build();

        PartitionKey result = PartitionKeyHelper.ensureIdIsInPartitionKey(definition, provided, "myId");

        assertThat(toInternal(result).toObjectArray()).containsExactly("pkValue", "myId");
    }

    @Test(groups = "unit")
    public void ensureIdIsInPartitionKey_partitionKeyOverload_nonIdLastPath_returnsSameInstance() {
        PartitionKeyDefinition definition = pkDefinition(PartitionKind.HASH, "/pk");
        PartitionKey provided = new PartitionKey("pkValue");

        PartitionKey result = PartitionKeyHelper.ensureIdIsInPartitionKey(definition, provided, "myId");

        assertThat(result).isSameAs(provided);
    }

    @Test(groups = "unit")
    public void ensureId_emptyPathDefinition_returnsOriginalPartitionKey() {
        PartitionKeyDefinition definition = new PartitionKeyDefinition();
        definition.setKind(PartitionKind.HASH);
        // getPaths() returns an empty list by default.
        assertThat(definition.getPaths()).isEqualTo(Collections.emptyList());

        PartitionKeyInternal provided = toInternal(new PartitionKey("pkValue"));
        PartitionKeyInternal result =
            PartitionKeyHelper.ensureIdIsInPartitionKeyInternal(definition, provided, "myId");

        assertThat(result).isSameAs(provided);
    }
}
