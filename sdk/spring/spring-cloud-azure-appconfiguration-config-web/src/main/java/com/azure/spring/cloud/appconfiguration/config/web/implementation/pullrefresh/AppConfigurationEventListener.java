// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.web.implementation.pullrefresh;

import static com.azure.spring.cloud.appconfiguration.config.web.implementation.AppConfigurationWebConstants.ACTUATOR;
import static com.azure.spring.cloud.appconfiguration.config.web.implementation.AppConfigurationWebConstants.APPCONFIGURATION_REFRESH;
import static com.azure.spring.cloud.appconfiguration.config.web.implementation.AppConfigurationWebConstants.APPCONFIGURATION_REFRESH_BUS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.web.context.support.ServletRequestHandledEvent;

import com.azure.spring.cloud.appconfiguration.config.AppConfigurationRefresh;

/**
 * Listens for ServletRequestHandledEvents to check if the configurations need to be updated.
 */
public final class AppConfigurationEventListener implements ApplicationListener<ServletRequestHandledEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationEventListener.class);

    private final AppConfigurationRefresh appConfigurationRefresh;

    /**
     * Listens for ServletRequestHandledEvents to check if the configurations need to be updated.
     * 
     * @param appConfigurationRefresh Refresher for App Configuration stores.
     */
    public AppConfigurationEventListener(AppConfigurationRefresh appConfigurationRefresh) {
        this.appConfigurationRefresh = appConfigurationRefresh;
    }

    @Override
    public void onApplicationEvent(@NonNull ServletRequestHandledEvent event) {
        try {
            if (!(event.getRequestUrl().equals(ACTUATOR + APPCONFIGURATION_REFRESH)
                || event.getRequestUrl().equals(ACTUATOR + APPCONFIGURATION_REFRESH_BUS))) {
                appConfigurationRefresh.refreshConfigurations().subscribe();
            }
        } catch (Exception e) {
            LOGGER.error("Refresh failed with unexpected exception.", e);
        }

    }

}
