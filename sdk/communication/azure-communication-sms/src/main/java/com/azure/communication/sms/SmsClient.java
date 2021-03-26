// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.communication.sms.models.SmsSendOptions;
import com.azure.communication.sms.models.SmsSendResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

/**
 * Client for sending SMS messages with Azure Communication SMS Services.
 */
@ServiceClient(builder = SmsClientBuilder.class)
public final class SmsClient {
    private final SmsAsyncClient smsAsyncClient;

    SmsClient(SmsAsyncClient smsAsyncClient) {
        this.smsAsyncClient = smsAsyncClient;
    }

    /**
     * Sends an SMS message from a phone number that belongs to the authenticated account.
     *
     * @param from Number that is sending the message.
     * @param to The recipient's phone number.
     * @param message message to send to recipient.
     * @return response for a successful send Sms request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SmsSendResult send(String from, String to, String message) {
        return smsAsyncClient.send(from, to, message).block();
    }

    /**
     * Sends an SMS message from a phone number that belongs to the authenticated account.
     *
     * @param from Number that is sending the message.
     * @param to The recipient's phone number.
     * @param message message to send to recipient.
     * @param options set options on the SMS request, like enable delivery report, which sends a report
     * for this message to the Azure Resource Event Grid.
     * @return The Sms send result.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SmsSendResult send(String from, String to, String message, SmsSendOptions options) {
        return smsAsyncClient.send(from, to, message, options).block();
    }

    /**
     * Sends an SMS message from a phone number that belongs to the authenticated account.
     *
     * @param from Number that is sending the message.
     * @param to A list of the recipient's phone numbers.
     * @param message message to send to recipient.
     * @return response for a successful send Sms request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Iterable<SmsSendResult> send(String from, Iterable<String> to, String message) {
        return smsAsyncClient.send(from, to, message).block();
    }

    /**
     * Sends an SMS message from a phone number that belongs to the authenticated account.
     *
     * @param from Number that is sending the message.
     * @param to A list of the recipient's phone numbers.
     * @param message message to send to recipient.
     * @param options set options on the SMS request, like enable delivery report, which sends a report
     *                   for this message to the Azure Resource Event Grid.
     * @param context A {@link Context} representing the request context
     * @return response for a successful send Sms request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Iterable<SmsSendResult>> sendWithResponse(String from, Iterable<String> to, String message, SmsSendOptions options, Context context) {
        return smsAsyncClient.sendWithResponse(from, to, message, options, context).block();
    }
}
