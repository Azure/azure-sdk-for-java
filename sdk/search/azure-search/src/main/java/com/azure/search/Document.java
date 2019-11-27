// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import java.util.HashMap;

/**
 * Represents a document
 * A property bag is used for scenarios where the index schema is only known at run-time
 * If the schema is known, user can convert the properties to a specific object type
 */
public class Document extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;
}
