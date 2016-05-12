/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for SecurityRuleDirection.
 */
public class SecurityRuleDirection {
    /** Static value Inbound for SecurityRuleDirection. */
    public static final String INBOUND = "Inbound";

    /** Static value Outbound for SecurityRuleDirection. */
    public static final String OUTBOUND = "Outbound";

}
