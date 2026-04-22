// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.web.implementation.pushrefresh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;

import com.azure.spring.cloud.appconfiguration.config.AppConfigurationRefresh;

/**
 * Listens for AppConfigurationRefreshEvents and sets the App Configuration watch interval to zero.
 */
@SuppressWarnings("deprecation")
public final class AppConfigurationRefreshEventListener implements ApplicationListener<AppConfigurationRefreshEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationRefreshEventListener.class);

    private final AppConfigurationRefresh appConfigurationRefresh;

    /**
     * Listener for AppConfigurationRefreshEvents, used to trigger an early cache expiration of a given config store.
     * 
     * @param appConfigurationRefresh refresher for App Config stores.
     */
    public AppConfigurationRefreshEventListener(AppConfigurationRefresh appConfigurationRefresh) {
        this.appConfigurationRefresh = appConfigurationRefresh;
    }

    /**
     * Handles an appConfigurationRefreshEvent. Expires refresh interval for a single config store.
     * 
     * @param event Event Triggering refresh, contains valid config store endpoint.
     */
    @Override
    public void onApplicationEvent(@NonNull AppConfigurationRefreshEvent event) {
        try {
            appConfigurationRefresh.expireRefreshInterval(event.getEndpoint(), event.getSyncToken());
        } catch (Exception e) {
            LOGGER.error("Refresh failed with unexpected exception.", e);
        }

    }

}
