// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.web.pullrefresh;

import static com.azure.spring.cloud.config.web.Constants.ACTUATOR;
import static com.azure.spring.cloud.config.web.Constants.APPCONFIGURATION_REFRESH;
import static com.azure.spring.cloud.config.web.Constants.APPCONFIGURATION_REFRESH_BUS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.ServletRequestHandledEvent;

import com.azure.spring.cloud.config.AppConfigurationRefresh;

/**
 * Listens for ServletRequestHandledEvents to check if the configurations need to be updated.
 */
@Component
public class AppConfigurationEventListener implements ApplicationListener<ServletRequestHandledEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationEventListener.class);

    private final AppConfigurationRefresh appConfigurationRefresh;

    public AppConfigurationEventListener(AppConfigurationRefresh appConfigurationRefresh) {
        this.appConfigurationRefresh = appConfigurationRefresh;
    }

    @Override
    public void onApplicationEvent(ServletRequestHandledEvent event) {
        try {
            if (!(event.getRequestUrl().equals(ACTUATOR + APPCONFIGURATION_REFRESH)
                || event.getRequestUrl().equals(ACTUATOR + APPCONFIGURATION_REFRESH_BUS))) {
                appConfigurationRefresh.refreshConfigurations();
            }
        } catch (Exception e) {
            LOGGER.error("Refresh failed with unexpected exception.", e);
        }

    }

}
