// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.administration;

import com.azure.communication.administration.models.*;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Asynchronous client for Communication service phone number operations
 */
@ServiceClient(builder = PhoneNumberClientBuilder.class, isAsync = true)
public final class PhoneNumberAsyncClient {

    PhoneNumberAsyncClient() {}

    /**
     * Search PhoneNumber LRO
     *
     * @param countryCode country code for the phone number search
     * @param searchRequest object that contains search options
     * @return A {@link PollerFlux} object with search result
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<Operation, SearchResult> beginSearchPhoneNumber(
        String countryCode, SearchRequest searchRequest) {
        return new PollerFlux<Operation, SearchResult>(
            Duration.ofMillis(1000),
            (context) -> Mono.empty(),
            (context) -> Mono.empty(),
            (activationResponse, context) -> Mono.empty(),
            (context) -> Mono.empty());
    }

    /**
     * Purchase PhoneNumber LRO
     *
     * @param searchId Id of the search to be purchased
     * @return A {@link PollerFlux} object with search result
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<Operation, SearchResult> beginPurchasePhoneNumber(String searchId) {
        return new PollerFlux<Operation, SearchResult>(
            Duration.ofMillis(1000),
            (context) -> Mono.empty(),
            (context) -> Mono.empty(),
            (activationResponse, context) -> Mono.empty(),
            (context) -> Mono.empty());
    }

    /**
     * Gets the search result
     *
     * @param searchId associated with the search
     * @return A {@link Mono} for the Search Result
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchResult> getSearchResult(String searchId) {
        return Mono.error(new UnsupportedOperationException("not yet implemented"));
    }

    /**
     * Gets the Operation details
     *
     * @param operationId associated with the operation
     * @return A {@link Mono} for the Operation
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Operation> getOperation(String operationId) {
        return Mono.error(new UnsupportedOperationException("not yet implemented"));
    }

    /**
     * Cancels the operation
     *
     * @param operationId associated with the operation
     * @return A {@link Mono} for the async return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> cancelOperation(String operationId) {
        return Mono.error(new UnsupportedOperationException("not yet implemented"));
    }

    /**
     * Lists the acquired phone numbers
     *
     * @return A {@link PagedFlux} for the Acquired Phone Numbers
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<AcquiredPhoneNumber> listAcquiredPhoneNumbers() {
        return new PagedFlux<AcquiredPhoneNumber>(
            () -> Mono.error(new UnsupportedOperationException("not yet implemented")));
    }

    /**
     * Gets the Acquired Phone Number
     *
     * @param phoneNumber The phone number
     * @return A {@link Mono} for the Acquired Phone Number
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AcquiredPhoneNumber> getPhoneNumber(String phoneNumber) {
        return Mono.error(new UnsupportedOperationException("not yet implemented"));
    }

    /**
     * Updates an already acquired phone number
     *
     * @param phoneNumber The phone number to update
     * @param update object representing the update properties
     * @return A {@link PollerFlux} object with the Acquired Phone Number
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<Operation, AcquiredPhoneNumber> beginUpdatePhoneNumber(
        String phoneNumber, AcquiredPhoneNumberUpdate update) {
        return new PollerFlux<Operation, AcquiredPhoneNumber>(
            Duration.ofMillis(1000),
            (context) -> Mono.empty(),
            (context) -> Mono.empty(),
            (activationResponse, context) -> Mono.empty(),
            (context) -> Mono.empty());
    }

    /**
     * Releases an already acquired phone number
     *
     * @param phoneNumber the phone number to release
     * @return A {@link PollerFlux} object for the operation
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<Operation, Void> beginReleasePhoneNumber(String phoneNumber) {
        return new PollerFlux<Operation, Void>(
            Duration.ofMillis(1000),
            (context) -> Mono.empty(),
            (context) -> Mono.empty(),
            (activationResponse, context) -> Mono.empty(),
            (context) -> Mono.empty());
    }
}
