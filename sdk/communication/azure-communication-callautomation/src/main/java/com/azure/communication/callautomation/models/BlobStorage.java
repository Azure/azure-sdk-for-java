// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** Used to specify Blob container url to recording storage. */
@Fluent
public final class BlobStorage extends ExternalStorage {

    /*
     * Url of a container or a location within a container
     */
    private final String containerUrl;

    /**
     * Constructor
     *
     * @param containerUrl Url of a container or a location within a container.
     */

    public BlobStorage(String containerUrl) {
        super(RecordingStorageType.BLOB_STORAGE);
        this.containerUrl = containerUrl;
    }

    /**
     * Get the containerUrl property: Url of a container or a location within a container.
     *
     * @return the containerUrl value.
     */
    public String getContainerUrl() {
        return this.containerUrl;
    }

    @Override
    void writeJsonImpl(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStringField("containerUrl", this.containerUrl);
    }

    static BlobStorage readJsonImpl(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String containerUrl = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("containerUrl".equals(fieldName)) {
                    containerUrl = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return new BlobStorage(containerUrl);
        });
    }
}
