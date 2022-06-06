// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.service.kafka;

public final class AzureKafkaConfigs {
    private AzureKafkaConfigs() {

    }

    public static final String CLIENT_ID_CONFIG = "spring.cloud.azure.kafka.credential.client-id";
    public static final String TENANT_ID_CONFIG = "spring.cloud.azure.kafka.profile.tenant-id";
    public static final String AAD_ENDPOINT_CONFIG = "spring.cloud.azure.kafka.profile.environment.active-directory-endpoint";
}
