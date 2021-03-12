// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.communication.sms.models.SendSmsOptions;
import com.azure.communication.sms.models.SendSmsResponse;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

import java.util.List;
import java.util.ArrayList;

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
     * @param to The recipient's phone number. In this version, only one recipient in the list is supported.
     * @param message message to send to recipient.
     * @param smsOptions set options on the SMS request, like enable delivery report, which sends a report
     * for this message to the Azure Resource Event Grid.
     * @param context the context of the request. Can also be null or Context.NONE.
     * @return response for a successful send Sms request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SendSmsResponse> sendMessageWithResponse(PhoneNumberIdentifier from, 
        List<PhoneNumberIdentifier> to, String message, 
        SendSmsOptions smsOptions, Context context) {
        return this.smsAsyncClient.sendMessageWithResponse(from, to, message, smsOptions, context).block();
    }

    /**
     * Sends an SMS message from a phone number that belongs to the authenticated account.
     *
     * @param from Number that is sending the message.
     * @param to The recipient's phone number. In this version, only one recipient in the
     * list is supported.
     * @param message message to send to recipient.
     * @return response for a successful send Sms request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)    
    public SendSmsResponse sendMessage(PhoneNumberIdentifier from, List<PhoneNumberIdentifier> to, String message) {
        return this.smsAsyncClient.sendMessage(from, to, message).block();
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
    public SendSmsResponse sendMessage(PhoneNumberIdentifier from, PhoneNumberIdentifier to, String message) {
        if (to == null) {
            logger.logThrowableAsError(new NullPointerException("Argument 'to' cannot be null."));
        }

        List<PhoneNumberIdentifier> toList = new ArrayList<PhoneNumberIdentifier>();
        toList.add(to);

        return this.smsAsyncClient.sendMessage(from, toList, message).block();
    }     

    /**
     * Sends an SMS message from a phone number that belongs to the authenticated account.
     *
     * @param from Number that is sending the message.
     * @param to The recipient's phone number. In this version, only one recipient in the
     * list is supported.
     * @param message message to send to recipient.
     * @param smsOptions set options on the SMS request, like enable delivery report, which sends a report
     * for this message to the Azure Resource Event Grid.
     * @return response for a successful send Sms request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SendSmsResponse sendMessage(PhoneNumberIdentifier from, List<PhoneNumberIdentifier> to, String message, 
        SendSmsOptions smsOptions) {
        return this.smsAsyncClient.sendMessage(from, to, message, smsOptions).block();
    }
}
