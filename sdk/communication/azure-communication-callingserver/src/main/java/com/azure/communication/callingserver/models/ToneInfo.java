// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

/**
 * Gets or sets the tone info
 */
public final class ToneInfo {
    /// <summary>
    /// Gets or sets the sequence id. This id can be used to determine if the same
    /// tone
    /// was played multiple times or if any tones were missed.
    /// </summary>
    private int sequenceId;

    /**
     * Get the sequenceId.
     *
     * @return the sequenceId.
     */
    public int getSequenceId() {
        return this.sequenceId;
    }

    /**
     * Set the sequenceId.
     *
     * @param sequenceId the sequenceId.
     * @return the ToneInfo object itself.
     */
    public ToneInfo setSubject(int sequenceId) {
        this.sequenceId = sequenceId;
        return this;
    }

    /// <summary>
    /// Gets or sets the tone detected.
    /// </summary>
    private ToneValue tone;

    /**
     * Get the tone.
     *
     * @return the sequenceId.
     */
    public ToneValue getTone() {
        return this.tone;
    }

    /**
     * Set the tone.
     *
     * @param tone the sequenceId.
     * @return the ToneInfo object itself.
     */
    public ToneInfo setTone(ToneValue tone) {
        this.tone = tone;
        return this;
    }

    /**
     * Initializes a new instance of ToneInfo.
     * 
     * @param sequenceId Communication Identifier.
     * @param tone Participant Id.
     * @throws IllegalArgumentException if any parameter is null or empty.
     */
    public ToneInfo(int sequenceId, ToneValue tone) {
        if (tone == null) {
            throw new IllegalArgumentException("object tone cannot be null");
        }

        this.sequenceId = sequenceId;
        this.tone = tone;
    }
}
