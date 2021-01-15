// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.query.query;

import com.azure.spring.data.gremlin.query.GremlinOperations;
import com.azure.spring.data.gremlin.query.paramerter.GremlinParameterAccessor;
import com.azure.spring.data.gremlin.query.paramerter.GremlinParametersParameterAccessor;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.ResultProcessor;
import org.springframework.lang.NonNull;

public abstract class AbstractGremlinQuery implements RepositoryQuery {

    private final GremlinQueryMethod method;
    private final GremlinOperations operations;

    public AbstractGremlinQuery(@NonNull GremlinQueryMethod method, @NonNull GremlinOperations operations) {
        this.method = method;
        this.operations = operations;
    }

    protected abstract GremlinQuery createQuery(GremlinParameterAccessor accessor);

    protected boolean isDeleteQuery() {
        // panli: always return false as only take care find in one PR.
        return false;
    }

    @Override
    public Object execute(@NonNull Object[] parameters) {
        final GremlinParameterAccessor accessor = new GremlinParametersParameterAccessor(this.method, parameters);

        final GremlinQuery query = this.createQuery(accessor);
        final ResultProcessor processor = method.getResultProcessor().withDynamicProjection(accessor);
        final GremlinQueryExecution execution = this.getExecution();

        return execution.execute(query, processor.getReturnedType().getDomainType());
    }

    @Override
    @NonNull
    public GremlinQueryMethod getQueryMethod() {
        return this.method;
    }

    @NonNull
    private GremlinQueryExecution getExecution() {
        if (this.isDeleteQuery()) {
            throw new UnsupportedOperationException("Not implemented yet");
        } else {
            return new GremlinQueryExecution.FindExecution(this.operations);
        }
    }
}
