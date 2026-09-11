// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue.implementation.util;

import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.queue.QueueMessageEncoding;
import com.azure.storage.queue.implementation.models.ListQueuesSegmentResponse;
import com.azure.storage.queue.implementation.models.MessageIdsUpdateHeaders;
import com.azure.storage.queue.implementation.models.PeekedMessageItemInternal;
import com.azure.storage.queue.implementation.models.QueueMessageItemInternal;
import com.azure.storage.queue.implementation.models.QueuesGetPropertiesHeaders;
import com.azure.storage.queue.implementation.models.QueueStorageExceptionInternal;
import com.azure.storage.queue.models.PeekedMessageItem;
import com.azure.storage.queue.models.QueueItem;
import com.azure.storage.queue.models.QueueMessageItem;
import com.azure.storage.queue.models.QueueProperties;
import com.azure.storage.queue.models.QueueStorageException;
import com.azure.storage.queue.models.UpdateMessageResult;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ModelHelper {
    private static final ClientLogger LOGGER = new ClientLogger(ModelHelper.class);

    private static BinaryData decodeMessageBody(String messageText, QueueMessageEncoding messageEncoding) {
        if (messageText == null) {
            return null;
        }
        switch (messageEncoding) {
            case NONE:
                return BinaryData.fromString(messageText);

            case BASE64:
                try {
                    return BinaryData.fromBytes(Base64.getDecoder().decode(messageText));
                } catch (IllegalArgumentException e) {
                    throw LOGGER.logExceptionAsError(e);
                }
            default:
                throw LOGGER.logExceptionAsError(
                    new IllegalArgumentException("Unsupported message encoding=" + messageEncoding));
        }
    }

    public static QueueMessageItem transformQueueMessageItemInternal(QueueMessageItemInternal queueMessageItemInternal,
        QueueMessageEncoding messageEncoding) {
        QueueMessageItem queueMessageItem = new QueueMessageItem().setMessageId(queueMessageItemInternal.getMessageId())
            .setDequeueCount(queueMessageItemInternal.getDequeueCount())
            .setExpirationTime(queueMessageItemInternal.getExpirationTime())
            .setInsertionTime(queueMessageItemInternal.getInsertionTime())
            .setPopReceipt(queueMessageItemInternal.getPopReceipt())
            .setTimeNextVisible(queueMessageItemInternal.getTimeNextVisible());
        BinaryData decodedMessageBody = decodeMessageBody(queueMessageItemInternal.getMessageText(), messageEncoding);
        if (decodedMessageBody != null) {
            queueMessageItem.setBody(decodedMessageBody);
        }
        return queueMessageItem;
    }

    public static PeekedMessageItem transformPeekedMessageItemInternal(
        PeekedMessageItemInternal peekedMessageItemInternal, QueueMessageEncoding messageEncoding) {
        PeekedMessageItem peekedMessageItem
            = new PeekedMessageItem().setMessageId(peekedMessageItemInternal.getMessageId())
                .setDequeueCount(peekedMessageItemInternal.getDequeueCount())
                .setExpirationTime(peekedMessageItemInternal.getExpirationTime())
                .setInsertionTime(peekedMessageItemInternal.getInsertionTime());
        BinaryData decodedMessage = decodeMessageBody(peekedMessageItemInternal.getMessageText(), messageEncoding);
        if (decodedMessage != null) {
            peekedMessageItem.setBody(decodedMessage);
        }
        return peekedMessageItem;
    }

    public static String encodeMessage(BinaryData message, QueueMessageEncoding messageEncoding) {
        Objects.requireNonNull(message, "'message' cannot be null.");
        switch (messageEncoding) {
            case NONE:
                return message.toString();

            case BASE64:
                return Base64.getEncoder().encodeToString(message.toBytes());

            default:
                throw LOGGER.logExceptionAsError(
                    new IllegalArgumentException("Unsupported message encoding=" + messageEncoding));
        }
    }

    public static QueueProperties transformQueueProperties(QueuesGetPropertiesHeaders propertiesHeaders) {
        Long count = propertiesHeaders.getApproximateMessagesCount();
        return new QueueProperties(propertiesHeaders.getMetadata(), count == null ? 0L : count);
    }

    public static UpdateMessageResult transformUpdateMessageResult(MessageIdsUpdateHeaders updateHeaders) {
        return new UpdateMessageResult(updateHeaders.getPopReceipt(), updateHeaders.getTimeNextVisible());
    }

    /**
     * Maps the internal exception to a public exception, if and only if {@code internal} is an instance of
     * {@link QueueStorageExceptionInternal} and it will be mapped to {@link QueueStorageException}.
     * <p>
     * The internal exception is required as the public exception was created using Object as the exception value. This
     * was incorrect and should have been a specific type that was XML deserializable. So, an internal exception was
     * added to handle this and we map that to the public exception, keeping the API the same.
     *
     * @param internal The internal exception.
     * @return The public exception.
     */
    public static QueueStorageException mapToQueueStorageException(QueueStorageExceptionInternal internal) {
        String code = internal.getValue() == null ? null : internal.getValue().getCode();
        String headerName = internal.getValue() == null ? null : internal.getValue().getHeaderName();
        return new QueueStorageException(StorageImplUtils.convertStorageExceptionMessage(internal.getMessage(),
            internal.getResponse(), code, headerName), internal.getResponse(), internal.getValue());
    }

    /**
     * Wire prefix for user-defined queue metadata headers. The generated protocol methods document a single
     * {@code x-ms-meta} header collection; on the wire each entry is emitted as {@code x-ms-meta-<key>}.
     */

    /**
     * Converts a {@code List Queues} response into a {@link PagedResponse} of {@link QueueItem}, preserving
     * the {@code NextMarker}-based continuation the hand-written paging depends on.
     * <p>
     * The service returns an empty {@code NextMarker} element on the final page. {@link com.azure.core.http.rest.PagedFlux}
     * / {@link com.azure.core.http.rest.PagedIterable} treat any non-null continuation token as "more pages available",
     * so an empty marker is normalized to {@code null} to terminate paging (mirroring the {@code len(NextMarker) > 0}
     * check the other language SDKs use).
     *
     * @param response The typed list response from {@code getQueuesWithResponse[Async]}.
     * @return The page of queue items with the continuation token populated from {@code NextMarker}.
     */
    public static PagedResponse<QueueItem> toQueueItemPage(Response<ListQueuesSegmentResponse> response) {
        ListQueuesSegmentResponse body = response.getValue();
        List<QueueItem> items = (body == null) ? Collections.emptyList() : body.getQueueItems();
        String nextMarker = (body == null) ? null : body.getNextMarker();
        String continuationToken = (nextMarker == null || nextMarker.isEmpty()) ? null : nextMarker;
        return new PagedResponseBase<Void, QueueItem>(response.getRequest(), response.getStatusCode(),
            response.getHeaders(), items, continuationToken, null);
    }

}
