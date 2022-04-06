// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.connectionstring;

import com.azure.core.http.HttpResponse;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.core.properties.resource.AzureResourceMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

abstract class AbstractArmConnectionStringProviderTests<T> {
    protected AzureResourceManager resourceManager;
    protected AzureResourceMetadata resourceMetadata;
    protected ArmConnectionStringProvider<T> provider;

    abstract ArmConnectionStringProvider<T> getArmConnectionStringProvider();

    @BeforeEach
    void beforeEach() {
        resourceManager = mock(AzureResourceManager.class);
        resourceMetadata = mock(AzureResourceMetadata.class);
        provider = getArmConnectionStringProvider();
    }

    @Test
    void failedWhenGettingConnectionStringNamespaceDoesNotExist() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusCode()).thenReturn(403);
        ManagementException exception = new ManagementException("AuthorizationFailed", response);
        when(resourceManager.eventHubNamespaces()).thenThrow(exception);
        assertThrows(RuntimeException.class, () -> provider.getConnectionString());
    }
}
