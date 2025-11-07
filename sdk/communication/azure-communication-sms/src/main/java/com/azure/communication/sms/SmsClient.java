// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.communication.sms.implementation.models.DeliveryReport;
import com.azure.communication.sms.models.SmsSendOptions;
import com.azure.communication.sms.models.SmsSendResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

/**
 * Client for sending SMS messages with Azure Communication SMS Services.
 *
 * <p>
 * This client provides synchronous methods to send SMS messages, retrieve
 * delivery reports,
 * and manage opt-outs.
 * </p>
 *
 * <p>
 * <strong>Instantiating a synchronous Sms Client</strong>
 * </p>
 *
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
 * <p>
 * View {@link SmsClientBuilder} for additional ways to construct the client.
 * </p>
 *
 * @see SmsClientBuilder
 * @see SmsAsyncClient
 */
@ServiceClient(builder = SmsClientBuilder.class)
public final class SmsClient {
    private final SmsAsyncClient smsAsyncClient;
    private OptOutsClient optOutsClient;

    SmsClient(SmsAsyncClient smsAsyncClient) {
        this.smsAsyncClient = smsAsyncClient;
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
    public SmsSendResult send(String from, String to, String message) {
        return smsAsyncClient.send(from, to, message).block();
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
    public SmsSendResult send(String from, String to, String message, SmsSendOptions options) {
        return smsAsyncClient.send(from, to, message, options).block();
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
    public Iterable<SmsSendResult> send(String from, Iterable<String> to, String message) {
        return smsAsyncClient.send(from, to, message).block();
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
     * @param context A {@link Context} representing the request context
     * @return response for a successful send Sms request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Iterable<SmsSendResult>> sendWithResponse(String from, Iterable<String> to, String message,
        SmsSendOptions options, Context context) {
        return smsAsyncClient.sendWithResponse(from, to, message, options, context).block();
    }

    /**
     * Gets delivery report for a specific outgoing message.
     *
     * @param outgoingMessageId The identifier of the outgoing message.
     * @return The delivery report for the specified message.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException    thrown if the request is rejected by server.
     * @throws RuntimeException         all other wrapped checked exceptions if the
     *                                  request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DeliveryReport getDeliveryReport(String outgoingMessageId) {
        return smsAsyncClient.getDeliveryReport(outgoingMessageId).block();
    }

    /**
     * Gets delivery report for a specific outgoing message.
     *
     * @param outgoingMessageId The identifier of the outgoing message.
     * @param context           A {@link Context} representing the request context.
     * @return The delivery report for the specified message along with HTTP
     *         response information.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException    thrown if the request is rejected by server.
     * @throws RuntimeException         all other wrapped checked exceptions if the
     *                                  request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DeliveryReport> getDeliveryReportWithResponse(String outgoingMessageId, Context context) {
        return smsAsyncClient.getDeliveryReportWithResponse(outgoingMessageId, context).block();
    }

    /**
     * Gets a client to manage SMS opt-outs.
     *
     * <p>
     * The OptOutsClient provides methods to add, remove, and check opt-out status
     * for phone numbers.
     * </p>
     *
     * @return An {@link OptOutsClient} instance for managing opt-outs.
     */
    public OptOutsClient getOptOutsClient() {
        if (optOutsClient == null) {
            optOutsClient = new OptOutsClient(smsAsyncClient.getOptOutsAsyncClient());
        }
        return optOutsClient;
    }
}
