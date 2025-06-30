// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.persistent.implementation;

import com.azure.ai.agents.persistent.MessagesAsyncClient;
import com.azure.ai.agents.persistent.MessagesClient;
import com.azure.ai.agents.persistent.models.MessageAttachment;
import com.azure.ai.agents.persistent.models.MessageRole;
import com.azure.ai.agents.persistent.models.MessageTextAnnotation;
import com.azure.ai.agents.persistent.models.MessageTextContent;
import com.azure.ai.agents.persistent.models.ThreadMessage;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.tracing.Tracer;
import reactor.core.publisher.Mono;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tracer for the convenience methods in {@link MessagesClient} and
 * {@link MessagesAsyncClient}.
 * <p>
 * For more about the OTel semantic conventions this type enables, see
 * <a href="https://github.com/open-telemetry/semantic-conventions/blob/v1.27.0/docs/gen-ai">Gen AI semantic conventions</a>.
 * </p>
 */
public class MessagesClientTracer extends ClientTracer {

    private static final ClientLogger LOGGER = new ClientLogger(MessagesClientTracer.class);

    // Message-specific constants
    static final String GEN_AI_THREAD_ID_KEY = "gen_ai.thread.id";
    static final String GEN_AI_MESSAGE_ID_KEY = "gen_ai.message.id";
    static final String GEN_AI_RUN_ID_KEY = "gen_ai.thread.run.id";
    static final String GEN_AI_MESSAGE_STATUS_KEY = "gen_ai.message.status";
    static final String GEN_AI_MESSAGE_ROLE_KEY = "gen_ai.message.role";
    static final String OPERATION_CREATE_MESSAGE = "create_message";
    static final String OPERATION_LIST_MESSAGE = "list_messages";
    static final String EVENT_NAME_USER_MESSAGE = "gen_ai.user.message";
    static final String EVENT_NAME_ASSISTANT_MESSAGE = "gen_ai.assistant.message";

    /**
     * Creates MessagesClientTracer.
     *
     * @param endpoint the service endpoint.
     * @param configuration the {@link Configuration} instance to check if message content needs to be captured,
     *     if {@code null} is passed then {@link Configuration#getGlobalConfiguration()} will be used.
     * @param tracer the Tracer instance.
     */
    public MessagesClientTracer(String endpoint, Configuration configuration, Tracer tracer, Meter meter) {
        super(endpoint, configuration, tracer, meter);
    }

    //<editor-fold desc="Tracing CreateMessage">

    /**
     * Traces the synchronous convenience API - create message operation.
     *
     * @param threadId The ID of the thread to which the message will be added.
     * @param role The role of the message creator (user or assistant)
     * @param content The content of the message
     * @param attachments Message attachments if any
     * @param operation the operation performing the actual create message call.
     * @param requestOptions The requestOptions parameter for the {@code operation}.
     * @return persistent message created from the request.
     */
    public ThreadMessage traceCreateMessageSync(String threadId, MessageRole role, BinaryData content,
        List<MessageAttachment> attachments, Operation<ThreadMessage> operation, RequestOptions requestOptions) {

        return this.traceSyncOperation(OPERATION_CREATE_MESSAGE, operation, requestOptions, (span) -> {
            traceCreateMessageInvocationAttributes(threadId, role, content, attachments, span);
        }, this::traceCreateMessageResponseAttributes);
    }

    /**
     * Traces the asynchronous convenience API - create message operation.
     *
     * @param threadId The ID of the thread to which the message will be added.
     * @param role The role of the message creator (user or assistant)
     * @param content The content of the message
     * @param attachments Message attachments if any
     * @param operation the operation performing the actual create message call.
     * @param requestOptions The requestOptions parameter for the {@code operation}.
     * @return persistent message created from the request.
     */
    public Mono<ThreadMessage> traceCreateMessageAsync(String threadId, MessageRole role, BinaryData content,
        List<MessageAttachment> attachments, Operation<Mono<ThreadMessage>> operation, RequestOptions requestOptions) {

        return this.traceAsyncMonoOperation(OPERATION_CREATE_MESSAGE, operation, requestOptions, (span) -> {
            traceCreateMessageInvocationAttributes(threadId, role, content, attachments, span);
        }, (span, traceAttributes, result) -> result.flatMap(message -> {
            traceCreateMessageResponseAttributes(span, traceAttributes, message);
            return Mono.empty();
        }));
    }

