// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;

/**
 * DtmfConfigurationOptions for the call.
 */
@Fluent
public final class DtmfConfigurationOptions {
    /*
     * Enabling DTMF broadcast allows DTMF tones to be transmitted during group calls.
     */
    private boolean enableDtmfBroadcastInGroupCalls;

    /**
     * Creates an instance of DtmfConfigurationOptions class.
     */
    public DtmfConfigurationOptions() {
    }

    /**
     * Get the enableDtmfBroadcastInGroupCalls property: Enabling DTMF broadcast allows DTMF tones to be transmitted
     * during group calls.
     * 
     * @return the enableDtmfBroadcastInGroupCalls value.
     */
    public boolean isEnableDtmfBroadcastInGroupCalls() {
        return this.enableDtmfBroadcastInGroupCalls;
    }

    /**
     * Set the enableDtmfBroadcastInGroupCalls property: Enabling DTMF broadcast allows DTMF tones to be transmitted
     * during group calls.
     * 
     * @param enableDtmfBroadcastInGroupCalls the enableDtmfBroadcastInGroupCalls value to set.
     * @return the DtmfConfigurationOptions object itself.
     */
    public DtmfConfigurationOptions setEnableDtmfBroadcastInGroupCalls(boolean enableDtmfBroadcastInGroupCalls) {
        this.enableDtmfBroadcastInGroupCalls = enableDtmfBroadcastInGroupCalls;
        return this;
    }
}
