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
public final class AppConfigurationBusRefreshEventListener implements ApplicationListener<AppConfigurationBusRefreshEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationBusRefreshEventListener.class);

    private final AppConfigurationRefresh appConfigurationRefresh;

    /**
     * Listener for AppConfigurationBusRefreshEvents, used to trigger an early cache expiration of a given config store.
     * 
     * @param appConfigurationRefresh refresher for App Config stores.
     */
    public AppConfigurationBusRefreshEventListener(AppConfigurationRefresh appConfigurationRefresh) {
        this.appConfigurationRefresh = appConfigurationRefresh;
    }

    /**
     * Handles an appConfigurationRefreshEvent. Expires refresh interval for a single config store.
     * 
     * @param event Event Triggering refresh, contains valid config store endpoint.
     */
    @Override
    public void onApplicationEvent(AppConfigurationBusRefreshEvent event) {
        try {
            appConfigurationRefresh.expireRefreshInterval(event.getEndpoint());
        } catch (Exception e) {
            LOGGER.error("Refresh failed with unexpected exception.", e);
        }

    }

}
