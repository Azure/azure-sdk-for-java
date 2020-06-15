// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.implementation.Resource;
import reactor.core.publisher.Flux;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public interface IDocumentQueryExecutionComponent<T extends Resource> {

    Flux<FeedResponse<T>> drainAsync(int maxPageSize);
}
