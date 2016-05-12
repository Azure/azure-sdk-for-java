/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for VpnType.
 */
public class VpnType {
    /** Static value PolicyBased for VpnType. */
    public static final String POLICYBASED = "PolicyBased";

    /** Static value RouteBased for VpnType. */
    public static final String ROUTEBASED = "RouteBased";

}
