// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example;

import com.microsoft.azure.spring.cloud.feature.manager.FeatureManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
public class TestComponent {

    @Autowired
    private FeatureManager featureManager;

    public String test() throws InterruptedException, ExecutionException {
        if (featureManager.isEnabledAsync("Beta").block()) {
            return "Beta";
        }
        return "Original";
    }

}
