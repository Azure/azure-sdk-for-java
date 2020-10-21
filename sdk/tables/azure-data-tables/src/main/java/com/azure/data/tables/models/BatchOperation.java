// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.models;

public interface BatchOperation {
    class CreateEntity implements BatchOperation {
        private final TableEntity entity;

        public CreateEntity(TableEntity entity) {
            this.entity = entity;
        }

        public TableEntity getEntity() {
            return entity;
        }
    }

    class UpsertEntity implements BatchOperation {
        private final TableEntity entity;
        private final UpdateMode updateMode;

        public UpsertEntity(TableEntity entity, UpdateMode updateMode) {
            this.entity = entity;
            this.updateMode = updateMode;
        }

        public TableEntity getEntity() {
            return entity;
        }

        public UpdateMode getUpdateMode() {
            return updateMode;
        }
    }

    class UpdateEntity implements BatchOperation {
        private final TableEntity entity;
        private final UpdateMode updateMode;
        private final boolean ifUnchanged;

        public UpdateEntity(TableEntity entity, UpdateMode updateMode, boolean ifUnchanged) {
            this.entity = entity;
            this.updateMode = updateMode;
            this.ifUnchanged = ifUnchanged;
        }

        public TableEntity getEntity() {
            return entity;
        }

        public UpdateMode getUpdateMode() {
            return updateMode;
        }

        public boolean getIfUnchanged() {
            return ifUnchanged;
        }
    }

    class DeleteEntity implements BatchOperation {
        private final String partitionKey;
        private final String rowKey;
        private final String eTag;

        public DeleteEntity(String partitionKey, String rowKey, String eTag) {
            this.partitionKey = partitionKey;
            this.rowKey = rowKey;
            this.eTag = eTag;
        }

        public String getPartitionKey() {
            return partitionKey;
        }

        public String getRowKey() {
            return rowKey;
        }

        public String getETag() {
            return eTag;
        }
    }

}
