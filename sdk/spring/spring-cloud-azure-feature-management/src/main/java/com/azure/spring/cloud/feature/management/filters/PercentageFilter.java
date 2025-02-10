// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.filters;

import com.azure.spring.cloud.feature.management.models.FeatureFilterEvaluationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.azure.spring.cloud.feature.management.models.FilterParameters.PERCENTAGE_FILTER_SETTING;

/**
 * A feature filter that can be used to activate a feature based on a random percentage.
 */
public final class PercentageFilter implements FeatureFilter {

    /**
     * Creates an instance of {@link PercentageFilter}
     */
    public PercentageFilter() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(PercentageFilter.class);

    /**
     * Performs a percentage based evaluation to determine whether a feature is enabled.
     *
     * @param context The feature evaluation context.
     * @return True if the feature is enabled, false otherwise.
     * @throws NumberFormatException if the percentage filter setting is not a parsable double
     */
    @Override
    public boolean evaluate(FeatureFilterEvaluationContext context) {
        String value = String.valueOf(context.getParameters().get(PERCENTAGE_FILTER_SETTING));

        boolean result = true;

        if ("null".equals(value) || Double.parseDouble(value) < 0) {
            LOGGER.warn("The {} feature filter does not have a valid {} value for feature {}.",
                this.getClass().getSimpleName(), PERCENTAGE_FILTER_SETTING, context.getName());
            result = false;
        } else {
            result = (Math.random() * 100) <= Double.parseDouble(value);
        }

        return result;
    }

}
