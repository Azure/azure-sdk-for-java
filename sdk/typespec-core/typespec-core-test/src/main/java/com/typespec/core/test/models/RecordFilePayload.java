// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model type for serializing the record file path passsed to the test proxy.
 */
public class RecordFilePayload {

    /**
     * The record file path
     */
    @JsonProperty(value = "x-recording-file", access = JsonProperty.Access.READ_ONLY)
    private String recordingFile;

    /**
     * The asset file path
     */
    @JsonProperty(value = "x-recording-assets-file", access = JsonProperty.Access.READ_ONLY)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String assetFile;

    /**
     * Creates an instance of {@link RecordFilePayload}.
     * @param recordingFile The partial path to the recording file.
     * @param assetFile The path to asset file.
     */
    public RecordFilePayload(String recordingFile, String assetFile) {
        this.recordingFile = recordingFile;
        this.assetFile = assetFile;
    }
}
