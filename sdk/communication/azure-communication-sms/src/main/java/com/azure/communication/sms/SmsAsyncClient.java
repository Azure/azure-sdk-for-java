// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.communication.sms.implementation.AzureCommunicationSMSServiceImpl;
import com.azure.communication.sms.implementation.SmsImpl;
import com.azure.communication.sms.implementation.models.DeliveryReport;
import com.azure.communication.sms.implementation.models.DeliveryAttempt;
import com.azure.communication.sms.implementation.models.DeliveryReportDeliveryStatus;
import com.azure.communication.sms.implementation.models.ErrorResponse;
import com.azure.communication.sms.implementation.models.MessagingConnectOptions;
import com.azure.communication.sms.implementation.models.SmsSendResponseItem;
import com.azure.communication.sms.implementation.models.SendMessageRequest;
import com.azure.communication.sms.implementation.models.SmsRecipient;
import com.azure.communication.sms.implementation.models.SmsSendResponse;
import com.azure.communication.sms.models.SmsDeliveryReport;
import com.azure.communication.sms.models.SmsDeliveryStatus;
import com.azure.communication.sms.models.SmsDeliveryAttempt;
import com.azure.communication.sms.models.SmsSendOptions;
import com.azure.communication.sms.models.SmsSendResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
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
import java.util.stream.Collectors;
import java.util.UUID;
import reactor.core.publisher.Mono;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Async client for sending SMS messages with Azure Communication SMS Service.
 *
 * <p>
 * <strong>Instantiating an asynchronous SMS Client</strong>
 * </p>
 * <!-- src_embed readme-sample-createSmsClientWithConnectionString -->
 * <pre>
 * &#47;&#47; You can find your connection string from your resource in the Azure Portal
 * String connectionString = &quot;https:&#47;&#47;&lt;resource-name&gt;.communication.azure.com&#47;;&lt;access-key&gt;&quot;;
 *
 * SmsClient smsClient = new SmsClientBuilder&#40;&#41;
 *         .connectionString&#40;connectionString&#41;
 *         .buildClient&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-createSmsClientWithConnectionString -->
 *
 * @see SmsClientBuilder
 */
@ServiceClient(builder = SmsClientBuilder.class, isAsync = true)
public final class SmsAsyncClient {
    private final SmsImpl smsClient;
    private final AzureCommunicationSMSServiceImpl serviceClient;
    private final ClientLogger logger = new ClientLogger(SmsAsyncClient.class);
    private OptOutsAsyncClient optOutsAsyncClient;

    SmsAsyncClient(AzureCommunicationSMSServiceImpl smsServiceClient) {
        this.serviceClient = smsServiceClient;
        this.smsClient = smsServiceClient.getSms();
    }

    /**
     * Gets an async client for managing SMS opt-outs.
     *
     * @return An {@link OptOutsAsyncClient} instance for managing opt-outs.
     */
    public OptOutsAsyncClient getOptOutsAsyncClient() {
        if (optOutsAsyncClient == null) {
            optOutsAsyncClient = new OptOutsAsyncClient(serviceClient);
        }
        return optOutsAsyncClient;
    }

