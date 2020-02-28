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
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class MetaDataDiagnosticsContext {
    public volatile List<MetaDataDiagnostic> metaDataDiagnosticList;

    public void addMetaDataDiagnostic(MetaDataDiagnostic metaDataDiagnostic) {
        if (metaDataDiagnosticList == null) {
            metaDataDiagnosticList = new ArrayList<>();
        }

        metaDataDiagnosticList.add(metaDataDiagnostic);
    }

    @JsonSerialize(using = MetaDataDiagnosticSerializer.class)
    public static class MetaDataDiagnostic {
        public volatile ZonedDateTime startTimeUTC;
        public volatile ZonedDateTime endTimeUTC;
        public volatile MetaDataEnum metaDataName;

        public MetaDataDiagnostic(ZonedDateTime startTimeUTC, ZonedDateTime endTimeUTC, MetaDataEnum metaDataName) {
            this.startTimeUTC = startTimeUTC;
            this.endTimeUTC = endTimeUTC;
            this.metaDataName = metaDataName;
        }
    }

    static class MetaDataDiagnosticSerializer extends StdSerializer<MetaDataDiagnostic> {

        public MetaDataDiagnosticSerializer() {
            super(MetaDataDiagnostic.class);
        }

        @Override
        public void serialize(MetaDataDiagnostic metaDataDiagnostic, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            Duration durationinMS = metaDataDiagnostic.startTimeUTC == null ?
                null : metaDataDiagnostic.endTimeUTC == null ?
                Duration.ZERO : Duration.between(metaDataDiagnostic.startTimeUTC, metaDataDiagnostic.endTimeUTC);
            jsonGenerator.writeStartObject();
            jsonGenerator.writeObjectField("metaDataName", metaDataDiagnostic.metaDataName);
            jsonGenerator.writeStringField("startTimeUTC", ZonedDateTimeSerializer.formatDateTime(metaDataDiagnostic.startTimeUTC));
            jsonGenerator.writeStringField("endTimeUTC", ZonedDateTimeSerializer.formatDateTime(metaDataDiagnostic.endTimeUTC));
            if(durationinMS != null) {
                jsonGenerator.writeNumberField("durationinMS", durationinMS.toMillis());
            }

            jsonGenerator.writeEndObject();
        }
    }
    public enum  MetaDataEnum{
        ContainerLookUp,
        PartitionKeyRangeLookUp,
        ServerAddressLookup,
        MasterAddressLookUp
    }

}
