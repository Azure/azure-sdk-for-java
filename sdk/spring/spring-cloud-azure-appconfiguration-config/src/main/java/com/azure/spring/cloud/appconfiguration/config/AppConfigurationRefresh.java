// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config;

import java.util.Map;

import org.springframework.context.ApplicationEventPublisherAware;

import reactor.core.publisher.Mono;

/**
 * Enables checking of Configuration updates.
 */
public interface AppConfigurationRefresh extends ApplicationEventPublisherAware {

    /**
     * Checks configurations to see if configurations should be reloaded. If the refresh interval has passed and a
     * trigger has been updated configuration are reloaded.
     * The returned Mono must be subscribed to initiate the check. The default implementation performs the check
     * asynchronously without blocking the subscribing thread. Use {@code block()} to wait for completion when needed.
     *
     * @return Mono with a boolean of if a RefreshEvent was published. If refreshConfigurations is currently being run
     * elsewhere the Mono completes with <b>false</b> without performing another check. Refresh failures are signaled
     * through the Mono.
     */
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
