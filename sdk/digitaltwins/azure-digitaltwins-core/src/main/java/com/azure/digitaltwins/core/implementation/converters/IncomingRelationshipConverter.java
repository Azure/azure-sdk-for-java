package com.azure.digitaltwins.core.implementation.converters;

import com.azure.digitaltwins.core.models.IncomingRelationship;

/**
 * A converter between {@link com.azure.digitaltwins.core.implementation.models.IncomingRelationship} and
 * {@link IncomingRelationship}.
 */
public final class IncomingRelationshipConverter {

    /**
     * Maps from {@link com.azure.digitaltwins.core.implementation.models.IncomingRelationship} to
     * {@link IncomingRelationship}.
     */
    public static IncomingRelationship map(com.azure.digitaltwins.core.implementation.models.IncomingRelationship input) {
        IncomingRelationship mappedIncomingRelationship = new IncomingRelationship();
        mappedIncomingRelationship.setRelationshipId(input.getRelationshipId());
        mappedIncomingRelationship.setSourceId(input.getSourceId());
        mappedIncomingRelationship.setRelationshipName(input.getRelationshipName());
        mappedIncomingRelationship.setRelationshipLink(input.getRelationshipLink());
        return mappedIncomingRelationship;
    }

    /**
     * Maps from {@link IncomingRelationship} to
     * {@link com.azure.digitaltwins.core.implementation.models.IncomingRelationship}.
     */
    public static com.azure.digitaltwins.core.implementation.models.IncomingRelationship map(IncomingRelationship input) {
        com.azure.digitaltwins.core.implementation.models.IncomingRelationship mappedIncomingRelationship = new com.azure.digitaltwins.core.implementation.models.IncomingRelationship();
        mappedIncomingRelationship.setRelationshipId(input.getRelationshipId());
        mappedIncomingRelationship.setSourceId(input.getSourceId());
        mappedIncomingRelationship.setRelationshipName(input.getRelationshipName());
        mappedIncomingRelationship.setRelationshipLink(input.getRelationshipLink());
        return mappedIncomingRelationship;
    }

    private IncomingRelationshipConverter() {}
}
