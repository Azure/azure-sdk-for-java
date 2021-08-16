// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.query;

import com.azure.spring.data.cosmos.core.CosmosOperations;
import com.azure.spring.data.cosmos.core.query.CosmosQuery;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.ResultProcessor;
import org.springframework.data.repository.query.ReturnedType;

/**
 * Abstract class for cosmos query.
 */
public abstract class AbstractCosmosQuery implements RepositoryQuery {

    private final CosmosQueryMethod method;
    protected final CosmosOperations operations;

    /**
     * Initialization
     *
     * @param method CosmosQueryMethod
     * @param operations CosmosOperations
     */
    public AbstractCosmosQuery(CosmosQueryMethod method, CosmosOperations operations) {
        this.method = method;
        this.operations = operations;
    }

    /**
     * Executes the {@link AbstractCosmosQuery} with the given parameters.
     *
     * @param parameters must not be {@literal null}.
     * @return execution result. Can be {@literal null}.
     */
    @Override
    public Object execute(Object[] parameters) {
        final CosmosParameterAccessor accessor = new CosmosParameterParameterAccessor(method, parameters);
        final CosmosQuery query = createQuery(accessor);

        final ResultProcessor processor = method.getResultProcessor().withDynamicProjection(accessor);
        final String container = ((CosmosEntityMetadata) method.getEntityInformation()).getContainerName();

        final CosmosQueryExecution execution = getExecution(accessor, processor.getReturnedType());

        return execution.execute(query, processor.getReturnedType().getDomainType(), container);
    }


    /**
     * Determines the appropriate execution path for a query
     *
     * @param returnedType The return type of the method
     * @param accessor Object for accessing method parameters
     * @return the execution type needed to handle the query
     */
    protected CosmosQueryExecution getExecution(CosmosParameterAccessor accessor, ReturnedType returnedType) {
        if (isDeleteQuery()) {
            return new CosmosQueryExecution.DeleteExecution(operations);
        } else if (isPageQuery()) {
            return new CosmosQueryExecution.PagedExecution(operations, accessor.getPageable());
        } else if (isSliceQuery()) {
            return new CosmosQueryExecution.SliceExecution(operations, accessor.getPageable());
        } else if (isExistsQuery()) {
            return new CosmosQueryExecution.ExistsExecution(operations);
        } else if (isCountQuery()) {
            return new CosmosQueryExecution.CountExecution(operations);
        } else if (isCollectionQuery()) {
            return new CosmosQueryExecution.MultiEntityExecution(operations);
        } else {
            return new CosmosQueryExecution.SingleEntityExecution(operations, returnedType);
        }
    }

    /**
     * Get method of query
     *
     * @return CosmosQueryMethod
     */
    public CosmosQueryMethod getQueryMethod() {
        return method;
    }

    protected abstract CosmosQuery createQuery(CosmosParameterAccessor accessor);

    protected abstract boolean isDeleteQuery();

    protected abstract boolean isExistsQuery();

    protected abstract boolean isCountQuery();

    protected boolean isPageQuery() {
        return method.isPageQuery();
    }

    protected boolean isCollectionQuery() {
        return method.isCollectionQuery();
    }

    protected boolean isSliceQuery() {
        return method.isSliceQuery();
    }

}
