/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
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

//    @Test
//    public void canSetAllPropertiesToDocumentClient() {
//        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
//            context.register(CosmosAutoConfiguration.class);
//            context.refresh();
//            final CosmosClient cosmosClient = context.getBean(CosmosClient.class);
//
//            // No way to verify the setting of key value and ConsistencyLevel.
//            final URI uri = cosmosClient.getServiceEndpoint();
//            assertThat(uri.toString()).isEqualTo(PropertySettingUtil.URI);
//
//            assertThat(cosmosClient.getConnectionPolicy()).isEqualTo(ConnectionPolicy.GetDefault());
//        }
//    }
//
//    @Test
//    public void canSetConnectionPolicyToDocumentClient() {
//        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
//            context.register(CosmosAutoConfiguration.class, ConnectionPolicyConfig.class);
//            context.refresh();
//            final CosmosClient cosmosClient = context.getBean(CosmosClient.class);
//
//            final ConnectionPolicy connectionPolicy = cosmosClient;
//            assertThat(connectionPolicy.requestTimeoutInMillis()).isEqualTo(PropertySettingUtil.REQUEST_TIMEOUT);
//            assertThat(connectionPolicy.getMediaRequestTimeout()).
//                    isEqualTo(PropertySettingUtil.MEDIA_REQUEST_TIMEOUT);
//            assertThat(connectionPolicy.getConnectionMode()).isEqualTo(PropertySettingUtil.CONNECTION_MODE);
//            assertThat(connectionPolicy.getMediaReadMode()).isEqualTo(PropertySettingUtil.MEDIA_READ_MODE);
//            assertThat(connectionPolicy.getMaxPoolSize()).isEqualTo(PropertySettingUtil.MAX_POOL_SIZE);
//            assertThat(connectionPolicy.getIdleConnectionTimeout()).
//                    isEqualTo(PropertySettingUtil.IDLE_CONNECTION_TIMEOUT);
//            // TODO (data) User agent from configured ConnectionPolicy is not taken
//            // assertThat(connectionPolicy.getUserAgentSuffix()).contains(PropertySettingUtil.USER_AGENT_SUFFIX);
//            assertThat(connectionPolicy.getUserAgentSuffix()).contains(PropertySettingUtil.DEFAULT_USER_AGENT_SUFFIX);
//            assertThat(connectionPolicy.getRetryOptions().getMaxRetryAttemptsOnThrottledRequests()).
//                    isEqualTo(PropertySettingUtil.RETRY_OPTIONS_MAX_RETRY_ATTEMPTS_ON_THROTTLED_REQUESTS);
//            assertThat(connectionPolicy.getRetryOptions().getMaxRetryWaitTimeInSeconds()).
//                    isEqualTo(PropertySettingUtil.RETRY_OPTIONS_MAX_RETRY_WAIT_TIME_IN_SECONDS);
//            assertThat(connectionPolicy.getEnableEndpointDiscovery()).
//                    isEqualTo(PropertySettingUtil.ENABLE_ENDPOINT_DISCOVERY);
//            assertThat(connectionPolicy.getPreferredLocations().toString()).
//                    isEqualTo(PropertySettingUtil.PREFERRED_LOCATIONS.toString());
//        }
//    }
//
//    @Test
//    public void canSetAllowTelemetryFalse() {
//        PropertySettingUtil.setAllowTelemetryFalse();
//
//        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
//            context.register(CosmosAutoConfiguration.class, ConnectionPolicyConfig.class);
//            context.refresh();
//            final DocumentClient documentClient = context.getBean(DocumentClient.class);
//
//            final ConnectionPolicy connectionPolicy = documentClient.getConnectionPolicy();
//            // TODO (data) User agent from configured ConnectionPolicy is not taken
//            // assertThat(connectionPolicy.getUserAgentSuffix()).contains(PropertySettingUtil.USER_AGENT_SUFFIX);
//            assertThat(connectionPolicy.getUserAgentSuffix()).contains(
//                    PropertySettingUtil.DEFAULT_USER_AGENT_SUFFIX);
//        }
//        PropertySettingUtil.unsetAllowTelemetry();
//    }

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
