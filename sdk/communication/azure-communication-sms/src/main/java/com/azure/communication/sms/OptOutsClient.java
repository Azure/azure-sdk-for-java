// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.communication.sms.models.OptOutResult;
import com.azure.communication.sms.models.OptOutCheckResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import java.util.List;

/**
 * Client for managing SMS opt-outs with Azure Communication SMS Services.
 */
@ServiceClient(builder = SmsClientBuilder.class)
public final class OptOutsClient {
    private final OptOutsAsyncClient asyncClient;

    OptOutsClient(OptOutsAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    /**
     * Add phone numbers to the opt-outs list which shall stop receiving messages
     * from a sender number.
     *
     * @param from The sender's identifier (typically phone number in E.164 format).
     * @param to   The recipient's phone number.
     * @return A list of opt-out results.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public List<OptOutResult> addOptOut(String from, String to) {
        return asyncClient.addOptOut(from, to).block();
    }

    /**
     * Add phone numbers to the opt-outs list which shall stop receiving messages
     * from a sender number.
     *
     * @param from The sender's identifier (typically phone number in E.164 format).
     * @param to   A list of the recipient's phone numbers.
     * @return A list of opt-out results.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public List<OptOutResult> addOptOut(String from, Iterable<String> to) {
        return asyncClient.addOptOut(from, to).block();
    }

    /**
     * Add phone numbers to the opt-outs list which shall stop receiving messages
     * from a sender number.
     *
     * @param from    The sender's identifier (typically phone number in E.164
     *                format).
     * @param to      A list of the recipient's phone numbers.
     * @param context A {@link Context} representing the request context.
     * @return A list of opt-out results with HTTP response information.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<List<OptOutResult>> addOptOutWithResponse(String from, Iterable<String> to, Context context) {
        return asyncClient.addOptOutWithResponse(from, to, context).block();
    }

    /**
     * Remove phone numbers from the opt-outs list.
     *
     * @param from The sender's identifier (typically phone number in E.164 format).
     * @param to   The recipient's phone number.
     * @return A list of opt-out results.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public List<OptOutResult> removeOptOut(String from, String to) {
        return asyncClient.removeOptOut(from, to).block();
    }

    /**
     * Remove phone numbers from the opt-outs list.
     *
     * @param from The sender's identifier (typically phone number in E.164 format).
     * @param to   A list of the recipient's phone numbers.
     * @return A list of opt-out results.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public List<OptOutResult> removeOptOut(String from, Iterable<String> to) {
        return asyncClient.removeOptOut(from, to).block();
    }

    /**
     * Remove phone numbers from the opt-outs list.
     *
     * @param from    The sender's identifier (typically phone number in E.164
     *                format).
     * @param to      A list of the recipient's phone numbers.
     * @param context A {@link Context} representing the request context.
     * @return A list of opt-out results with HTTP response information.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<List<OptOutResult>> removeOptOutWithResponse(String from, Iterable<String> to, Context context) {
        return asyncClient.removeOptOutWithResponse(from, to, context).block();
    }

    /**
     * Check the opt-out status for recipient phone numbers with a sender phone
     * number.
     *
     * @param from The sender's identifier (typically phone number in E.164 format).
     * @param to   The recipient's phone number.
     * @return A list of opt-out check results.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public List<OptOutCheckResult> checkOptOut(String from, String to) {
        return asyncClient.checkOptOut(from, to).block();
    }

    /**
     * Check the opt-out status for recipient phone numbers with a sender phone
     * number.
     *
     * @param from The sender's identifier (typically phone number in E.164 format).
     * @param to   A list of the recipient's phone numbers.
     * @return A list of opt-out check results.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public List<OptOutCheckResult> checkOptOut(String from, Iterable<String> to) {
        return asyncClient.checkOptOut(from, to).block();
    }

    /**
     * Check the opt-out status for recipient phone numbers with a sender phone
     * number.
     *
     * @param from    The sender's identifier (typically phone number in E.164
     *                format).
     * @param to      A list of the recipient's phone numbers.
     * @param context A {@link Context} representing the request context.
     * @return A list of opt-out check results with HTTP response information.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<List<OptOutCheckResult>> checkOptOutWithResponse(String from, Iterable<String> to,
        Context context) {
        return asyncClient.checkOptOutWithResponse(from, to, context).block();
    }
}
