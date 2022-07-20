// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The FileSource model. */
@Fluent
public final class FileSource extends PlaySource {
    /*
     * Uri for the audio file to be played
     */
    @JsonProperty(value = "uri", required = true)
    private String uri;

    /**
     * Get the uri property: Uri for the audio file to be played.
     *
     * @return the uri value.
     */
    public String getUri() {
        return this.uri;
    }

    /**
     * Set the uri property: Uri for the audio file to be played.
     *
     * @param uri the uri value to set.
     * @return the FileSourceInternal object itself.
     */
    public FileSource setUri(String uri) {
        this.uri = uri;
        return this;
    }
}
