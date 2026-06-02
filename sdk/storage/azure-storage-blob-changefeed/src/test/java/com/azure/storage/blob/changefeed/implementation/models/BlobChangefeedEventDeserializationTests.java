// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed.implementation.models;

import com.azure.storage.blob.changefeed.models.BlobChangefeedEventType;
import com.azure.storage.blob.changefeed.models.BlobOperationName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests deserialization of BlobChangefeedEvent and BlobChangefeedEventData for schema versions V6, V7, and V8.
 */
public class BlobChangefeedEventDeserializationTests {

    // Values from EventSchemaV6.json / EventSchemaV7.json / EventSchemaV8.json
    private static final long CONTENT_OFFSET = 256L;
    private static final String CREATE_TIME = "2022-02-17T13:11:52.5901564Z";
    private static final String LAST_ACCESS_TIME = "2022-02-17T13:11:53.5901564Z";
    private static final String RESTORED_CONTAINER_VERSION = "0000000000000002";

    // ======================== Schema V6 ========================

    @Test
    public void schemaV6AppendBlobDataUpdatedEventTypeDeserializes() {
        assertEquals(BlobChangefeedEventType.APPEND_BLOB_DATA_UPDATED,
            BlobChangefeedEventType.fromString("AppendBlobDataUpdated"));
    }

    @Test
    public void schemaV6AppendBlockOperationNameDeserializes() {
        assertEquals(BlobOperationName.APPEND_BLOCK, BlobOperationName.fromString("AppendBlock"));
    }

    @Test
    public void schemaV6ContentOffsetDeserializes() {
        InternalBlobChangefeedEventData data = InternalBlobChangefeedEventData.fromRecord(buildDataRecord(r -> {
            r.put("api", "PutBlob");
            r.put("contentOffset", CONTENT_OFFSET);
        }));
        assertEquals(CONTENT_OFFSET, data.getContentOffset());
    }

    @Test
    public void schemaV6CreationTimeDeserializes() {
        InternalBlobChangefeedEventData data = InternalBlobChangefeedEventData.fromRecord(buildDataRecord(r -> {
            r.put("api", "PutBlob");
            r.put("createTime", CREATE_TIME);
        }));
        assertEquals(OffsetDateTime.parse(CREATE_TIME), data.getCreationTime());
    }

    @Test
    public void schemaV6ContentOffsetAndCreationTimeNullWhenAbsent() {
        InternalBlobChangefeedEventData data = InternalBlobChangefeedEventData.fromRecord(buildDataRecord(r -> {
        }));
        assertNull(data.getContentOffset());
        assertNull(data.getCreationTime());
    }

    @Test
    public void schemaV6FullEventDeserializes() {
        Map<String, Object> eventMap = buildEventRecord(r -> {
            r.put("eventType", "BlobCreated");
            r.put("data", buildDataRecord(d -> {
                d.put("api", "PutBlob");
                d.put("contentOffset", CONTENT_OFFSET);
                d.put("createTime", CREATE_TIME);
            }));
        });

        InternalBlobChangefeedEvent event = InternalBlobChangefeedEvent.fromRecord(eventMap);

        assertEquals(BlobChangefeedEventType.BLOB_CREATED, event.getEventType());
        assertEquals("PutBlob", event.getData().getApi());
        assertEquals(CONTENT_OFFSET, event.getData().getContentOffset());
        assertEquals(OffsetDateTime.parse(CREATE_TIME), event.getData().getCreationTime());
        assertNull(event.getData().getLastAccessTime());
        assertNull(event.getData().getRestoredContainerVersion());
    }

    // ======================== Schema V7 ========================

    @Test
    public void schemaV7BlobLastAccessTimeUpdatedEventTypeDeserializes() {
        assertEquals(BlobChangefeedEventType.BLOB_LAST_ACCESS_TIME_UPDATED,
            BlobChangefeedEventType.fromString("BlobLastAccessTimeUpdated"));
    }

    @Test
    public void schemaV7UpdateLastAccessTimeOperationNameDeserializes() {
        assertEquals(BlobOperationName.UPDATE_LAST_ACCESS_TIME, BlobOperationName.fromString("UpdateLastAccessTime"));
    }

