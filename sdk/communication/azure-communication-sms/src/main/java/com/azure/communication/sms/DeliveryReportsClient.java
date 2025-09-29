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
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

/**
 * Client for retrieving SMS delivery reports with Azure Communication SMS Services.
 */
@ServiceClient(builder = TelcoMessagingClientBuilder.class)
public final class DeliveryReportsClient {
    private final DeliveryReportsAsyncClient asyncClient;

    DeliveryReportsClient(DeliveryReportsAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    /**
     * Gets delivery report for a specific outgoing message.
     *
     * @param outgoingMessageId The identifier of the outgoing message.
     * @return The delivery report for the specified message.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DeliveryReport getDeliveryReport(String outgoingMessageId) {
        return asyncClient.getDeliveryReport(outgoingMessageId).block();
    }

    /**
     * Gets delivery report for a specific outgoing message.
     *
     * @param outgoingMessageId The identifier of the outgoing message.
     * @param context A {@link Context} representing the request context.
     * @return The delivery report for the specified message along with HTTP response information.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DeliveryReport> getDeliveryReportWithResponse(String outgoingMessageId, Context context) {
        return asyncClient.getDeliveryReportWithResponse(outgoingMessageId, context).block();
    }
}
