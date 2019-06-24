package com.microsoft.azure.eventgrid;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.microsoft.azure.eventgrid.TopicCredentials;
import com.microsoft.azure.eventgrid.implementation.EventGridClientImpl;
import com.microsoft.azure.eventgrid.models.EventGridEvent;
import com.microsoft.azure.eventgrid.models.StorageBlobCreatedEventData;

import org.joda.time.DateTime;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class EventGridTests {
    @Test
    public void canPublishEvent() throws Exception {
        String endpoint = System.getenv("EG_ENDPOINT");
        String key = System.getenv("EG_KEY");

        TopicCredentials topicCredentials = new TopicCredentials(key);
        EventGridClient client = new EventGridClientImpl(topicCredentials);
        client.publishEvents(endpoint, getEventsList());
    }

    @Test
    public void canDeserializeEvent() throws Exception {
        String storageEventJson = "{\"topic\": \"/subscriptions/subscriptionID/resourceGroups/Storage/providers/Microsoft.Storage/storageAccounts/xstoretestaccount\",\"subject\": \"/blobServices/default/containers/testcontainer/blobs/testfile.txt\",   \"eventType\": \"Microsoft.Storage.BlobCreated\",  \"eventTime\": \"2017-06-26T18:41:00.9584103Z\",  \"id\": \"831e1650-001e-001b-66ab-eeb76e069631\",  \"data\": {    \"api\": \"PutBlockList\",    \"clientRequestId\": \"6d79dbfb-0e37-4fc4-981f-442c9ca65760\",    \"requestId\": \"831e1650-001e-001b-66ab-eeb76e000000\",    \"eTag\": \"0x8D4BCC2E4835CD0\",    \"contentType\": \"text/plain\",    \"contentLength\": 524288,    \"blobType\": \"BlockBlob\",    \"url\": \"https://example.blob.core.windows.net/testcontainer/testfile.txt\",    \"sequencer\": \"00000000000004420000000000028963\",    \"storageDiagnostics\": {      \"batchId\": \"b68529f3-68cd-4744-baa4-3c0498ec19f0\" }},  \"dataVersion\": \"\",  \"metadataVersion\": \"1\"}";

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());

        EventGridEvent eventGridEvent = mapper.readValue(storageEventJson, EventGridEvent.class);

        assertNotNull(eventGridEvent);
        assertEquals("Event types do not match", eventGridEvent.eventType(), "Microsoft.Storage.BlobCreated");

        JsonNode storageEventNode = mapper.readTree(storageEventJson);
        JsonNode storageEventDataNode = storageEventNode.get("data");
        StorageBlobCreatedEventData storageEventData = mapper.treeToValue(storageEventDataNode, StorageBlobCreatedEventData.class);

        assertNotNull(storageEventData);
        assertEquals(storageEventData.blobType(), "BlockBlob");
    }

    private List<EventGridEvent> getEventsList() {
        List<EventGridEvent> eventsList = new ArrayList<>();

        for (int i = 0; i < 10; i++)
        {
            eventsList.add(new EventGridEvent(
                    UUID.randomUUID().toString(),
                    "TestSubject",
                    new HashMap<String, String>() {{
                        put("Field1", "Value1");
                        put("Field2", "Value2");
                        put("Field3", "Value3");
                    }},
                    "Microsoft.MockPublisher.TestEvent",
                    DateTime.now(),
                    "1.0"));
        }
        return eventsList;
    }
}
