// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.feature.manager.feature.filters;

import static com.microsoft.azure.spring.cloud.feature.manager.FilterParameters.PERCENTAGE_FILTER_SETTING;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.microsoft.azure.spring.cloud.feature.manager.entities.FeatureFilterEvaluationContext;
import java.util.LinkedHashMap;
import org.junit.Test;

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
