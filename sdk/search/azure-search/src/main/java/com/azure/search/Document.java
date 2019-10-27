// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import java.util.LinkedHashMap;

/**
 * Represents a document
 * A property bag is used for scenarios where the index schema is only known at run-time
 * If the schema is known, user can convert the properties to a specific object type
 */
public class Document extends LinkedHashMap<String, Object> {
    private static final long serialVersionUID = 1L;
}
