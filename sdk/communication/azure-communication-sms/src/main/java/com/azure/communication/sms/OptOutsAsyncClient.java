// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.communication.sms.implementation.AzureCommunicationSMSServiceImpl;
import com.azure.communication.sms.implementation.models.OptOutRequest;
import com.azure.communication.sms.implementation.models.OptOutResponse;
import com.azure.communication.sms.implementation.models.OptOutResponseItem;
import com.azure.communication.sms.implementation.models.OptOutRecipient;
import com.azure.communication.sms.models.OptOutResult;
import com.azure.communication.sms.models.OptOutCheckResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Async client for managing SMS opt-outs with Azure Communication SMS Services.
 */
@ServiceClient(builder = TelcoMessagingClientBuilder.class, isAsync = true)
public final class OptOutsAsyncClient {
    private final AzureCommunicationSMSServiceImpl serviceClient;
    private final ClientLogger logger = new ClientLogger(OptOutsAsyncClient.class);

    OptOutsAsyncClient(AzureCommunicationSMSServiceImpl serviceClient) {
        this.serviceClient = serviceClient;
    }

    /**
     * Add phone numbers to the opt-outs list which shall stop receiving messages from a sender number.
     *
     * @param from The sender's identifier (typically phone number in E.164 format).
     * @param to The recipient's phone number.
     * @return A list of opt-out results on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<List<OptOutResult>> addOptOut(String from, String to) {
        return addOptOut(from, Arrays.asList(to));
    }

    /**
     * Add phone numbers to the opt-outs list which shall stop receiving messages from a sender number.
     *
     * @param from The sender's identifier (typically phone number in E.164 format).
     * @param to A list of the recipient's phone numbers.
     * @return A list of opt-out results on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<List<OptOutResult>> addOptOut(String from, Iterable<String> to) {
        return addOptOutWithResponse(from, to, Context.NONE)
            .flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * Add phone numbers to the opt-outs list which shall stop receiving messages from a sender number.
     *
     * @param from The sender's identifier (typically phone number in E.164 format).
     * @param to A list of the recipient's phone numbers.
     * @param context A {@link Context} representing the request context.
     * @return A list of opt-out results with HTTP response information on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<List<OptOutResult>>> addOptOutWithResponse(String from, Iterable<String> to, Context context) {
        try {
            Objects.requireNonNull(from, "'from' cannot be null.");
            Objects.requireNonNull(to, "'to' cannot be null.");
            OptOutRequest request = createOptOutRequest(from, to);
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return serviceClient.getOptOuts().addWithResponseAsync(request, contextValue)
                    .map(response -> new SimpleResponse<>(
                        response.getRequest(),
                        response.getStatusCode(),
                        response.getHeaders(),
                        convertToOptOutResults(response.getValue())
                    ));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Remove phone numbers from the opt-outs list.
     *
     * @param from The sender's identifier (typically phone number in E.164 format).
     * @param to The recipient's phone number.
     * @return A list of opt-out results on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<List<OptOutResult>> removeOptOut(String from, String to) {
        return removeOptOut(from, Arrays.asList(to));
    }

    /**
     * Remove phone numbers from the opt-outs list.
     *
     * @param from The sender's identifier (typically phone number in E.164 format).
     * @param to A list of the recipient's phone numbers.
     * @return A list of opt-out results on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<List<OptOutResult>> removeOptOut(String from, Iterable<String> to) {
        return removeOptOutWithResponse(from, to, Context.NONE)
            .flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * Remove phone numbers from the opt-outs list.
     *
     * @param from The sender's identifier (typically phone number in E.164 format).
     * @param to A list of the recipient's phone numbers.
     * @param context A {@link Context} representing the request context.
     * @return A list of opt-out results with HTTP response information on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<List<OptOutResult>>> removeOptOutWithResponse(String from, Iterable<String> to, Context context) {
        try {
            Objects.requireNonNull(from, "'from' cannot be null.");
            Objects.requireNonNull(to, "'to' cannot be null.");
            OptOutRequest request = createOptOutRequest(from, to);
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return serviceClient.getOptOuts().removeWithResponseAsync(request, contextValue)
                    .map(response -> new SimpleResponse<>(
                        response.getRequest(),
                        response.getStatusCode(),
                        response.getHeaders(),
                        convertToOptOutResults(response.getValue())
                    ));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Check the opt-out status for recipient phone numbers with a sender phone number.
     *
     * @param from The sender's identifier (typically phone number in E.164 format).
     * @param to The recipient's phone number.
     * @return A list of opt-out check results on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<List<OptOutCheckResult>> checkOptOut(String from, String to) {
        return checkOptOut(from, Arrays.asList(to));
    }

    /**
     * Check the opt-out status for recipient phone numbers with a sender phone number.
     *
     * @param from The sender's identifier (typically phone number in E.164 format).
     * @param to A list of the recipient's phone numbers.
     * @return A list of opt-out check results on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<List<OptOutCheckResult>> checkOptOut(String from, Iterable<String> to) {
        return checkOptOutWithResponse(from, to, Context.NONE)
            .flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * Check the opt-out status for recipient phone numbers with a sender phone number.
     *
     * @param from The sender's identifier (typically phone number in E.164 format).
     * @param to A list of the recipient's phone numbers.
     * @param context A {@link Context} representing the request context.
     * @return A list of opt-out check results with HTTP response information on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<List<OptOutCheckResult>>> checkOptOutWithResponse(String from, Iterable<String> to, Context context) {
        try {
            Objects.requireNonNull(from, "'from' cannot be null.");
            Objects.requireNonNull(to, "'to' cannot be null.");
            OptOutRequest request = createOptOutRequest(from, to);
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return serviceClient.getOptOuts().checkWithResponseAsync(request, contextValue)
                    .map(response -> new SimpleResponse<>(
                        response.getRequest(),
                        response.getStatusCode(),
                        response.getHeaders(),
                        convertToOptOutCheckResults(response.getValue())
                    ));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private OptOutRequest createOptOutRequest(String from, Iterable<String> to) {
        List<OptOutRecipient> recipients = new ArrayList<>();
        for (String recipient : to) {
            recipients.add(new OptOutRecipient().setTo(recipient));
        }

        return new OptOutRequest()
            .setFrom(from)
            .setRecipients(recipients);
    }

    private List<OptOutResult> convertToOptOutResults(OptOutResponse response) {
        if (response == null || response.getValue() == null) {
            return new ArrayList<>();
        }

        return response.getValue().stream()
            .map(item -> new OptOutResult(
                item.getTo(),
                item.getHttpStatusCode(),
                item.getErrorMessage()
            ))
            .collect(Collectors.toList());
    }

    private List<OptOutCheckResult> convertToOptOutCheckResults(OptOutResponse response) {
        if (response == null || response.getValue() == null) {
            return new ArrayList<>();
        }

        return response.getValue().stream()
            .map(item -> new OptOutCheckResult(
                item.getTo(),
                item.getHttpStatusCode(),
                item.isOptedOut(),
                item.getErrorMessage()
            ))
            .collect(Collectors.toList());
    }
}
