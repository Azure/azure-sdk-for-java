// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.systemevents;

import com.azure.messaging.eventgrid.systemevents.models.StorageBlobCreatedEventData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SystemEventNamesTests {

    @Test
    public void testParseEventName() {
        // Arrange
        String eventTypeName = "Microsoft.Storage.BlobCreated";

        // Act
        Class<?> clazz = SystemEventNames.getSystemEventMappings().get(eventTypeName);

        // Assert
        assertEquals(StorageBlobCreatedEventData.class, clazz);
    }

}
