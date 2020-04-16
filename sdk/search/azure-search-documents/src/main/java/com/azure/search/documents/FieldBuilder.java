// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.search.documents.models.ComplexSearchField;
import com.azure.search.documents.models.SimpleDataType;
import com.azure.search.documents.models.SimpleSearchField;

/**
 * Helper to build search field
 */
public class FieldBuilder {
    public static SimpleSearchField getSimpleSearchField() {
        return new SimpleSearchField();
    }

    public static ComplexSearchField getComplexSearchField() {
        return new ComplexSearchField();
    }
}

