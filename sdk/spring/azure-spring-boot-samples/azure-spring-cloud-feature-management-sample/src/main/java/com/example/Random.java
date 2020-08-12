/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.example;

import org.springframework.stereotype.Component;

import com.microsoft.azure.spring.cloud.feature.manager.FeatureFilter;
import com.microsoft.azure.spring.cloud.feature.manager.entities.FeatureFilterEvaluationContext;

@Component("Random")
public class Random implements FeatureFilter{

    @Override
    public boolean evaluate(FeatureFilterEvaluationContext context) {
        double chance = Double.valueOf((String) context.getParameters().get("chance"));
        return Math.random() > chance / 100;
    }

}

