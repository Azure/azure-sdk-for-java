// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.conversion.source;

import com.azure.spring.data.gremlin.conversion.MappingGremlinConverter;

/**
 * Provider Entity type dependent read method.
 */
public interface GremlinSourceReader<T> {
    /**
     * Read data from GremlinSource to domain
     *
     * @param domainClass The class type for the domain object.
     * @param converter The entity converter.
     * @param source The gremlin source object.
     *
     * @return The domain object read from gremlin source.
     */
    T read(Class<T> domainClass, MappingGremlinConverter converter, GremlinSource<T> source);
}
