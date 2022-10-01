// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.core.util.ExpandableStringEnum;

/** Defines values for ReasonCodeName. */
public class ReasonCodeName extends ExpandableStringEnum<ReasonCodeName> {
    /** Reason code names for the Recognize operation */
    public static class Recognize extends ExpandableStringEnum<ReasonCodeName> {
        /** Action failed, initial silence timeout reached. */
        public static final ReasonCodeName INITIAL_SILENCE_TIMEOUT = fromReasonCode(8510);
        /** Action failed, inter-digit silence timeout reached. */
        public static final ReasonCodeName INTER_DIGIT_TIMEOUT = fromReasonCode(8532);
        /** Action failed, encountered failure while trying to play the prompt. */
        public static final ReasonCodeName PLAY_PROMPT_FAILED = fromReasonCode(8511);

        /** Action completed, max digits received. */
        public static final ReasonCodeName MAX_DIGITS_RECEIVED = fromReasonCode(8531);
        /** Action completed as stop tone was detected. */
        public static final ReasonCodeName STOP_TONE_DETECTED = fromReasonCode(8514);
    }

    /** Reason code names for the Play operation */
    public static class Play extends ExpandableStringEnum<ReasonCodeName> {
        /** Action failed, file could not be downloaded. */
        public static final ReasonCodeName DOWNLOAD_FAILED = fromReasonCode(8536);
        /** Action failed, file could not be downloaded. */
        public static final ReasonCodeName INVALID_FILE_FORMAT = fromReasonCode(8535);
    }

    /** Action completed successfully. */
    public static final ReasonCodeName COMPLETED_SUCCESSFULLY = fromReasonCode(0);
    /** Unknown internal server error. */
    public static final ReasonCodeName UNSPECIFIED_ERROR = fromReasonCode(9999);

    /**
     * Creates or finds a ReasonCodeName from its string representation.
     *
     * @param reasonCode a reasonCode to look for.
     * @return the corresponding ResourceCodeName.
     */
    private static ReasonCodeName fromString(String reasonCode) {
        return fromString(reasonCode, ReasonCodeName.class);
    }

    /**
     * Creates or finds a ReasonCodeName from its reasonCode integer.
     *
     * @param reasonCode a reasonCode to look for.
     * @return the corresponding ResourceCodeName.
     */
    static ReasonCodeName fromReasonCode(int reasonCode) {
        return fromString(Integer.toString(reasonCode));
    }
}
