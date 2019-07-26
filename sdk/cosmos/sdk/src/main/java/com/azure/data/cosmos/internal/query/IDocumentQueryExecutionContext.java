// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.query;

import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.Resource;
import reactor.core.publisher.Flux;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public interface IDocumentQueryExecutionContext<T extends Resource> {

    Flux<FeedResponse<T>> executeAsync();
}
