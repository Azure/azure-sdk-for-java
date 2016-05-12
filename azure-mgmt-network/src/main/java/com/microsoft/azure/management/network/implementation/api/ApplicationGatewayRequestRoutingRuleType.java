/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for ApplicationGatewayRequestRoutingRuleType.
 */
public class ApplicationGatewayRequestRoutingRuleType {
    /** Static value Basic for ApplicationGatewayRequestRoutingRuleType. */
    public static final String BASIC = "Basic";

    /** Static value PathBasedRouting for ApplicationGatewayRequestRoutingRuleType. */
    public static final String PATHBASEDROUTING = "PathBasedRouting";

}
