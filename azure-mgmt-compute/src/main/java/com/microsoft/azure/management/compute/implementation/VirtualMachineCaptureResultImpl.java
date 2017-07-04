/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.management.compute.VirtualMachineCaptureResult;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import rx.exceptions.Exceptions;

/**
 * Implementation for VirtualMachineCaptureResult.
 */
class VirtualMachineCaptureResultImpl
        extends WrapperImpl<VirtualMachineCaptureResultInner>
        implements VirtualMachineCaptureResult {
    protected VirtualMachineCaptureResultImpl(VirtualMachineCaptureResultInner innerObject) {
        super(innerObject);
    }

    @Override
    public String template() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this.inner().output());
        } catch (JsonProcessingException e) {
            throw Exceptions.propagate(e);
        }
    }
}
