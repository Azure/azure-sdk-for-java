// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.filters;

import com.azure.spring.cloud.feature.management.models.FeatureFilterEvaluationContext;

/**
 * A filter that always returns true
 */
public final class AlwaysOnFilter implements FeatureFilter {

    /**
     * Creates an instance of {@link AlwaysOnFilter}
     */
    public AlwaysOnFilter() {
    }

    @Override
    public boolean evaluate(FeatureFilterEvaluationContext context) {
        return true;
    }

}
