// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.core.util.ExpandableStringEnum;

/** Defines values for ReasonCode. */
public class ReasonCode extends ExpandableStringEnum<ReasonCode> {
    /** Reason code names for the Recognize operation */
    public static class Recognize extends ExpandableStringEnum<ReasonCode> {
        /** Action failed, initial silence timeout reached. */
        public static final ReasonCode INITIAL_SILENCE_TIMEOUT = fromReasonCode(8510);
        /** Action failed, inter-digit silence timeout reached. */
        public static final ReasonCode INTER_DIGIT_TIMEOUT = fromReasonCode(8532);
        /** Action failed, encountered failure while trying to play the prompt. */
        public static final ReasonCode PLAY_PROMPT_FAILED = fromReasonCode(8511);

        /** Action completed, max digits received. */
        public static final ReasonCode MAX_DIGITS_RECEIVED = fromReasonCode(8531);
        /** Action completed as stop tone was detected. */
        public static final ReasonCode STOP_TONE_DETECTED = fromReasonCode(8514);
    }

    /** Reason code names for the Play operation */
    public static class Play extends ExpandableStringEnum<ReasonCode> {
        /** Action failed, file could not be downloaded. */
        public static final ReasonCode DOWNLOAD_FAILED = fromReasonCode(8536);
        /** Action failed, file could not be downloaded. */
        public static final ReasonCode INVALID_FILE_FORMAT = fromReasonCode(8535);
    }

    /** Action completed successfully. */
    public static final ReasonCode COMPLETED_SUCCESSFULLY = fromReasonCode(0);
    /** Unknown internal server error. */
    public static final ReasonCode UNSPECIFIED_ERROR = fromReasonCode(9999);

    /**
     * Creates or finds a ReasonCode from its string representation.
     *
     * @param reasonCode a reasonCode to look for.
     * @return the corresponding ResourceCode.
     */
    private static ReasonCode fromString(String reasonCode) {
        return fromString(reasonCode, ReasonCode.class);
    }

    /**
     * Creates or finds a ReasonCode from its reasonCode integer.
     *
     * @param reasonCode a reasonCode to look for.
     * @return the corresponding ResourceCode.
     */
    static ReasonCode fromReasonCode(int reasonCode) {
        return fromString(Integer.toString(reasonCode));
    }
}
