// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.servicebus.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.servicebus.implementation.DurationSerializer;
import com.azure.resourcemanager.servicebus.models.EntityStatus;
import com.azure.resourcemanager.servicebus.models.MessageCountDetails;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * The Queue Properties definition.
 */
@Fluent
public final class SBQueueProperties implements JsonSerializable<SBQueueProperties> {
    /*
     * Message Count Details.
     */
    private MessageCountDetails countDetails;

    /*
     * The exact time the message was created.
     */
    private OffsetDateTime createdAt;

    /*
     * The exact time the message was updated.
     */
    private OffsetDateTime updatedAt;

    /*
     * Last time a message was sent, or the last time there was a receive request to this queue.
     */
    private OffsetDateTime accessedAt;

    /*
     * The size of the queue, in bytes.
     */
    private Long sizeInBytes;

    /*
     * The number of messages in the queue.
     */
    private Long messageCount;

    /*
     * ISO 8601 timespan duration of a peek-lock; that is, the amount of time that the message is locked for other
     * receivers. The maximum value for LockDuration is 5 minutes; the default value is 1 minute.
     */
    private Duration lockDuration;

    /*
     * The maximum size of the queue in megabytes, which is the size of memory allocated for the queue. Default is 1024.
     */
    private Integer maxSizeInMegabytes;

    /*
     * Maximum size (in KB) of the message payload that can be accepted by the queue. This property is only used in
     * Premium today and default is 1024.
     */
    private Long maxMessageSizeInKilobytes;

    /*
     * A value indicating if this queue requires duplicate detection.
     */
    private Boolean requiresDuplicateDetection;

    /*
     * A value that indicates whether the queue supports the concept of sessions.
     */
    private Boolean requiresSession;

    /*
     * ISO 8601 default message timespan to live value. This is the duration after which the message expires, starting
     * from when the message is sent to Service Bus. This is the default value used when TimeToLive is not set on a
     * message itself.
     */
    private Duration defaultMessageTimeToLive;

    /*
     * A value that indicates whether this queue has dead letter support when a message expires.
     */
    private Boolean deadLetteringOnMessageExpiration;

    /*
     * ISO 8601 timeSpan structure that defines the duration of the duplicate detection history. The default value is 10
     * minutes.
     */
    private Duration duplicateDetectionHistoryTimeWindow;

    /*
     * The maximum delivery count. A message is automatically deadlettered after this number of deliveries. default
     * value is 10.
     */
    private Integer maxDeliveryCount;

    /*
     * Enumerates the possible values for the status of a messaging entity.
     */
    private EntityStatus status;

    /*
     * Value that indicates whether server-side batched operations are enabled.
     */
    private Boolean enableBatchedOperations;

    /*
     * ISO 8061 timeSpan idle interval after which the queue is automatically deleted. The minimum duration is 5
     * minutes.
     */
    private Duration autoDeleteOnIdle;

    /*
     * A value that indicates whether the queue is to be partitioned across multiple message brokers.
     */
    private Boolean enablePartitioning;

    /*
     * A value that indicates whether Express Entities are enabled. An express queue holds a message in memory
     * temporarily before writing it to persistent storage.
     */
    private Boolean enableExpress;

    /*
     * Queue/Topic name to forward the messages
     */
    private String forwardTo;

    /*
     * Queue/Topic name to forward the Dead Letter message
     */
    private String forwardDeadLetteredMessagesTo;

    /**
     * Creates an instance of SBQueueProperties class.
     */
    public SBQueueProperties() {
    }

    /**
     * Get the countDetails property: Message Count Details.
     * 
     * @return the countDetails value.
     */
    public MessageCountDetails countDetails() {
        return this.countDetails;
    }

    /**
     * Get the createdAt property: The exact time the message was created.
     * 
     * @return the createdAt value.
     */
    public OffsetDateTime createdAt() {
        return this.createdAt;
    }

    /**
     * Get the updatedAt property: The exact time the message was updated.
     * 
     * @return the updatedAt value.
     */
    public OffsetDateTime updatedAt() {
        return this.updatedAt;
    }

    /**
     * Get the accessedAt property: Last time a message was sent, or the last time there was a receive request to this
     * queue.
     * 
     * @return the accessedAt value.
     */
    public OffsetDateTime accessedAt() {
        return this.accessedAt;
    }

    /**
     * Get the sizeInBytes property: The size of the queue, in bytes.
     * 
     * @return the sizeInBytes value.
     */
    public Long sizeInBytes() {
        return this.sizeInBytes;
    }

