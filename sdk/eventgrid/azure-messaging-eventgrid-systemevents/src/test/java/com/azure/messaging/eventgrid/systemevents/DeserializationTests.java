// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.systemevents;

import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.messaging.eventgrid.systemevents.models.StorageBlobCreatedEventData;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeserializationTests {

    @Test
    void getStorageDiagnostics() {
        try {
            String payload = getTestPayloadFromFile("storageBlobCreated.json");
            StorageBlobCreatedEventData storageBlobCreatedEventData
                = StorageBlobCreatedEventData.fromJson(JsonProviders.createReader(payload));
            BinaryData data = storageBlobCreatedEventData.getStorageDiagnostics().get("batchId");
            assertEquals("\"23f68872-a006-0065-0049-9240f2000000\"", data.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getTestPayloadFromFile(String fileName) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream("testJsons/" + fileName)) {
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            return new String(bytes);
        }
    }
}
