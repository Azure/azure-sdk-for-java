// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class WritingJsonExamples {
    public void writeJsonOutputStream() throws IOException {
        // BEGIN: com.azure.json.JsonWriter.writeJsonOutputStream
        ImmutableJsonSerializableExample object = new ImmutableJsonSerializableExample(10, true, "hello", null);

        ByteArrayOutputStream json = new ByteArrayOutputStream();
        try (JsonWriter jsonWriter = JsonProviders.createWriter(json)) {
            // JsonWriter automatically flushes on close.
            object.toJson(jsonWriter);
        }

        System.out.println(json);
        // END: com.azure.json.JsonWriter.writeJsonOutputStream
    }

    public void writeJsonWriter() throws IOException {
        // BEGIN: com.azure.json.JsonWriter.writeJsonWriter
        ImmutableJsonSerializableExample object = new ImmutableJsonSerializableExample(10, true, "hello", null);

        Writer json = new StringWriter();
        try (JsonWriter jsonWriter = JsonProviders.createWriter(json)) {
            // JsonWriter automatically flushes on close.
            object.toJson(jsonWriter);
        }

        System.out.println(json);
        // END: com.azure.json.JsonWriter.writeJsonWriter
    }
}