    /**
     * Get the messageCount property: The number of messages in the queue.
     * 
     * @return the messageCount value.
     */
    public Long messageCount() {
        return this.messageCount;
    }

    /**
     * Get the lockDuration property: ISO 8601 timespan duration of a peek-lock; that is, the amount of time that the
     * message is locked for other receivers. The maximum value for LockDuration is 5 minutes; the default value is 1
     * minute.
     * 
     * @return the lockDuration value.
     */
    public Duration lockDuration() {
        return this.lockDuration;
    }

    /**
     * Set the lockDuration property: ISO 8601 timespan duration of a peek-lock; that is, the amount of time that the
     * message is locked for other receivers. The maximum value for LockDuration is 5 minutes; the default value is 1
     * minute.
     * 
     * @param lockDuration the lockDuration value to set.
     * @return the SBQueueProperties object itself.
     */
    public SBQueueProperties withLockDuration(Duration lockDuration) {
        this.lockDuration = lockDuration;
        return this;
    }

    /**
     * Get the maxSizeInMegabytes property: The maximum size of the queue in megabytes, which is the size of memory
     * allocated for the queue. Default is 1024.
     * 
     * @return the maxSizeInMegabytes value.
     */
    public Integer maxSizeInMegabytes() {
        return this.maxSizeInMegabytes;
    }

    /**
     * Set the maxSizeInMegabytes property: The maximum size of the queue in megabytes, which is the size of memory
     * allocated for the queue. Default is 1024.
     * 
     * @param maxSizeInMegabytes the maxSizeInMegabytes value to set.
     * @return the SBQueueProperties object itself.
     */
    public SBQueueProperties withMaxSizeInMegabytes(Integer maxSizeInMegabytes) {
        this.maxSizeInMegabytes = maxSizeInMegabytes;
        return this;
    }

    /**
     * Get the maxMessageSizeInKilobytes property: Maximum size (in KB) of the message payload that can be accepted by
     * the queue. This property is only used in Premium today and default is 1024.
     * 
     * @return the maxMessageSizeInKilobytes value.
     */
    public Long maxMessageSizeInKilobytes() {
        return this.maxMessageSizeInKilobytes;
    }

    /**
     * Set the maxMessageSizeInKilobytes property: Maximum size (in KB) of the message payload that can be accepted by
     * the queue. This property is only used in Premium today and default is 1024.
     * 
     * @param maxMessageSizeInKilobytes the maxMessageSizeInKilobytes value to set.
     * @return the SBQueueProperties object itself.
     */
    public SBQueueProperties withMaxMessageSizeInKilobytes(Long maxMessageSizeInKilobytes) {
        this.maxMessageSizeInKilobytes = maxMessageSizeInKilobytes;
        return this;
    }

    /**
     * Get the requiresDuplicateDetection property: A value indicating if this queue requires duplicate detection.
     * 
     * @return the requiresDuplicateDetection value.
     */
    public Boolean requiresDuplicateDetection() {
        return this.requiresDuplicateDetection;
    }

    /**
     * Set the requiresDuplicateDetection property: A value indicating if this queue requires duplicate detection.
     * 
     * @param requiresDuplicateDetection the requiresDuplicateDetection value to set.
     * @return the SBQueueProperties object itself.
     */
    public SBQueueProperties withRequiresDuplicateDetection(Boolean requiresDuplicateDetection) {
        this.requiresDuplicateDetection = requiresDuplicateDetection;
        return this;
    }

    /**
     * Get the requiresSession property: A value that indicates whether the queue supports the concept of sessions.
     * 
     * @return the requiresSession value.
     */
    public Boolean requiresSession() {
        return this.requiresSession;
    }

    /**
     * Set the requiresSession property: A value that indicates whether the queue supports the concept of sessions.
     * 
     * @param requiresSession the requiresSession value to set.
     * @return the SBQueueProperties object itself.
     */
    public SBQueueProperties withRequiresSession(Boolean requiresSession) {
        this.requiresSession = requiresSession;
        return this;
    }

    /**
     * Get the defaultMessageTimeToLive property: ISO 8601 default message timespan to live value. This is the duration
     * after which the message expires, starting from when the message is sent to Service Bus. This is the default value
     * used when TimeToLive is not set on a message itself.
     * 
     * @return the defaultMessageTimeToLive value.
     */
    public Duration defaultMessageTimeToLive() {
        return this.defaultMessageTimeToLive;
    }

