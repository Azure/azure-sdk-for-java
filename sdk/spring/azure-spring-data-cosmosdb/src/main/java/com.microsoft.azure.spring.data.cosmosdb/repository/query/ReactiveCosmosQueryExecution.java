// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.repository.query;

import com.microsoft.azure.spring.data.cosmosdb.core.ReactiveCosmosOperations;
import com.microsoft.azure.spring.data.cosmosdb.core.query.DocumentQuery;

public interface ReactiveCosmosQueryExecution {
    Object execute(DocumentQuery query, Class<?> type, String container);

    final class ContainerExecution implements ReactiveCosmosQueryExecution {

        private final ReactiveCosmosOperations operations;

        public ContainerExecution(ReactiveCosmosOperations operations) {
            this.operations = operations;
        }

        @Override
        public Object execute(DocumentQuery query, Class<?> type, String container) {
            return operations.getContainerName(type);
        }
    }

    final class MultiEntityExecution implements ReactiveCosmosQueryExecution {

        private final ReactiveCosmosOperations operations;

        public MultiEntityExecution(ReactiveCosmosOperations operations) {
            this.operations = operations;
        }

        @Override
        public Object execute(DocumentQuery query, Class<?> type, String container) {
            return operations.find(query, type, container);
        }
    }

    final class ExistsExecution implements ReactiveCosmosQueryExecution {

        private final ReactiveCosmosOperations operations;

        public ExistsExecution(ReactiveCosmosOperations operations) {
            this.operations = operations;
        }

        @Override
        public Object execute(DocumentQuery query, Class<?> type, String container) {
            return operations.exists(query, type, container);
        }
    }

    final class DeleteExecution implements ReactiveCosmosQueryExecution {

        private final ReactiveCosmosOperations operations;

        public DeleteExecution(ReactiveCosmosOperations operations) {
            this.operations = operations;
        }

        @Override
        public Object execute(DocumentQuery query, Class<?> type, String container) {
            return operations.delete(query, type, container);
        }
    }
}
