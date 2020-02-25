// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a document
 * <p>
 * A property bag is used for scenarios where the index schema is only known at run-time
 * <p>
 * If the schema is known, user can convert the properties to a specific object type
 */
public class Document extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    /**
     * Default empty constructor
     */
    public Document() {
        super();
    }

    /**
     * Constructs a new {@code Document} with the same mappings as the specified {@code Map}.
     *
     * @param m the map whose mappings are to be placed in this map
     */
    public Document(Map<? extends String, ?> m) {
        super(m);
    }
}
