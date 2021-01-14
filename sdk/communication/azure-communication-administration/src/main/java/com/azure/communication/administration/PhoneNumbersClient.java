// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.administration;

import com.azure.communication.administration.implementation.PhoneNumberAdminClientImpl;
import com.azure.communication.administration.implementation.PhoneNumbersImpl;
import com.azure.communication.administration.models.AcquiredPhoneNumber;
import com.azure.communication.administration.models.AcquiredPhoneNumberUpdate;
import com.azure.communication.administration.models.PhoneNumberCapabilitiesRequest;
import com.azure.communication.administration.models.PhoneNumberSearchRequest;
import com.azure.communication.administration.models.PhoneNumberSearchResult;
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

    private final ClientLogger logger = new ClientLogger(PhoneNumbersAsyncClient.class);
    private final PhoneNumbersImpl phoneNumbersImpl;

    PhoneNumbersClient(PhoneNumberAdminClientImpl phoneNumberAdminClient) {
        this.phoneNumbersImpl = phoneNumberAdminClient.getPhoneNumbers();
    }
  
   /**
     * Gets information about an acquired phone number.
     * @param phoneNumber The phone number id in E.164 format. The leading plus can be either + or encoded
     *                    as %2B.
     * @return {@link AcquiredPhoneNumber} representing the acquired telephone number.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public AcquiredPhoneNumber getPhoneNumber(String phoneNumber) {
        return phoneNumbersImpl.getPhoneNumber(phoneNumber);
    }

    /**
     * Gets information about an acquired phone number with response.
     * @param phoneNumber The phone number id in E.164 format. The leading plus can be either + or encoded
     *                    as %2B.
     * @param context A {@link Context} representing the request context.
     * @return {@link AcquiredPhoneNumber} representing the acquired telephone number.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Response<AcquiredPhoneNumber> getPhoneNumberWithResponse(String phoneNumber, Context context) {
        return phoneNumbersImpl.getPhoneNumberWithResponseAsync(phoneNumber, context).block();
    }

    /**
     * Gets the list of the acquired phone numbers.
     *
     * @return A {@link PagedIterable} of {@link AcquiredPhoneNumber} instances representing acquired telephone numbers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AcquiredPhoneNumber> listPhoneNumbers() {
        return phoneNumbersImpl.listPhoneNumbers();
    }

    /**
     * Gets the list of the acquired phone numbers with context.
     *
     * @param context the context of the request. Can also be null or Context.NONE.
     * @return A {@link PagedIterable} of {@link AcquiredPhoneNumber} instances representing acquired telephone numbers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AcquiredPhoneNumber> listPhoneNumbers(Context context) {
        return phoneNumbersImpl.listPhoneNumbers(context);
    }

      /**
     * Update an acquired phone number.
     *
     * @param phoneNumber The phone number id in E.164 format. The leading plus can be either + or encoded
     *                    as %2B.
     * @param update {@link AcquiredPhoneNumberUpdate} specifying updates to an acquired phone number.
     * @return {@link AcquiredPhoneNumber} representing the updated acquired phone number
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AcquiredPhoneNumber updatePhoneNumber(String phoneNumber, AcquiredPhoneNumberUpdate update){
        return phoneNumbersImpl.updatePhoneNumber(phoneNumber, update.getCallbackUri(), update.getApplicationId());
    }

    /**
     * Update an acquired phone number with response.
     *
     * @param phoneNumber The phone number id in E.164 format. The leading plus can be either + or encoded
     *                    as %2B.
     * @param update Update to an acquired phone number.
     * @param context A {@link Context} representing the request context.
     * @return {@link AcquiredPhoneNumber} representing the updated acquired phone number
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AcquiredPhoneNumber> updatePhoneNumberWithResponse(String phoneNumber, AcquiredPhoneNumberUpdate update, Context context){
        return phoneNumbersImpl.updatePhoneNumberWithResponseAsync(phoneNumber, update.getCallbackUri(), update.getApplicationId()).block();
    }

    /**
     * Starts the search for available phone numbers to purchase.
     * 
     * This function returns a Long Running Operation poller that allows you to wait indefinitely until the 
     * operation is complete.
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param searchRequest The search request
     * @return A {@link SyncPoller} object with the reservation result
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<PhoneNumberSearchResult, PhoneNumberSearchResult> beginSearchAvailablePhoneNumbers(
        String countryCode, PhoneNumberSearchRequest searchRequest) {
            return null;
    }

    /**
     * Starts the purchase of the phone number(s) in the search result associated with a given id.
     * 
     * This function returns a Long Running Operation poller that allows you to wait indefinitely until the 
     * operation is complete.
     *
     * @param searchId ID of the search
     * @return A {@link SyncPoller} object.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<Void, Void> beginPurchasePhoneNumbers(String searchId) {
        return null;
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
     * @return A {@link SyncPoller} object.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<Void, Void> beginReleasePhoneNumbers(String phoneNumber) {
        return null;
    }

    /**
     * Update capabilities of an acquired phone number.
     * 
     * This function returns a Long Running Operation poller that allows you to wait indefinitely until the 
     * operation is complete.
     * @param phoneNumber The phone number id in E.164 format. The leading plus can be either + or encoded
     *                    as %2B.
     * @param capabilitiesUpdateRequest Update capabilities of an acquired phone number.
     * @return A {@link SyncPoller} object
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<AcquiredPhoneNumber, AcquiredPhoneNumber> beginUpdatePhoneNumberCapabilities(String phoneNumber, PhoneNumberCapabilitiesRequest capabilitiesUpdateRequest) {
        return null;
    }
}
