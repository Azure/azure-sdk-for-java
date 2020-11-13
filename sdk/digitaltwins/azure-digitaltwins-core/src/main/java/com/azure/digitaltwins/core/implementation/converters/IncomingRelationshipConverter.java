// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.implementation.converters;

import com.azure.digitaltwins.core.models.IncomingRelationship;

/**
 * A converter between {@link com.azure.digitaltwins.core.implementation.models.IncomingRelationship} and
 * {@link IncomingRelationship}.
 */
public final class IncomingRelationshipConverter {

    /**
     * Maps from {@link com.azure.digitaltwins.core.implementation.models.IncomingRelationship} to
     * {@link IncomingRelationship}. If the input is null, then the output will be null as well.
     */
    public static IncomingRelationship map(com.azure.digitaltwins.core.implementation.models.IncomingRelationship input) {
        if (input == null) {
            return null;
        }
        
        return new IncomingRelationship(
            input.getRelationshipId(),
            input.getSourceId(),
            input.getRelationshipName(),
            input.getRelationshipLink());
    }

    /**
     * Maps from {@link IncomingRelationship} to
     * {@link com.azure.digitaltwins.core.implementation.models.IncomingRelationship}. If the input is null, then the output will be null as well.
     */
    public static com.azure.digitaltwins.core.implementation.models.IncomingRelationship map(IncomingRelationship input) {
        if (input == null) {
            return null;
        }

        com.azure.digitaltwins.core.implementation.models.IncomingRelationship mappedIncomingRelationship = new com.azure.digitaltwins.core.implementation.models.IncomingRelationship();
        mappedIncomingRelationship.setRelationshipId(input.getRelationshipId());
        mappedIncomingRelationship.setSourceId(input.getSourceId());
        mappedIncomingRelationship.setRelationshipName(input.getRelationshipName());
        mappedIncomingRelationship.setRelationshipLink(input.getRelationshipLink());
        return mappedIncomingRelationship;
    }

    private IncomingRelationshipConverter() {}
}
