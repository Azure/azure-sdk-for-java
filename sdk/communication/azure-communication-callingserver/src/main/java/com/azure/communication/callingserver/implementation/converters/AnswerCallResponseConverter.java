// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.AnswerCallResponseInternal;
import com.azure.communication.callingserver.models.AnswerCallResponse;

/**
 * A converter for {@link AnswerCallResponse}
 */
public final class AnswerCallResponseConverter {

    /**
     * Converts to {@link AnswerCallResponse}.
     */
    public static AnswerCallResponse convert(AnswerCallResponseInternal answerCallResponseInternal) {
        if (answerCallResponseInternal == null) {
            return null;
        }

        return new AnswerCallResponse(answerCallResponseInternal.getServerCallId(),
            answerCallResponseInternal.getCallConnectionId());
    }

    private AnswerCallResponseConverter() {
    }
}
