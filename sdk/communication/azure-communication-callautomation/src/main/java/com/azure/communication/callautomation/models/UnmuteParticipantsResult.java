// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.implementation.accesshelpers.UnmuteParticipantsResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.models.UnmuteParticipantsResponseInternal;
import com.azure.core.annotation.Immutable;

import java.util.Objects;

/** The UnmuteParticipantsResult model. */
@Immutable
public final class UnmuteParticipantsResult {
    /*
     * The operation context provided by client.
     */
    private final String operationContext;

    static {
        UnmuteParticipantsResponseConstructorProxy.setAccessor(UnmuteParticipantsResult::new);
    }

    /**
     * Public constructor.
     *
     */
    public UnmuteParticipantsResult() {
        this.operationContext = null;
    }

    /**
     * Package-private constructor of the class, used internally only.
     *
     * @param  unmuteParticipantsResponseInternal The response from the service
     */
    UnmuteParticipantsResult(UnmuteParticipantsResponseInternal unmuteParticipantsResponseInternal) {
        Objects.requireNonNull(unmuteParticipantsResponseInternal, "unmuteParticipantsResponseInternal must not be null");

        this.operationContext = unmuteParticipantsResponseInternal.getOperationContext();
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
