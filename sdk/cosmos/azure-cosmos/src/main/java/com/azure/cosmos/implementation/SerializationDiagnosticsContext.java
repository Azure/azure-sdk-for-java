// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.ZonedDateTimeSerializer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class SerializationDiagnosticsContext {
    public volatile List<SerializationDiagnosticsContext.SerializationDiagnostics> serializationDiagnosticsList;

    public void addMetaDataDiagnostic(SerializationDiagnosticsContext.SerializationDiagnostics serializationDiagnostics) {
        if (serializationDiagnosticsList == null) {
            serializationDiagnosticsList = new ArrayList<>();
        }

        serializationDiagnosticsList.add(serializationDiagnostics);
    }

    public <T> T getResource(Callable<T> function, SerializationType serializationType) {
        ZonedDateTime serializationStartTime = ZonedDateTime.now(ZoneOffset.UTC);
        T t = null;
        try {
            t = function.call();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        ZonedDateTime serializationEndTime = ZonedDateTime.now(ZoneOffset.UTC);
        if (serializationDiagnosticsList == null) {
            serializationDiagnosticsList = new ArrayList<>();
        }

        serializationDiagnosticsList.add(new SerializationDiagnostics(serializationStartTime, serializationEndTime, serializationType));
        return t;
    }

    @JsonSerialize(using = SerializationDiagnosticsContext.SerializationDiagnosticsSerializer.class)
    public static class SerializationDiagnostics {
        public volatile ZonedDateTime startTimeUTC;
        public volatile ZonedDateTime endTimeUTC;
        public volatile SerializationDiagnosticsContext.SerializationType serializationType;

        public SerializationDiagnostics(ZonedDateTime startTimeUTC, ZonedDateTime endTimeUTC, SerializationDiagnosticsContext.SerializationType serializationType) {
            this.startTimeUTC = startTimeUTC;
            this.endTimeUTC = endTimeUTC;
            this.serializationType = serializationType;
        }
    }

    static class SerializationDiagnosticsSerializer extends StdSerializer<SerializationDiagnosticsContext.SerializationDiagnostics> {

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
            jsonGenerator.writeStringField("startTimeUTC", ZonedDateTimeSerializer.formatDateTime(serializationDiagnostics.startTimeUTC));
            jsonGenerator.writeStringField("endTimeUTC", ZonedDateTimeSerializer.formatDateTime(serializationDiagnostics.endTimeUTC));
            if(durationinMS != null) {
                jsonGenerator.writeNumberField("durationInMicroSec", durationinMS.toNanos()/1000);
            }

            jsonGenerator.writeEndObject();
        }
    }
    public enum  SerializationType{
        DatabaseSerialization,
        CollectionSerialization,
        ItemSerialization,
        PartitionKeyFetchSerialization
    }
}
