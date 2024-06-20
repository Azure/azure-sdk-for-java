// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;

import java.io.IOException;

/** The RecognizeCanceled model. */
@Fluent
public final class RecognizeCanceled extends CallAutomationEventBase {
    static RecognizeFailed fromJsonImpl(JsonReader jsonReader) throws IOException {
        return new RecognizeFailed();
    }
}
