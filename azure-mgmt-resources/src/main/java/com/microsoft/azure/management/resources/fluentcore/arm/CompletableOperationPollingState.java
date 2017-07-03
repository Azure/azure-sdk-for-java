/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm;

import com.microsoft.azure.PollingState;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

/**
 * Implementation of PollingOperationState for completable long running operation (the operations
 * that does not return a value on completion).
 */
public class CompletableOperationPollingState extends
        PollingOperationState<Void, CompletableOperationPollingState.VoidResult> {
    /**
     * Creates CompletableOperationPollingState.
     *
     * @param innerPollingState the inner polling state of the completable resource LRO.
     */
    public CompletableOperationPollingState(PollingState<Void> innerPollingState) {
        super(Void.class);
        this.setInnerPollingState(innerPollingState);
    }

    /**
     * Creates CompletableOperationPollingState from the given polling state represented in json string format.
     *
     * @param serializedPollingState the polling state in json format
     * @return CompletableOperationPollingState
     */
    public static CompletableOperationPollingState deserialize(String serializedPollingState) {
        PollingState<Void> pollingState = (PollingState<Void>)deserializePollingState(serializedPollingState);
        return new CompletableOperationPollingState(pollingState);
    }

    @Override
    public VoidResult result() {
        return null;
    }

    class VoidResult extends WrapperImpl<Void> {
        protected VoidResult(Void innerObject) {
            super(innerObject);
        }
    }
}
