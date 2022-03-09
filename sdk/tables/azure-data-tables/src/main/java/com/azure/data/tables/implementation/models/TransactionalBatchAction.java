// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation.models;

import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Response;
import com.azure.data.tables.TableAsyncClient;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableEntityUpdateMode;
import reactor.core.publisher.Mono;

public interface TransactionalBatchAction {
    Mono<HttpRequest> prepareRequest(TableAsyncClient preparer);

    class CreateEntity implements TransactionalBatchAction {
        private final TableEntity entity;

        public CreateEntity(TableEntity entity) {
            this.entity = entity;
        }

        public TableEntity getEntity() {
            return entity;
        }

        @Override
        public Mono<HttpRequest> prepareRequest(TableAsyncClient client) {
            return client.createEntityWithResponse(entity).map(Response::getRequest);
        }

        @Override
        public String toString() {
            return "CreateEntity{"
                + "partitionKey='" + entity.getPartitionKey() + '\''
                + ", rowKey='" + entity.getRowKey() + '\''
                + '}';
        }
    }

    class UpsertEntity implements TransactionalBatchAction {
        private final TableEntity entity;
        private final TableEntityUpdateMode updateMode;

        public UpsertEntity(TableEntity entity, TableEntityUpdateMode updateMode) {
            this.entity = entity;
            this.updateMode = updateMode;
        }

        public TableEntity getEntity() {
            return entity;
        }

        public TableEntityUpdateMode getUpdateMode() {
            return updateMode;
        }

        @Override
        public Mono<HttpRequest> prepareRequest(TableAsyncClient preparer) {
            return preparer.upsertEntityWithResponse(entity, updateMode).map(Response::getRequest);
        }

        @Override
        public String toString() {
            return "UpsertEntity{"
                + "partitionKey='" + entity.getPartitionKey() + '\''
                + ", rowKey='" + entity.getRowKey() + '\''
                + ", updateMode=" + updateMode
                + '}';
        }
    }

    class UpdateEntity implements TransactionalBatchAction {
        private final TableEntity entity;
        private final TableEntityUpdateMode updateMode;
        private final boolean ifUnchanged;

        public UpdateEntity(TableEntity entity, TableEntityUpdateMode updateMode, boolean ifUnchanged) {
            this.entity = entity;
            this.updateMode = updateMode;
            this.ifUnchanged = ifUnchanged;
        }

        public TableEntity getEntity() {
            return entity;
        }

        public TableEntityUpdateMode getUpdateMode() {
            return updateMode;
        }

        public boolean getIfUnchanged() {
            return ifUnchanged;
        }

        @Override
        public Mono<HttpRequest> prepareRequest(TableAsyncClient preparer) {
            return preparer.updateEntityWithResponse(entity, updateMode, ifUnchanged).map(Response::getRequest);
        }

        @Override
        public String toString() {
            return "UpdateEntity{"
                + "partitionKey='" + entity.getPartitionKey() + '\''
                + ", rowKey='" + entity.getRowKey() + '\''
                + ", updateMode=" + updateMode
                + ", ifUnchanged=" + ifUnchanged
                + '}';
        }
    }

    class DeleteEntity implements TransactionalBatchAction {
        private final TableEntity entity;
        private final boolean ifUnchanged;

        public DeleteEntity(TableEntity entity, boolean ifUnchanged) {
            this.entity = entity;
            this.ifUnchanged = ifUnchanged;
        }

        public TableEntity getEntity() {
            return entity;
        }

        public boolean getIfUnchanged() {
            return ifUnchanged;
        }

        @Override
        public Mono<HttpRequest> prepareRequest(TableAsyncClient preparer) {
            return preparer.deleteEntityWithResponse(entity, ifUnchanged).map(Response::getRequest);
        }

        @Override
        public String toString() {
            return "DeleteEntity{"
                + "partitionKey='" + entity.getPartitionKey() + '\''
                + ", rowKey='" + entity.getRowKey() + '\''
                + ", eTag='" + entity.getETag() + '\''
                + ", ifUnchanged=" + ifUnchanged
                + '}';
        }
    }

}
