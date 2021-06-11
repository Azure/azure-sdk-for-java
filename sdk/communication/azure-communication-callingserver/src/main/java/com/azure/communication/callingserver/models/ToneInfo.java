// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

/** Gets or sets the tone info. */
public final class ToneInfo {
    /*
     * Gets or sets the sequence id. This id can be used to determine if the
     * same tone
     * was played multiple times or if any tones were missed.
     */
    private final Integer sequenceId;

    /*
     * Gets or sets the tone detected.
     */
    private final ToneValue tone;

    /**
     * Get the sequenceId property: Gets or sets the sequence id. This id can be used to determine if the same tone was
     * played multiple times or if any tones were missed.
     *
     * @return the sequenceId value.
     */
    public Integer getSequenceId() {
        return sequenceId;
    }

    /**
     * Get the tone property: Gets or sets the tone detected.
     *
     * @return the tone value.
     */
    public ToneValue getTone() {
        return tone;
    }

    /**
     * Initializes a new instance of ToneInfo.
     *
     * @param sequenceId the sequenceId value.
     * @param tone the tone value.
     */
    public ToneInfo(Integer sequenceId, ToneValue tone) {
        this.sequenceId = sequenceId;
        this.tone = tone;
    }

}
