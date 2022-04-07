// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.crud;

import com.azure.core.http.HttpResponse;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.core.properties.resource.AzureResourceMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

abstract class AbstractResourceCrudTests<T, K> {

    protected AzureResourceManager resourceManager;
    protected AzureResourceMetadata resourceMetadata;
    protected AbstractResourceCrud<T, K> crud;

    abstract AbstractResourceCrud<T, K> getResourceCrud();
    abstract void getStubManagementException(int statusCode, String exception);
    abstract void createStubManagementException();
    abstract K getKey();

    ManagementException getManagementException(int statusCode, String message) {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusCode()).thenReturn(statusCode);
        return new ManagementException(message, response);
    }

    @BeforeEach
    void beforeEach() {
        resourceManager = mock(AzureResourceManager.class);
        resourceMetadata = mock(AzureResourceMetadata.class);
        when(resourceMetadata.getResourceGroup()).thenReturn("test-rg");
        when(resourceMetadata.getRegion()).thenReturn("eastasia");
        crud = getResourceCrud();
    }

    @Test
    void getResourceFoundManagementException() {
        getStubManagementException(500, "exception");
        assertThrows(ManagementException.class, () -> crud.internalGet(getKey()));
    }

    @Test
    void getResourceReturnNullWhen404Exception() {
        getStubManagementException(404, "Resource not exist");
        assertNull(crud.internalGet(getKey()));
    }

    @Test
    void createResourceFoundManagementException() {
        createStubManagementException();
        assertThrows(ManagementException.class, () -> crud.internalCreate(getKey()));
    }
}
