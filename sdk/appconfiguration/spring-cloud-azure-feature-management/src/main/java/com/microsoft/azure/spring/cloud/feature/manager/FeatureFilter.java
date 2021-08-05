// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.feature.manager;

import com.microsoft.azure.spring.cloud.feature.manager.entities.FeatureFilterEvaluationContext;

/**
 * A Filter for Feature Management that is attached to Features. The filter needs to
 * have @Component set to be found by feature management.
 */
public interface FeatureFilter {

    /**
     * Evaluates if the filter is on or off. Returning true results in Feature evaluation
     * ending and returning true. Returning false results in the next Feature evaluation
     * to continue.
     *
     * @param context The context for whether or not the filter is passed.
     * @return True if the feature is enabled, false otherwise.
     */
    boolean evaluate(FeatureFilterEvaluationContext context);

}
