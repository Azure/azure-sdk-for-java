// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.spring.data.gremlin.conversion.result;

import com.microsoft.spring.data.gremlin.conversion.source.GremlinSource;
import org.apache.tinkerpop.gremlin.driver.Result;

import java.util.List;

public interface GremlinResultsReader {
    /**
     * Read the Gremlin returned Results to GremlinSource.
     */
    <T> void read(List<Result> results, GremlinSource<T> source);
}
