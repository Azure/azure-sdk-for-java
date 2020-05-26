// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.EntityCategory;

import static com.azure.search.documents.implementation.util.Constants.ENUM_EXTERNAL_ERROR_MSG;
import static com.azure.search.documents.implementation.util.Constants.ENUM_INTERNAL_ERROR_MSG;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.EntityCategory} and
 * {@link EntityCategory}.
 */
public final class EntityCategoryConverter {
    private static final ClientLogger LOGGER = new ClientLogger(EntityCategoryConverter.class);

    /**
     * Maps from enum {@link com.azure.search.documents.implementation.models.EntityCategory} to enum
     * {@link EntityCategory}.
     */
    public static EntityCategory map(com.azure.search.documents.implementation.models.EntityCategory obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case LOCATION:
                return EntityCategory.LOCATION;
            case ORGANIZATION:
                return EntityCategory.ORGANIZATION;
            case PERSON:
                return EntityCategory.PERSON;
            case QUANTITY:
                return EntityCategory.QUANTITY;
            case DATETIME:
                return EntityCategory.DATETIME;
            case URL:
                return EntityCategory.URL;
            case EMAIL:
                return EntityCategory.EMAIL;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_EXTERNAL_ERROR_MSG, obj)));
        }
    }

    /**
     * Maps from enum {@link EntityCategory} to enum
     * {@link com.azure.search.documents.implementation.models.EntityCategory}.
     */
    public static com.azure.search.documents.implementation.models.EntityCategory map(EntityCategory obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case LOCATION:
                return com.azure.search.documents.implementation.models.EntityCategory.LOCATION;
            case ORGANIZATION:
                return com.azure.search.documents.implementation.models.EntityCategory.ORGANIZATION;
            case PERSON:
                return com.azure.search.documents.implementation.models.EntityCategory.PERSON;
            case QUANTITY:
                return com.azure.search.documents.implementation.models.EntityCategory.QUANTITY;
            case DATETIME:
                return com.azure.search.documents.implementation.models.EntityCategory.DATETIME;
            case URL:
                return com.azure.search.documents.implementation.models.EntityCategory.URL;
            case EMAIL:
                return com.azure.search.documents.implementation.models.EntityCategory.EMAIL;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_INTERNAL_ERROR_MSG, obj)));
        }
    }

    private EntityCategoryConverter() {
    }
}
