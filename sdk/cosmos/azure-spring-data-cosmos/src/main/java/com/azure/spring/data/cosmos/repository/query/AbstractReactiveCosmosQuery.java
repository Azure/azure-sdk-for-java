// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.query;

import com.azure.spring.data.cosmos.core.ReactiveCosmosOperations;
import com.azure.spring.data.cosmos.core.query.CosmosQuery;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.ResultProcessor;
import org.springframework.data.repository.query.ReturnedType;
import reactor.core.publisher.Mono;

/**
 * Abstract class for reactive cosmos query.
 */
public abstract class AbstractReactiveCosmosQuery implements RepositoryQuery {

    private final ReactiveCosmosQueryMethod method;
    protected final ReactiveCosmosOperations operations;

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
    @Override
    public Object execute(Object[] parameters) {
        final ReactiveCosmosParameterAccessor accessor =
            new ReactiveCosmosParameterParameterAccessor(method, parameters);
        final CosmosQuery query = createQuery(accessor);

        final ResultProcessor processor =
            method.getResultProcessor().withDynamicProjection(accessor);
        final String containerName =
            ((ReactiveCosmosEntityMetadata) method.getEntityInformation()).getContainerName();

        final ReactiveCosmosQueryExecution execution = getExecution(processor.getReturnedType());
        return execution.execute(query, processor.getReturnedType().getDomainType(), containerName);
    }

    /**
     * Determines the appropriate execution path for a reactive query
     *
     * @throws IllegalArgumentException if execution requires paging
     * @param returnedType The return type of the method
     * @return the execution type needed to handle the query
     */
    protected ReactiveCosmosQueryExecution getExecution(ReturnedType returnedType) {
        if (isDeleteQuery()) {
            return new ReactiveCosmosQueryExecution.DeleteExecution(operations);
        } else if (isPageQuery()) {
            throw new IllegalArgumentException("Paged Query is not supported by reactive cosmos "
                + "db");
        } else if (isExistsQuery()) {
            return new ReactiveCosmosQueryExecution.ExistsExecution(operations);
        } else if (isCountQuery()) {
            return new ReactiveCosmosQueryExecution.CountExecution(operations);
        } else if (isReactiveSingleResultQuery()) {
            return new ReactiveCosmosQueryExecution.SingleEntityExecution(operations, returnedType);
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

    protected abstract CosmosQuery createQuery(ReactiveCosmosParameterAccessor accessor);

    protected abstract boolean isDeleteQuery();

    protected abstract boolean isExistsQuery();

    protected abstract boolean isCountQuery();

    protected boolean isPageQuery() {
        return method.isPageQuery();
    }

    private boolean isReactiveSingleResultQuery() {
        return method.getReactiveWrapper() != null && method.getReactiveWrapper().equals(Mono.class);
    }

}
