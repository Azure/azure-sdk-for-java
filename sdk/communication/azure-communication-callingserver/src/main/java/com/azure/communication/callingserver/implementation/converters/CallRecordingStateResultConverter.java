// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.CallRecordingStateResultInternal;
import com.azure.communication.callingserver.models.CallRecordingStateResult;


public final class CallRecordingStateResultConverter {
    public static CallRecordingStateResult convert(CallRecordingStateResultInternal callRecordingStateResultInternal) {
        return new CallRecordingStateResult(callRecordingStateResultInternal.getRecordingState());
    }
}
