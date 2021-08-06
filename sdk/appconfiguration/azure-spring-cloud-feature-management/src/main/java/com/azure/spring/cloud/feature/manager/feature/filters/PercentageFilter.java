// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.feature.filters;

import static com.azure.spring.cloud.feature.manager.FilterParameters.PERCENTAGE_FILTER_SETTING;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.azure.spring.cloud.feature.manager.FeatureFilter;
import com.azure.spring.cloud.feature.manager.entities.FeatureFilterEvaluationContext;

/**
 * A feature filter that can be used to activate a feature based on a random percentage.
 */
@Component("PercentageFilter")
public class PercentageFilter implements FeatureFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PercentageFilter.class);

    /**
     * Performs a percentage based evaluation to determine whether a feature is enabled.
     *
     * @param context The feature evaluation context.
     * @return True if the feature is enabled, false otherwise.
     */
    @Override
    public boolean evaluate(FeatureFilterEvaluationContext context) {
        String value = (String) context.getParameters().get(PERCENTAGE_FILTER_SETTING);

        boolean result = true;

        if (value == null || Double.parseDouble(value) < 0) {
            LOGGER.warn("The {} feature filter does not have a valid {} value for feature {}.",
                this.getClass().getSimpleName(), PERCENTAGE_FILTER_SETTING, context.getName());
            result = false;
        } else {
            result = (Math.random() * 100) <= Double.parseDouble(value);
        }

        return result;
    }

}
