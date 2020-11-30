// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.config.web.pushrefresh;

import com.microsoft.azure.spring.cloud.config.AppConfigurationRefresh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class AppConfigurationRefreshEventListener implements ApplicationListener<AppConfigurationRefreshEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationRefreshEventListener.class);

    private AppConfigurationRefresh appConfigurationRefresh;

    public AppConfigurationRefreshEventListener(AppConfigurationRefresh appConfigurationRefresh) {
        this.appConfigurationRefresh = appConfigurationRefresh;
    }

    @Override
    public void onApplicationEvent(AppConfigurationRefreshEvent event) {
        try {
            appConfigurationRefresh.resetCache(event.getEndpoint());
        } catch (Exception e) {
            LOGGER.error("Refresh failed with unexpected exception.", e);
        }

    }

}
