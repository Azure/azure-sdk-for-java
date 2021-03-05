// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.communication.sms.implementation.AzureCommunicationSMSServiceImpl;
import com.azure.communication.sms.implementation.models.SmsSendResponseItem;
import com.azure.communication.sms.implementation.models.SendMessageRequest;
import com.azure.communication.sms.implementation.models.SmsRecipient;
import com.azure.communication.sms.implementation.models.SmsSendResponse;
import com.azure.communication.sms.models.SmsSendOptions;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.logging.ClientLogger;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
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
     * Phone number has to be in the format 000 - 00 - 00
     *
     * @param from Number that is sending the message.
     * @param to The recipient's phone number.
     * @param message message to send to recipient.
     * @return response for a successful send Sms request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SmsSendResult> send(String from, String to, String message) {
        return send(from, to, message, null);
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
    public Mono<SmsSendResult> send(String from, String to, String message, SmsSendOptions smsOptions) {
        List<String> recipients = new ArrayList<String>();
        recipients.add(to);
        SendMessageRequest request = createSendMessageRequest(from, recipients, message, smsOptions);
        try {
            Objects.requireNonNull(from, "'from' cannot be null.");
            Objects.requireNonNull(to, "'to' cannot be null.");
            Mono<Response<SmsSendResponse>> responseMono = withContext(context -> this.smsServiceClient.getSms().sendWithResponseAsync(request, context));
            Response<SmsSendResponse> response = responseMono.block();
            SmsSendResponse smsSendResponse = response.getValue();

            List<SmsSendResult> result =  convertSmsResults(smsSendResponse.getValue());
            if (result.size() == 1) {
                return Mono.just(result.get(0));
            } else {
                return monoError(logger, new NullPointerException("no response"));
            }
        } catch (NullPointerException ex) {
            return monoError(logger, ex);
        } catch (RuntimeException  ex) {
            return monoError(logger, ex);
        }

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
    public Mono<Iterable<SmsSendResult>> send(String from, Iterable<String> to, String message) {
        return send(from, to, message, null);
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
    public Mono<Iterable<SmsSendResult>> send(String from, Iterable<String> to, String message, SmsSendOptions smsOptions) {
        SendMessageRequest request = createSendMessageRequest(from, to, message, smsOptions);
        try {
            Objects.requireNonNull(from, "'from' cannot be null.");
            Objects.requireNonNull(to, "'to' cannot be null.");
            Mono<Response<SmsSendResponse>> responseMono = withContext(context -> this.smsServiceClient.getSms().sendWithResponseAsync(request, context));
            Response<SmsSendResponse> response = responseMono.block();
            SmsSendResponse smsSendResponse = response.getValue();
            List<SmsSendResult> result = convertSmsResults(smsSendResponse.getValue());
            return Mono.just(result);
        } catch (NullPointerException ex) {
            return monoError(logger, ex);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private List<SmsSendResult>  convertSmsResults(Iterable<SmsSendResponseItem> resultsIterable) {
        List <SmsSendResult> iterableWrapper = new ArrayList<>();
        for (SmsSendResponseItem item : resultsIterable
             ) {
            iterableWrapper.add(new SmsSendResult(item));
        }
        return iterableWrapper;
    }

    private SendMessageRequest createSendMessageRequest(String from, Iterable<String> smsRecipient, String message, SmsSendOptions smsOptions) {
        SendMessageRequest request = new SendMessageRequest();
        List<SmsRecipient> recipients = new ArrayList<SmsRecipient>();
        for (String s : smsRecipient) {
            recipients.add(new SmsRecipient().setTo(s));
        }
        request.setFrom(from)
            .setSmsRecipients(recipients)
            .setMessage(message)
            .setSmsSendOptions(smsOptions);
        return request;
    }
}
