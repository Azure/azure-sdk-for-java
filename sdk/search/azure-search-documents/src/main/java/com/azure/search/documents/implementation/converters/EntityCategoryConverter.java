package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.EntityCategory;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.EntityCategory} and
 * {@link EntityCategory} mismatch.
 */
public final class EntityCategoryConverter {
    public static EntityCategory convert(com.azure.search.documents.models.EntityCategory obj) {
        return DefaultConverter.convert(obj, EntityCategory.class);
    }

    public static com.azure.search.documents.models.EntityCategory convert(EntityCategory obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.EntityCategory.class);
    }
}
