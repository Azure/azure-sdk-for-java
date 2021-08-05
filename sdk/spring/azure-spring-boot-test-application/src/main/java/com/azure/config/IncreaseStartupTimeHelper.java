// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

public class IncreaseStartupTimeHelper implements EnvironmentPostProcessor, Ordered {

    public static final int DEFAULT_ORDER = ConfigFileApplicationListener.DEFAULT_ORDER;
    private int order = DEFAULT_ORDER;

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        try {
            Thread.sleep(100_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getOrder() {
        return order;
    }

}
