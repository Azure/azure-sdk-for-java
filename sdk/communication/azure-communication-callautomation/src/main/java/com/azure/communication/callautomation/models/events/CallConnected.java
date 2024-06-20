// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;

/** The CallConnected model. */
@Immutable
public final class CallConnected extends CallAutomationEventBase {
    private CallConnected() {
    }

    static CallConnected fromJsonImpl(JsonReader jsonReader) {
        return new CallConnected();
    }
}
