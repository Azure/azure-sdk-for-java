package com.microsoft.azure.eventhubs.impl;

import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Event;

public abstract class DispatchHandler extends BaseHandler {
    @Override
    public void onTimerTask(Event e) {
        this.onEvent();
    }

    public abstract void onEvent();
}
