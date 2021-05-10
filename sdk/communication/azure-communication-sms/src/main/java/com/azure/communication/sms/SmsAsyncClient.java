// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.communication.sms.implementation.AzureCommunicationSMSServiceImpl;
import com.azure.communication.sms.implementation.SmsImpl;
import com.azure.communication.sms.implementation.models.SmsSendResponseItem;
import com.azure.communication.sms.implementation.models.SendMessageRequest;
import com.azure.communication.sms.implementation.models.SmsRecipient;
import com.azure.communication.sms.implementation.models.SmsSendResponse;
import com.azure.communication.sms.models.SmsSendOptions;
import com.azure.communication.sms.models.SmsSendResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.Locale;
import reactor.core.publisher.Mono;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Async client for sending SMS messages with Azure Communication SMS Service.
 */
@ServiceClient(builder = SmsClientBuilder.class, isAsync = true)
public final class SmsAsyncClient {
    private final SmsImpl smsClient;
    private final ClientLogger logger = new ClientLogger(SmsAsyncClient.class);

    SmsAsyncClient(AzureCommunicationSMSServiceImpl smsServiceClient) {
        smsClient = smsServiceClient.getSms();
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
    public Mono<SmsSendResult> send(String from, String to, String message) {
        return send(from, to, message, null, null);
    }

    /**
     * Sends an SMS message from a phone number that belongs to the authenticated account.
     *
     * @param from Number that is sending the message.
     * @param to The recipient's phone number.
     * @param message message to send to recipient.
     * @param options set options on the SMS request, like enable delivery report, which sends a report
     *                   for this message to the Azure Resource Event Grid.
     * @return The Sms send result.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SmsSendResult> send(String from, String to, String message, SmsSendOptions options) {
        return send(from, to, message, options, null);
    }

    Mono<SmsSendResult> send(String from, String to, String message, SmsSendOptions options, Context context) {
        try {
            Objects.requireNonNull(from, "'from' cannot be null.");
            Objects.requireNonNull(to, "'to' cannot be null.");
            List<String> recipients = Arrays.asList(to);
            SendMessageRequest request = createSendMessageRequest(from, recipients, message, options);
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return smsClient.sendAsync(request, contextValue)
                    .flatMap((SmsSendResponse response) -> {
                        List<SmsSendResult> smsSendResults = convertSmsSendResults(response.getValue());
                        return Mono.just(smsSendResults.get(0));
                    });
            });
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
        return sendWithResponse(from, to, message, null)
            .map(response -> response.getValue());
    }

    /**
     * Sends an SMS message from a phone number that belongs to the authenticated account.
     *
     * @param from Number that is sending the message.
     * @param to A list of the recipient's phone numbers.
     * @param message message to send to recipient.
     * @param options set options on the SMS request, like enable delivery report, which sends a report
     * for this message to the Azure Resource Event Grid.
     * @return response for a successful send Sms request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Iterable<SmsSendResult>>> sendWithResponse(String from, Iterable<String> to, String message, SmsSendOptions options) {
        return sendWithResponse(from, to, message, options, null);
    }

    Mono<Response<Iterable<SmsSendResult>>> sendWithResponse(String from, Iterable<String> to, String message, SmsSendOptions options, Context context) {
        try {
            Objects.requireNonNull(from, "'from' cannot be null.");
            Objects.requireNonNull(to, "'to' cannot be null.");
            SendMessageRequest request = createSendMessageRequest(from, to, message, options);
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return this.smsClient.sendWithResponseAsync(request, contextValue)
                    .flatMap((Response<SmsSendResponse> response) -> {
                        Iterable<SmsSendResult> smsSendResults = convertSmsSendResults(response.getValue().getValue());
                        return Mono.just(new SimpleResponse<Iterable<SmsSendResult>>(response, smsSendResults));
                    });
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private List<SmsSendResult> convertSmsSendResults(Iterable<SmsSendResponseItem> resultsIterable) {
        List<SmsSendResult> iterableWrapper = new ArrayList<>();
        for (SmsSendResponseItem item : resultsIterable) {
            iterableWrapper.add(new SmsSendResult(
                item.getTo(),
                item.getMessageId(),
                item.getHttpStatusCode(),
                item.isSuccessful(),
                item.getErrorMessage()));
        }
        return iterableWrapper;
    }

    private SendMessageRequest createSendMessageRequest(String from, Iterable<String> smsRecipient, String message, SmsSendOptions options) {
        SendMessageRequest request = new SendMessageRequest();
        List<SmsRecipient> recipients = new ArrayList<SmsRecipient>();

        for (String s : smsRecipient) {
            SmsRecipient recipient = new SmsRecipient()
                .setTo(s)
                .setRepeatabilityRequestId(UUID.randomUUID().toString())
                .setRepeatabilityFirstSent(OffsetDateTime.now(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US)));
            recipients.add(recipient);
        }
        request.setFrom(from)
            .setSmsRecipients(recipients)
            .setMessage(message)
            .setSmsSendOptions(options);
        return request;
    }
}
