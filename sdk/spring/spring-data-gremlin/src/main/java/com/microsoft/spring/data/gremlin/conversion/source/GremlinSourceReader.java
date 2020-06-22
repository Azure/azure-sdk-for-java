// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.spring.data.gremlin.conversion.source;

import com.microsoft.spring.data.gremlin.conversion.MappingGremlinConverter;

/**
 * Provider Entity type dependent read method.
 */
public interface GremlinSourceReader<T> {
    /**
     * Read data from GremlinSource to domain
     */
    T read(Class<T> domainClass, MappingGremlinConverter converter, GremlinSource<T> source);
}
