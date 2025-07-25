// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.resourcemanager.hardwaresecuritymodules.fluent;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.resourcemanager.hardwaresecuritymodules.fluent.models.PrivateLinkResourceInner;

/**
 * An instance of this class provides access to all the operations defined in CloudHsmClusterPrivateLinkResourcesClient.
 */
public interface CloudHsmClusterPrivateLinkResourcesClient {
    /**
     * Gets the private link resources supported for the Cloud Hsm Cluster.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param cloudHsmClusterName The name of the Cloud HSM Cluster within the specified resource group. Cloud HSM
     * Cluster names must be between 3 and 23 characters in length.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the private link resources supported for the Cloud Hsm Cluster as paginated response with
     * {@link PagedIterable}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    PagedIterable<PrivateLinkResourceInner> listByCloudHsmCluster(String resourceGroupName, String cloudHsmClusterName);

    /**
     * Gets the private link resources supported for the Cloud Hsm Cluster.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param cloudHsmClusterName The name of the Cloud HSM Cluster within the specified resource group. Cloud HSM
     * Cluster names must be between 3 and 23 characters in length.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the private link resources supported for the Cloud Hsm Cluster as paginated response with
     * {@link PagedIterable}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    PagedIterable<PrivateLinkResourceInner> listByCloudHsmCluster(String resourceGroupName, String cloudHsmClusterName,
        Context context);
}
