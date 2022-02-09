// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.config.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.ServletRequestHandledEvent;

import com.microsoft.azure.spring.cloud.config.AppConfigurationRefresh;

@Component
public class ConfigListener implements ApplicationListener<ServletRequestHandledEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigListener.class);

    private final AppConfigurationRefresh appConfigurationRefresh;

    public ConfigListener(AppConfigurationRefresh appConfigurationRefresh) {
        this.appConfigurationRefresh = appConfigurationRefresh;
    }

    @Override
    public void onApplicationEvent(ServletRequestHandledEvent event) {
        try {
            appConfigurationRefresh.refreshConfigurations();
        } catch (Exception e) {
            LOGGER.error("Refresh failed with unexpected exception.", e);
        }
    }

}
