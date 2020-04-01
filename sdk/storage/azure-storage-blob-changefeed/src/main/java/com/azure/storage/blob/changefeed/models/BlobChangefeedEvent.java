// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed.models;

import org.apache.avro.generic.GenericRecord;

import java.time.OffsetDateTime;

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

    public static BlobChangefeedEvent fromRecord(GenericRecord record) {
        Object topic = record.get("topic");
        Object subject = record.get("subject");
        Object eventType = record.get("eventType");
        Object eventTime = record.get("eventTime");
        Object id = record.get("id");
        Object data = record.get("data");
        Object dataVersion = record.get("dataVersion");
        Object metadataVersion = record.get("metadataVersion");

        return new BlobChangefeedEvent(topic == null ? null : topic.toString(),
            subject == null ? null : subject.toString(),
            eventType == null ? null : BlobChangefeedEventType.fromString(eventType.toString()),
            eventTime == null ? null : OffsetDateTime.parse(eventTime.toString()),
            id == null ? null : id.toString(),
            data == null ? null : BlobChangefeedEventData.fromRecord((GenericRecord) data),
            dataVersion == null ? null : (Long) dataVersion,
            metadataVersion== null ? null : metadataVersion.toString());
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
}
