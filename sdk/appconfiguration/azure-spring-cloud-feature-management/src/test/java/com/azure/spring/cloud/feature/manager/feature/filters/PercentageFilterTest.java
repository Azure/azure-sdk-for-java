// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.feature.filters;

import static com.azure.spring.cloud.feature.manager.FilterParameters.PERCENTAGE_FILTER_SETTING;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;

import org.junit.jupiter.api.Test;

import com.azure.spring.cloud.feature.manager.entities.FeatureFilterEvaluationContext;

public class PercentageFilterTest {

    @Test
    public void zeroPercentage() {
        PercentageFilter filter = new PercentageFilter();
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();
        parameters.put(PERCENTAGE_FILTER_SETTING, "0");
        context.setParameters(parameters);
        assertFalse(filter.evaluate(context));
    }

    @Test
    public void hundredPercentage() {
        PercentageFilter filter = new PercentageFilter();
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();
        parameters.put(PERCENTAGE_FILTER_SETTING, "100");
        context.setParameters(parameters);
        assertTrue(filter.evaluate(context));
    }

    @Test
    public void errorPercentage() {
        PercentageFilter filter = new PercentageFilter();
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();
        parameters.put(PERCENTAGE_FILTER_SETTING, "-1");
        context.setParameters(parameters);
        assertFalse(filter.evaluate(context));
    }

    @Test
    public void nullPercentage() {
        PercentageFilter filter = new PercentageFilter();
        FeatureFilterEvaluationContext context = new FeatureFilterEvaluationContext();
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();
        context.setParameters(parameters);
        assertFalse(filter.evaluate(context));
    }

}
