// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.query;

import com.azure.spring.data.cosmos.core.CosmosOperations;
import com.azure.spring.data.cosmos.core.query.CosmosPageRequest;
import com.azure.spring.data.cosmos.core.query.CosmosQuery;
import com.azure.spring.data.cosmos.exception.CosmosAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.ReturnedType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Interface to execute cosmos query operations
 */
public interface CosmosQueryExecution {

    /**
     * Declare an execute function for different operations to call
     *
     * @param query document query operation
     * @param type domain type
     * @param container container to conduct query
     * @return Object according to execution result
     */
    Object execute(CosmosQuery query, Class<?> type, String container);

    /**
     * Container operation implementation to execute a container name query
     */
    final class ContainerExecution implements CosmosQueryExecution {

        private final CosmosOperations operations;

        public ContainerExecution(CosmosOperations operations) {
            this.operations = operations;
        }

        @Override
        public Object execute(CosmosQuery query, Class<?> type, String container) {
            return operations.getContainerName(type);
        }
    }

    /**
     * Find operation implementation to execute a find query for multiple items
     */
    final class MultiEntityExecution implements CosmosQueryExecution {

        private final CosmosOperations operations;

        public MultiEntityExecution(CosmosOperations operations) {
            this.operations = operations;
        }

        @Override
        public Object execute(CosmosQuery query, Class<?> type, String container) {
            return operations.find(query, type, container);
        }
    }

    /**
     * Find operation implementation to execute a find query for a single item
     */
    final class SingleEntityExecution implements CosmosQueryExecution {

        private final CosmosOperations operations;
        private final ReturnedType returnedType;

        public SingleEntityExecution(CosmosOperations operations, ReturnedType returnedType) {
            this.operations = operations;
            this.returnedType = returnedType;
        }

        @Override
        public Object execute(CosmosQuery query, Class<?> type, String collection) {
            Iterable<?> resultsIterable = operations.find(query, type, collection);
            final List<Object> results = new ArrayList<>();
            resultsIterable.forEach(results::add);
            final Object result;
            if (results.isEmpty()) {
                result = null;
            } else if (results.size() == 1) {
                result = results.get(0);
            } else {
                throw new CosmosAccessException("Too many results - return type "
                    + returnedType.getReturnedType()
                    + " is not of type Iterable but find returned "
                    + results.size()
                    + " results");
            }

            if (returnedType.getReturnedType() == Optional.class) {
                return result == null ? Optional.empty() : Optional.of(result);
            } else {
                return result;
            }
        }
    }

    /**
     * exist operation implementation to execute a exists query
     */
    final class ExistsExecution implements CosmosQueryExecution {

        private final CosmosOperations operations;

        public ExistsExecution(CosmosOperations operations) {
            this.operations = operations;
        }

        @Override
        public Object execute(CosmosQuery query, Class<?> type, String container) {
            return operations.exists(query, type, container);
        }
    }

    /**
     * delete operation implementation to execute a delete query
     */
    final class DeleteExecution implements CosmosQueryExecution {

        private final CosmosOperations operations;

        public DeleteExecution(CosmosOperations operations) {
            this.operations = operations;
        }

        @Override
        public Object execute(CosmosQuery query, Class<?> type, String container) {
            return operations.delete(query, type, container);
        }
    }

    /**
     * paginationQuery operation implementation to execute a paginationQuery query
     */
    final class PagedExecution implements CosmosQueryExecution {
        private final CosmosOperations operations;
        private final Pageable pageable;

        public PagedExecution(CosmosOperations operations, Pageable pageable) {
            this.operations = operations;
            this.pageable = pageable;
        }

        @Override
        public Object execute(CosmosQuery query, Class<?> type, String container) {
            if (pageable.getPageNumber() != 0
                && !(pageable instanceof CosmosPageRequest)) {
                throw new IllegalStateException("Not the first page but Pageable is not a valid "
                    + "CosmosPageRequest, requestContinuation is required for non first page request");
            }

            query.with(pageable);

            return operations.paginationQuery(query, type, container);
        }
    }
}