    /**
     * Trace the attributes for a create message request.
     *
     * @param threadId The ID of the thread to which the message will be added.
     * @param role The role of the message creator (user or assistant)
     * @param content The content of the message
     * @param attachments Message attachments if any
     * @param span The current span context.
     */
    void traceCreateMessageInvocationAttributes(String threadId, MessageRole role, BinaryData content,
        List<MessageAttachment> attachments, Context span) {
        // Set request attributes
        this.setAttributeIfNotNull(GEN_AI_THREAD_ID_KEY, threadId, span);

        // Record role if present
        if (role != null) {
            this.setAttributeIfNotNull(GEN_AI_MESSAGE_ROLE_KEY, role.toString(), span);
        }

        // Record message content as an event if content capture is enabled
        if (traceContent) {
            Map<String, Object> eventAttributes = new HashMap<>();
            eventAttributes.put(GEN_AI_SYSTEM_KEY, GEN_AI_SYSTEM_VALUE);
            eventAttributes.put(GEN_AI_THREAD_ID_KEY, threadId);

            Map<String, Object> contentMap = new HashMap<>();
            putIfNotNullOrEmpty(contentMap, "content", content.toString());
            putIfNotNullOrEmpty(contentMap, "role", role.toString());

            // Add attachments if present
            if (attachments != null && !attachments.isEmpty()) {
                List<Map<String, Object>> attachmentsArray
                    = attachments.stream().map(this::attachmentToMap).collect(Collectors.toList());

                putIfNotNullOrEmpty(contentMap, "attachments", attachmentsArray);
            }

            String eventContent = toJsonString(contentMap);
            if (eventContent != null) {
                eventAttributes.put(GEN_AI_EVENT_CONTENT, eventContent);
                tracer.addEvent(EVENT_NAME_USER_MESSAGE, eventAttributes, null, span);
            }
        }
    }

    /**
     * Convert a MessageAttachment to a Map for JSON serialization.
     *
     * @param attachment The attachment to convert.
     * @return A Map representation of the attachment.
     */
    private Map<String, Object> attachmentToMap(MessageAttachment attachment) {
        Map<String, Object> attachmentMap = new HashMap<>();
        putIfNotNull(attachmentMap, "file_id", attachment.getFileId());

        if (attachment.getDataSource() != null) {
            Map<String, Object> dataSourceMap = new HashMap<>();
            putIfNotNull(dataSourceMap, "asset_identifier", attachment.getDataSource().getAssetIdentifier());
            putIfNotNull(dataSourceMap, "asset_type", attachment.getDataSource().getAssetType());
            attachmentMap.put("data_source", dataSourceMap);
        }

        putIfNotNullOrEmpty(attachmentMap, "tools", attachment.getTools());
        return attachmentMap;
    }

    /**
     * Record the response attributes from a create message operation.
     *
     * @param span The current span context.
     * @param message The persistent message created.
     */
    void traceCreateMessageResponseAttributes(Context span, Map<String, Object> traceAttributes,
        ThreadMessage message) {
        traceThreadMessage(span, traceAttributes, message);
    }
    //</editor-fold>

    //<editor-fold desc="Tracing ListMessages">

    /**
     * Traces the synchronous list messages operation.
     *
     * @param threadId The ID of the thread whose messages are being listed.
     * @param runId Optional run ID to filter by.
     * @param operation the operation performing the actual list messages call.
     * @param requestOptions The requestOptions parameter for the {@code operation}.
     * @return paged list of messages.
     */
    public PagedIterable<ThreadMessage> traceListMessagesSync(String threadId, String runId,
        Operation<PagedIterable<ThreadMessage>> operation, RequestOptions requestOptions) {

        return this.traceSyncOperation(OPERATION_LIST_MESSAGE, operation, requestOptions, (span) -> {
            traceListMessagesInvocationAttributes(span, threadId, runId);
        }, (span, attributes, result) -> {
            // For paged collections, we trace thread and run IDs since we can't access actual items yet
            traceListMessagesResponseAttributes(span, threadId, runId);
        });
    }

    /**
     * Traces the synchronous list messages operation.
     *
     * @param threadId The ID of the thread whose messages are being listed.
     * @param runId Optional run ID to filter by.
     * @param operation the operation performing the actual list messages call.
     * @param requestOptions The requestOptions parameter for the {@code operation}.
     * @return paged list of messages.
     */
    public PagedFlux<ThreadMessage> traceListMessagesAsync(String threadId, String runId,
        Operation<PagedFlux<ThreadMessage>> operation, RequestOptions requestOptions) {

        return this.traceAsyncFluxOperation(OPERATION_LIST_MESSAGE, operation, requestOptions, (span) -> {
            traceListMessagesInvocationAttributes(span, threadId, runId);
        }, (span, attributes, result) -> {
            traceListMessagesResponseAttributes(span, threadId, runId);
            return Mono.empty();
        });
    }

