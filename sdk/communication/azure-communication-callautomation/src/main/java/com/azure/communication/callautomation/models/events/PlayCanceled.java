// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;

import java.io.IOException;

/** The PlayCanceled model. */
@Fluent
public final class PlayCanceled extends CallAutomationEventBase {
    static PlayCanceled fromJsonImpl(JsonReader jsonReader) throws IOException {
        return new PlayCanceled();
    }
}
