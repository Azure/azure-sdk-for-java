// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.web.pushbusrefresh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.azure.spring.cloud.config.AppConfigurationRefresh;

/**
 * Listens for AppConfigurationBusRefreshEvents and sets the App Configuration watch interval to zero.
 */
@Component
public class AppConfigurationBusRefreshEventListener implements ApplicationListener<AppConfigurationBusRefreshEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationBusRefreshEventListener.class);

    private final AppConfigurationRefresh appConfigurationRefresh;

    public AppConfigurationBusRefreshEventListener(AppConfigurationRefresh appConfigurationRefresh) {
        this.appConfigurationRefresh = appConfigurationRefresh;
    }

    @Override
    public void onApplicationEvent(AppConfigurationBusRefreshEvent event) {
        try {
            appConfigurationRefresh.expireRefreshInterval(event.getEndpoint());
        } catch (Exception e) {
            LOGGER.error("Refresh failed with unexpected exception.", e);
        }

    }

}
