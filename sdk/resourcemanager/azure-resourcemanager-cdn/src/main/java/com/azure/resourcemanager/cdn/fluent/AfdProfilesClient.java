// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.cdn.fluent;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.resourcemanager.cdn.fluent.models.UsageInner;
import com.azure.resourcemanager.cdn.fluent.models.ValidateCustomDomainOutputInner;
import reactor.core.publisher.Mono;

/** An instance of this class provides access to all the operations defined in AfdProfilesClient. */
public interface AfdProfilesClient {
    /**
     * Checks the quota and actual usage of endpoints under the given CDN profile.
     *
     * @param resourceGroupName Name of the Resource group within the Azure subscription.
     * @param profileName Name of the CDN profile which is unique within the resource group.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the list usages operation response.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    PagedFlux<UsageInner> listResourceUsageAsync(String resourceGroupName, String profileName);

    /**
     * Checks the quota and actual usage of endpoints under the given CDN profile.
     *
     * @param resourceGroupName Name of the Resource group within the Azure subscription.
     * @param profileName Name of the CDN profile which is unique within the resource group.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the list usages operation response.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    PagedIterable<UsageInner> listResourceUsage(String resourceGroupName, String profileName);

    /**
     * Checks the quota and actual usage of endpoints under the given CDN profile.
     *
     * @param resourceGroupName Name of the Resource group within the Azure subscription.
     * @param profileName Name of the CDN profile which is unique within the resource group.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the list usages operation response.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    PagedIterable<UsageInner> listResourceUsage(String resourceGroupName, String profileName, Context context);

    /**
     * Validates the custom domain mapping to ensure it maps to the correct CDN endpoint in DNS.
     *
     * @param resourceGroupName Name of the Resource group within the Azure subscription.
     * @param profileName Name of the CDN profile which is unique within the resource group.
     * @param hostname The host name of the custom domain. Must be a domain name.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return output of custom domain validation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    Mono<Response<ValidateCustomDomainOutputInner>> checkHostnameAvailabilityWithResponseAsync(
        String resourceGroupName, String profileName, String hostname);

    /**
     * Validates the custom domain mapping to ensure it maps to the correct CDN endpoint in DNS.
     *
     * @param resourceGroupName Name of the Resource group within the Azure subscription.
     * @param profileName Name of the CDN profile which is unique within the resource group.
     * @param hostname The host name of the custom domain. Must be a domain name.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return output of custom domain validation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    Mono<ValidateCustomDomainOutputInner> checkHostnameAvailabilityAsync(
        String resourceGroupName, String profileName, String hostname);

    /**
     * Validates the custom domain mapping to ensure it maps to the correct CDN endpoint in DNS.
     *
     * @param resourceGroupName Name of the Resource group within the Azure subscription.
     * @param profileName Name of the CDN profile which is unique within the resource group.
     * @param hostname The host name of the custom domain. Must be a domain name.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return output of custom domain validation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    ValidateCustomDomainOutputInner checkHostnameAvailability(
        String resourceGroupName, String profileName, String hostname);

    /**
     * Validates the custom domain mapping to ensure it maps to the correct CDN endpoint in DNS.
     *
     * @param resourceGroupName Name of the Resource group within the Azure subscription.
     * @param profileName Name of the CDN profile which is unique within the resource group.
     * @param hostname The host name of the custom domain. Must be a domain name.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return output of custom domain validation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    Response<ValidateCustomDomainOutputInner> checkHostnameAvailabilityWithResponse(
        String resourceGroupName, String profileName, String hostname, Context context);
}
