// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed.implementation.models;

import com.azure.storage.blob.changefeed.models.BlobChangefeedEvent;
import com.azure.storage.blob.changefeed.models.BlobChangefeedEventData;
import com.azure.storage.blob.changefeed.models.BlobChangefeedEventType;
import com.azure.storage.internal.avro.implementation.AvroConstants;
import com.azure.storage.internal.avro.implementation.schema.AvroSchema;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * This class contains properties of a BlobChangefeedEvent.
 */
public class InternalBlobChangefeedEvent implements BlobChangefeedEvent {

    private final String topic;
    private final String subject;
    private final BlobChangefeedEventType eventType;
    private final OffsetDateTime eventTime;
    private final String id;
    private final com.azure.storage.blob.changefeed.models.BlobChangefeedEventData data;
    private final Long dataVersion;
    private final String metadataVersion;

    /**
     * Constructs a {@link InternalBlobChangefeedEvent}.
     *
     * @param topic The topic.
     * @param subject The subject.
     * @param eventType {@link BlobChangefeedEventType}
     * @param eventTime The {@link OffsetDateTime event time}.
     * @param id The identifier.
     * @param data {@link InternalBlobChangefeedEventData}
     * @param dataVersion The data version.
     * @param metadataVersion The metadata version.
     */
    public InternalBlobChangefeedEvent(String topic, String subject, BlobChangefeedEventType eventType,
        OffsetDateTime eventTime, String id, BlobChangefeedEventData data,
        Long dataVersion, String metadataVersion) {
        this.topic = topic;
        this.subject = subject;
        this.eventType = eventType;
        this.eventTime = eventTime;
        this.id = id;
        this.data = data;
        this.dataVersion = dataVersion;
        this.metadataVersion = metadataVersion;
    }

    /**
     * Constructs a {@link InternalBlobChangefeedEvent}.
     *
     * @param record The record.
     * @return The {@link InternalBlobChangefeedEvent} representing the record.
     * @throws IllegalArgumentException if the record is not valid.
     */
    public static InternalBlobChangefeedEvent fromRecord(Object record) {
        AvroSchema.checkType("r", record, Map.class);
        Map<?, ?> r = (Map<?, ?>) record;

        if (!r.get(AvroConstants.RECORD).equals("BlobChangeEvent")) {
            throw new IllegalArgumentException("Not a valid BlobChangefeedEvent.");
        }

        Object topic = r.get("topic");
        Object subject = r.get("subject");
        Object eventType = r.get("eventType");
        Object eventTime = r.get("eventTime");
        Object id = r.get("id");
        Object data = r.get("data");
        Object dataVersion = r.get("dataVersion");
        Object metadataVersion = r.get("metadataVersion");

        return new InternalBlobChangefeedEvent(
            ChangefeedTypeValidator.nullOr("topic", topic, String.class),
            ChangefeedTypeValidator.nullOr("subject", subject, String.class),
            ChangefeedTypeValidator.isNull(eventType) ? null
                : BlobChangefeedEventType.fromString(
                ChangefeedTypeValidator.nullOr("eventType", eventType, String.class)),
            ChangefeedTypeValidator.isNull(eventTime) ? null
                : OffsetDateTime.parse(Objects.requireNonNull(
                    ChangefeedTypeValidator.nullOr("eventTime", eventTime, String.class))),
            ChangefeedTypeValidator.nullOr("id", id, String.class),
            ChangefeedTypeValidator.isNull(data) ? null : InternalBlobChangefeedEventData.fromRecord(data),
            ChangefeedTypeValidator.nullOr("dataVersion", dataVersion, Long.class),
            ChangefeedTypeValidator.nullOr("metadataVersion", metadataVersion, String.class)
        );
    }

    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public BlobChangefeedEventType getEventType() {
        return eventType;
    }

    @Override
    public OffsetDateTime getEventTime() {
        return eventTime;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public BlobChangefeedEventData getData() {
        return data;
    }

    public Long getDataVersion() {
        return dataVersion;
    }

    @Override
    public String getMetadataVersion() {
        return metadataVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InternalBlobChangefeedEvent)) {
            return false;
        }
        InternalBlobChangefeedEvent event = (InternalBlobChangefeedEvent) o;
        return Objects.equals(getTopic(), event.getTopic())
            && Objects.equals(getSubject(), event.getSubject())
            && Objects.equals(getEventType(), event.getEventType())
            && Objects.equals(getEventTime(), event.getEventTime())
            && Objects.equals(getId(), event.getId())
            && Objects.equals(getData(), event.getData())
            && Objects.equals(getDataVersion(), event.getDataVersion())
            && Objects.equals(getMetadataVersion(), event.getMetadataVersion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTopic(), getSubject(), getEventType(), getEventTime(), getId(), getData(),
            getDataVersion(), getMetadataVersion());
    }

    @Override
    public String toString() {
        return "BlobChangefeedEvent{"
            + "topic='" + topic + '\''
            + ", subject='" + subject + '\''
            + ", eventType=" + eventType
            + ", eventTime=" + eventTime
            + ", id='" + id + '\''
            + ", data=" + data
            + ", dataVersion=" + dataVersion
            + ", metadataVersion='" + metadataVersion + '\''
            + '}';
    }
}