    /**
     * Trace the attributes for a list messages request.
     *
     * @param threadId The ID of the thread whose messages are being listed.
     * @param runId Optional run ID to filter by.
     * @param span The current span context.
     */
    void traceListMessagesInvocationAttributes(Context span, String threadId, String runId) {
        // Set request attributes
        this.setAttributeIfNotNull(GEN_AI_THREAD_ID_KEY, threadId, span);
        this.setAttributeIfNotNull(GEN_AI_RUN_ID_KEY, runId, span);
    }

    /**
     * Record the response attributes from a list messages operation.
     *
     * @param threadId The ID of the thread whose messages are being listed.
     * @param runId Optional run ID to filter by.
     * @param span The current span context.
     */
    void traceListMessagesResponseAttributes(Context span, String threadId, String runId) {
        // For paged collections, we mainly just include the thread and run IDs
        // The actual message details would be analyzed after pagination
        this.setAttributeIfNotNull(GEN_AI_THREAD_ID_KEY, threadId, span);
        this.setAttributeIfNotNull(GEN_AI_RUN_ID_KEY, runId, span);
    }
    //</editor-fold>

    protected void traceThreadMessage(Context span, Map<String, Object> traceAttributes, ThreadMessage message) {
        if (message == null) {
            return;
        }
        this.setAttributeIfNotNullOrEmpty(GEN_AI_MESSAGE_ID_KEY, message.getId(), span);
        this.setAttributeIfNotNullOrEmpty(GEN_AI_THREAD_ID_KEY, message.getThreadId(), span);
        this.setAttributeIfNotNull(GEN_AI_MESSAGE_STATUS_KEY, message.getStatus(), span);
        this.setAttributeIfNotNull(GEN_AI_MESSAGE_ROLE_KEY, message.getRole(), span);
        this.setAttributeIfNotNullOrEmpty(GEN_AI_RUN_ID_KEY, message.getRunId(), span);

        String eventName;
        switch (message.getRole().toString().toLowerCase()) {
            case "user":
                eventName = EVENT_NAME_USER_MESSAGE;
                break;

            case "assistant":
                eventName = EVENT_NAME_ASSISTANT_MESSAGE;
                break;

            default:
                eventName = "gen_ai." + message.getRole().toString().toLowerCase() + ".message";
                break;
        }

        Map<String, Object> eventBody = new HashMap<>();

        if (this.traceContent) {
            Map<String, Object> contentBody = new HashMap<>();
            if (message.getContent() != null) {
                message.getContent().forEach(contentItem -> {
                    if (contentItem instanceof MessageTextContent) {
                        MessageTextContent textContent = (MessageTextContent) contentItem;
                        Map<String, Object> contentDetails = new HashMap<>();
                        contentDetails.put("value", textContent.getText());

                        if (textContent.getText() != null
                            && textContent.getText().getAnnotations() != null
                            && !textContent.getText().getAnnotations().isEmpty()) {
                            contentDetails.put("annotations",
                                textContent.getText()
                                    .getAnnotations()
                                    .stream()
                                    .map(MessageTextAnnotation::getText)
                                    .collect(java.util.stream.Collectors.joining(", ")));
                        }

                        contentBody.put("text", contentDetails);
                    }
                });
            }
            eventBody.put("content", contentBody);
        }

        if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
            List<Map<String, Object>> attachmentList = message.getAttachments().stream().map(attachment -> {
                Map<String, Object> attachmentBody = new HashMap<>();
                attachmentBody.put("id", attachment.getFileId());

                if (attachment.getTools() != null && !attachment.getTools().isEmpty()) {
                    attachmentBody.put("tools",
                        attachment.getTools()
                            .stream()
                            .map(Object::toString)
                            .collect(java.util.stream.Collectors.toList()));
                }
                return attachmentBody;
            }).collect(java.util.stream.Collectors.toList());

            eventBody.put("attachments", attachmentList);
        }

        if (message.getIncompleteDetails() != null) {
            eventBody.put("incomplete_details", message.getIncompleteDetails());
        }

        eventBody.put("role", message.getRole().toString());

        String serializedEventBody = toJsonString(eventBody);

        Map<String, Object> attributes = new HashMap<>(traceAttributes);
        attributes.put(GEN_AI_SYSTEM_KEY, GEN_AI_SYSTEM_VALUE);
        putIfNotNullOrEmpty(attributes, GEN_AI_THREAD_ID_KEY, message.getThreadId());
        putIfNotNullOrEmpty(attributes, GEN_AI_MESSAGE_ID_KEY, message.getId());
        putIfNotNullOrEmpty(attributes, GEN_AI_RUN_ID_KEY, message.getRunId());
        putIfNotNull(attributes, GEN_AI_MESSAGE_STATUS_KEY, message.getStatus());

        attributes.put(GEN_AI_EVENT_CONTENT, serializedEventBody);

        tracer.addEvent(eventName, attributes, null, span);
    }
}
