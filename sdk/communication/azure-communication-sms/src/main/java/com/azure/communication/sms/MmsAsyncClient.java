// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.communication.sms.implementation.AzureCommunicationSMSServiceImpl;
import com.azure.communication.sms.implementation.MmsImpl;
import com.azure.communication.sms.implementation.models.MmsRecipient;
import com.azure.communication.sms.implementation.models.MmsSendMessageRequest;
import com.azure.communication.sms.implementation.models.MmsSendResponse;
import com.azure.communication.sms.implementation.models.MmsSendResponseItem;
import com.azure.communication.sms.models.MmsAttachment;
import com.azure.communication.sms.models.MmsSendOptions;
import com.azure.communication.sms.models.MmsSendResult;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import reactor.core.publisher.Mono;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Async client for sending MMS messages with Azure Communication SMS Service.
 */
@ServiceClient(builder = MmsClientBuilder.class, isAsync = true)
public final class MmsAsyncClient {
    private final MmsImpl mmsClient;
    private ClientLogger logger = new ClientLogger(MmsAsyncClient.class);

    MmsAsyncClient(AzureCommunicationSMSServiceImpl smsServiceClient) {
        mmsClient = smsServiceClient.getMms();
    }

    /**
     * Sends an MMS message from a phone number that belongs to the authenticated account.
     *
     * @param from number that is sending the message.
     * @param to the recipient's phone number.
     * @param message message to send to recipient.
     * @param attachments a list of media attachments to include as part of the MMS. You can have maximum 10 attachments.
     * @return response for a successful send Mms request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<MmsSendResult> send(String from, String to, String message, List<MmsAttachment> attachments) {
        return send(from, to, message, attachments, null);
    }

    /**
     * Sends an MMS message from a phone number that belongs to the authenticated account.
     *
     * @param from number that is sending the message.
     * @param to the recipient's phone number.
     * @param message message to send to recipient.
     * @param attachments a list of media attachments to include as part of the MMS. You can have maximum 10 attachments.
     * @param options set options on the SMS request, like enable delivery report, which sends a report
     * for this message to the Azure Resource Event Grid.
     * @return response for a successful send Mms request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<MmsSendResult> send(String from, String to, String message, List<MmsAttachment> attachments, MmsSendOptions options) {
        return send(from, to, message, attachments, options, null);
    }

    Mono<MmsSendResult> send(String from, String to, String message, List<MmsAttachment> attachments, MmsSendOptions options, Context context) {
        try {
            Objects.requireNonNull(from, "'from' cannot be null.");
            Objects.requireNonNull(to, "'to' cannot be null.");

            List<String> recipients = Arrays.asList(to);

            MmsSendMessageRequest request = createMmsSendMessageRequest(from, recipients, message, attachments, options);

            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return this.mmsClient.sendAsync(request, contextValue)
                    .flatMap((MmsSendResponse response) -> {
                        List<MmsSendResult> mmsSendResults = convertToMmsSendResults(response.getValue());
                        return Mono.just(mmsSendResults.get(0));
                    });
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Sends an MMS message from a phone number that belongs to the authenticated account.
     *
     * @param from number that is sending the message.
     * @param to a list of the recipient's phone numbers.
     * @param message message to send to recipient.
     * @param attachments a list of media attachments to include as part of the MMS. You can have maximum 10 attachments.
     * @return response for a successful send Mms request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Iterable<MmsSendResult>> send(String from, Iterable<String> to, String message, List<MmsAttachment> attachments) {
        return sendWithResponse(from, to, message, attachments, null)
            .map(response -> response.getValue());
    }

    /**
     * Sends an MMS message from a phone number that belongs to the authenticated account.
     *
     * @param from number that is sending the message.
     * @param to a list of the recipient's phone numbers.
     * @param message message to send to recipient.
     * @param attachments a list of media attachments to include as part of the MMS. You can have maximum 10 attachments.
     * @param options set options on the SMS request, like enable delivery report, which sends a report
     * for this message to the Azure Resource Event Grid.
     * @return response for a successful send Mms request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Iterable<MmsSendResult>>> sendWithResponse(String from, Iterable<String> to, String message, List<MmsAttachment> attachments, MmsSendOptions options) {
        return sendWithResponse(from, to, message, attachments, options, null);
    }

    Mono<Response<Iterable<MmsSendResult>>> sendWithResponse(String from, Iterable<String> to, String message, List<MmsAttachment> attachments, MmsSendOptions options, Context context) {
        try {
            Objects.requireNonNull(from, "'from' cannot be null.");
            Objects.requireNonNull(to, "'to' cannot be null.");

            MmsSendMessageRequest request = createMmsSendMessageRequest(from, to, message, attachments, options);

            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return this.mmsClient.sendWithResponseAsync(request, contextValue)
                    .flatMap((Response<MmsSendResponse> response) -> {
                        Iterable<MmsSendResult> mmsSendResults = convertToMmsSendResults(response.getValue().getValue());
                        return Mono.just(new SimpleResponse<Iterable<MmsSendResult>>(response, mmsSendResults));
                    });
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private List<MmsSendResult> convertToMmsSendResults(Iterable<MmsSendResponseItem> responsesIterable) {
        List<MmsSendResult> iterableWrapper = new ArrayList<>();
        for (MmsSendResponseItem item : responsesIterable) {
            iterableWrapper.add(new MmsSendResult(
                item.getTo(),
                item.getMessageId(),
                item.getHttpStatusCode(),
                item.isSuccessful(),
                item.getErrorMessage()));
        }
        return iterableWrapper;
    }

    private MmsSendMessageRequest createMmsSendMessageRequest(String from, Iterable<String> mmsRecipients, String message, List<MmsAttachment> attachments, MmsSendOptions options) {
        MmsSendMessageRequest request = new MmsSendMessageRequest();
        List<MmsRecipient> recipients = new ArrayList<>();

        for (String recipient : mmsRecipients) {
            MmsRecipient mmsRecipient = new MmsRecipient()
                .setTo(recipient)
                .setRepeatabilityRequestId(UUID.randomUUID().toString())
                .setRepeatabilityFirstSent(OffsetDateTime.now(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US)));

            recipients.add(mmsRecipient);
        }

        request.setFrom(from)
            .setRecipients(recipients)
            .setMessage(message)
            .setAttachments(attachments)
            .setSendOptions(options);

        return request;
    }
}
