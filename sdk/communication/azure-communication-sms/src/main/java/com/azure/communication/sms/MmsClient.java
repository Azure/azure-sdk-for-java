// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.communication.sms.models.MmsAttachment;
import com.azure.communication.sms.models.MmsSendOptions;
import com.azure.communication.sms.models.MmsSendResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import java.util.List;

/**
 * Client for sending MMS messages with Azure Communication SMS Services.
 */
@ServiceClient(builder = MmsClientBuilder.class)
public final class MmsClient {
    private final MmsAsyncClient mmsAsyncClient;

    MmsClient(MmsAsyncClient mmsAsyncClient) {
        this.mmsAsyncClient = mmsAsyncClient;
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
    public MmsSendResult send(String from, String to, String message, List<MmsAttachment> attachments) {
        return mmsAsyncClient.send(from, to, message, attachments).block();
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
    public MmsSendResult send(String from, String to, String message, List<MmsAttachment> attachments, MmsSendOptions options) {
        return mmsAsyncClient.send(from, to, message, attachments, options).block();
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
    public Iterable<MmsSendResult> send(String from, Iterable<String> to, String message, List<MmsAttachment> attachments) {
        return mmsAsyncClient.send(from, to, message, attachments).block();
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
     * @param context A {@link Context} representing the request context
     * @return response for a successful send Mms request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Iterable<MmsSendResult>> sendWithResponse(String from, Iterable<String> to, String message, List<MmsAttachment> attachments, MmsSendOptions options, Context context) {
        return mmsAsyncClient.sendWithResponse(from, to, message, attachments, options, context).block();
    }
}
