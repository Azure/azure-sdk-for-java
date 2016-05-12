/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for IPAllocationMethod.
 */
public class IPAllocationMethod {
    /** Static value Static for IPAllocationMethod. */
    public static final String STATIC = "Static";

    /** Static value Dynamic for IPAllocationMethod. */
    public static final String DYNAMIC = "Dynamic";

}
