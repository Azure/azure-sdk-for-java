/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.PollingState;
import com.microsoft.azure.management.compute.implementation.OperationStatusResponseInner;
import com.microsoft.azure.management.resources.fluentcore.arm.PollingOperationState;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import org.joda.time.DateTime;

/**
 * Implementation of PollingOperationState for VM restart long running operation.
 */
public class VirtualMachineRestartPollingState
        extends PollingOperationState<OperationStatusResponseInner, VirtualMachineRestartPollingState.Result> {

    /**
     * Creates VirtualMachineRestartPollingState.
     *
     * @param innerPollingState the inner polling state of the VM restart LRO.
     */
    public VirtualMachineRestartPollingState(PollingState<OperationStatusResponseInner> innerPollingState) {
        super(OperationStatusResponseInner.class);
        this.setInnerPollingState(innerPollingState);
    }

    /**
     * Creates VirtualMachineRestartPollingState from the given polling state represented in json string format.
     *
     * @param serializedPollingState the polling state in json format
     * @return VirtualMachineRestartPollingState
     */
    public static VirtualMachineRestartPollingState deserialize(String serializedPollingState) {
        PollingState<OperationStatusResponseInner> pollingState = (PollingState<OperationStatusResponseInner>)deserializePollingState(serializedPollingState);
        return new VirtualMachineRestartPollingState(pollingState);
    }

    @Override
    public Result result() {
        if (this.innerResult() != null) {
            return new Result(this.innerResult());
        }
        return null;
    }

    class Result extends WrapperImpl<OperationStatusResponseInner> {
        protected Result(OperationStatusResponseInner innerObject) {
            super(innerObject);
        }

        public String operationId() {
           return this.inner().name();
        }

        public String status() {
            return this.inner().status();
        }

        public DateTime startTime() {
            return this.inner().startTime();
        }

        public DateTime endTime() {
            return this.inner().endTime();
        }
    }
}
