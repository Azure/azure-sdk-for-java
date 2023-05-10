// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config;

import java.util.Map;

import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.scheduling.annotation.Async;

import reactor.core.publisher.Mono;

/**
 * Enables checking of Configuration updates.
 */
public interface AppConfigurationRefresh extends ApplicationEventPublisherAware {
    /**
     * Checks configurations to see if configurations should be reloaded. If the refresh interval has passed and a
     * trigger has been updated configuration are reloaded.
     *
     * @return Mono with a boolean of if a RefreshEvent was published. If refreshConfigurations is currently being run
     * elsewhere this method will return right away as <b>false</b>.
     */
    @Async
    Mono<Boolean> refreshConfigurations();

    /**
     * Soft expires refresh interval. Sets amount of time to next refresh to be a random value between 0 and 15 seconds,
     * unless value is less than the amount of time to the next refresh check.
     * @param endpoint Config Store endpoint to expire refresh interval on.
     * @param syncToken syncToken to verify the latest changes are available on pull
     */
    void expireRefreshInterval(String endpoint, String syncToken);

    /**
     * Gets the latest Health connection info for refresh.
     *
     * @return Map of String, endpoint, and Health information.
     */
    Map<String, AppConfigurationStoreHealth> getAppConfigurationStoresHealth();

}
