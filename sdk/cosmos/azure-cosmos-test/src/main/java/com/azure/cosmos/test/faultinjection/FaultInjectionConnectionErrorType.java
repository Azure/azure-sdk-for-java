// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test.faultinjection;

/***
 * Fault injection connection error type.
 */
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
