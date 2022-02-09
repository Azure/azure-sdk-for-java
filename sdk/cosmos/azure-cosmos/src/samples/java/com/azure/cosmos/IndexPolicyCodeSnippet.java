// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.DataType;
import com.azure.cosmos.implementation.HashIndex;
import com.azure.cosmos.implementation.Index;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.implementation.RangeIndex;
import com.azure.cosmos.implementation.SpatialIndex;
import com.azure.cosmos.models.ModelBridgeInternal;

/**
 * Code snippets for {@link IndexingPolicy}
 */
public class IndexPolicyCodeSnippet {

    public void codeSnippetForIndexPolicyConstructor() {
        // BEGIN: com.azure.cosmos.indexingPolicy.defaultOverride
        HashIndex hashIndexOverride = Index.hash(DataType.STRING, 5);
        RangeIndex rangeIndexOverride = Index.range(DataType.NUMBER, 2);
        SpatialIndex spatialIndexOverride = Index.spatial(DataType.POINT);

        IndexingPolicy indexingPolicy = ModelBridgeInternal.createIndexingPolicy(new Index[]{hashIndexOverride,
            rangeIndexOverride, spatialIndexOverride});
        // END: com.azure.cosmos.indexingPolicy.defaultOverride
    }
}
