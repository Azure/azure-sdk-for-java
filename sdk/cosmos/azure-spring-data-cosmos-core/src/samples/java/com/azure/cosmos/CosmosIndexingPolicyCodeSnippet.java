// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 */

import com.azure.cosmos.models.IndexingMode;
import com.azure.spring.data.cosmos.Constants;

public @interface CosmosIndexingPolicyCodeSnippet {

    // Indicate if indexing policy use automatic or not
    // Default value is true
    boolean automatic() default Constants.DEFAULT_INDEXING_POLICY_AUTOMATIC;

    // Indexing policy mode, option Consistent.
    IndexingMode mode() default IndexingMode.CONSISTENT;

    // Included paths for indexing
    String[] includePaths() default {};

    // Excluded paths for indexing
    String[] excludePaths() default {};
}
