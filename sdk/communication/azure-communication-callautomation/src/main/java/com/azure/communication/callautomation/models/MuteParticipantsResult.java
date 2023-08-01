// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.implementation.accesshelpers.MuteParticipantsResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.models.MuteParticipantsResultInternal;
import com.azure.core.annotation.Immutable;

import java.util.Objects;

/** The MuteParticipantsResult model. */
@Immutable
public final class MuteParticipantsResult {
    /*
     * The operation context provided by client.
     */
    private final String operationContext;

    static {
        MuteParticipantsResponseConstructorProxy.setAccessor(MuteParticipantsResult::new);
    }

    /**
     * Public constructor.
     *
     */
    public MuteParticipantsResult() {
        this.operationContext = null;
    }

    /**
     * Package-private constructor of the class, used internally only.
     *
     * @param  muteParticipantsResultInternal The response from the service
     */
    MuteParticipantsResult(MuteParticipantsResultInternal muteParticipantsResultInternal) {
        Objects.requireNonNull(muteParticipantsResultInternal, "muteParticipantsResultInternal must not be null");

        this.operationContext = muteParticipantsResultInternal.getOperationContext();
    }

    /**
     * Get the operationContext property: The operation context provided by client.
     *
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return this.operationContext;
    }
}
