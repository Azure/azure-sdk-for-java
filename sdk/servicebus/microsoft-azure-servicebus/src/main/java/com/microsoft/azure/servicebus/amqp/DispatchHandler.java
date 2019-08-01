// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.amqp;

import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Event;

public abstract class DispatchHandler extends BaseHandler {
    @Override public void onTimerTask(Event e) {
        this.onEvent();
    }

    public abstract void onEvent();
}
