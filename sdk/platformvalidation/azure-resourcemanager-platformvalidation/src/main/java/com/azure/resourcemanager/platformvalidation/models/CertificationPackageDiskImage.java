// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.platformvalidation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * A disk image in a certification package.
 */
@Fluent
public final class CertificationPackageDiskImage implements JsonSerializable<CertificationPackageDiskImage> {
    private String sourceVhdUri;

    /**
     * Gets the source VHD URI.
     *
     * @return the source VHD URI.
     */
    public String sourceVhdUri() {
        return sourceVhdUri;
    }

    /**
     * Sets the source VHD URI.
     *
     * @param sourceVhdUri the source VHD URI.
     * @return this disk image.
     */
    public CertificationPackageDiskImage withSourceVhdUri(String sourceVhdUri) {
        this.sourceVhdUri = sourceVhdUri;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        validate("diskImage");

        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("sourceVhdUri", sourceVhdUri);
        return jsonWriter.writeEndObject();
    }

    void validate(String path) {
        ValidationUtils.requireNonBlank(sourceVhdUri, path + ".sourceVhdUri");
    }
}