    /**
     * Sends an SMS message from a phone number that belongs to the authenticated
     * account.
     *
     * @param from    Number that is sending the message.
     * @param to      The recipient's phone number.
     * @param message message to send to recipient.
     * @return response for a successful send Sms request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SmsSendResult> send(String from, String to, String message) {
        return send(from, to, message, null, null);
    }

    /**
     * Sends an SMS message from a phone number that belongs to the authenticated
     * account.
     *
     * @param from    Number that is sending the message.
     * @param to      The recipient's phone number.
     * @param message message to send to recipient.
     * @param options set options on the SMS request, like enable delivery report,
     *                which sends a report
     *                for this message to the Azure Resource Event Grid.
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
                return smsClient.sendAsync(request, contextValue).flatMap(response -> {
                    List<SmsSendResult> smsSendResults = convertSmsSendResults(response.getValue());
                    if (smsSendResults.isEmpty()) {
                        return Mono.error(new RuntimeException("No SMS send results returned"));
                    }
                    return Mono.just(smsSendResults.get(0));
                });
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Sends an SMS message from a phone number that belongs to the authenticated
     * account.
     *
     * @param from    Number that is sending the message.
     * @param to      A list of the recipient's phone numbers.
     * @param message message to send to recipient.
     * @return response for a successful send Sms request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Iterable<SmsSendResult>> send(String from, Iterable<String> to, String message) {
        return sendWithResponse(from, to, message, null).map(response -> response.getValue());
    }

    /**
     * Sends an SMS message from a phone number that belongs to the authenticated
     * account.
     *
     * @param from    Number that is sending the message.
     * @param to      A list of the recipient's phone numbers.
     * @param message message to send to recipient.
     * @param options set options on the SMS request, like enable delivery report,
     *                which sends a report
     *                for this message to the Azure Resource Event Grid.
     * @return response for a successful send Sms request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Iterable<SmsSendResult>>> sendWithResponse(String from, Iterable<String> to, String message,
        SmsSendOptions options) {
        return sendWithResponse(from, to, message, options, null);
    }

    Mono<Response<Iterable<SmsSendResult>>> sendWithResponse(String from, Iterable<String> to, String message,
        SmsSendOptions options, Context context) {
        try {
            Objects.requireNonNull(from, "'from' cannot be null.");
            Objects.requireNonNull(to, "'to' cannot be null.");
            SendMessageRequest request = createSendMessageRequest(from, to, message, options);
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return this.smsClient.sendWithResponseAsync(request, contextValue).flatMap(result -> {
                    Response<SmsSendResponse> rawResponse = result;
                    SmsSendResponse responseValue = rawResponse.getValue();
                    Iterable<SmsSendResult> smsSendResults = convertSmsSendResults(responseValue.getValue());
                    return Mono.just(new SimpleResponse<Iterable<SmsSendResult>>(rawResponse, smsSendResults));
                });
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private List<SmsSendResult> convertSmsSendResults(Iterable<SmsSendResponseItem> resultsIterable) {
        List<SmsSendResult> iterableWrapper = new ArrayList<>();
        if (resultsIterable != null) {
            for (SmsSendResponseItem item : resultsIterable) {
                iterableWrapper.add(new SmsSendResult(item.getTo(), item.getMessageId(), item.getHttpStatusCode(),
                    item.isSuccessful(), item.getErrorMessage()));
            }
        }
        return iterableWrapper;
    }

    private SendMessageRequest createSendMessageRequest(String from, Iterable<String> smsRecipient, String message,
        SmsSendOptions options) {
        validateSmsSendOptions(options);

        SendMessageRequest request = new SendMessageRequest();
        List<SmsRecipient> recipients = new ArrayList<SmsRecipient>();

        for (String s : smsRecipient) {
            SmsRecipient recipient = new SmsRecipient().setTo(s)
                .setRepeatabilityRequestId(UUID.randomUUID().toString())
                .setRepeatabilityFirstSent(OffsetDateTime.now(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US)));
            recipients.add(recipient);
        }
        request.setFrom(from).setSmsRecipients(recipients).setMessage(message).setSmsSendOptions(options);
        return request;
    }

    private void validateSmsSendOptions(SmsSendOptions options) {
        if (options != null && options.getMessagingConnect() != null) {
            MessagingConnectOptions messagingConnect = options.getMessagingConnect();
            if (messagingConnect.getPartnerParams() == null
                || (messagingConnect.getPartnerParams() instanceof java.util.Map
                    && ((java.util.Map<?, ?>) messagingConnect.getPartnerParams()).isEmpty())) {
                throw new IllegalArgumentException(
                    "MessagingConnect partnerParams cannot be null or empty when MessagingConnect is provided.");
            }
            if (messagingConnect.getPartner() == null || messagingConnect.getPartner().trim().isEmpty()) {
                throw new IllegalArgumentException(
                    "MessagingConnect partner cannot be null or empty when MessagingConnect is provided.");
            }
        }
    }

    /**
     * Gets delivery report for a specific outgoing message.
     *
     * @param outgoingMessageId The identifier of the outgoing message.
     * @return The delivery report for the specified message on successful
     *         completion of {@link Mono}.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException    thrown if the request is rejected by server.
     * @throws RuntimeException         all other wrapped checked exceptions if the
     *                                  request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SmsDeliveryReport> getDeliveryReport(String outgoingMessageId) {
        if (outgoingMessageId == null) {
            return Mono.error(new IllegalArgumentException("'outgoingMessageId' cannot be null."));
        }
        if (outgoingMessageId.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("'outgoingMessageId' cannot be empty."));
        }
        return getDeliveryReportWithResponse(outgoingMessageId, Context.NONE)
            .flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * Gets delivery report for a specific outgoing message.
     *
     * @param outgoingMessageId The identifier of the outgoing message.
     * @param context           A {@link Context} representing the request context.
     * @return The delivery report for the specified message along with HTTP
     *         response information on successful
     *         completion of {@link Mono}.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException    thrown if the request is rejected by server.
     * @throws RuntimeException         all other wrapped checked exceptions if the
     *                                  request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SmsDeliveryReport>> getDeliveryReportWithResponse(String outgoingMessageId, Context context) {
        if (outgoingMessageId == null) {
            return Mono.error(new IllegalArgumentException("'outgoingMessageId' cannot be null."));
        }
        if (outgoingMessageId.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("'outgoingMessageId' cannot be empty."));
        }

        return serviceClient.getDeliveryReports()
            .getWithResponseAsync(outgoingMessageId, context)
            .cast(Response.class)
            .flatMap(response -> {
                Object responseValue = response.getValue();

                if (responseValue instanceof DeliveryReport) {
                    SmsDeliveryReport publicDeliveryReport
                        = convertToPublicDeliveryReport((DeliveryReport) responseValue);
                    return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(), publicDeliveryReport));
                } else if (responseValue instanceof ErrorResponse) {
                    ErrorResponse errorResponse = (ErrorResponse) responseValue;
                    String errorMessage
                        = String.format("Delivery report request failed. Status: %d, Title: %s, Type: %s",
                            errorResponse.getStatus(), errorResponse.getTitle(), errorResponse.getType());
                    HttpResponseException exception = new HttpResponseException(errorMessage, (HttpResponse) response);
                    return Mono.error(exception);
                } else if (responseValue != null) {
                    // Try to deserialize unknown object type using BinaryData
                    try {
                        BinaryData binaryData = BinaryData.fromObject(responseValue);

                        // First try to deserialize as ErrorResponse
                        try {
                            ErrorResponse errorResponse = binaryData.toObject(ErrorResponse.class);
                            String errorMessage
                                = String.format("Delivery report request failed. Status: %d, Title: %s, Type: %s",
                                    errorResponse.getStatus(), errorResponse.getTitle(), errorResponse.getType());
                            HttpResponseException exception
                                = new HttpResponseException(errorMessage, (HttpResponse) response);
                            return Mono.error(exception);
                        } catch (Exception e) {
                            // If not ErrorResponse, try DeliveryReport
                            DeliveryReport deliveryReport = binaryData.toObject(DeliveryReport.class);
                            SmsDeliveryReport publicDeliveryReport = convertToPublicDeliveryReport(deliveryReport);
                            return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                                response.getHeaders(), publicDeliveryReport));
                        }
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Failed to deserialize response", e));
                    }
                } else {
                    return Mono.error(new RuntimeException("Response value is null"));
                }
            });
    }

    // Private conversion methods
    private SmsDeliveryReport convertToPublicDeliveryReport(DeliveryReport implDeliveryReport) {
        if (implDeliveryReport == null) {
            return null;
        }

        SmsDeliveryStatus deliveryStatus = null;
        if (implDeliveryReport.getDeliveryStatus() != null) {
            deliveryStatus = SmsDeliveryStatus.fromString(implDeliveryReport.getDeliveryStatus().toString());
        }

        List<SmsDeliveryAttempt> publicDeliveryAttempts = null;
        if (implDeliveryReport.getDeliveryAttempts() != null) {
            publicDeliveryAttempts = implDeliveryReport.getDeliveryAttempts()
                .stream()
                .map(attempt -> new SmsDeliveryAttempt(attempt.getTimestamp(), attempt.getSegmentsSucceeded(),
                    attempt.getSegmentsFailed()))
                .collect(Collectors.toList());
        }

        return new SmsDeliveryReport(implDeliveryReport.getMessageId(), implDeliveryReport.getFrom(),
            implDeliveryReport.getTo(), deliveryStatus, implDeliveryReport.getDeliveryStatusDetails(),
            publicDeliveryAttempts, implDeliveryReport.getReceivedTimestamp(), implDeliveryReport.getTag(),
            implDeliveryReport.getMessagingConnectPartnerMessageId());
    }
}
