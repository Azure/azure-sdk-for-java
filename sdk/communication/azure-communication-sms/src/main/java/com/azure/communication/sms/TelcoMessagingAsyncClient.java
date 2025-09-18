// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.communication.sms.implementation.AzureCommunicationSMSServiceImpl;
import com.azure.core.annotation.ServiceClient;

/**
 * Async client that organizes SMS functionality into specialized sub-clients.
 * Provides access to SMS sending, delivery reports, and opt-out management.
 */
@ServiceClient(builder = TelcoMessagingClientBuilder.class, isAsync = true)
public final class TelcoMessagingAsyncClient {
    private final SmsAsyncClient smsAsyncClient;
    private final DeliveryReportsAsyncClient deliveryReportsAsyncClient;
    private final OptOutsAsyncClient optOutsAsyncClient;

    TelcoMessagingAsyncClient(AzureCommunicationSMSServiceImpl serviceClient) {
        this.smsAsyncClient = new SmsAsyncClient(serviceClient);
        this.deliveryReportsAsyncClient = new DeliveryReportsAsyncClient(serviceClient);
        this.optOutsAsyncClient = new OptOutsAsyncClient(serviceClient);
    }

    /**
     * Gets the SMS client for sending SMS messages.
     *
     * @return The SMS async client.
     */
    public SmsAsyncClient getSmsAsyncClient() {
        return smsAsyncClient;
    }

    /**
     * Gets the delivery reports client for retrieving SMS delivery reports.
     *
     * @return The delivery reports async client.
     */
    public DeliveryReportsAsyncClient getDeliveryReportsAsyncClient() {
        return deliveryReportsAsyncClient;
    }

    /**
     * Gets the opt-outs client for managing SMS opt-outs.
     *
     * @return The opt-outs async client.
     */
    public OptOutsAsyncClient getOptOutsAsyncClient() {
        return optOutsAsyncClient;
    }
}
