// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.serializer.gson;

import com.azure.search.documents.serializer.SearchSerializer;
import com.azure.search.documents.serializer.SearchSerializerProvider;

public class SearchGsonSerializerProvider implements SearchSerializerProvider {
    @Override
    public SearchSerializer createInstance() {
        return new SearchGsonSerializerBuilder().build();
    }
}
