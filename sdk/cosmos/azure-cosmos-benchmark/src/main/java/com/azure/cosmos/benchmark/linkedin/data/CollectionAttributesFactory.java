// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.data;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;


public class CollectionAttributesFactory {

    private static final CollectionAttributes INVITATIONS_COLLECTION_ATTRIBUTES
        = new InvitationsCollectionAttributes();

    private CollectionAttributesFactory() {
    }

    /**
     * TODO: Return different CollectionAttributes depending on the entity types, as new entities are added
     *
     * @param entityName Name of the entity for which we are requesting it's collection attributes
     * @return Entity specific Collection attributes
     */
    public static CollectionAttributes getCollectionAttributes(final String entityName) {
        Preconditions.checkArgument(StringUtils.isNotBlank(entityName),
            "The associated entity can not be null");

        return INVITATIONS_COLLECTION_ATTRIBUTES;
    }
}
