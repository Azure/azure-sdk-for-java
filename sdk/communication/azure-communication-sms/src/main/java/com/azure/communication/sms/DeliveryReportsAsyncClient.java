// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.communication.sms.implementation.AzureCommunicationSMSServiceImpl;
import com.azure.communication.sms.implementation.models.DeliveryReport;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import reactor.core.publisher.Mono;

/**
 * Async client for retrieving SMS delivery reports with Azure Communication SMS Services.
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
     * @return The delivery report for the specified message on successful completion of {@link Mono}.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DeliveryReport> getDeliveryReport(String outgoingMessageId) {
        if (outgoingMessageId == null) {
            return Mono.error(new IllegalArgumentException("'outgoingMessageId' cannot be null."));
        }
        return getDeliveryReportWithResponse(outgoingMessageId, Context.NONE)
            .flatMap(response -> Mono.justOrEmpty(response.getValue()));
    }

    /**
     * Gets delivery report for a specific outgoing message.
     *
     * @param outgoingMessageId The identifier of the outgoing message.
     * @param context A {@link Context} representing the request context.
     * @return The delivery report for the specified message along with HTTP response information on successful
     *         completion of {@link Mono}.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DeliveryReport>> getDeliveryReportWithResponse(String outgoingMessageId, Context context) {
        if (outgoingMessageId == null) {
            return Mono.error(new IllegalArgumentException("'outgoingMessageId' cannot be null."));
        }
        return serviceClient.getDeliveryReports().getWithResponseAsync(outgoingMessageId, context).map(response -> {
            Object responseValue = response.getValue();
            if (responseValue instanceof DeliveryReport) {
                return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                    (DeliveryReport) responseValue);
            } else {
                // Handle the case where the response is not a DeliveryReport
                throw new RuntimeException("Unexpected response type: " + responseValue.getClass().getName());
            }
        });
    }
}
