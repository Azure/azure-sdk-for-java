// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.administration;

import com.azure.communication.administration.models.*;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Asynchronous client for Communication service phone number operations
 */
@ServiceClient(builder = PhoneNumberClientBuilder.class, isAsync = true)
public final class PhoneNumberAsyncClient {

    PhoneNumberAsyncClient() {}

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Flux<PhoneNumberCountry> listAllSupportedCountries(String locale) {
        return Flux.error(new UnsupportedOperationException("not yet implemented"));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TollFreeAreaCodes> getTollFreeAreaCodes(String countryCode) {
        return Mono.error(new UnsupportedOperationException("not yet implemented"));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<GeographicAreaCodes> getGeographicAreaCodes(String countryCode) {
        return Mono.error(new UnsupportedOperationException("not yet implemented"));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<GeographicAreaCodes>> getGeographicAreaCodesWithResponse(
        String countryCode, String locale, String locationPath) {
        return Mono.error(new UnsupportedOperationException("not yet implemented"));
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Flux<CountryOffering> listAllOfferings(String countryCode) {
        return Flux.error(new UnsupportedOperationException("not yet implemented"));
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Flux<Response<CountryOffering>> listAllOfferingsWithResponse(
        String countryCode, PhoneNumberType phoneNumberType, AssignmentType assignmentType) {
        return Flux.error(new UnsupportedOperationException("not yet implemented"));
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<Operation, SearchResult> beginPhoneNumberSearch(
        SearchRequest searchRequest,  String countryCode) {
        return new PollerFlux<Operation, SearchResult>(
            Duration.ofMillis(1000),
            (context) -> Mono.empty(),
            (context) -> Mono.empty(),
            (activationResponse, context) -> Mono.empty(),
            (context) -> Mono.empty());
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<Operation, SearchResult> beginPhoneNumberPurchase(String searchId) {
        return new PollerFlux<Operation, SearchResult>(
            Duration.ofMillis(1000),
            (context) -> Mono.empty(),
            (context) -> Mono.empty(),
            (activationResponse, context) -> Mono.empty(),
            (context) -> Mono.empty());
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchResult> getSearchResults(String searchId) {
        return Mono.error(new UnsupportedOperationException("not yet implemented"));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Operation> getOperation(String operationId) {
        return Mono.error(new UnsupportedOperationException("not yet implemented"));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> cancelOperation(String operationId) {
        return Mono.error(new UnsupportedOperationException("not yet implemented"));
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Flux<AcquiredPhoneNumbers> listAllPhoneNumbers() {
        return Flux.error(new UnsupportedOperationException("not yet implemented"));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AcquiredPhoneNumber> getPhoneNumber(String phoneNumber) {
        return Mono.error(new UnsupportedOperationException("not yet implemented"));
    }

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
