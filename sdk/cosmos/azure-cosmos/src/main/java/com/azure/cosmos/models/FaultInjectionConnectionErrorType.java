// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

public enum FaultInjectionConnectionErrorType {
    /***
     * Simulate connection close exception.
     */
    CONNECTION_CLOSE,

    /***
     * Simulate connection reset exception.
     */
    CONNECTION_RESET
}
