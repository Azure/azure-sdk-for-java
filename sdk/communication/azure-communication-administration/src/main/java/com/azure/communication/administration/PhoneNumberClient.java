// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.administration;

import com.azure.communication.administration.models.*;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;

import java.time.Duration;

/**
 * Synchronous client for Communication service phone number operations
 */
@ServiceClient(builder = PhoneNumberClientBuilder.class, isAsync = false)
public final class PhoneNumberClient {

    PhoneNumberClient() {}

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PhoneNumberCountry> listAllSupportedCountries(String locale) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public TollFreeAreaCodes getTollFreeAreaCodes(String countryCode) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public GeographicAreaCodes getGeographicAreaCodes(String countryCode) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<GeographicAreaCodes> getGeographicAreaCodesWithResponse(
        String countryCode, String locale, String locationPath, Context context) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CountryOffering> listAllOfferings(String countryCode) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Response<CountryOffering>> listAllOfferingsWithResponse(
        String countryCode, PhoneNumberType phoneNumberType, AssignmentType assignmentType, Context context) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<Operation, SearchResult> beginPhoneNumberSearch(
        SearchRequest searchRequest,  String countryCode) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<Operation, SearchResult> beginPhoneNumberPurchase(String searchId) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchResult getSearchResults(String searchId) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Operation getOperation(String operationId) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void cancelOperation(String operationId) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AcquiredPhoneNumbers> listAllPhoneNumbers() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public AcquiredPhoneNumber getPhoneNumber(String phoneNumber) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<Operation, AcquiredPhoneNumber> beginUpdatePhoneNumber(
        String phoneNumber, AcquiredPhoneNumberUpdate update) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<Operation, Void> beginReleasePhoneNumber(String phoneNumber) {
        throw new UnsupportedOperationException("not yet implemented");
    }
}
