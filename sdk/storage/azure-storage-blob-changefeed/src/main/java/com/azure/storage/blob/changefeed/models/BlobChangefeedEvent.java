// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed.models;

import com.azure.storage.internal.avro.implementation.AvroConstants;
import com.azure.storage.internal.avro.implementation.schema.AvroSchema;
import com.azure.storage.internal.avro.implementation.schema.primitive.AvroNullSchema;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;

public class BlobChangefeedEvent {

    private final String topic;
    private final String subject;
    private final BlobChangefeedEventType eventType;
    private final OffsetDateTime eventTime;
    private final String id;
    private final BlobChangefeedEventData data;
    private final Long dataVersion;
    private final String metadataVersion;

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

    public static BlobChangefeedEvent fromRecord(Object r) {
        AvroSchema.checkType("record", r, Map.class);
        Map<?, ?> record = (Map<?, ?>) r;

        if (!record.get(AvroConstants.RECORD).equals("BlobChangeEvent")) {
            throw new IllegalArgumentException("Not a valid BlobChangefeedEvent.");
        }

        Object topic = record.get("topic");
        Object subject = record.get("subject");
        Object eventType = record.get("eventType");
        Object eventTime = record.get("eventTime");
        Object id = record.get("id");
        Object data = record.get("data");
        Object dataVersion = record.get("dataVersion");
        Object metadataVersion = record.get("metadataVersion");

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

    public String getTopic() {
        return topic;
    }

    public String getSubject() {
        return subject;
    }

    public BlobChangefeedEventType getEventType() {
        return eventType;
    }

    public OffsetDateTime getEventTime() {
        return eventTime;
    }

    public String getId() {
        return id;
    }

    public BlobChangefeedEventData getData() {
        return data;
    }

    public Long getDataVersion() {
        return dataVersion;
    }

    public String getMetadataVersion() {
        return metadataVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlobChangefeedEvent)) return false;
        BlobChangefeedEvent event = (BlobChangefeedEvent) o;
        return getTopic().equals(event.getTopic())
            && getSubject().equals(event.getSubject())
            && getEventType().equals(event.getEventType())
            && getEventTime().equals(event.getEventTime())
            && getId().equals(event.getId())
            && getData().equals(event.getData())
            && getDataVersion().equals(event.getDataVersion())
            && getMetadataVersion().equals(event.getMetadataVersion());
    }
}
