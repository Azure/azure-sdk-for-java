// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.repository.query;

import com.microsoft.azure.spring.data.cosmosdb.core.ReactiveCosmosOperations;
import com.microsoft.azure.spring.data.cosmosdb.core.query.DocumentQuery;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.ResultProcessor;

public abstract class AbstractReactiveCosmosQuery implements RepositoryQuery {

    private final ReactiveCosmosQueryMethod method;
    private final ReactiveCosmosOperations operations;

    public AbstractReactiveCosmosQuery(ReactiveCosmosQueryMethod method,
                                       ReactiveCosmosOperations operations) {
        this.method = method;
        this.operations = operations;
    }

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
            throw new IllegalArgumentException("Paged Query is not supported by reactive cosmos " +
                "db");
        } else if (isExistsQuery()) {
            return new ReactiveCosmosQueryExecution.ExistsExecution(operations);
        } else {
            return new ReactiveCosmosQueryExecution.MultiEntityExecution(operations);
        }
    }

    public ReactiveCosmosQueryMethod getQueryMethod() {
        return method;
    }

    protected abstract DocumentQuery createQuery(ReactiveCosmosParameterAccessor accessor);

    protected abstract boolean isDeleteQuery();

    protected abstract boolean isExistsQuery();

}
