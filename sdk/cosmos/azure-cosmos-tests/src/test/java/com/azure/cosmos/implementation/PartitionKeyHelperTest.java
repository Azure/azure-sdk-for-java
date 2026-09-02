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
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link PartitionKeyHelper#canCompletePartitionKeyWithId} and partition key completion.
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
    public void canCompletePartitionKeyWithId_requiresIdAsLastPathAndExactPrefix() {
        assertThat(PartitionKeyHelper.canCompletePartitionKeyWithId(
            pkDefinition(PartitionKind.HASH, "/id"), null)).isTrue();
        assertThat(PartitionKeyHelper.canCompletePartitionKeyWithId(
            pkDefinition(PartitionKind.MULTI_HASH, "/id"), null)).isTrue();
        assertThat(PartitionKeyHelper.canCompletePartitionKeyWithId(
            pkDefinition(PartitionKind.MULTI_HASH, "/ZipCode", "/City", "/id"),
            toInternal(new PartitionKeyBuilder().add("10001").add("Seattle").build()))).isTrue();

        assertThat(PartitionKeyHelper.canCompletePartitionKeyWithId(
            pkDefinition(PartitionKind.HASH, "/pk"), null)).isFalse();
        assertThat(PartitionKeyHelper.canCompletePartitionKeyWithId(
            pkDefinition(PartitionKind.MULTI_HASH, "/ZipCode", "/City"),
            toInternal(new PartitionKeyBuilder().add("10001").build()))).isFalse();
        // "/id" must be the LAST path, not just present.
        assertThat(PartitionKeyHelper.canCompletePartitionKeyWithId(
            pkDefinition(PartitionKind.MULTI_HASH, "/id", "/City"),
            toInternal(new PartitionKey("myId")))).isFalse();
        assertThat(PartitionKeyHelper.canCompletePartitionKeyWithId(null, null)).isFalse();
    }

    @Test(groups = "unit")
    public void ensureId_nonIdLastPath_returnsOriginalPartitionKey() {
        PartitionKeyDefinition definition = pkDefinition(PartitionKind.MULTI_HASH, "/ZipCode", "/City");
        PartitionKeyInternal provided = toInternal(new PartitionKeyBuilder().add("10001").build());

        PartitionKeyInternal result =
            PartitionKeyHelper.completePartitionKeyInternalWithIdIfNeeded(definition, provided, "myId");

        assertThat(result).isSameAs(provided);
    }

    @Test(groups = "unit")
    public void ensureId_prefixPartitionKey_appendsId() {
        PartitionKeyDefinition definition = pkDefinition(PartitionKind.MULTI_HASH, "/ZipCode", "/City", "/id");
        PartitionKeyInternal provided =
            toInternal(new PartitionKeyBuilder().add("10001").add("Seattle").build());

        PartitionKeyInternal result =
            PartitionKeyHelper.completePartitionKeyInternalWithIdIfNeeded(definition, provided, "myId");

        assertThat(result.getComponents()).hasSize(3);
        assertThat(result.toObjectArray()).containsExactly("10001", "Seattle", "myId");
    }

    @Test(groups = "unit")
    public void isFullPartitionKey_requiresComponentForEveryPath() {
        PartitionKeyDefinition definition =
            pkDefinition(PartitionKind.MULTI_HASH, "/ZipCode", "/City", "/Region");

        assertThat(PartitionKeyHelper.isFullPartitionKey(
            pkDefinition(PartitionKind.HASH, "/pk"),
            toInternal(new PartitionKey("value")))).isTrue();
        assertThat(PartitionKeyHelper.isFullPartitionKey(
            definition,
            toInternal(new PartitionKeyBuilder().add("10001").add("Seattle").add("west").build()))).isTrue();
        assertThat(PartitionKeyHelper.isFullPartitionKey(
            definition,
            toInternal(new PartitionKeyBuilder().add("10001").add("Seattle").build()))).isFalse();
        assertThat(PartitionKeyHelper.isFullPartitionKey(definition, null)).isFalse();
    }

    @Test(groups = "unit")
    public void requireFullPartitionKey_rejectsIncompleteKey() {
        PartitionKeyDefinition definition =
            pkDefinition(PartitionKind.MULTI_HASH, "/ZipCode", "/City");
        PartitionKeyInternal provided = toInternal(new PartitionKey("10001"));

        assertThatThrownBy(() -> PartitionKeyHelper.requireFullPartitionKey(definition, provided))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(RMResources.PartitionKeyMismatch);
    }

    @Test(groups = "unit")
    public void ensureId_fullySpecifiedPartitionKey_returnsOriginalPartitionKey() {
        PartitionKeyDefinition definition = pkDefinition(PartitionKind.MULTI_HASH, "/ZipCode", "/City", "/id");
        PartitionKeyInternal provided =
            toInternal(new PartitionKeyBuilder().add("10001").add("Seattle").add("myId").build());

        PartitionKeyInternal result =
            PartitionKeyHelper.completePartitionKeyInternalWithIdIfNeeded(definition, provided, "myId");

        assertThat(result).isSameAs(provided);
        assertThat(result.getComponents()).hasSize(3);
    }

    @Test(groups = "unit")
    public void ensureId_nonePartitionKey_returnsOriginalPartitionKey() {
        PartitionKeyDefinition definition = pkDefinition(PartitionKind.MULTI_HASH, "/ZipCode", "/City", "/id");
        PartitionKeyInternal provided = toInternal(PartitionKey.NONE);

        assertThat(PartitionKeyHelper.canCompletePartitionKeyWithId(definition, provided)).isFalse();
        assertThat(PartitionKeyHelper.completePartitionKeyInternalWithIdIfNeeded(
            definition, provided, "myId"))
            .isSameAs(provided);
        assertThat(PartitionKeyHelper.completePartitionKeyWithIdIfNeeded(
            definition, PartitionKey.NONE, "myId"))
            .isSameAs(PartitionKey.NONE);
    }

    @Test(groups = "unit")
    public void ensureId_nullPartitionKey_buildsPartitionKeyFromId() {
        PartitionKeyDefinition definition = pkDefinition(PartitionKind.MULTI_HASH, "/ZipCode", "/City", "/id");

        PartitionKeyInternal result =
            PartitionKeyHelper.completePartitionKeyInternalWithIdIfNeeded(definition, null, "myId");

        assertThat(result.getComponents()).hasSize(3);
        assertThat(result.toObjectArray()).containsExactly(null, null, "myId");
    }

    @Test(groups = "unit")
    public void ensureId_singleIdPath_nullPartitionKey_buildsPartitionKeyFromId() {
        PartitionKeyDefinition definition = pkDefinition(PartitionKind.HASH, "/id");

        PartitionKeyInternal result =
            PartitionKeyHelper.completePartitionKeyInternalWithIdIfNeeded(definition, null, "myId");

        assertThat(result.getComponents()).hasSize(1);
        assertThat(result.toObjectArray()).containsExactly("myId");
    }

    @Test(groups = "unit")
    public void ensureId_prefixPartitionKey_nullItemId_throws() {
        PartitionKeyDefinition definition = pkDefinition(PartitionKind.MULTI_HASH, "/ZipCode", "/City", "/id");
        PartitionKeyInternal provided =
            toInternal(new PartitionKeyBuilder().add("10001").add("Seattle").build());

        assertThatThrownBy(() ->
            PartitionKeyHelper.completePartitionKeyInternalWithIdIfNeeded(definition, provided, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("itemId needs to be specified");
    }

    @Test(groups = "unit")
    public void ensureId_prefixPartitionKey_emptyItemId_throws() {
        PartitionKeyDefinition definition = pkDefinition(PartitionKind.MULTI_HASH, "/ZipCode", "/City", "/id");
        PartitionKeyInternal provided =
            toInternal(new PartitionKeyBuilder().add("10001").add("Seattle").build());

        assertThatThrownBy(() ->
            PartitionKeyHelper.completePartitionKeyInternalWithIdIfNeeded(definition, provided, ""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("itemId needs to be specified");
    }

    @Test(groups = "unit")
    public void ensureId_wrongComponentCount_returnsOriginalPartitionKey() {
        // definition has 3 paths, but the provided partition key has only 1 component (not pathCount - 1).
        PartitionKeyDefinition definition = pkDefinition(PartitionKind.MULTI_HASH, "/ZipCode", "/City", "/id");
        PartitionKeyInternal provided = toInternal(new PartitionKeyBuilder().add("10001").build());

        PartitionKeyInternal result =
            PartitionKeyHelper.completePartitionKeyInternalWithIdIfNeeded(definition, provided, "myId");

        assertThat(result).isSameAs(provided);
    }

    @Test(groups = "unit")
    public void ensureId_fullySpecifiedPartitionKey_doesNotResolveItemId() {
        PartitionKeyDefinition definition = pkDefinition(PartitionKind.MULTI_HASH, "/pk", "/id");
        PartitionKeyInternal provided =
            toInternal(new PartitionKeyBuilder().add("pkValue").add("myId").build());
        AtomicBoolean itemIdRequested = new AtomicBoolean();

        PartitionKeyInternal result = PartitionKeyHelper.completePartitionKeyInternalWithIdIfNeededLazy(
            definition,
            provided,
            () -> {
                itemIdRequested.set(true);
                return "otherId";
            });

        assertThat(result).isSameAs(provided);
        assertThat(itemIdRequested).isFalse();
    }

    @Test(groups = "unit")
    public void ensureId_twoLevelPrefix_appendsId() {
        PartitionKeyDefinition definition = pkDefinition(PartitionKind.MULTI_HASH, "/pk", "/id");
        PartitionKeyInternal provided = toInternal(new PartitionKeyBuilder().add("pkValue").build());

        PartitionKeyInternal result =
            PartitionKeyHelper.completePartitionKeyInternalWithIdIfNeeded(definition, provided, "myId");

        assertThat(result.getComponents()).hasSize(2);
        assertThat(result.toObjectArray()).containsExactly("pkValue", "myId");
    }

    @Test(groups = "unit")
    public void completePartitionKeyWithIdIfNeeded_appendsId() {
        PartitionKeyDefinition definition = pkDefinition(PartitionKind.MULTI_HASH, "/pk", "/id");
        PartitionKey provided = new PartitionKeyBuilder().add("pkValue").build();

        PartitionKey result =
            PartitionKeyHelper.completePartitionKeyWithIdIfNeeded(definition, provided, "myId");

        assertThat(toInternal(result).toObjectArray()).containsExactly("pkValue", "myId");
    }

    @Test(groups = "unit")
    public void completePartitionKeyWithIdIfNeeded_nonIdLastPath_returnsSameInstance() {
        PartitionKeyDefinition definition = pkDefinition(PartitionKind.HASH, "/pk");
        PartitionKey provided = new PartitionKey("pkValue");

        PartitionKey result =
            PartitionKeyHelper.completePartitionKeyWithIdIfNeeded(definition, provided, "myId");

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
            PartitionKeyHelper.completePartitionKeyInternalWithIdIfNeeded(definition, provided, "myId");

        assertThat(result).isSameAs(provided);
    }
}
