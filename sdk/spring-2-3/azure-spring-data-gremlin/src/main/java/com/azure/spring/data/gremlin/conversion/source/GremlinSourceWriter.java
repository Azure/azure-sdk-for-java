// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.conversion.source;

import com.azure.spring.data.gremlin.conversion.MappingGremlinConverter;

/**
 * Provider Entity type dependent write method.
 */
public interface GremlinSourceWriter<T> {
    /**
     * Write the domain class information to GremlinSource
     *
     * @param domain The domain object needed to be written into the gremlin source.
     * @param converter The entity converter.
     * @param source The gremlin source to write the information from domain object.
     */
    void write(Object domain, MappingGremlinConverter converter, GremlinSource<T> source);
}
