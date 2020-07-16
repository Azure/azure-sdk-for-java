// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.serializer;

import java.io.Reader;

public interface SearchSerializer {
    public <T> T convertValue(Object fromValue, SearchType<T> type);
    public <T> T convertValue(Object fromValue, SearchType<T> type, SerializationInclusion inclusion);
    public <T> T convertValue(Object fromValue, Class<T> clazz);
    public <T> T convertValue(Object fromValue, Class<T> clazz, SerializationInclusion inclusion);
    public <T> T readValue(Reader fromValue, SearchType<T> type);
    public <T> T readValue(String fromValue, SearchType<T> type);
}
