// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.serializer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class SearchType<T> implements Comparable<SearchType<T>>{
    private final Type _type;

    public SearchType() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof Class) {
            throw new IllegalArgumentException(
                "Internal error: TypeReference constructed without actual type information");
        } else {
            this._type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
        }
    }

    public Type getType() {
        return _type;
    }

    @Override
    public int compareTo(SearchType<T> o) { return 0; }

}