    @Test
    public void schemaV7LastAccessTimeDeserializes() {
        InternalBlobChangefeedEventData data = InternalBlobChangefeedEventData.fromRecord(buildDataRecord(r -> {
            r.put("api", "PutBlob");
            r.put("lastAccessTime", LAST_ACCESS_TIME);
        }));
        assertEquals(OffsetDateTime.parse(LAST_ACCESS_TIME), data.getLastAccessTime());
    }

    @Test
    public void schemaV7LastAccessTimeNullWhenAbsent() {
        InternalBlobChangefeedEventData data = InternalBlobChangefeedEventData.fromRecord(buildDataRecord(r -> {
        }));
        assertNull(data.getLastAccessTime());
    }

    @Test
    public void schemaV7FullEventDeserializes() {
        Map<String, Object> eventMap = buildEventRecord(r -> {
            r.put("eventType", "BlobCreated");
            r.put("data", buildDataRecord(d -> {
                d.put("api", "PutBlob");
                d.put("contentOffset", CONTENT_OFFSET);
                d.put("createTime", CREATE_TIME);
                d.put("lastAccessTime", LAST_ACCESS_TIME);
            }));
        });

        InternalBlobChangefeedEvent event = InternalBlobChangefeedEvent.fromRecord(eventMap);

        assertEquals(CONTENT_OFFSET, event.getData().getContentOffset());
        assertEquals(OffsetDateTime.parse(CREATE_TIME), event.getData().getCreationTime());
        assertEquals(OffsetDateTime.parse(LAST_ACCESS_TIME), event.getData().getLastAccessTime());
        assertNull(event.getData().getRestoredContainerVersion());
    }

    // ======================== Schema V8 / Container Change Feed ========================

    @Test
    public void schemaV8ContainerCreatedEventTypeDeserializes() {
        assertEquals(BlobChangefeedEventType.CONTAINER_CREATED, BlobChangefeedEventType.fromString("ContainerCreated"));
    }

    @Test
    public void schemaV8ContainerDeletedEventTypeDeserializes() {
        assertEquals(BlobChangefeedEventType.CONTAINER_DELETED, BlobChangefeedEventType.fromString("ContainerDeleted"));
    }

    @Test
    public void schemaV8ContainerPropertiesUpdatedEventTypeDeserializes() {
        assertEquals(BlobChangefeedEventType.CONTAINER_PROPERTIES_UPDATED,
            BlobChangefeedEventType.fromString("ContainerPropertiesUpdated"));
    }

    @Test
    public void schemaV8CreateContainerOperationNameDeserializes() {
        assertEquals(BlobOperationName.CREATE_CONTAINER, BlobOperationName.fromString("ContainerCreated"));
    }

    @Test
    public void schemaV8DeleteContainerOperationNameDeserializes() {
        assertEquals(BlobOperationName.DELETE_CONTAINER, BlobOperationName.fromString("ContainerDeleted"));
    }

    @Test
    public void schemaV8RestoreContainerOperationNameDeserializes() {
        assertEquals(BlobOperationName.RESTORE_CONTAINER, BlobOperationName.fromString("RestoreContainer"));
    }

    @Test
    public void schemaV8SetContainerMetadataOperationNameDeserializes() {
        assertEquals(BlobOperationName.SET_CONTAINER_METADATA, BlobOperationName.fromString("SetContainerMetadata"));
    }

    @Test
    public void schemaV8RestoredContainerVersionDeserializes() {
        InternalBlobChangefeedEventData data = InternalBlobChangefeedEventData.fromRecord(buildDataRecord(r -> {
            r.put("api", "PutBlob");
            r.put("restoredContainerVersion", RESTORED_CONTAINER_VERSION);
        }));
        assertEquals(RESTORED_CONTAINER_VERSION, data.getRestoredContainerVersion());
    }

    @Test
    public void schemaV8RestoredContainerVersionNullWhenAbsent() {
        InternalBlobChangefeedEventData data = InternalBlobChangefeedEventData.fromRecord(buildDataRecord(r -> {
        }));
        assertNull(data.getRestoredContainerVersion());
    }

