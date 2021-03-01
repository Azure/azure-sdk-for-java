// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;
<<<<<<< HEAD
=======


>>>>>>> 94f7a8b318 (draft of the implementation)
import com.azure.communication.sms.models.SmsSendOptions;
import com.azure.communication.sms.models.SmsSendResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

import java.util.List;



/**
 * Client for sending SMS messages with Azure Communication SMS Services.
 */
@ServiceClient(builder = SmsClientBuilder.class)
public final class SmsClient {
    private final SmsAsyncClient smsAsyncClient;
    private final ClientLogger logger = new ClientLogger(SmsClient.class);

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
     * @param smsOptions set options on the SMS request, like enable delivery report, which sends a report
     *                   for this message to the Azure Resource Event Grid.
     * @return response for a successful send Sms request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SmsSendResult send(String from, String to, String message,
                              SmsSendOptions smsOptions) {
        return smsAsyncClient.send(from, to, message,
            smsOptions).block();
    }

    /**
     * Sends an SMS message from a phone number that belongs to the authenticated account.
     *
     * @param from Number that is sending the message.
     * @param to The recipient's phone number.
     * @param message message to send to recipient.
     * @param smsOptions set options on the SMS request, like enable delivery report, which sends a report
     *                   for this message to the Azure Resource Event Grid.
     * @param context context to use
     * @return response for a successful send Sms request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SmsSendResult send(String from, String to, String message,
                              SmsSendOptions smsOptions, Context context) {
<<<<<<< HEAD
        return smsAsyncClient.send(from, to, message, smsOptions).block();
=======
        return null;
>>>>>>> 94f7a8b318 (draft of the implementation)
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
<<<<<<< HEAD
    public Iterable<SmsSendResult> send(String from, Iterable<String> to, String message) {
=======
    public List<SmsSendResult> send(String from, Iterable<String> to, String message) {
>>>>>>> 94f7a8b318 (draft of the implementation)
        return smsAsyncClient.send(from, to, message).block();
    }


    /**
     * Sends an SMS message from a phone number that belongs to the authenticated account.
     *
     * @param from Number that is sending the message.
     * @param to A list of the recipient's phone numbers.
     * @param message message to send to recipient.
     * @param smsOptions set options on the SMS request, like enable delivery report, which sends a report
     *                   for this message to the Azure Resource Event Grid.
     * @param context sets the context for the call
     * @return response for a successful send Sms request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
<<<<<<< HEAD
    public Iterable<SmsSendResult> send(String from, Iterable<String> to, String message,
=======
    public List<SmsSendResult> send(String from, Iterable<String> to, String message,
>>>>>>> 94f7a8b318 (draft of the implementation)
                                    SmsSendOptions smsOptions, Context context) {
        return smsAsyncClient.send(from, to, message,
            smsOptions).block();
    }


}
