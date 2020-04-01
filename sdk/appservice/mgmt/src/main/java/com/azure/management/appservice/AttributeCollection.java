/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class AttributeCollection<T> {
    private final List<T> values = new ArrayList<>();

    T addValue(T value) {
        values.add(value);
        return value;
    }

    Collection<T> getAllValues() {
        return Collections.unmodifiableCollection(new ArrayList<>(values));
    }
}
