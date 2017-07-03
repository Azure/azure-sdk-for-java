/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.PollingState;
import com.microsoft.azure.management.compute.implementation.VirtualMachineCaptureResultInner;
import com.microsoft.azure.management.resources.fluentcore.arm.PollingOperationState;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import rx.exceptions.Exceptions;

/**
 * Implementation of PollingOperationState for VM capture long running operation.
 */
public class VirtualMachineCapturePollingState extends
        PollingOperationState<VirtualMachineCaptureResultInner, VirtualMachineCapturePollingState.Resource> {
    /**
     * Creates VirtualMachineCapturePollingState.
     *
     * @param innerPollingState the inner polling state of the VM capture LRO.
     */
    public VirtualMachineCapturePollingState (PollingState<VirtualMachineCaptureResultInner> innerPollingState) {
        super(VirtualMachineCaptureResultInner.class);
        this.setInnerPollingState(innerPollingState);
    }

    /**
     * Creates VirtualMachineCapturePollingState from the given polling state represented in json string format.
     *
     * @param serializedPollingState the polling state in json format
     * @return VirtualMachineCapturePollingState
     */
    public static VirtualMachineCapturePollingState deserialize(String serializedPollingState) {
        PollingState<VirtualMachineCaptureResultInner> pollingState = (PollingState<VirtualMachineCaptureResultInner>)deserializePollingState(serializedPollingState);
        return new VirtualMachineCapturePollingState(pollingState);
    }

    @Override
    public Resource result() {
        if (this.innerResult() != null) {
            return new Resource(this.innerResult());
        }
        return null;
    }

    class Resource extends WrapperImpl<VirtualMachineCaptureResultInner> {
        protected Resource(VirtualMachineCaptureResultInner innerObject) {
            super(innerObject);
        }

        public String template() {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.writeValueAsString(this.inner().output());
            } catch (JsonProcessingException e) {
                throw Exceptions.propagate(e);
            }
        }
    }
}
