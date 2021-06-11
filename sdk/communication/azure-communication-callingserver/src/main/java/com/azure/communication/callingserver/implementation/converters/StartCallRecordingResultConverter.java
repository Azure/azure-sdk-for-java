// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.StartCallRecordingResultInternal;
import com.azure.communication.callingserver.models.StartCallRecordingResult;

public final class StartCallRecordingResultConverter {

    public static StartCallRecordingResult convert(StartCallRecordingResultInternal startCallRecordingResultInternal) {
        return new StartCallRecordingResult(startCallRecordingResultInternal.getRecordingId());
    }
}