    /**
     * Set the defaultMessageTimeToLive property: ISO 8601 default message timespan to live value. This is the duration
     * after which the message expires, starting from when the message is sent to Service Bus. This is the default value
     * used when TimeToLive is not set on a message itself.
     * 
     * @param defaultMessageTimeToLive the defaultMessageTimeToLive value to set.
     * @return the SBQueueProperties object itself.
     */
    public SBQueueProperties withDefaultMessageTimeToLive(Duration defaultMessageTimeToLive) {
        this.defaultMessageTimeToLive = defaultMessageTimeToLive;
        return this;
    }

    /**
     * Get the deadLetteringOnMessageExpiration property: A value that indicates whether this queue has dead letter
     * support when a message expires.
     * 
     * @return the deadLetteringOnMessageExpiration value.
     */
    public Boolean deadLetteringOnMessageExpiration() {
        return this.deadLetteringOnMessageExpiration;
    }

    /**
     * Set the deadLetteringOnMessageExpiration property: A value that indicates whether this queue has dead letter
     * support when a message expires.
     * 
     * @param deadLetteringOnMessageExpiration the deadLetteringOnMessageExpiration value to set.
     * @return the SBQueueProperties object itself.
     */
    public SBQueueProperties withDeadLetteringOnMessageExpiration(Boolean deadLetteringOnMessageExpiration) {
        this.deadLetteringOnMessageExpiration = deadLetteringOnMessageExpiration;
        return this;
    }

    /**
     * Get the duplicateDetectionHistoryTimeWindow property: ISO 8601 timeSpan structure that defines the duration of
     * the duplicate detection history. The default value is 10 minutes.
     * 
     * @return the duplicateDetectionHistoryTimeWindow value.
     */
    public Duration duplicateDetectionHistoryTimeWindow() {
        return this.duplicateDetectionHistoryTimeWindow;
    }

    /**
     * Set the duplicateDetectionHistoryTimeWindow property: ISO 8601 timeSpan structure that defines the duration of
     * the duplicate detection history. The default value is 10 minutes.
     * 
     * @param duplicateDetectionHistoryTimeWindow the duplicateDetectionHistoryTimeWindow value to set.
     * @return the SBQueueProperties object itself.
     */
    public SBQueueProperties withDuplicateDetectionHistoryTimeWindow(Duration duplicateDetectionHistoryTimeWindow) {
        this.duplicateDetectionHistoryTimeWindow = duplicateDetectionHistoryTimeWindow;
        return this;
    }

    /**
     * Get the maxDeliveryCount property: The maximum delivery count. A message is automatically deadlettered after this
     * number of deliveries. default value is 10.
     * 
     * @return the maxDeliveryCount value.
     */
    public Integer maxDeliveryCount() {
        return this.maxDeliveryCount;
    }

    /**
     * Set the maxDeliveryCount property: The maximum delivery count. A message is automatically deadlettered after this
     * number of deliveries. default value is 10.
     * 
     * @param maxDeliveryCount the maxDeliveryCount value to set.
     * @return the SBQueueProperties object itself.
     */
    public SBQueueProperties withMaxDeliveryCount(Integer maxDeliveryCount) {
        this.maxDeliveryCount = maxDeliveryCount;
        return this;
    }

    /**
     * Get the status property: Enumerates the possible values for the status of a messaging entity.
     * 
     * @return the status value.
     */
    public EntityStatus status() {
        return this.status;
    }

