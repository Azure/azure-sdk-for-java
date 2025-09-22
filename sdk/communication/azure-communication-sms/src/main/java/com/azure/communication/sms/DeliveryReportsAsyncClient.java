// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.communication.sms.implementation.AzureCommunicationSMSServiceImpl;
import com.azure.communication.sms.implementation.models.DeliveryReport;
import com.azure.communication.sms.implementation.models.ErrorResponse;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import reactor.core.publisher.Mono;

/**
 * Async client for retrieving SMS delivery reports with Azure Communication SMS
 * Services.
 */
@ServiceClient(builder = TelcoMessagingClientBuilder.class, isAsync = true)
public final class DeliveryReportsAsyncClient {
    private final AzureCommunicationSMSServiceImpl serviceClient;

    DeliveryReportsAsyncClient(AzureCommunicationSMSServiceImpl serviceClient) {
        this.serviceClient = serviceClient;
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
    public Mono<DeliveryReport> getDeliveryReport(String outgoingMessageId) {
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
    public Mono<Response<DeliveryReport>> getDeliveryReportWithResponse(String outgoingMessageId, Context context) {
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
                    return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(), (DeliveryReport) responseValue));
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
                            return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                                response.getHeaders(), deliveryReport));
                        }
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Failed to deserialize response", e));
                    }
                } else {
                    return Mono.error(new RuntimeException("Response value is null"));
                }
            });
    }
}
