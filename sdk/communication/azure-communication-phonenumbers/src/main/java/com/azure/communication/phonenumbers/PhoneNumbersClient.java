// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.phonenumbers;

import java.util.Objects;

import com.azure.communication.phonenumbers.implementation.PhoneNumberAdminClientImpl;
import com.azure.communication.phonenumbers.implementation.PhoneNumbersImpl;
import com.azure.communication.phonenumbers.models.AreaCodeResult;
import com.azure.communication.phonenumbers.models.PhoneNumberAssignmentType;
import com.azure.communication.phonenumbers.models.PhoneNumberCapabilities;
import com.azure.communication.phonenumbers.models.PhoneNumberCountry;
import com.azure.communication.phonenumbers.models.PhoneNumberLocality;
import com.azure.communication.phonenumbers.models.PhoneNumberOffering;
import com.azure.communication.phonenumbers.models.PhoneNumberOperation;
import com.azure.communication.phonenumbers.models.PhoneNumberSearchOptions;
import com.azure.communication.phonenumbers.models.PhoneNumberSearchResult;
import com.azure.communication.phonenumbers.models.PhoneNumberType;
import com.azure.communication.phonenumbers.models.PurchasePhoneNumbersResult;
import com.azure.communication.phonenumbers.models.PurchasedPhoneNumber;
import com.azure.communication.phonenumbers.models.ReleasePhoneNumberResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.SyncPoller;

/**
 * Synchronous client for Communication service phone number operations
 */
@ServiceClient(builder = PhoneNumbersClientBuilder.class, isAsync = false)
public final class PhoneNumbersClient {

    private final ClientLogger logger = new ClientLogger(PhoneNumbersClient.class);
    private final PhoneNumbersImpl client;
    private final PhoneNumbersAsyncClient asyncClient;

    private final String acceptLanguage;

    PhoneNumbersClient(PhoneNumberAdminClientImpl phoneNumberAdminClient, PhoneNumbersAsyncClient asyncClient) {
        this(phoneNumberAdminClient, asyncClient, null);
    }

    PhoneNumbersClient(PhoneNumberAdminClientImpl phoneNumberAdminClient, PhoneNumbersAsyncClient asyncClient, String acceptLanguage) {
        this.client = phoneNumberAdminClient.getPhoneNumbers();
        this.asyncClient = asyncClient;
        this.acceptLanguage = acceptLanguage;
    }

