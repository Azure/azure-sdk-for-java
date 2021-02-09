/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.web.pushbusrefresh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.microsoft.azure.spring.cloud.config.AppConfigurationRefresh;

@Component
public class AppConfigurationBusRefreshEventListener implements ApplicationListener<AppConfigurationBusRefreshEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationBusRefreshEventListener.class);

    private AppConfigurationRefresh appConfigurationRefresh;

    public AppConfigurationBusRefreshEventListener(AppConfigurationRefresh appConfigurationRefresh) {
        this.appConfigurationRefresh = appConfigurationRefresh;
    }

    @Override
    public void onApplicationEvent(AppConfigurationBusRefreshEvent event) {
        try {
            appConfigurationRefresh.resetCache(event.getEndpoint());
        } catch (Exception e) {
            LOGGER.error("Refresh failed with unexpected exception.", e);
        }

    }

}
