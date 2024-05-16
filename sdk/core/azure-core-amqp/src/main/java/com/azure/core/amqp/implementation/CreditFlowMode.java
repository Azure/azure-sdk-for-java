// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

/**
 * Indicates how to compute the credit and when to send it to the broker via a flow performative.
 */
public enum CreditFlowMode {
    /**
     * When the number of total messages emitted 'n' since the last broker flow is greater than or equal
     * to a fraction (e.g. 0.5) of the Prefetch, the message-flux will send a flow for the credit 'n'.
     */
    EmissionDriven,
    /**
     * When the accumulated downstream request 'n' since the last broker flow is greater than or equal to
     * the Prefetch, the message-flux will send a flow for the credit 'n'.
     */
    RequestDriven
}
