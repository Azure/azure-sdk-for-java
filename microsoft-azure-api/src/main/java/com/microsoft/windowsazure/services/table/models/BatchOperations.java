package com.microsoft.windowsazure.services.table.models;

import java.util.ArrayList;
import java.util.List;

public class BatchOperations {
    private List<Operation> operations = new ArrayList<Operation>();

    public List<Operation> getOperations() {
        return operations;
    }

    public void setOperations(List<Operation> operations) {
        this.operations = operations;
    }

    public BatchOperations addInsertEntity(String table, Entity entity) {
        this.operations.add(new InsertOperation().setTable(table).setEntity(entity));
        return this;
    }

    public BatchOperations addUpdateEntity(String table, Entity entity) {
        this.operations.add(new UpdateOperation().setTable(table).setEntity(entity));
        return this;
    }

    public BatchOperations addMergeEntity(String table, Entity entity) {
        this.operations.add(new MergeOperation().setTable(table).setEntity(entity));
        return this;
    }

    public BatchOperations addInsertOrReplaceEntity(String table, Entity entity) {
        this.operations.add(new InsertOrReplaceOperation().setTable(table).setEntity(entity));
        return this;
    }

    public BatchOperations addInsertOrMergeEntity(String table, Entity entity) {
        this.operations.add(new InsertOrMergeOperation().setTable(table).setEntity(entity));
        return this;
    }

    public BatchOperations addDeleteEntity(String table, String partitionKey, String rowKey) {
        this.operations.add(new DeleteOperation().setTable(table).setPartitionKey(partitionKey).setRowKey(rowKey));
        return this;
    }

    public abstract class Operation {
    }

    public class InsertOperation extends Operation {
        private String table;
        private Entity entity;

        public String getTable() {
            return table;
        }

        public InsertOperation setTable(String table) {
            this.table = table;
            return this;
        }

        public Entity getEntity() {
            return entity;
        }

        public InsertOperation setEntity(Entity entity) {
            this.entity = entity;
            return this;
        }
    }

    public class UpdateOperation extends Operation {
        private String table;
        private Entity entity;

        public String getTable() {
            return table;
        }

        public UpdateOperation setTable(String table) {
            this.table = table;
            return this;
        }

        public Entity getEntity() {
            return entity;
        }

        public UpdateOperation setEntity(Entity entity) {
            this.entity = entity;
            return this;
        }
    }

    public class MergeOperation extends Operation {
        private String table;
        private Entity entity;

        public String getTable() {
            return table;
        }

        public MergeOperation setTable(String table) {
            this.table = table;
            return this;
        }

        public Entity getEntity() {
            return entity;
        }

        public MergeOperation setEntity(Entity entity) {
            this.entity = entity;
            return this;
        }
    }

    public class InsertOrReplaceOperation extends Operation {
        private String table;
        private Entity entity;

        public String getTable() {
            return table;
        }

        public InsertOrReplaceOperation setTable(String table) {
            this.table = table;
            return this;
        }

        public Entity getEntity() {
            return entity;
        }

        public InsertOrReplaceOperation setEntity(Entity entity) {
            this.entity = entity;
            return this;
        }
    }

    public class InsertOrMergeOperation extends Operation {
        private String table;
        private Entity entity;

        public String getTable() {
            return table;
        }

        public InsertOrMergeOperation setTable(String table) {
            this.table = table;
            return this;
        }

        public Entity getEntity() {
            return entity;
        }

        public InsertOrMergeOperation setEntity(Entity entity) {
            this.entity = entity;
            return this;
        }
    }

    public class DeleteOperation extends Operation {
        private String table;
        private String partitionKey;
        private String rowKey;

        public String getTable() {
            return table;
        }

        public DeleteOperation setTable(String table) {
            this.table = table;
            return this;
        }

        public String getPartitionKey() {
            return partitionKey;
        }

        public DeleteOperation setPartitionKey(String partitionKey) {
            this.partitionKey = partitionKey;
            return this;
        }

        public String getRowKey() {
            return rowKey;
        }

        public DeleteOperation setRowKey(String rowKey) {
            this.rowKey = rowKey;
            return this;
        }
    }
}
