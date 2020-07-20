// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.serializer;

import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.OffsetDateTime;

public class DateTimeDeserializerTests {

    @Test
    public void testDeserializationWithNoZone() throws IOException {
        SerializerAdapter serializerAdapter = new AzureJacksonAdapter();

        DateModel dateModelWithZone = serializerAdapter.deserialize("{\"date\": \"2020-02-18T10:14:43.06Z\"}",
            DateModel.class, SerializerEncoding.JSON);

        // some service returns date-time without time zone
        DateModel dateModelWithNoZone = serializerAdapter.deserialize("{\"date\": \"2020-02-18T10:14:43.06\"}",
            DateModel.class, SerializerEncoding.JSON);
        Assertions.assertEquals(dateModelWithZone.date, dateModelWithNoZone.date);
    }

    public static class DateModel {
        private OffsetDateTime date;

        @JsonProperty("date")
        public OffsetDateTime getDate() {
            return date;
        }

        @JsonProperty("date")
        public void setDate(OffsetDateTime date) {
            this.date = date;
        }
    }
}
