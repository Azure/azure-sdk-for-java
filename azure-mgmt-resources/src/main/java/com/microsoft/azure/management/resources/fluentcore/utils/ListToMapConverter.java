/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class to convert a list of inners to map of implementation.
 * @param <ImplT> implementation of the interface
 * @param <InnerT> inner class which needs to be wrapped
 */
public abstract class ListToMapConverter<ImplT, InnerT> {
    protected abstract String name(InnerT innerT);

    protected abstract ImplT impl(InnerT innerT);

    /**
     * Converts the passed list of inners to unmodifiable map of impls.
     * @param innerList list of the inners.
     * @return map of the impls
     */
    public Map<String, ImplT> convertToUnmodifiableMap(List<InnerT> innerList) {
        Map<String, ImplT> result = new HashMap<>();
        for (InnerT inner : innerList) {
            result.put(name(inner), impl(inner));
        }

        return Collections.unmodifiableMap(result);
    }
}
