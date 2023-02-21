// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;

import java.util.List;

/** The Recognize configurations specific for Recognize Choice. **/
public class CallMediaRecognizeChoiceOptions extends CallMediaRecognizeOptions {
    /*
     * List of recognize choice.
     */
    private final List<RecognizeChoice> recognizeChoices;

    /**
     * Get the list of recognize choice.
     *
     * @return the list of recognize choice.
     */
    public List<RecognizeChoice> getRecognizeChoices() {
        return this.recognizeChoices;
    }

    /**
     * Initializes a CallMediaRecognizeDtmfOptions object.
     *
     * @param targetParticipant Target participant of DTFM tone recognition.
     * @param recognizeChoices Maximum number of DTMF tones to be collected.
     */
    public CallMediaRecognizeChoiceOptions(CommunicationIdentifier targetParticipant,  List<RecognizeChoice> recognizeChoices) {
        super(RecognizeInputType.CHOICES, targetParticipant);
        this.recognizeChoices = recognizeChoices;
    }
}
