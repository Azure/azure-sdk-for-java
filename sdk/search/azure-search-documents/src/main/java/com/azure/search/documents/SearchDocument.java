// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a document
 * <p>
 * A property bag is used for scenarios where the index schema is only known at run-time
 * <p>
 * If the schema is known, user can convert the properties to a specific object type
 */
public final class SearchDocument extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    /**
     * Default empty constructor
     */
    public SearchDocument() {
        super();
    }

    /**
     * Constructs a new {@code Document} with the same mappings as the specified {@code Map}.
     *
     * @param propertyMap the map whose mappings are to be placed in this map
     */
    public SearchDocument(Map<? extends String, ?> propertyMap) {
        super(propertyMap);
    }
}