    @Test
    public void schemaV8FullEventDeserializes() {
        Map<String, Object> eventMap = buildEventRecord(r -> {
            r.put("eventType", "BlobCreated");
            r.put("data", buildDataRecord(d -> {
                d.put("api", "PutBlob");
                d.put("contentOffset", CONTENT_OFFSET);
                d.put("createTime", CREATE_TIME);
                d.put("lastAccessTime", LAST_ACCESS_TIME);
                d.put("restoredContainerVersion", RESTORED_CONTAINER_VERSION);
            }));
        });

        InternalBlobChangefeedEvent event = InternalBlobChangefeedEvent.fromRecord(eventMap);

        assertEquals(CONTENT_OFFSET, event.getData().getContentOffset());
        assertEquals(OffsetDateTime.parse(CREATE_TIME), event.getData().getCreationTime());
        assertEquals(OffsetDateTime.parse(LAST_ACCESS_TIME), event.getData().getLastAccessTime());
        assertEquals(RESTORED_CONTAINER_VERSION, event.getData().getRestoredContainerVersion());
    }

    // ======================== Regression Tests ========================

    @Test
    public void olderSchemaPayloadDeserializesWithoutNewFields() {
        Map<String, Object> eventMap = buildEventRecord(r -> {
            r.put("eventType", "BlobCreated");
            r.put("data", buildDataRecord(d -> {
                d.put("api", "PutBlob");
                d.put("etag", "0x8D9F2171BE32588");
                d.put("contentType", "application/octet-stream");
                d.put("contentLength", 128L);
                d.put("blobType", "BlockBlob");
                d.put("url", "https://www.myurl.com");
            }));
        });

        InternalBlobChangefeedEvent event = InternalBlobChangefeedEvent.fromRecord(eventMap);

        assertEquals(BlobChangefeedEventType.BLOB_CREATED, event.getEventType());
        assertEquals("PutBlob", event.getData().getApi());
        assertNull(event.getData().getContentOffset());
        assertNull(event.getData().getCreationTime());
        assertNull(event.getData().getLastAccessTime());
        assertNull(event.getData().getRestoredContainerVersion());
    }

    @Test
    public void existingBlobEventsUnaffected() {
        Map<String, Object> eventMap = buildEventRecord(r -> {
            r.put("eventType", "BlobDeleted");
            r.put("data", buildDataRecord(d -> {
                d.put("api", "DeleteBlob");
                d.put("sequencer", "00000000000000010000000000000002000000000000001d");
            }));
        });

        InternalBlobChangefeedEvent event = InternalBlobChangefeedEvent.fromRecord(eventMap);

        assertEquals(BlobChangefeedEventType.BLOB_DELETED, event.getEventType());
        assertEquals("DeleteBlob", event.getData().getApi());
        assertNull(event.getData().getCreationTime());
        assertNull(event.getData().getLastAccessTime());
        assertNull(event.getData().getRestoredContainerVersion());
    }

    @Test
    public void unknownOptionalFieldsDoNotFailDeserialization() {
        Map<String, Object> dataRecord = buildDataRecord(r -> {
            r.put("unknownFutureField", "someValue");
            r.put("anotherUnknownField", 42L);
        });

        InternalBlobChangefeedEventData data = InternalBlobChangefeedEventData.fromRecord(dataRecord);
        assertEquals("PutBlob", data.getApi());
    }

    @Test
    public void dataVersionFieldUnaffected() {
        Map<String, Object> eventMap = buildEventRecord(r -> r.put("dataVersion", 8L));

        InternalBlobChangefeedEvent event = InternalBlobChangefeedEvent.fromRecord(eventMap);
        assertEquals(8L, event.getDataVersion());
    }

    // ======================== Helpers ========================

    @FunctionalInterface
    private interface MapCustomizer {
        void customize(Map<String, Object> map);
    }

    private static Map<String, Object> buildEventRecord(MapCustomizer customizer) {
        Map<String, Object> record = new HashMap<>();
        record.put("$record", "BlobChangeEvent");
        record.put("schemaVersion", 1);
        record.put("topic", "topic");
        record.put("subject", "subject");
        record.put("eventType", "BlobCreated");
        record.put("eventTime", OffsetDateTime.of(2022, 2, 17, 13, 12, 11, 0, ZoneOffset.UTC).toString());
        record.put("id", "62616073-8020-0000-00ff-233467060cc0");
        record.put("dataVersion", 1L);
        record.put("metadataVersion", "1");
        record.put("data", buildDataRecord(d -> {
        }));
        customizer.customize(record);
        return record;
    }

    private static Map<String, Object> buildDataRecord(MapCustomizer customizer) {
        Map<String, Object> record = new HashMap<>();
        record.put("$record", "BlobChangeEventData");
        record.put("api", "PutBlob");
        customizer.customize(record);
        return record;
    }
}
