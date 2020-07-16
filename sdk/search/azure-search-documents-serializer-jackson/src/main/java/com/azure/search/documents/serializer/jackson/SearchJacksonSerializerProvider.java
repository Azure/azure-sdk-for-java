// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.serializer.jackson;

import com.azure.search.documents.serializer.SearchSerializer;
import com.azure.search.documents.serializer.SearchSerializerProvider;

public class SearchJacksonSerializerProvider implements SearchSerializerProvider {
    @Override
    public SearchSerializer createInstance() {
        return new SearchJacksonSerializerBuilder().build();
    }
}
