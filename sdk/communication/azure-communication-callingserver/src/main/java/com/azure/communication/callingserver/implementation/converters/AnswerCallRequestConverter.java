// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import java.util.ArrayList;

import com.azure.communication.callingserver.implementation.models.AnswerCallRequest;
import com.azure.communication.callingserver.models.AnswerCallOptions;

/**
 * A converter for {@link AnswerCallRequest}
 */
public final class AnswerCallRequestConverter {

    /**
     * Converts to {@link AnswerCallRequest}.
     */
    public static AnswerCallRequest convert(String incomingCallContext, AnswerCallOptions answerCallOptions) {
        if (incomingCallContext == null) {
            return null;
        }

        AnswerCallRequest answerCallRequest = new AnswerCallRequest()
            .setIncomingCallContext(incomingCallContext);
        if (answerCallOptions != null)
        {
            if (answerCallOptions.getCallbackUri() != null)
            {
                answerCallRequest.setCallbackUri(answerCallOptions.getCallbackUri().toString());
            }
            answerCallRequest.setRequestedMediaTypes(new ArrayList<>(answerCallOptions.getRequestedMediaTypes()));
            answerCallRequest.setRequestedCallEvents(new ArrayList<>(answerCallOptions.getRequestedCallEvents()));
        }
        return answerCallRequest;
    }

    private AnswerCallRequestConverter() {
    }
}

