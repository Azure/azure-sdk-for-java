// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos;

import com.azure.cosmos.models.IndexingMode;

public @interface CosmosIndexingPolicyCodeSnippet {

    // BEGIN: readme-sample-CosmosIndexingPolicyCodeSnippet
    // Indicate if indexing policy use automatic or not
    // Default value is true
    boolean automatic() default Constants.DEFAULT_INDEXING_POLICY_AUTOMATIC;

    // Indexing policy mode, option Consistent.
    IndexingMode mode() default IndexingMode.CONSISTENT;

    // Included paths for indexing
    String[] includePaths() default {};

    // Excluded paths for indexing
    String[] excludePaths() default {};
    // END: readme-sample-CosmosIndexingPolicyCodeSnippet
}
