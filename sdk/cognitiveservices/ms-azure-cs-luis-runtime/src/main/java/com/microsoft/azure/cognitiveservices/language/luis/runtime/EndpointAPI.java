/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.language.luis.runtime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.microsoft.rest.ExpandableStringEnum;

import java.util.Collection;

/**
 * Defines values for LUIS endpoint API.
 */
public final class EndpointAPI extends ExpandableStringEnum<EndpointAPI> {
    /** Static value US_WEST for EndpointAPI. */
    public static final EndpointAPI US_WEST = fromString("westus.api.cognitive.microsoft.com");

    /** Static value US_WEST2 for EndpointAPI. */
    public static final EndpointAPI US_WEST2 = fromString("westus2.api.cognitive.microsoft.com");

    /** Static value US_EAST for EndpointAPI. */
    public static final EndpointAPI US_EAST = fromString("eastus.api.cognitive.microsoft.com");

    /** Static value US_EAST2 for EndpointAPI. */
    public static final EndpointAPI US_EAST2 = fromString("eastus2.api.cognitive.microsoft.com");

    /** Static value US_WEST_CENTRAL for EndpointAPI. */
    public static final EndpointAPI US_WEST_CENTRAL = fromString("westcentralus.api.cognitive.microsoft.com");

    /** Static value US_SOUTH_CENTRAL for EndpointAPI. */
    public static final EndpointAPI US_SOUTH_CENTRAL = fromString("southcentralus.api.cognitive.microsoft.com");

    /** Static value EUROPE_WEST for EndpointAPI. */
    public static final EndpointAPI EUROPE_WEST = fromString("westeurope.api.cognitive.microsoft.com");

    /** Static value EUROPE_NORTH for EndpointAPI. */
    public static final EndpointAPI EUROPE_NORTH = fromString("northeurope.api.cognitive.microsoft.com");

    /** Static value ASIA_SOUTHEAST for EndpointAPI. */
    public static final EndpointAPI ASIA_SOUTHEAST = fromString("southeastasia.api.cognitive.microsoft.com");

    /** Static value ASIA_EAST for EndpointAPI. */
    public static final EndpointAPI ASIA_EAST = fromString("eastasia.api.cognitive.microsoft.com");

    /** Static value AUSTRALIA_EAST for EndpointAPI. */
    public static final EndpointAPI AUSTRALIA_EAST = fromString("australiaeast.api.cognitive.microsoft.com");

    /** Static value BRAZIL_SOUTH for EndpointAPI. */
    public static final EndpointAPI BRAZIL_SOUTH = fromString("brazilsouth.api.cognitive.microsoft.com");

    /**
     * Creates or finds a EndpointAPI from its string representation.
     * @param name a name to look for
     * @return the corresponding EndpointAPI
     */
    @JsonCreator
    public static EndpointAPI fromString(String name) {
        return fromString(name, EndpointAPI.class);
    }

    /**
     * @return known CatalogCollationType values
     */
    public static Collection<EndpointAPI> values() {
        return values(EndpointAPI.class);
    }
}
