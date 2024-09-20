// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion.perf;

import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Objects;

public class LogData implements JsonSerializable<LogData> {
    private OffsetDateTime time;
    private String extendedColumn;
    private String additionalContext;

    public LogData() {
    }

    public OffsetDateTime getTime() {
        return time;
    }

    public LogData setTime(OffsetDateTime time) {
        this.time = time;
        return this;
    }

    public String getExtendedColumn() {
        return extendedColumn;
    }

    public LogData setExtendedColumn(String extendedColumn) {
        this.extendedColumn = extendedColumn;
        return this;
    }

    public String getAdditionalContext() {
        return additionalContext;
    }

    public LogData setAdditionalContext(String additionalContext) {
        this.additionalContext = additionalContext;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("Time", Objects.toString(time, null))
            .writeStringField("ExtendedColumn", extendedColumn)
            .writeStringField("AdditionalContext", additionalContext)
            .writeEndObject();
    }

    /**
     * Deserializes an instance of {@link LogData} from the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} to read.
     * @return An instance of {@link LogData}, or null if {@link JsonReader} was pointing to {@link JsonToken#NULL}.
     * @throws IOException If an error occurs while reading the {@link JsonReader}.
     */
    public static LogData fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            LogData logData = new LogData();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("Time".equals(fieldName)) {
                    logData.time = CoreUtils.parseBestOffsetDateTime(reader.getString());
                } else if ("ExtendedColumn".equals(fieldName)) {
                    logData.extendedColumn = reader.getString();
                } else if ("AdditionalContext".equals(fieldName)) {
                    logData.additionalContext = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return logData;
        });
    }
}
