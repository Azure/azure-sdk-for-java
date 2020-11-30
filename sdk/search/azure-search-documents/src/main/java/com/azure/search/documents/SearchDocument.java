// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an untyped document returned from a search or document lookup.
 */
public final class SearchDocument extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    /**
     * Initializes a new instance of the SearchDocument class.
     */
    public SearchDocument() {
        super();
    }

    /**
     * Initializes a new instance of the SearchDocument class with initial values.
     *
     * @param propertyMap Initial values of the document.
     */
    public SearchDocument(Map<? extends String, ?> propertyMap) {
        super(propertyMap);
    }
}
