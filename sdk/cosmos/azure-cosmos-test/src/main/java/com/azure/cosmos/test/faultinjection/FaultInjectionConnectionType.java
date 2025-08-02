// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test.faultinjection;

/***
 * Fault injection connection type.
 */
public enum FaultInjectionConnectionType {
    /***
     * Direct connection type.
     */
    DIRECT,
    /***
     * Gateway connection type.
     */
    GATEWAY,
    /***
     * Gateway connection type to V2 endpoint
     */
    GATEWAY_V2
}
