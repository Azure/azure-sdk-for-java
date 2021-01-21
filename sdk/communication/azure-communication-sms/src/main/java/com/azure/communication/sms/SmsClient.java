// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.communication.common.PhoneNumber;
import com.azure.communication.sms.models.SendSmsOptions;
import com.azure.communication.sms.models.SendSmsResponse;
import com.azure.communication.sms.models.SendSmsResponseItem;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
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
     * @param to The recipient's phone number.
     * @param message message to send to recipient.
     * @param repeatabilityRequestID If specified, the client directs that the request is repeatable; that is, the
     *       client can make the request multiple times with the same Repeatability-Request-ID and get back an appropriate
     *       response without the server executing the request multiple times. The value of the Repeatability-Request-ID
     *     is an opaque string representing a client-generated, GUID, identifier for the request.
     * @return response for a successful send Sms request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SendSmsResponseItem sendMessage(PhoneNumber from, String to, String message,String repeatabilityRequestID) {
        return null;
    }

    /**
     * Sends an SMS message from a phone number that belongs to the authenticated account.
     *
     * @param from Number that is sending the message.
     * @param to The recipient's phone number.
     * @param message message to send to recipient.
     * @param repeatabilityRequestID If specified, the client directs that the request is repeatable; that is, the
     *       client can make the request multiple times with the same Repeatability-Request-ID and get back an appropriate
     *       response without the server executing the request multiple times. The value of the Repeatability-Request-ID
     *       is an opaque string representing a client-generated, GUID, identifier for the request.
     * @param smsOptions set options on the SMS request, like enable delivery report, which sends a report
     * for this message to the Azure Resource Event Grid.
     * @return response for a successful send Sms request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SendSmsResponseItem sendMessage(PhoneNumber from, String to, String message, String repeatabilityRequestID,
                                                      SendSmsOptions smsOptions) {
        return null;
    }

    /**
     * Sends an SMS message from a phone number that belongs to the authenticated account.
     *
     * @param from Number that is sending the message.
     * @param to A list of the recipient's phone numbers.
     * @param message message to send to recipient.
     * @param repeatabilityRequestID If specified, the client directs that the request is repeatable; that is, the
     *       client can make the request multiple times with the same Repeatability-Request-ID and get back an appropriate
     *       response without the server executing the request multiple times. The value of the Repeatability-Request-ID
     *       is an opaque string representing a client-generated, GUID, identifier for the request.
     * @return response for a successful send Sms request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedIterable<SendSmsResponseItem> sendMessage(PhoneNumber from, List<String> to, String message, String repeatabilityRequestID) {


        return null;
    }

    /**
     * Sends an SMS message from a phone number that belongs to the authenticated account.
     *
     * @param from Number that is sending the message.
     * @param to A list of the recipient's phone numbers.
     * @param message message to send to recipient.
     * @param smsOptions set options on the SMS request, like enable delivery report, which sends a report
     * for this message to the Azure Resource Event Grid.
     * @param repeatabilityRequestID If specified, the client directs that the request is repeatable; that is, the
     *       client can make the request multiple times with the same Repeatability-Request-ID and get back an appropriate
     *       response without the server executing the request multiple times. The value of the Repeatability-Request-ID
     *       is an opaque string representing a client-generated, GUID, identifier for the request.
     * @return response for a successful send Sms request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedIterable<SendSmsResponseItem> sendMessage(PhoneNumber from, List<String> to, String message, String repeatabilityRequestID,
                                                      SendSmsOptions smsOptions) {


        return null;
    }

    /**
     * Sends an SMS message from a phone number that belongs to the authenticated account.
     *
     * @param from Number that is sending the message.
     * @param to A list of the recipient's phone numbers.
     * @param message message to send to recipient.
     * @param repeatabilityRequestID If specified, the client directs that the request is repeatable; that is, the
     *       client can make the request multiple times with the same Repeatability-Request-ID and get back an appropriate
     *       response without the server executing the request multiple times. The value of the Repeatability-Request-ID
     *       is an opaque string representing a client-generated, GUID, identifier for the request.
     * @param smsOptions set options on the SMS request, like enable delivery report, which sends a report
     * for this message to the Azure Resource Event Grid.
     * @param context The context to associate with this operation.
     * @return response for a successful send Sms request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedIterable<SendSmsResponseItem> sendMessageWithContext(PhoneNumber from, List<String> to, String message, String repeatabilityRequestID,
                                                                 SendSmsOptions smsOptions, Context context) {


        return null;
    }


}
