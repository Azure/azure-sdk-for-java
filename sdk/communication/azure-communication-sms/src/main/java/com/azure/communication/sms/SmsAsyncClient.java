// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;


import com.azure.communication.sms.implementation.AzureCommunicationSMSServiceImpl;
import com.azure.communication.sms.models.SendMessageRequest;
import com.azure.communication.sms.models.SendSmsOptions;
import com.azure.communication.sms.models.SendSmsResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import reactor.core.publisher.Mono;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Async client for sending SMS messages with Azure Communication SMS Service.
 */
@ServiceClient(builder = SmsClientBuilder.class, isAsync = true)
public final class SmsAsyncClient {
    private final AzureCommunicationSMSServiceImpl smsServiceClient;
    private final ClientLogger logger = new ClientLogger(SmsAsyncClient.class);

    SmsAsyncClient(AzureCommunicationSMSServiceImpl smsServiceClient) {
        this.smsServiceClient = smsServiceClient;
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
    public Mono<SendSmsResult> send(String from, String to, String message) {
        return null;
    }

    /**
     * Sends an SMS message from a phone number that belongs to the authenticated account.
     *
     * @param from Number that is sending the message.
     * @param to The recipient's phone number.
     * @param message message to send to recipient.
     * @param smsOptions set options on the SMS request, like enable delivery report, which sends a report
     * for this message to the Azure Resource Event Grid.
     * @return response for a successful send Sms request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SendSmsResult> send(String from, String to, String message,
                                          SendSmsOptions smsOptions) {
        return null;
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
    public PagedFlux<SendSmsResult> send(String from, List<String> to, String message) {


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
     * @return response for a successful send Sms request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedFlux<SendSmsResult> send(String from, List<String> to, String message,
                                               SendSmsOptions smsOptions) {


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
     * @param context The context to associate with this operation.
     * @return response for a successful send Sms request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedFlux<SendSmsResult> sendWithContext(String from, List<String> to, String message,
                                                          SendSmsOptions smsOptions, Context context) {


        return null;
    }

    private SendMessageRequest createSmsMessageRequest(String from, List<String> to, String message,
                                                       SendSmsOptions smsOptions) {

        SendMessageRequest sendMessageRequest = new SendMessageRequest();
        sendMessageRequest.setFrom(from)
            .setTo(to)
            .setMessage(message)
            .setSendSmsOptions(smsOptions);

        return sendMessageRequest;
    }
}
