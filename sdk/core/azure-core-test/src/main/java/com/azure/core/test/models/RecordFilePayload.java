// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model type for serializing the record file path passsed to the test proxy.
 */
public class RecordFilePayload {

    /**
     * The record file path
     */
    @JsonProperty("x-recording-file")
    private String recordingFile;

    /**
     * Creates an instance of {@link RecordFilePayload}.
     * @param recordingFile The partial path to the recording file.
     */
    public RecordFilePayload(String recordingFile) {
        this.recordingFile = recordingFile;
    }

}
