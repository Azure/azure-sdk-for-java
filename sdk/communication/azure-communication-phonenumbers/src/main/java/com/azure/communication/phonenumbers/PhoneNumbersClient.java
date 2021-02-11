// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.phonenumbers;

import java.util.Objects;

import com.azure.communication.phonenumbers.implementation.PhoneNumberAdminClientImpl;
import com.azure.communication.phonenumbers.implementation.PhoneNumbersImpl;
import com.azure.communication.phonenumbers.models.AcquiredPhoneNumber;
import com.azure.communication.phonenumbers.models.PhoneNumberCapabilitiesRequest;
import com.azure.communication.phonenumbers.models.PhoneNumberOperation;
import com.azure.communication.phonenumbers.models.PhoneNumberSearchRequest;
import com.azure.communication.phonenumbers.models.PhoneNumberSearchResult;
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

    PhoneNumbersClient(PhoneNumberAdminClientImpl phoneNumberAdminClient, PhoneNumbersAsyncClient asyncClient) {
        this.client = phoneNumberAdminClient.getPhoneNumbers();
        this.asyncClient = asyncClient;
    }
  
   /**
     * Gets information about an acquired phone number.
     * @param phoneNumber The phone number id in E.164 format. The leading plus can be either + or encoded
     *                    as %2B.
     * @return {@link AcquiredPhoneNumber} representing the acquired telephone number.
     * @throws NullPointerException if {@code phoneNumber} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AcquiredPhoneNumber getPhoneNumber(String phoneNumber) {
        Objects.requireNonNull(phoneNumber, "'phoneNumber' cannot be null.");
        return client.getByNumber(phoneNumber);
    }

    /**
     * Gets information about an acquired phone number with response.
     * @param phoneNumber The phone number id in E.164 format. The leading plus can be either + or encoded
     *                    as %2B.
     * @param context A {@link Context} representing the request context.
     * @return {@link AcquiredPhoneNumber} representing the acquired telephone number.
     * @throws NullPointerException if {@code phoneNumber} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AcquiredPhoneNumber> getPhoneNumberWithResponse(String phoneNumber, Context context) {
        Objects.requireNonNull(phoneNumber, "'phoneNumber' cannot be null.");
        context = context == null ? Context.NONE : context;
        return client.getByNumberWithResponseAsync(phoneNumber, context).block();
    }

    /**
     * Gets the list of the acquired phone numbers with context.
     *
     * @param context A {@link Context} representing the request context.
     * @return A {@link PagedIterable} of {@link AcquiredPhoneNumber} instances representing acquired telephone numbers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AcquiredPhoneNumber> listPhoneNumbers(Context context) {
        context = context == null ? Context.NONE : context;
        return client.listPhoneNumbers(null, null, context);
    }

    /**
     * Starts the search for available phone numbers to purchase.
     * 
     * This function returns a Long Running Operation poller that allows you to wait indefinitely until the 
     * operation is complete.
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param searchRequest The search request
     * @param context A {@link Context} representing the request context.
     * @return A {@link SyncPoller} object with the reservation result
     * @throws NullPointerException if {@code countryCode} or {@code searchRequest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SyncPoller<PhoneNumberOperation, PhoneNumberSearchResult> beginSearchAvailablePhoneNumbers(
        String countryCode, PhoneNumberSearchRequest searchRequest, Context context) {
        return asyncClient.beginSearchAvailablePhoneNumbers(countryCode, searchRequest, context).getSyncPoller();
    }

    /**
     * Starts the purchase of the phone number(s) in the search result associated with a given id.
     * 
     * This function returns a Long Running Operation poller that allows you to wait indefinitely until the 
     * operation is complete.
     *
     * @param searchId ID of the search
     * @param context A {@link Context} representing the request context.
     * @return A {@link SyncPoller} object.
     * @throws NullPointerException if {@code searchId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SyncPoller<PhoneNumberOperation, Void> beginPurchasePhoneNumbers(String searchId, Context context) {
        return asyncClient.beginPurchasePhoneNumbers(searchId, context).getSyncPoller();
    }

    /**
     * Starts the update of capabilities for an acquired phone number.
     *
     * This function returns a Long Running Operation poller that allows you to wait indefinitely until the 
     * operation is complete.
     *
     *
     * @param phoneNumber The phone number id in E.164 format. The leading plus can be either + or encoded
     *                    as %2B.
     * @param context A {@link Context} representing the request context.
     * @return A {@link SyncPoller} object.
     * @throws NullPointerException if {@code phoneNumber} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SyncPoller<PhoneNumberOperation, Void> beginReleasePhoneNumber(String phoneNumber, Context context) {
        return asyncClient.beginReleasePhoneNumber(phoneNumber).getSyncPoller();
    }

    /**
     * Update capabilities of an acquired phone number.
     * 
     * This function returns a Long Running Operation poller that allows you to wait indefinitely until the 
     * operation is complete.
     * @param phoneNumber The phone number id in E.164 format. The leading plus can be either + or encoded
     *                    as %2B.
     * @param capabilitiesUpdateRequest Update capabilities of an acquired phone number.
     * @param context A {@link Context} representing the request context.
     * @return A {@link SyncPoller} object
     * @throws NullPointerException if {@code phoneNumber} or {@code capabilitiesUpdateRequest} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SyncPoller<PhoneNumberOperation, AcquiredPhoneNumber> beginUpdatePhoneNumberCapabilities(String phoneNumber, PhoneNumberCapabilitiesRequest capabilitiesUpdateRequest, Context context) {
        return asyncClient.beginUpdatePhoneNumberCapabilities(phoneNumber, capabilitiesUpdateRequest, context).getSyncPoller();
    }
}
