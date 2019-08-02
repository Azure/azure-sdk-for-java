// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed;

import com.azure.data.cosmos.CosmosItemRequestOptions;
import com.azure.data.cosmos.FeedOptions;

/**
 * Defines request options for lease requests to use with {@link LeaseStoreManager}.
 */
public interface RequestOptionsFactory {

    CosmosItemRequestOptions createRequestOptions(Lease lease);

    FeedOptions createFeedOptions();
}
