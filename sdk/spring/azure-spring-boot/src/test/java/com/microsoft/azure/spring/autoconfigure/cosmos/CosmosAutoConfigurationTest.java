// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.autoconfigure.cosmos;


import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.implementation.ConnectionPolicy;
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
            final ConnectionPolicy connectionPolicy = ConnectionPolicy.getDefaultPolicy();

            connectionPolicy.setRequestTimeout(PropertySettingUtil.REQUEST_TIMEOUT);
            connectionPolicy.setConnectionMode(PropertySettingUtil.CONNECTION_MODE);
            connectionPolicy.setMaxConnectionPoolSize(PropertySettingUtil.MAX_CONNECTION_POOL_SIZE);
            connectionPolicy.setIdleHttpConnectionTimeout(PropertySettingUtil.IDLE_HTTP_CONNECTION_TIMEOUT);
            connectionPolicy.setIdleTcpConnectionTimeout(PropertySettingUtil.IDLE_TCP_CONNECTION_TIMEOUT);
            // TODO (data) User agent from configured ConnectionPolicy is not taken
            connectionPolicy.setUserAgentSuffix(PropertySettingUtil.USER_AGENT_SUFFIX);
            final ThrottlingRetryOptions retryOptions = new ThrottlingRetryOptions();
            retryOptions.setMaxRetryAttemptsOnThrottledRequests(
                PropertySettingUtil.RETRY_OPTIONS_MAX_RETRY_ATTEMPTS_ON_THROTTLED_REQUESTS);
            retryOptions.setMaxRetryWaitTime(PropertySettingUtil.MAX_RETRY_WAIT_TIME);
            connectionPolicy.setThrottlingRetryOptions(retryOptions);

            connectionPolicy.setEndpointDiscoveryEnabled(PropertySettingUtil.ENDPOINT_DISCOVERY_ENABLED);
            connectionPolicy.setPreferredRegions(PropertySettingUtil.PREFERRED_REGIONS);
            return connectionPolicy;
        }
    }
}