    /**
     * Set the status property: Enumerates the possible values for the status of a messaging entity.
     * 
     * @param status the status value to set.
     * @return the SBQueueProperties object itself.
     */
    public SBQueueProperties withStatus(EntityStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Get the enableBatchedOperations property: Value that indicates whether server-side batched operations are
     * enabled.
     * 
     * @return the enableBatchedOperations value.
     */
    public Boolean enableBatchedOperations() {
        return this.enableBatchedOperations;
    }

    /**
     * Set the enableBatchedOperations property: Value that indicates whether server-side batched operations are
     * enabled.
     * 
     * @param enableBatchedOperations the enableBatchedOperations value to set.
     * @return the SBQueueProperties object itself.
     */
    public SBQueueProperties withEnableBatchedOperations(Boolean enableBatchedOperations) {
        this.enableBatchedOperations = enableBatchedOperations;
        return this;
    }

    /**
     * Get the autoDeleteOnIdle property: ISO 8061 timeSpan idle interval after which the queue is automatically
     * deleted. The minimum duration is 5 minutes.
     * 
     * @return the autoDeleteOnIdle value.
     */
    public Duration autoDeleteOnIdle() {
        return this.autoDeleteOnIdle;
    }

    /**
     * Set the autoDeleteOnIdle property: ISO 8061 timeSpan idle interval after which the queue is automatically
     * deleted. The minimum duration is 5 minutes.
     * 
     * @param autoDeleteOnIdle the autoDeleteOnIdle value to set.
     * @return the SBQueueProperties object itself.
     */
    public SBQueueProperties withAutoDeleteOnIdle(Duration autoDeleteOnIdle) {
        this.autoDeleteOnIdle = autoDeleteOnIdle;
        return this;
    }

    /**
     * Get the enablePartitioning property: A value that indicates whether the queue is to be partitioned across
     * multiple message brokers.
     * 
     * @return the enablePartitioning value.
     */
    public Boolean enablePartitioning() {
        return this.enablePartitioning;
    }

    /**
     * Set the enablePartitioning property: A value that indicates whether the queue is to be partitioned across
     * multiple message brokers.
     * 
     * @param enablePartitioning the enablePartitioning value to set.
     * @return the SBQueueProperties object itself.
     */
    public SBQueueProperties withEnablePartitioning(Boolean enablePartitioning) {
        this.enablePartitioning = enablePartitioning;
        return this;
    }

    /**
     * Get the enableExpress property: A value that indicates whether Express Entities are enabled. An express queue
     * holds a message in memory temporarily before writing it to persistent storage.
     * 
     * @return the enableExpress value.
     */
    public Boolean enableExpress() {
        return this.enableExpress;
    }

    /**
     * Set the enableExpress property: A value that indicates whether Express Entities are enabled. An express queue
     * holds a message in memory temporarily before writing it to persistent storage.
     * 
     * @param enableExpress the enableExpress value to set.
     * @return the SBQueueProperties object itself.
     */
    public SBQueueProperties withEnableExpress(Boolean enableExpress) {
        this.enableExpress = enableExpress;
        return this;
    }

    /**
     * Get the forwardTo property: Queue/Topic name to forward the messages.
     * 
     * @return the forwardTo value.
     */
    public String forwardTo() {
        return this.forwardTo;
    }

    /**
     * Set the forwardTo property: Queue/Topic name to forward the messages.
     * 
     * @param forwardTo the forwardTo value to set.
     * @return the SBQueueProperties object itself.
     */
    public SBQueueProperties withForwardTo(String forwardTo) {
        this.forwardTo = forwardTo;
        return this;
    }

    /**
     * Get the forwardDeadLetteredMessagesTo property: Queue/Topic name to forward the Dead Letter message.
     * 
     * @return the forwardDeadLetteredMessagesTo value.
     */
    public String forwardDeadLetteredMessagesTo() {
        return this.forwardDeadLetteredMessagesTo;
    }

    /**
     * Set the forwardDeadLetteredMessagesTo property: Queue/Topic name to forward the Dead Letter message.
     * 
     * @param forwardDeadLetteredMessagesTo the forwardDeadLetteredMessagesTo value to set.
     * @return the SBQueueProperties object itself.
     */
    public SBQueueProperties withForwardDeadLetteredMessagesTo(String forwardDeadLetteredMessagesTo) {
        this.forwardDeadLetteredMessagesTo = forwardDeadLetteredMessagesTo;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (countDetails() != null) {
            countDetails().validate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("lockDuration", DurationSerializer.serialize(this.lockDuration));
        jsonWriter.writeNumberField("maxSizeInMegabytes", this.maxSizeInMegabytes);
        jsonWriter.writeNumberField("maxMessageSizeInKilobytes", this.maxMessageSizeInKilobytes);
        jsonWriter.writeBooleanField("requiresDuplicateDetection", this.requiresDuplicateDetection);
        jsonWriter.writeBooleanField("requiresSession", this.requiresSession);
        jsonWriter.writeStringField("defaultMessageTimeToLive",
            DurationSerializer.serialize(this.defaultMessageTimeToLive));
        jsonWriter.writeBooleanField("deadLetteringOnMessageExpiration", this.deadLetteringOnMessageExpiration);
        jsonWriter.writeStringField("duplicateDetectionHistoryTimeWindow",
            DurationSerializer.serialize(this.duplicateDetectionHistoryTimeWindow));
        jsonWriter.writeNumberField("maxDeliveryCount", this.maxDeliveryCount);
        jsonWriter.writeStringField("status", this.status == null ? null : this.status.toString());
        jsonWriter.writeBooleanField("enableBatchedOperations", this.enableBatchedOperations);
        jsonWriter.writeStringField("autoDeleteOnIdle", DurationSerializer.serialize(this.autoDeleteOnIdle));
        jsonWriter.writeBooleanField("enablePartitioning", this.enablePartitioning);
        jsonWriter.writeBooleanField("enableExpress", this.enableExpress);
        jsonWriter.writeStringField("forwardTo", this.forwardTo);
        jsonWriter.writeStringField("forwardDeadLetteredMessagesTo", this.forwardDeadLetteredMessagesTo);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of SBQueueProperties from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of SBQueueProperties if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the SBQueueProperties.
     */
    public static SBQueueProperties fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            SBQueueProperties deserializedSBQueueProperties = new SBQueueProperties();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("countDetails".equals(fieldName)) {
                    deserializedSBQueueProperties.countDetails = MessageCountDetails.fromJson(reader);
                } else if ("createdAt".equals(fieldName)) {
                    deserializedSBQueueProperties.createdAt = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else if ("updatedAt".equals(fieldName)) {
                    deserializedSBQueueProperties.updatedAt = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else if ("accessedAt".equals(fieldName)) {
                    deserializedSBQueueProperties.accessedAt = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else if ("sizeInBytes".equals(fieldName)) {
                    deserializedSBQueueProperties.sizeInBytes = reader.getNullable(JsonReader::getLong);
                } else if ("messageCount".equals(fieldName)) {
                    deserializedSBQueueProperties.messageCount = reader.getNullable(JsonReader::getLong);
                } else if ("lockDuration".equals(fieldName)) {
                    deserializedSBQueueProperties.lockDuration
                        = reader.getNullable(nonNullReader -> Duration.parse(nonNullReader.getString()));
                } else if ("maxSizeInMegabytes".equals(fieldName)) {
                    deserializedSBQueueProperties.maxSizeInMegabytes = reader.getNullable(JsonReader::getInt);
                } else if ("maxMessageSizeInKilobytes".equals(fieldName)) {
                    deserializedSBQueueProperties.maxMessageSizeInKilobytes = reader.getNullable(JsonReader::getLong);
                } else if ("requiresDuplicateDetection".equals(fieldName)) {
                    deserializedSBQueueProperties.requiresDuplicateDetection
                        = reader.getNullable(JsonReader::getBoolean);
                } else if ("requiresSession".equals(fieldName)) {
                    deserializedSBQueueProperties.requiresSession = reader.getNullable(JsonReader::getBoolean);
                } else if ("defaultMessageTimeToLive".equals(fieldName)) {
                    deserializedSBQueueProperties.defaultMessageTimeToLive
                        = reader.getNullable(nonNullReader -> Duration.parse(nonNullReader.getString()));
                } else if ("deadLetteringOnMessageExpiration".equals(fieldName)) {
                    deserializedSBQueueProperties.deadLetteringOnMessageExpiration
                        = reader.getNullable(JsonReader::getBoolean);
                } else if ("duplicateDetectionHistoryTimeWindow".equals(fieldName)) {
                    deserializedSBQueueProperties.duplicateDetectionHistoryTimeWindow
                        = reader.getNullable(nonNullReader -> Duration.parse(nonNullReader.getString()));
                } else if ("maxDeliveryCount".equals(fieldName)) {
                    deserializedSBQueueProperties.maxDeliveryCount = reader.getNullable(JsonReader::getInt);
                } else if ("status".equals(fieldName)) {
                    deserializedSBQueueProperties.status = EntityStatus.fromString(reader.getString());
                } else if ("enableBatchedOperations".equals(fieldName)) {
                    deserializedSBQueueProperties.enableBatchedOperations = reader.getNullable(JsonReader::getBoolean);
                } else if ("autoDeleteOnIdle".equals(fieldName)) {
                    deserializedSBQueueProperties.autoDeleteOnIdle
                        = reader.getNullable(nonNullReader -> Duration.parse(nonNullReader.getString()));
                } else if ("enablePartitioning".equals(fieldName)) {
                    deserializedSBQueueProperties.enablePartitioning = reader.getNullable(JsonReader::getBoolean);
                } else if ("enableExpress".equals(fieldName)) {
                    deserializedSBQueueProperties.enableExpress = reader.getNullable(JsonReader::getBoolean);
                } else if ("forwardTo".equals(fieldName)) {
                    deserializedSBQueueProperties.forwardTo = reader.getString();
                } else if ("forwardDeadLetteredMessagesTo".equals(fieldName)) {
                    deserializedSBQueueProperties.forwardDeadLetteredMessagesTo = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedSBQueueProperties;
        });
    }
}