   /**
     * Gets information about a purchased phone number.
     * @param phoneNumber The phone number id in E.164 format. The leading plus can be either + or encoded
     *                    as %2B.
     * @return {@link PurchasedPhoneNumber} representing the purchased telephone number.
     * @throws NullPointerException if {@code phoneNumber} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PurchasedPhoneNumber getPurchasedPhoneNumber(String phoneNumber) {
        Objects.requireNonNull(phoneNumber, "'phoneNumber' cannot be null.");
        return client.getByNumber(phoneNumber, acceptLanguage);
    }

    /**
     * Gets information about a purchased phone number with response.
     * @param phoneNumber The phone number id in E.164 format. The leading plus can be either + or encoded
     *                    as %2B.
     * @param context A {@link Context} representing the request context.
     * @return {@link PurchasedPhoneNumber} representing the purchased telephone number.
     * @throws NullPointerException if {@code phoneNumber} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PurchasedPhoneNumber> getPurchasedPhoneNumberWithResponse(String phoneNumber, Context context) {
        Objects.requireNonNull(phoneNumber, "'phoneNumber' cannot be null.");
        context = context == null ? Context.NONE : context;
        return client.getByNumberWithResponseAsync(phoneNumber, acceptLanguage, context).block();
    }

    /**
     * Gets the list of the purchased phone numbers.
     *
     * @return A {@link PagedIterable} of {@link PurchasedPhoneNumber} instances representing purchased telephone numbers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PurchasedPhoneNumber> listPurchasedPhoneNumbers() {
        return this.listPurchasedPhoneNumbers(null);
    }

    /**
     * Gets the list of the purchased phone numbers with context.
     *
     * @param context A {@link Context} representing the request context.
     * @return A {@link PagedIterable} of {@link PurchasedPhoneNumber} instances representing purchased telephone numbers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PurchasedPhoneNumber> listPurchasedPhoneNumbers(Context context) {
        context = context == null ? Context.NONE : context;
        return client.listPhoneNumbers(null, null, acceptLanguage, context);
    }

    /**
     * Starts the search for available phone numbers to purchase.
     *
     * This function returns a Long Running Operation poller that allows you to wait indefinitely until the
     * operation is complete.
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param phoneNumberType {@link PhoneNumberType} The phone number type.
     * @param assignmentType {@link PhoneNumberAssignmentType} The phone number assignment type.
     * @param capabilities {@link PhoneNumberCapabilities} The phone number capabilities.
     * @return A {@link SyncPoller} object with the reservation result.
     * @throws NullPointerException if {@code countryCode} or {@code searchRequest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SyncPoller<PhoneNumberOperation, PhoneNumberSearchResult> beginSearchAvailablePhoneNumbers(
        String countryCode, PhoneNumberType phoneNumberType, PhoneNumberAssignmentType assignmentType, PhoneNumberCapabilities capabilities) {
        return asyncClient.beginSearchAvailablePhoneNumbers(countryCode, phoneNumberType, assignmentType, capabilities).getSyncPoller();
    }

    /**
     * Starts the search for available phone numbers to purchase.
     *
     * This function returns a Long Running Operation poller that allows you to wait indefinitely until the
     * operation is complete.
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param phoneNumberType {@link PhoneNumberType} The phone number type.
     * @param assignmentType {@link PhoneNumberAssignmentType} The phone number assignment type.
     * @param capabilities {@link PhoneNumberCapabilities} The phone number capabilities.
     * @param searchOptions The phone number search options.
     * @param context A {@link Context} representing the request context.
     * @return A {@link SyncPoller} object with the reservation result.
     * @throws NullPointerException if {@code countryCode} or {@code searchRequest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SyncPoller<PhoneNumberOperation, PhoneNumberSearchResult> beginSearchAvailablePhoneNumbers(
        String countryCode, PhoneNumberType phoneNumberType, PhoneNumberAssignmentType assignmentType,
        PhoneNumberCapabilities capabilities, PhoneNumberSearchOptions searchOptions, Context context) {
        return asyncClient.beginSearchAvailablePhoneNumbers(countryCode, phoneNumberType, assignmentType, capabilities, searchOptions, context).getSyncPoller();
    }

    /**
     * Starts the purchase of the phone number(s) in the search result associated with a given id.
     *
     * This function returns a Long Running Operation poller that allows you to wait indefinitely until the
     * operation is complete.
     *
     * @param searchId ID of the search
     * @return A {@link SyncPoller} object with PurchasePhoneNumbersResult.
     * @throws NullPointerException if {@code searchId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SyncPoller<PhoneNumberOperation, PurchasePhoneNumbersResult> beginPurchasePhoneNumbers(String searchId) {
        return asyncClient.beginPurchasePhoneNumbers(searchId).getSyncPoller();
    }

    /**
     * Starts the purchase of the phone number(s) in the search result associated with a given id.
     *
     * This function returns a Long Running Operation poller that allows you to wait indefinitely until the
     * operation is complete.
     *
     * @param searchId ID of the search
     * @param context A {@link Context} representing the request context.
     * @return A {@link SyncPoller} object with PurchasePhoneNumbersResult.
     * @throws NullPointerException if {@code searchId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SyncPoller<PhoneNumberOperation, PurchasePhoneNumbersResult> beginPurchasePhoneNumbers(String searchId, Context context) {
        return asyncClient.beginPurchasePhoneNumbers(searchId, context).getSyncPoller();
    }

    /**
     * Starts the update of capabilities for a purchased phone number.
     *
     * This function returns a Long Running Operation poller that allows you to wait indefinitely until the
     * operation is complete.
     *
     * @param phoneNumber The phone number id in E.164 format. The leading plus can be either + or encoded
     *                    as %2B.
     * @return A {@link SyncPoller} object with ReleasePhoneNumberResult.
     * @throws NullPointerException if {@code phoneNumber} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SyncPoller<PhoneNumberOperation, ReleasePhoneNumberResult> beginReleasePhoneNumber(String phoneNumber) {
        return asyncClient.beginReleasePhoneNumber(phoneNumber).getSyncPoller();
    }

    /**
     * Starts the update of capabilities for a purchased phone number.
     *
     * This function returns a Long Running Operation poller that allows you to wait indefinitely until the
     * operation is complete.
     *
     *
     * @param phoneNumber The phone number id in E.164 format. The leading plus can be either + or encoded
     *                    as %2B.
     * @param context A {@link Context} representing the request context.
     * @return A {@link SyncPoller} object with ReleasePhoneNumberResult.
     * @throws NullPointerException if {@code phoneNumber} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SyncPoller<PhoneNumberOperation, ReleasePhoneNumberResult> beginReleasePhoneNumber(String phoneNumber, Context context) {
        return asyncClient.beginReleasePhoneNumber(phoneNumber, context).getSyncPoller();
    }

    /**
     * Update capabilities of a purchased phone number.
     *
     * This function returns a Long Running Operation poller that allows you to wait indefinitely until the
     * operation is complete.
     * @param phoneNumber The phone number id in E.164 format. The leading plus can be either + or encoded
     *                    as %2B.
     * @param capabilities Update capabilities of a purchased phone number.
     * @return A {@link SyncPoller} object with purchased phone number.
     * @throws NullPointerException if {@code phoneNumber} or {@code capabilities} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SyncPoller<PhoneNumberOperation, PurchasedPhoneNumber> beginUpdatePhoneNumberCapabilities(String phoneNumber, PhoneNumberCapabilities capabilities) {
        return asyncClient.beginUpdatePhoneNumberCapabilities(phoneNumber, capabilities).getSyncPoller();
    }

    /**
     * Update capabilities of a purchased phone number.
     *
     * This function returns a Long Running Operation poller that allows you to wait indefinitely until the
     * operation is complete.
     * @param phoneNumber The phone number id in E.164 format. The leading plus can be either + or encoded
     *                    as %2B.
     * @param capabilities Update capabilities of a purchased phone number.
     * @param context A {@link Context} representing the request context.
     * @return A {@link SyncPoller} object with purchased phone number.
     * @throws NullPointerException if {@code phoneNumber} or {@code capabilities} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SyncPoller<PhoneNumberOperation, PurchasedPhoneNumber> beginUpdatePhoneNumberCapabilities(String phoneNumber, PhoneNumberCapabilities capabilities, Context context) {
        return asyncClient.beginUpdatePhoneNumberCapabilities(phoneNumber, capabilities, context).getSyncPoller();
    }

    /**
     * Gets the list of the available countries.
     *
     * @return A {@link PagedIterable} of {@link PhoneNumberCountry} instances representing available countries.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PhoneNumberCountry> listAvailableCountries() {
        return this.listAvailableCountries(null);
    }

    /**
     * Gets the list of the purchased phone numbers with context.
     *
     * @param context A {@link Context} representing the request context.
     * @return A {@link PagedIterable} of {@link PhoneNumberCountry} instances representing purchased telephone numbers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PhoneNumberCountry> listAvailableCountries(Context context) {
        context = context == null ? Context.NONE : context;
        return client.listAvailableCountries(acceptLanguage, null, null, context);
    }

    /**
     * Gets the list of the available localities. I.e. cities, towns.
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param administrativeDivision An optional parameter. The name or short name of the state/province within which to list the localities.
     * @return A {@link PagedIterable} of {@link PhoneNumberLocality} instances representing available localities with phone numbers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PhoneNumberLocality> listAvailableLocalities(String countryCode, String administrativeDivision) {
        return this.listAvailableLocalities(countryCode, administrativeDivision, null);
    }

    /**
     * Gets the list of the available localities. I.e. cities, towns.
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param administrativeDivision An optional parameter. The name or short name of the state/province within which to list the localities.
     * @param context A {@link Context} representing the request context.
     * @return A {@link PagedIterable} of {@link PhoneNumberLocality} instances representing available localities with phone numbers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PhoneNumberLocality> listAvailableLocalities(String countryCode, String administrativeDivision, Context context) {
        context = context == null ? Context.NONE : context;
        return client.listAvailableLocalities(countryCode, acceptLanguage, null, null, administrativeDivision, context);
    }

    /**
     * Gets the list of the available Toll-Free area codes for a given country.
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param assignmentType {@link PhoneNumberAssignmentType} The phone number assignment type.
     * @return A {@link PagedIterable} of {@link AreaCodeResult} instances representing available area codes.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AreaCodeResult> listAvailableTollFreeAreaCodes(String countryCode, PhoneNumberAssignmentType assignmentType) {
        return this.listAvailableTollFreeAreaCodes(countryCode, assignmentType, null);
    }

/**
     * Gets the list of the available Toll-Free area codes for a given country.
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param assignmentType {@link PhoneNumberAssignmentType} The phone number assignment type.
     * @param context A {@link Context} representing the request context.
     * @return A {@link PagedIterable} of {@link AreaCodeResult} instances representing available area codes.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AreaCodeResult> listAvailableTollFreeAreaCodes(String countryCode, PhoneNumberAssignmentType assignmentType, Context context) {
        return client.listAreaCodes(countryCode, null, null, PhoneNumberType.TOLL_FREE, assignmentType, null, null);
    }

    /**
     * Gets the list of the available Geographic area codes for a given country and locality.
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param assignmentType {@link PhoneNumberAssignmentType} The phone number assignment type.
     * @param locality The name of the locality (e.g. city or town name) in which to fetch area codes.
     * @param administrativeDivision An optional parameter. The name of the administrative division (e.g. state or province) of the locality.
     * @return A {@link PagedIterable} of {@link AreaCodeResult} instances representing purchased telephone numbers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AreaCodeResult> listAvailableGeographicAreaCodes(String countryCode, PhoneNumberAssignmentType assignmentType, String locality, String administrativeDivision) {
        return this.listAvailableGeographicAreaCodes(countryCode, assignmentType, locality, administrativeDivision, null);
    }

    /**
     * Gets the list of the available Geographic area codes for a given country and locality.
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param assignmentType {@link PhoneNumberAssignmentType} The phone number assignment type.
     * @param locality The name of the locality (e.g. city or town name) in which to fetch area codes.
     * @param administrativeDivision An optional parameter. The name of the administrative division (e.g. state or province) of the locality.
     * @param context A {@link Context} representing the request context.
     * @return A {@link PagedIterable} of {@link AreaCodeResult} instances representing purchased telephone numbers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AreaCodeResult> listAvailableGeographicAreaCodes(String countryCode, PhoneNumberAssignmentType assignmentType, String locality, String administrativeDivision, Context context) {
        context = context == null ? Context.NONE : context;
        return client.listAreaCodes(countryCode, null, null, PhoneNumberType.GEOGRAPHIC, assignmentType, locality, administrativeDivision, context);
    }

    /**
     * Gets the list of the available phone number offerings for the given country.
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param phoneNumberType {@link PhoneNumberType} Optional parameter. Restrict the offerings to the phone number type.
     * @param assignmentType {@link PhoneNumberAssignmentType} Optional parameter. Restrict the offerings to the assignment type.
     * @return A {@link PagedIterable} of {@link PurchasedPhoneNumber} instances representing purchased telephone numbers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PhoneNumberOffering> listAvailableOfferings(String countryCode, PhoneNumberType phoneNumberType, PhoneNumberAssignmentType assignmentType) {
        return this.listAvailableOfferings(countryCode, phoneNumberType, assignmentType, null);
    }

    /**
     * Gets the list of the available phone number offerings for the given country.
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param phoneNumberType {@link PhoneNumberType} Optional parameter. Restrict the offerings to the phone number type.
     * @param assignmentType {@link PhoneNumberAssignmentType} Optional parameter. Restrict the offerings to the assignment type.
     * @param context A {@link Context} representing the request context.
     * @return A {@link PagedIterable} of {@link PurchasedPhoneNumber} instances representing purchased telephone numbers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PhoneNumberOffering> listAvailableOfferings(String countryCode, PhoneNumberType phoneNumberType, PhoneNumberAssignmentType assignmentType, Context context) {
        context = context == null ? Context.NONE : context;
        return client.listOfferings(countryCode, phoneNumberType, assignmentType, null, null);
    }
}
