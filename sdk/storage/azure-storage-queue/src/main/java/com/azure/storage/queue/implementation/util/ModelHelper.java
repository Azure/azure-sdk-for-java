// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue.implementation.util;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
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
import com.azure.xml.XmlReader;
import com.azure.xml.XmlSerializable;
import com.azure.xml.XmlWriter;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ModelHelper {
    private static final ClientLogger LOGGER = new ClientLogger(ModelHelper.class);

    private static final String X_MS_META_PREFIX = "x-ms-meta-";

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

    public static QueueProperties transformQueueProperties(HttpHeaders headers) {
        Map<String, String> metadata = new LinkedHashMap<>();
        for (HttpHeader header : headers) {
            String name = header.getName();
            if (name.regionMatches(true, 0, X_MS_META_PREFIX, 0, X_MS_META_PREFIX.length())) {
                metadata.put(name.substring(X_MS_META_PREFIX.length()), header.getValue());
            }
        }
        // The generated header model provides the typed approximate-messages-count; the metadata map is a dynamic
        // x-ms-meta-* collection the single-valued header model cannot represent, so it is still read manually.
        Long count = new QueuesGetPropertiesHeaders(headers).getApproximateMessagesCount();
        return new QueueProperties(metadata, count == null ? 0L : count);
    }

    public static UpdateMessageResult transformUpdateMessageResult(HttpHeaders headers) {
        MessageIdsUpdateHeaders updateHeaders = new MessageIdsUpdateHeaders(headers);
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
    private static final String METADATA_HEADER_PREFIX = "x-ms-meta-";

    /**
     * Translates a queue metadata map into {@code x-ms-meta-<key>} request headers on the supplied
     * {@link RequestOptions}. Replaces the {@code @HeaderCollection("x-ms-meta-")} binding the previous typed
     * implementation methods carried; {@code create} and {@code setMetadata} both delegate here.
     *
     * @param requestOptions The request options to mutate.
     * @param metadata The metadata to serialize, may be {@code null} or empty.
     */
    public static void addMetadataHeaders(RequestOptions requestOptions, java.util.Map<String, String> metadata) {
        if (metadata != null) {
            for (java.util.Map.Entry<String, String> entry : metadata.entrySet()) {
                requestOptions.addHeader(METADATA_HEADER_PREFIX + entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Serializes an {@link XmlSerializable} request body into XML {@link BinaryData} for the protocol layer.
     *
     * @param value The value to serialize, may be {@code null}.
     * @return The XML-encoded {@link BinaryData}, or {@code null} if {@code value} is {@code null}.
     */
    public static BinaryData serializeXmlBody(XmlSerializable<?> value) {
        if (value == null) {
            return null;
        }
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            try (XmlWriter xmlWriter = XmlWriter.toStream(stream)) {
                value.toXml(xmlWriter);
                xmlWriter.flush();
            }
            return BinaryData.fromBytes(stream.toByteArray());
        } catch (XMLStreamException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    /**
     * Deserializes an XML {@link BinaryData} protocol response using the provided reader function.
     *
     * @param data The XML response body, may be {@code null}.
     * @param deserializer The {@code fromXml} function of the target type.
     * @param <T> The deserialized type.
     * @return The deserialized value, or {@code null} if {@code data} is {@code null}.
     */
    public static <T> T deserializeXmlBody(BinaryData data, XmlDeserializer<T> deserializer) {
        if (data == null) {
            return null;
        }
        try (XmlReader xmlReader = XmlReader.fromBytes(data.toBytes())) {
            return deserializer.deserialize(xmlReader);
        } catch (XMLStreamException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    /**
     * Converts a raw {@code List Queues} protocol response into a {@link PagedResponse} of {@link QueueItem}, preserving
     * the {@code NextMarker}-based continuation the hand-written paging depends on.
     * <p>
     * The service returns an empty {@code NextMarker} element on the final page. {@link com.azure.core.http.rest.PagedFlux}
     * / {@link com.azure.core.http.rest.PagedIterable} treat any non-null continuation token as "more pages available",
     * so an empty marker is normalized to {@code null} to terminate paging (mirroring the {@code len(NextMarker) > 0}
     * check the other language SDKs use).
     *
     * @param response The raw XML list response from {@code getQueuesWithResponse[Async]}.
     * @return The page of queue items with the continuation token populated from {@code NextMarker}.
     */
    public static PagedResponse<QueueItem> toQueueItemPage(Response<BinaryData> response) {
        ListQueuesSegmentResponse body = deserializeXmlBody(response.getValue(), ListQueuesSegmentResponse::fromXml);
        List<QueueItem> items = (body == null) ? Collections.emptyList() : body.getQueueItems();
        String nextMarker = (body == null) ? null : body.getNextMarker();
        String continuationToken = (nextMarker == null || nextMarker.isEmpty()) ? null : nextMarker;
        return new PagedResponseBase<Void, QueueItem>(response.getRequest(), response.getStatusCode(),
            response.getHeaders(), items, continuationToken, null);
    }

    /**
     * Functional interface matching the generated {@code fromXml(XmlReader)} factory methods so protocol responses can
     * be deserialized generically.
     *
     * @param <T> The deserialized type.
     */
    @FunctionalInterface
    public interface XmlDeserializer<T> {
        /**
         * Reads an instance of {@code T} from the supplied {@link XmlReader}.
         *
         * @param reader The XML reader positioned at the response body.
         * @return The deserialized value.
         * @throws XMLStreamException If the XML is malformed.
         */
        T deserialize(XmlReader reader) throws XMLStreamException;
    }
}
