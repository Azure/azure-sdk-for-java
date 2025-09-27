// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.core.annotation.ServiceClient;

/**
 * Client that organizes SMS functionality into specialized sub-clients.
 * Provides access to SMS sending, delivery reports, and opt-out management.
 */
@ServiceClient(builder = TelcoMessagingClientBuilder.class)
public final class TelcoMessagingClient {
    private final TelcoMessagingAsyncClient asyncClient;
    private final SmsClient smsClient;
    private final DeliveryReportsClient deliveryReportsClient;
    private final OptOutsClient optOutsClient;

    TelcoMessagingClient(TelcoMessagingAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
        this.smsClient = new SmsClient(asyncClient.getSmsAsyncClient());
        this.deliveryReportsClient = new DeliveryReportsClient(asyncClient.getDeliveryReportsAsyncClient());
        this.optOutsClient = new OptOutsClient(asyncClient.getOptOutsAsyncClient());
    }

    /**
     * Gets the SMS client for sending SMS messages.
     *
     * @return The SMS client.
     */
    public SmsClient getSmsClient() {
        return smsClient;
    }

    /**
     * Gets the delivery reports client for retrieving SMS delivery reports.
     *
     * @return The delivery reports client.
     */
    public DeliveryReportsClient getDeliveryReportsClient() {
        return deliveryReportsClient;
    }

    /**
     * Gets the opt-outs client for managing SMS opt-outs.
     *
     * @return The opt-outs client.
     */
    public OptOutsClient getOptOutsClient() {
        return optOutsClient;
    }
}
