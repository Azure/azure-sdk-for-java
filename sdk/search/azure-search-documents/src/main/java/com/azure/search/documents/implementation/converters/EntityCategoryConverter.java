// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.indexes.models.EntityCategory;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.EntityCategory} and
 * {@link EntityCategory}.
 */
public final class EntityCategoryConverter {
    private static final ClientLogger LOGGER = new ClientLogger(EntityCategoryConverter.class);

    /**
     * Maps from enum {@link com.azure.search.documents.indexes.implementation.models.EntityCategory} to enum
     * {@link EntityCategory}.
     */
    public static EntityCategory map(com.azure.search.documents.indexes.implementation.models.EntityCategory obj) {
        if (obj == null) {
            return null;
        }
        return EntityCategory.fromString(obj.toString());
    }

    /**
     * Maps from enum {@link EntityCategory} to enum
     * {@link com.azure.search.documents.indexes.implementation.models.EntityCategory}.
     */
    public static com.azure.search.documents.indexes.implementation.models.EntityCategory map(EntityCategory obj) {
        if (obj == null) {
            return null;
        }
        return com.azure.search.documents.indexes.implementation.models.EntityCategory.fromString(obj.toString());
    }

    private EntityCategoryConverter() {
    }
}
