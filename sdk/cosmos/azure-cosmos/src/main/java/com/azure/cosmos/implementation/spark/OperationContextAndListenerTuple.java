// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.spark;

public class OperationContextAndListenerTuple {
    private final OperationContext operationContext;
    private final OperationListener operationListener;

    public OperationContextAndListenerTuple(OperationContext operationContext, OperationListener operationListener) {
        this.operationContext = operationContext;
        this.operationListener = operationListener;
    }

    public OperationContext getOperationContext() {
        return operationContext;
    }

    public OperationListener getOperationListener() {
        return operationListener;
    }
}
