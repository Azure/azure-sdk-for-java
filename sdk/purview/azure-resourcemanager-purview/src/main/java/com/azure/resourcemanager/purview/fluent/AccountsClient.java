// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.purview.fluent;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.management.polling.PollResult;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import com.azure.resourcemanager.purview.fluent.models.AccessKeysInner;
import com.azure.resourcemanager.purview.fluent.models.AccountInner;
import com.azure.resourcemanager.purview.fluent.models.CheckNameAvailabilityResultInner;
import com.azure.resourcemanager.purview.models.AccountUpdateParameters;
import com.azure.resourcemanager.purview.models.CheckNameAvailabilityRequest;
import com.azure.resourcemanager.purview.models.CollectionAdminUpdate;

/**
 * An instance of this class provides access to all the operations defined in AccountsClient.
 */
public interface AccountsClient {
    /**
     * Gets the accounts resources by resource group.
     * 
     * List accounts in ResourceGroup.
     * 
     * @param resourceGroupName The resource group name.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return paged list of account resources as paginated response with {@link PagedIterable}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    PagedIterable<AccountInner> listByResourceGroup(String resourceGroupName);

    /**
     * Gets the accounts resources by resource group.
     * 
     * List accounts in ResourceGroup.
     * 
     * @param resourceGroupName The resource group name.
     * @param skipToken The skip token.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return paged list of account resources as paginated response with {@link PagedIterable}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    PagedIterable<AccountInner> listByResourceGroup(String resourceGroupName, String skipToken, Context context);

    /**
     * Gets the accounts resources by subscription.
     * 
     * List accounts in Subscription.
     * 
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return paged list of account resources as paginated response with {@link PagedIterable}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    PagedIterable<AccountInner> list();

    /**
     * Gets the accounts resources by subscription.
     * 
     * List accounts in Subscription.
     * 
     * @param skipToken The skip token.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return paged list of account resources as paginated response with {@link PagedIterable}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    PagedIterable<AccountInner> list(String skipToken, Context context);

    /**
     * Gets the account resource.
     * 
     * Get an account.
     * 
     * @param resourceGroupName The resource group name.
     * @param accountName The name of the account.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return an account along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    Response<AccountInner> getByResourceGroupWithResponse(String resourceGroupName, String accountName,
        Context context);

    /**
     * Gets the account resource.
     * 
     * Get an account.
     * 
     * @param resourceGroupName The resource group name.
     * @param accountName The name of the account.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return an account.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    AccountInner getByResourceGroup(String resourceGroupName, String accountName);

    /**
     * Create or update an account resource
     * 
     * Creates or updates an account.
     * 
     * @param resourceGroupName The resource group name.
     * @param accountName The name of the account.
     * @param account The account.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link SyncPoller} for polling of account resource.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    SyncPoller<PollResult<AccountInner>, AccountInner> beginCreateOrUpdate(String resourceGroupName, String accountName,
        AccountInner account);

    /**
     * Create or update an account resource
     * 
     * Creates or updates an account.
     * 
     * @param resourceGroupName The resource group name.
     * @param accountName The name of the account.
     * @param account The account.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link SyncPoller} for polling of account resource.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    SyncPoller<PollResult<AccountInner>, AccountInner> beginCreateOrUpdate(String resourceGroupName, String accountName,
        AccountInner account, Context context);

    /**
     * Create or update an account resource
     * 
     * Creates or updates an account.
     * 
     * @param resourceGroupName The resource group name.
     * @param accountName The name of the account.
     * @param account The account.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return account resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    AccountInner createOrUpdate(String resourceGroupName, String accountName, AccountInner account);

    /**
     * Create or update an account resource
     * 
     * Creates or updates an account.
     * 
     * @param resourceGroupName The resource group name.
     * @param accountName The name of the account.
     * @param account The account.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return account resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    AccountInner createOrUpdate(String resourceGroupName, String accountName, AccountInner account, Context context);

    /**
     * Deletes the account resource.
     * 
     * Deletes an account resource.
     * 
     * @param resourceGroupName The resource group name.
     * @param accountName The name of the account.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link SyncPoller} for polling of long-running operation.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    SyncPoller<PollResult<Void>, Void> beginDelete(String resourceGroupName, String accountName);

    /**
     * Deletes the account resource.
     * 
     * Deletes an account resource.
     * 
     * @param resourceGroupName The resource group name.
     * @param accountName The name of the account.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link SyncPoller} for polling of long-running operation.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    SyncPoller<PollResult<Void>, Void> beginDelete(String resourceGroupName, String accountName, Context context);

    /**
     * Deletes the account resource.
     * 
     * Deletes an account resource.
     * 
     * @param resourceGroupName The resource group name.
     * @param accountName The name of the account.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    void delete(String resourceGroupName, String accountName);

    /**
     * Deletes the account resource.
     * 
     * Deletes an account resource.
     * 
     * @param resourceGroupName The resource group name.
     * @param accountName The name of the account.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    void delete(String resourceGroupName, String accountName, Context context);

    /**
     * Patches the account resource.
     * 
     * Updates an account.
     * 
     * @param resourceGroupName The resource group name.
     * @param accountName The name of the account.
     * @param accountUpdateParameters The account update parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link SyncPoller} for polling of account resource.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    SyncPoller<PollResult<AccountInner>, AccountInner> beginUpdate(String resourceGroupName, String accountName,
        AccountUpdateParameters accountUpdateParameters);

    /**
     * Patches the account resource.
     * 
     * Updates an account.
     * 
     * @param resourceGroupName The resource group name.
     * @param accountName The name of the account.
     * @param accountUpdateParameters The account update parameters.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link SyncPoller} for polling of account resource.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    SyncPoller<PollResult<AccountInner>, AccountInner> beginUpdate(String resourceGroupName, String accountName,
        AccountUpdateParameters accountUpdateParameters, Context context);

    /**
     * Patches the account resource.
     * 
     * Updates an account.
     * 
     * @param resourceGroupName The resource group name.
     * @param accountName The name of the account.
     * @param accountUpdateParameters The account update parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return account resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    AccountInner update(String resourceGroupName, String accountName, AccountUpdateParameters accountUpdateParameters);

    /**
     * Patches the account resource.
     * 
     * Updates an account.
     * 
     * @param resourceGroupName The resource group name.
     * @param accountName The name of the account.
     * @param accountUpdateParameters The account update parameters.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return account resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    AccountInner update(String resourceGroupName, String accountName, AccountUpdateParameters accountUpdateParameters,
        Context context);

    /**
     * Lists the keys asynchronous.
     * 
     * List the authorization keys associated with this account.
     * 
     * @param resourceGroupName The resource group name.
     * @param accountName The name of the account.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the Account access keys along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    Response<AccessKeysInner> listKeysWithResponse(String resourceGroupName, String accountName, Context context);

    /**
     * Lists the keys asynchronous.
     * 
     * List the authorization keys associated with this account.
     * 
     * @param resourceGroupName The resource group name.
     * @param accountName The name of the account.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the Account access keys.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    AccessKeysInner listKeys(String resourceGroupName, String accountName);

    /**
     * Add the administrator for root collection.
     * 
     * Add the administrator for root collection associated with this account.
     * 
     * @param resourceGroupName The resource group name.
     * @param accountName The name of the account.
     * @param collectionAdminUpdate The collection admin update payload.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    Response<Void> addRootCollectionAdminWithResponse(String resourceGroupName, String accountName,
        CollectionAdminUpdate collectionAdminUpdate, Context context);

    /**
     * Add the administrator for root collection.
     * 
     * Add the administrator for root collection associated with this account.
     * 
     * @param resourceGroupName The resource group name.
     * @param accountName The name of the account.
     * @param collectionAdminUpdate The collection admin update payload.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    void addRootCollectionAdmin(String resourceGroupName, String accountName,
        CollectionAdminUpdate collectionAdminUpdate);

    /**
     * Checks the account name availability.
     * 
     * Checks if account name is available.
     * 
     * @param checkNameAvailabilityRequest The check name availability request.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response payload for CheckNameAvailability API along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    Response<CheckNameAvailabilityResultInner>
        checkNameAvailabilityWithResponse(CheckNameAvailabilityRequest checkNameAvailabilityRequest, Context context);

    /**
     * Checks the account name availability.
     * 
     * Checks if account name is available.
     * 
     * @param checkNameAvailabilityRequest The check name availability request.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws com.azure.core.management.exception.ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response payload for CheckNameAvailability API.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    CheckNameAvailabilityResultInner checkNameAvailability(CheckNameAvailabilityRequest checkNameAvailabilityRequest);
}
