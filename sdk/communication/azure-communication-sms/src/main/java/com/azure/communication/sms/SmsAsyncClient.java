// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.communication.sms.implementation.AzureCommunicationSMSServiceImpl;
import com.azure.communication.sms.models.SendMessageRequest;
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
     * @param to The recipient's phone number. In this version, only one recipient in the list is supported.
     * @param message message to send to recipient.
     * @param smsOptions set options on the SMS request, like enable delivery report, which sends a report
     * for this message to the Azure Resource Event Grid.
     * @param context the context of the request. Can also be null or Context.NONE.
     * @return response for a successful send Sms request.
     */
    Mono<Response<SendSmsResponse>> sendMessageWithResponse(PhoneNumberIdentifier from, 
        List<PhoneNumberIdentifier> to, String message, 
        SendSmsOptions smsOptions, Context context) {
        if (from == null) {
            return monoError(logger, new NullPointerException("Argument 'from' cannot be null."));
        } else if (to == null) {
            return monoError(logger, new NullPointerException("Argument 'to' cannot be null."));
        }

        SendMessageRequest sendMessageRequest = createSmsMessageRequest(from, to, message, smsOptions);

        return this.smsServiceClient.getSms().sendWithResponseAsync(sendMessageRequest, context);
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
    public Mono<SendSmsResponse> sendMessage(PhoneNumberIdentifier from, 
        List<PhoneNumberIdentifier> to, String message) {
        if (from == null) {
            return monoError(logger, new NullPointerException("Argument 'from' cannot be null."));
        } else if (to == null) {
            return monoError(logger, new NullPointerException("Argument 'to' cannot be null."));
        }

        SendSmsOptions smsOptions = new SendSmsOptions();
        smsOptions.setEnableDeliveryReport(false);
        
        return sendMessage(from, to, message, smsOptions);
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
    public Mono<SendSmsResponse> sendMessage(PhoneNumberIdentifier from, PhoneNumberIdentifier to, String message) {
        if (from == null) {
            return monoError(logger, new NullPointerException("Argument 'from' cannot be null."));
        } else if (to == null) {
            return monoError(logger, new NullPointerException("Argument 'to' cannot be null."));
        }

        SendSmsOptions smsOptions = new SendSmsOptions();
        smsOptions.setEnableDeliveryReport(false);

        List<PhoneNumberIdentifier> toList = new ArrayList<PhoneNumberIdentifier>();
        toList.add(to);

        return sendMessage(from, toList, message, smsOptions);
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
    public Mono<SendSmsResponse> sendMessage(PhoneNumberIdentifier from, 
        List<PhoneNumberIdentifier> to, String message, 
        SendSmsOptions smsOptions) {
        if (from == null) {
            return monoError(logger, new NullPointerException("Argument 'from' cannot be null."));
        } else if (to == null) {
            return monoError(logger, new NullPointerException("Argument 'to' cannot be null."));
        }

        SendMessageRequest sendMessageRequest = createSmsMessageRequest(from, to, message, smsOptions);
        try {
            return withContext(context -> this.smsServiceClient.getSms().sendAsync(sendMessageRequest, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }        
    }

    private SendMessageRequest createSmsMessageRequest(PhoneNumberIdentifier from, 
        List<PhoneNumberIdentifier> to, String message, 
        SendSmsOptions smsOptions) {
        Stream<String> s = to.stream().map(n -> n.getValue());
        SendMessageRequest sendMessageRequest = new SendMessageRequest();
        sendMessageRequest.setFrom(from.getValue())
            .setTo(s.collect(Collectors.toList()))
            .setMessage(message)
            .setSendSmsOptions(smsOptions);
        
        return sendMessageRequest;
    }
}
