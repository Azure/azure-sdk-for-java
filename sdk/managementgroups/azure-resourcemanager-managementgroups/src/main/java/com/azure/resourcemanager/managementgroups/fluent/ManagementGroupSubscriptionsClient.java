// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.managementgroups.fluent;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.resourcemanager.managementgroups.fluent.models.SubscriptionUnderManagementGroupInner;

/**
 * An instance of this class provides access to all the operations defined in ManagementGroupSubscriptionsClient.
 */
public interface ManagementGroupSubscriptionsClient {
    /**
     * Associates existing subscription with the management group.
     * 
     * @param groupId Management Group ID.
     * @param subscriptionId Subscription ID.
     * @param cacheControl Indicates whether the request should utilize any caches. Populate the header with 'no-cache'
     * value to bypass existing caches.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the details of subscription under management group along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    Response<SubscriptionUnderManagementGroupInner> createWithResponse(String groupId, String subscriptionId,
        String cacheControl, Context context);

    /**
     * Associates existing subscription with the management group.
     * 
     * @param groupId Management Group ID.
     * @param subscriptionId Subscription ID.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the details of subscription under management group.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    SubscriptionUnderManagementGroupInner create(String groupId, String subscriptionId);

    /**
     * De-associates subscription from the management group.
     * 
     * @param groupId Management Group ID.
     * @param subscriptionId Subscription ID.
     * @param cacheControl Indicates whether the request should utilize any caches. Populate the header with 'no-cache'
     * value to bypass existing caches.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    Response<Void> deleteWithResponse(String groupId, String subscriptionId, String cacheControl, Context context);

    /**
     * De-associates subscription from the management group.
     * 
     * @param groupId Management Group ID.
     * @param subscriptionId Subscription ID.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    void delete(String groupId, String subscriptionId);

    /**
     * Retrieves details about given subscription which is associated with the management group.
     * 
     * @param groupId Management Group ID.
     * @param subscriptionId Subscription ID.
     * @param cacheControl Indicates whether the request should utilize any caches. Populate the header with 'no-cache'
     * value to bypass existing caches.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the details of subscription under management group along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    Response<SubscriptionUnderManagementGroupInner> getSubscriptionWithResponse(String groupId, String subscriptionId,
        String cacheControl, Context context);

    /**
     * Retrieves details about given subscription which is associated with the management group.
     * 
     * @param groupId Management Group ID.
     * @param subscriptionId Subscription ID.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the details of subscription under management group.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    SubscriptionUnderManagementGroupInner getSubscription(String groupId, String subscriptionId);

    /**
     * Retrieves details about all subscriptions which are associated with the management group.
     * 
     * @param groupId Management Group ID.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the details of all subscriptions under management group as paginated response with {@link PagedIterable}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    PagedIterable<SubscriptionUnderManagementGroupInner> getSubscriptionsUnderManagementGroup(String groupId);

    /**
     * Retrieves details about all subscriptions which are associated with the management group.
     * 
     * @param groupId Management Group ID.
     * @param skiptoken Page continuation token is only used if a previous operation returned a partial result.
     * If a previous response contains a nextLink element, the value of the nextLink element will include a token
     * parameter that specifies a starting point to use for subsequent calls.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the details of all subscriptions under management group as paginated response with {@link PagedIterable}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    PagedIterable<SubscriptionUnderManagementGroupInner> getSubscriptionsUnderManagementGroup(String groupId,
        String skiptoken, Context context);
}
