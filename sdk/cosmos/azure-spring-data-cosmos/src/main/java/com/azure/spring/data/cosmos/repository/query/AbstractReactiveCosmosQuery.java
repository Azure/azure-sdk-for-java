// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.query;

import com.azure.spring.data.cosmos.core.ReactiveCosmosOperations;
import com.azure.spring.data.cosmos.core.query.DocumentQuery;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.ResultProcessor;

/**
 * Abstract class for reactive cosmos query.
 */
public abstract class AbstractReactiveCosmosQuery implements RepositoryQuery {

    private final ReactiveCosmosQueryMethod method;
    private final ReactiveCosmosOperations operations;

    /**
     * Initialization
     *
     * @param method ReactiveCosmosQueryMethod
     * @param operations ReactiveCosmosOperations
     */
    public AbstractReactiveCosmosQuery(ReactiveCosmosQueryMethod method,
                                       ReactiveCosmosOperations operations) {
        this.method = method;
        this.operations = operations;
    }

    /**
     * Executes the {@link AbstractReactiveCosmosQuery} with the given parameters.
     *
     * @param parameters must not be {@literal null}.
     * @return execution result. Can be {@literal null}.
     */
    public Object execute(Object[] parameters) {
        final ReactiveCosmosParameterAccessor accessor =
            new ReactiveCosmosParameterParameterAccessor(method, parameters);
        final DocumentQuery query = createQuery(accessor);

        final ResultProcessor processor =
            method.getResultProcessor().withDynamicProjection(accessor);
        final String containerName =
            ((ReactiveCosmosEntityMetadata) method.getEntityInformation()).getContainerName();

        final ReactiveCosmosQueryExecution execution = getExecution(accessor);
        return execution.execute(query, processor.getReturnedType().getDomainType(), containerName);
    }


    private ReactiveCosmosQueryExecution getExecution(ReactiveCosmosParameterAccessor accessor) {
        if (isDeleteQuery()) {
            return new ReactiveCosmosQueryExecution.DeleteExecution(operations);
        } else if (method.isPageQuery()) {
            throw new IllegalArgumentException("Paged Query is not supported by reactive cosmos "
                + "db");
        } else if (isExistsQuery()) {
            return new ReactiveCosmosQueryExecution.ExistsExecution(operations);
        } else {
            return new ReactiveCosmosQueryExecution.MultiEntityExecution(operations);
        }
    }

    /**
     * Get method of query
     *
     * @return ReactiveCosmosQueryMethod
     */
    public ReactiveCosmosQueryMethod getQueryMethod() {
        return method;
    }

    protected abstract DocumentQuery createQuery(ReactiveCosmosParameterAccessor accessor);

    protected abstract boolean isDeleteQuery();

    protected abstract boolean isExistsQuery();

}
