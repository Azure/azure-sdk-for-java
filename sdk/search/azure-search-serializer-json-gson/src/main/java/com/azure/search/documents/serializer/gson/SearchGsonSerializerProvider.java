// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.serializer.gson;

public class SearchGsonSerializerProvider {
    @Override
    public SearchSerializer createInstance() {
        return new SearchGsonSerializerBuilder().build();
    }
}
