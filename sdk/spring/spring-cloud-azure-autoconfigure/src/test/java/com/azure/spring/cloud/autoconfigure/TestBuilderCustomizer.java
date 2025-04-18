// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure;

import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;

public class TestBuilderCustomizer<T> implements AzureServiceClientBuilderCustomizer<T> {

    private int customizedTimes = 0;

    @Override
    public void customize(T builder) {
        customizedTimes++;
    }

    public int getCustomizedTimes() {
        return customizedTimes;
    }

}
