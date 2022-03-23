package com.azure.spring.cloud.feature.manager.feature.filters;

import com.azure.spring.cloud.feature.manager.FeatureFilter;
import com.azure.spring.cloud.feature.manager.entities.FeatureFilterEvaluationContext;

public class AlwaysOnFilter implements FeatureFilter {

    @Override
    public boolean evaluate(FeatureFilterEvaluationContext context) {
        return true;
    }

}
