// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.administration;

import com.azure.communication.administration.models.*;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;

import com.azure.core.util.polling.SyncPoller;

/**
 * Synchronous client for Communication service phone number operations
 */
@ServiceClient(builder = PhoneNumberClientBuilder.class, isAsync = false)
public final class PhoneNumberClient {

    PhoneNumberClient() {}

    /**
     * Search PhoneNumber LRO
     *
     * @param countryCode country code for the phone number search
     * @param searchRequest object that contains search options
     * @return A {@link SyncPoller} object with search result
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<Operation, SearchResult> beginSearchPhoneNumber(
        String countryCode, SearchRequest searchRequest) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    /**
     * Purchase PhoneNumber LRO
     *
     * @param searchId Id of the search to be purchased
     * @return A {@link SyncPoller} object with search result
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<Operation, SearchResult> beginPurchasePhoneNumber(String searchId) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    /**
     * Gets the search result
     *
     * @param searchId associated with the search
     * @return the Search Result
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchResult getSearchResult(String searchId) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    /**
     * Gets the Operation details
     *
     * @param operationId associated with the operation
     * @return the Operation
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Operation getOperation(String operationId) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    /**
     * Cancels the operation
     *
     * @param operationId associated with the operation
     * @return void
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void cancelOperation(String operationId) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    /**
     * Lists the acquired phone numbers
     *
     * @return A {@link PagedIterable} for the Acquired Phone Numbers
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AcquiredPhoneNumber> listAcquiredPhoneNumbers() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    /**
     * Gets the Acquired Phone Number
     *
     * @param phoneNumber The phone number
     * @return the Acquired Phone Number
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AcquiredPhoneNumber getPhoneNumber(String phoneNumber) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    /**
     * Updates an already acquired phone number
     *
     * @param phoneNumber The phone number to update
     * @param update object representing the update properties
     * @return A {@link SyncPoller} object with the Acquired Phone Number
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<Operation, AcquiredPhoneNumber> beginUpdatePhoneNumber(
        String phoneNumber, AcquiredPhoneNumberUpdate update) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    /**
     * Releases an already acquired phone number
     *
     * @param phoneNumber the phone number to release
     * @return A {@link SyncPoller} object for the operation
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<Operation, Void> beginReleasePhoneNumber(String phoneNumber) {
        throw new UnsupportedOperationException("not yet implemented");
    }
}
