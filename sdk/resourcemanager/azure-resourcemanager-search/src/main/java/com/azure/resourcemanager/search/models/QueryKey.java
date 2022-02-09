// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.search.models;

import com.azure.core.annotation.Fluent;

/**
 * Describes an API key for a given Azure Cognitive Search service that has permissions
 * for query operations only.
 */
@Fluent
public interface QueryKey {
    /**
     * @return the name of the query API key
     */
    String name();

    /**
     * @return the key value
     */
    String key();
}
