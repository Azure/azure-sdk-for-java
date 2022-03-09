// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.data;

import com.azure.cosmos.models.IndexingPolicy;


public interface CollectionAttributes {

    /**
     * @return IndexingPolicy definition for a collection used to store a specific entity type
     */
    IndexingPolicy indexingPolicy();
}
