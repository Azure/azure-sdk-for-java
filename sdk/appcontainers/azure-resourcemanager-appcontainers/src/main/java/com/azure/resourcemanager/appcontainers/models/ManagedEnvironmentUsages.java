// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.appcontainers.models;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;

/**
 * Resource collection API of ManagedEnvironmentUsages.
 */
public interface ManagedEnvironmentUsages {
    /**
     * Gets the current usage information as well as the limits for environment.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param environmentName Name of the Environment.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.resourcemanager.appcontainers.models.DefaultErrorResponseErrorException thrown if the request
     * is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the current usage information as well as the limits for environment as paginated response with
     * {@link PagedIterable}.
     */
    PagedIterable<Usage> list(String resourceGroupName, String environmentName);

    /**
     * Gets the current usage information as well as the limits for environment.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param environmentName Name of the Environment.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.resourcemanager.appcontainers.models.DefaultErrorResponseErrorException thrown if the request
     * is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the current usage information as well as the limits for environment as paginated response with
     * {@link PagedIterable}.
     */
    PagedIterable<Usage> list(String resourceGroupName, String environmentName, Context context);
}
