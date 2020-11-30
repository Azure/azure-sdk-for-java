// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.conversion.result;

import com.azure.spring.data.gremlin.conversion.source.GremlinSource;
import org.apache.tinkerpop.gremlin.driver.Result;

import java.util.List;

public interface GremlinResultsReader {
    /**
     * Read the Gremlin returned Results to GremlinSource.
     *
     * @param <T> The type of the source domain.
     * @param results Results retrieved from the Gremlin server.
     * @param source The GremlinSource of the results.
     */
    <T> void read(List<Result> results, GremlinSource<T> source);
}
