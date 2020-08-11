// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.CosmosPagedFluxOptions;
import com.azure.cosmos.models.FeedResponse;
import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Flux;

import java.util.function.Function;

public interface Transformer<T> {
    Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> transform(Function<CosmosPagedFluxOptions, Flux<FeedResponse<JsonNode>>> func);
}
