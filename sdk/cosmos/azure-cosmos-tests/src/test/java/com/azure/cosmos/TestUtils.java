// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.ChangeFeedProcessorItem;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.function.BiConsumer;

public class TestUtils {

    // todo: @abhmohanty - remove when handleLatestVersionChanges with biConsumer is made public
    public static ChangeFeedProcessorBuilder injectHandleLatestVersionChangesBiConsumerToChangeFeedProcessor(
        ChangeFeedProcessorBuilder builder, BiConsumer<List<ChangeFeedProcessorItem>, ChangeFeedProcessorContext<ChangeFeedProcessorItem>> biConsumer) {
        builder = builder.handleLatestVersionChanges(biConsumer);
        return builder;
    }

    // todo: @abhmohanty - remove when handleChanges with biConsumer is made public
    public static ChangeFeedProcessorBuilder injectHandleChangesBiConsumerWithContext(
        ChangeFeedProcessorBuilder builder, BiConsumer<List<JsonNode>, ChangeFeedProcessorContext<JsonNode>> biConsumer) {
        builder = builder.handleChanges(biConsumer);
        return builder;
    }
}
