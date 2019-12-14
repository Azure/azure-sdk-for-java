// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

/**
 * Code snippets for {@link IndexingPolicy}
 */
public class IndexPolicyCodeSnippet {

    public void codeSnippetForIndexPolicyConstructor() {
        // BEGIN: com.azure.cosmos.indexingPolicy.defaultOverride
        HashIndex hashIndexOverride = Index.hash(DataType.STRING, 5);
        RangeIndex rangeIndexOverride = Index.range(DataType.NUMBER, 2);
        SpatialIndex spatialIndexOverride = Index.spatial(DataType.POINT);

        IndexingPolicy indexingPolicy = new IndexingPolicy(new Index[]{hashIndexOverride, rangeIndexOverride, spatialIndexOverride});
        // END: com.azure.cosmos.indexingPolicy.defaultOverride
    }
}
