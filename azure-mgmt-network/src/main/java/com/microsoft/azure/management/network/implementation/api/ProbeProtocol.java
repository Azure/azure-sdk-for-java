/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for ProbeProtocol.
 */
public class ProbeProtocol {
    /** Static value Http for ProbeProtocol. */
    public static final String HTTP = "Http";

    /** Static value Tcp for ProbeProtocol. */
    public static final String TCP = "Tcp";

}
