// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SerializationDiagnosticsContext {
    public volatile List<SerializationDiagnosticsContext.SerializationDiagnostics> serializationDiagnosticsList;

    public void addSerializationDiagnostics(SerializationDiagnosticsContext.SerializationDiagnostics serializationDiagnostics) {
        if (serializationDiagnosticsList == null) {
            serializationDiagnosticsList = Collections.synchronizedList(new ArrayList<>());
        }

        serializationDiagnosticsList.add(serializationDiagnostics);
    }

    @JsonSerialize(using = SerializationDiagnosticsContext.SerializationDiagnosticsSerializer.class)
    public static class SerializationDiagnostics {
        public volatile Instant startTimeUTC;
        public volatile Instant endTimeUTC;
        public volatile SerializationDiagnosticsContext.SerializationType serializationType;

        public SerializationDiagnostics(Instant startTimeUTC, Instant endTimeUTC, SerializationDiagnosticsContext.SerializationType serializationType) {
            this.startTimeUTC = startTimeUTC;
            this.endTimeUTC = endTimeUTC;
            this.serializationType = serializationType;
        }
    }

    static class SerializationDiagnosticsSerializer extends StdSerializer<SerializationDiagnosticsContext.SerializationDiagnostics> {

        private static final long serialVersionUID = -1679638551521266979L;

        public SerializationDiagnosticsSerializer() {
            super(SerializationDiagnosticsContext.SerializationDiagnostics.class);
        }

        @Override
        public void serialize(SerializationDiagnosticsContext.SerializationDiagnostics serializationDiagnostics, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            Duration durationinMS = serializationDiagnostics.startTimeUTC == null ?
                null : serializationDiagnostics.endTimeUTC == null ?
                Duration.ZERO : Duration.between(serializationDiagnostics.startTimeUTC, serializationDiagnostics.endTimeUTC);
            jsonGenerator.writeStartObject();
            jsonGenerator.writeObjectField("serializationType", serializationDiagnostics.serializationType);
            jsonGenerator.writeStringField("startTimeUTC", DiagnosticsInstantSerializer.fromInstant(serializationDiagnostics.startTimeUTC));
            jsonGenerator.writeStringField("endTimeUTC", DiagnosticsInstantSerializer.fromInstant(serializationDiagnostics.endTimeUTC));
            if (durationinMS != null) {
                jsonGenerator.writeNumberField("durationInMicroSec", durationinMS.toNanos() / 1000);
            }

            jsonGenerator.writeEndObject();
        }
    }

    public enum SerializationType {
        DATABASE_DESERIALIZATION,
        DATABASE_SERIALIZATION,
        CONTAINER_DESERIALIZATION,
        CONTAINER_SERIALIZATION,
        ITEM_DESERIALIZATION,
        ITEM_SERIALIZATION,
        PARTITION_KEY_FETCH_SERIALIZATION
    }
}
