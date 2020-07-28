// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.autoconfigure.cosmosdb;


import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.RetryOptions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Ignore
public class CosmosAutoConfigurationTest {
    @BeforeClass
    public static void beforeClass() {
        PropertySettingUtil.setProperties();
    }

    @AfterClass
    public static void afterClass() {
        PropertySettingUtil.unsetProperties();
    }

    @Configuration
    static class ConnectionPolicyConfig {
        @Bean
        public ConnectionPolicy connectionPolicy() {
            final ConnectionPolicy connectionPolicy = ConnectionPolicy.defaultPolicy();

            connectionPolicy.requestTimeoutInMillis(PropertySettingUtil.REQUEST_TIMEOUT);
            connectionPolicy.connectionMode(PropertySettingUtil.CONNECTION_MODE);
            connectionPolicy.maxPoolSize(PropertySettingUtil.MAX_POOL_SIZE);
            connectionPolicy.idleConnectionTimeoutInMillis(PropertySettingUtil.IDLE_CONNECTION_TIMEOUT);
            // TODO (data) User agent from configured ConnectionPolicy is not taken
            connectionPolicy.userAgentSuffix(PropertySettingUtil.USER_AGENT_SUFFIX);

            final RetryOptions retryOptions = new RetryOptions();
            retryOptions.maxRetryAttemptsOnThrottledRequests(
                    PropertySettingUtil.RETRY_OPTIONS_MAX_RETRY_ATTEMPTS_ON_THROTTLED_REQUESTS);
            retryOptions.maxRetryWaitTimeInSeconds(
                    PropertySettingUtil.RETRY_OPTIONS_MAX_RETRY_WAIT_TIME_IN_SECONDS);
            connectionPolicy.retryOptions(retryOptions);

            connectionPolicy.enableEndpointDiscovery(PropertySettingUtil.ENABLE_ENDPOINT_DISCOVERY);
            connectionPolicy.preferredLocations(PropertySettingUtil.PREFERRED_LOCATIONS);

            return connectionPolicy;
        }
    }
}
