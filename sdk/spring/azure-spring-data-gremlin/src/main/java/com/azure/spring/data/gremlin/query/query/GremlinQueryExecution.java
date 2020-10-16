// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.query.query;

import com.azure.spring.data.gremlin.common.GremlinUtils;
import com.azure.spring.data.gremlin.conversion.source.GremlinSource;
import com.azure.spring.data.gremlin.query.GremlinOperations;
import org.springframework.lang.NonNull;

public interface GremlinQueryExecution {
    Object execute(GremlinQuery query, Class<?> type);

    final class FindExecution implements GremlinQueryExecution {

        private final GremlinOperations operations;

        public FindExecution(@NonNull GremlinOperations operations) {
            this.operations = operations;
        }

        @Override
        public Object execute(@NonNull GremlinQuery query, @NonNull Class<?> domainClass) {
            final GremlinSource<?> source = GremlinUtils.toGremlinSource(domainClass);

            return this.operations.find(query, source);
        }
    }
}
