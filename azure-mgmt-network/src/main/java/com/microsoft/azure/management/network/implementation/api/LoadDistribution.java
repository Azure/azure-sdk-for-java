/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for LoadDistribution.
 */
public class LoadDistribution {
    /** Static value Default for LoadDistribution. */
    public static final String DEFAULT = "Default";

    /** Static value SourceIP for LoadDistribution. */
    public static final String SOURCEIP = "SourceIP";

    /** Static value SourceIPProtocol for LoadDistribution. */
    public static final String SOURCEIPPROTOCOL = "SourceIPProtocol";

}
