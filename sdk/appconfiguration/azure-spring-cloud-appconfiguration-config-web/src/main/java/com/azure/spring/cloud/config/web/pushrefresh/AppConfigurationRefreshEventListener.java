// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.web.pushrefresh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.azure.spring.cloud.config.AppConfigurationRefresh;

/**
 * Listens for AppConfigurationRefreshEvents and sets the App Configuration watch interval to zero.
 */
@Component
public class AppConfigurationRefreshEventListener implements ApplicationListener<AppConfigurationRefreshEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationRefreshEventListener.class);

    private final AppConfigurationRefresh appConfigurationRefresh;

    public AppConfigurationRefreshEventListener(AppConfigurationRefresh appConfigurationRefresh) {
        this.appConfigurationRefresh = appConfigurationRefresh;
    }

    @Override
    public void onApplicationEvent(AppConfigurationRefreshEvent event) {
        try {
            appConfigurationRefresh.expireRefreshInterval(event.getEndpoint());
        } catch (Exception e) {
            LOGGER.error("Refresh failed with unexpected exception.", e);
        }

    }

}
