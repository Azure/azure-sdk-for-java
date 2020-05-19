// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed.models;

import com.azure.storage.internal.avro.implementation.AvroConstants;
import com.azure.storage.internal.avro.implementation.schema.AvroSchema;
import com.azure.storage.internal.avro.implementation.schema.primitive.AvroNullSchema;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * This class contains properties of a BlobChangefeedEvent.
 */
public class BlobChangefeedEvent {

    private final String topic;
    private final String subject;
    private final BlobChangefeedEventType eventType;
    private final OffsetDateTime eventTime;
    private final String id;
    private final BlobChangefeedEventData data;
    private final Long dataVersion;
    private final String metadataVersion;

    /**
     * Constructs a {@link BlobChangefeedEvent}.
     *
     * @param topic The topic.
     * @param subject The subject.
     * @param eventType {@link BlobChangefeedEventType}
     * @param eventTime The {@link OffsetDateTime event time}.
     * @param id The identifier.
     * @param data {@link BlobChangefeedEventData}
     * @param dataVersion The data version.
     * @param metadataVersion The metadata version.
     */
    public BlobChangefeedEvent(String topic, String subject, BlobChangefeedEventType eventType,
        OffsetDateTime eventTime, String id, BlobChangefeedEventData data, Long dataVersion, String metadataVersion) {
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
     * Constructs a {@link BlobChangefeedEvent}.
     *
     * @param record The record.
     * @return The {@link BlobChangefeedEvent} representing the record.
     */
    public static BlobChangefeedEvent fromRecord(Object record) {
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

        return new BlobChangefeedEvent(
            nullOrString("topic", topic),
            nullOrString("subject", subject),
            isNull(eventType) ? null
                : BlobChangefeedEventType.fromString(BlobChangefeedEvent.nullOrString("eventType", eventType)),
            isNull(eventTime) ? null
                : OffsetDateTime.parse(Objects.requireNonNull(nullOrString("eventTime", eventTime))),
            nullOrString("id", id),
            isNull(data) ? null : BlobChangefeedEventData.fromRecord(data),
            nullOrLong("dataVersion", dataVersion),
            nullOrString("metadataVersion", metadataVersion)
        );
    }

    /**
     * Determines whether or not the object is null in the Avro sense.
     */
    static boolean isNull(Object o) {
        return o == null || o instanceof AvroNullSchema.Null;
    }

    /**
     * Returns either null or a String.
     */
    static String nullOrString(String name, Object o) {
        if (isNull(o)) {
            return null;
        }
        AvroSchema.checkType(name, o, String.class);
        return (String) o;
    }

    /**
     * Returns either null or a Long.
     */
    static Long nullOrLong(String name, Object o) {
        if (isNull(o)) {
            return null;
        }
        AvroSchema.checkType(name, o, Long.class);
        return (Long) o;
    }

    /**
     * Returns either null or a Boolean.
     */
    static Boolean nullOrBoolean(String name, Object o) {
        if (isNull(o)) {
            return null;
        }
        AvroSchema.checkType(name, o, Boolean.class);
        return (Boolean) o;
    }

    /**
     * @return the topic.
     */
    public String getTopic() {
        return topic;
    }

    /**
     * @return the subject.
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @return {@link BlobChangefeedEventType}
     */
    public BlobChangefeedEventType getEventType() {
        return eventType;
    }

    /**
     * @return The {@link OffsetDateTime event time}.
     */
    public OffsetDateTime getEventTime() {
        return eventTime;
    }

    /**
     * @return the identifer.
     */
    public String getId() {
        return id;
    }

    /**
     * @return {@link BlobChangefeedEventData}.
     */
    public BlobChangefeedEventData getData() {
        return data;
    }

    /**
     * @return the data version.
     */
    public Long getDataVersion() {
        return dataVersion;
    }

    /**
     * @return the metadata version.
     */
    public String getMetadataVersion() {
        return metadataVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlobChangefeedEvent)) return false;
        BlobChangefeedEvent event = (BlobChangefeedEvent) o;
        return Objects.equals(getTopic(), event.getTopic()) &&
            Objects.equals(getSubject(), event.getSubject()) &&
            Objects.equals(getEventType(), event.getEventType()) &&
            Objects.equals(getEventTime(), event.getEventTime()) &&
            Objects.equals(getId(), event.getId()) &&
            Objects.equals(getData(), event.getData()) &&
            Objects.equals(getDataVersion(), event.getDataVersion()) &&
            Objects.equals(getMetadataVersion(), event.getMetadataVersion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTopic(), getSubject(), getEventType(), getEventTime(), getId(), getData(),
            getDataVersion(), getMetadataVersion());
    }
}
