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
        this.operations.add(new InsertEntityOperation().setTable(table).setEntity(entity));
        return this;
    }

    public BatchOperations addUpdateEntity(String table, Entity entity) {
        this.operations.add(new UpdateEntityOperation().setTable(table).setEntity(entity));
        return this;
    }

    public BatchOperations addMergeEntity(String table, Entity entity) {
        this.operations.add(new MergeEntityOperation().setTable(table).setEntity(entity));
        return this;
    }

    public BatchOperations addInsertOrReplaceEntity(String table, Entity entity) {
        this.operations.add(new InsertOrReplaceEntityOperation().setTable(table).setEntity(entity));
        return this;
    }

    public BatchOperations addInsertOrMergeEntity(String table, Entity entity) {
        this.operations.add(new InsertOrMergeEntityOperation().setTable(table).setEntity(entity));
        return this;
    }

    public BatchOperations addDeleteEntity(String table, String partitionKey, String rowKey) {
        this.operations
                .add(new DeleteEntityOperation().setTable(table).setPartitionKey(partitionKey).setRowKey(rowKey));
        return this;
    }

    public static abstract class Operation {
    }

    public static class InsertEntityOperation extends Operation {
        private String table;
        private Entity entity;

        public String getTable() {
            return table;
        }

        public InsertEntityOperation setTable(String table) {
            this.table = table;
            return this;
        }

        public Entity getEntity() {
            return entity;
        }

        public InsertEntityOperation setEntity(Entity entity) {
            this.entity = entity;
            return this;
        }
    }

    public static class UpdateEntityOperation extends Operation {
        private String table;
        private Entity entity;

        public String getTable() {
            return table;
        }

        public UpdateEntityOperation setTable(String table) {
            this.table = table;
            return this;
        }

        public Entity getEntity() {
            return entity;
        }

        public UpdateEntityOperation setEntity(Entity entity) {
            this.entity = entity;
            return this;
        }
    }

    public static class MergeEntityOperation extends Operation {
        private String table;
        private Entity entity;

        public String getTable() {
            return table;
        }

        public MergeEntityOperation setTable(String table) {
            this.table = table;
            return this;
        }

        public Entity getEntity() {
            return entity;
        }

        public MergeEntityOperation setEntity(Entity entity) {
            this.entity = entity;
            return this;
        }
    }

    public static class InsertOrReplaceEntityOperation extends Operation {
        private String table;
        private Entity entity;

        public String getTable() {
            return table;
        }

        public InsertOrReplaceEntityOperation setTable(String table) {
            this.table = table;
            return this;
        }

        public Entity getEntity() {
            return entity;
        }

        public InsertOrReplaceEntityOperation setEntity(Entity entity) {
            this.entity = entity;
            return this;
        }
    }

    public static class InsertOrMergeEntityOperation extends Operation {
        private String table;
        private Entity entity;

        public String getTable() {
            return table;
        }

        public InsertOrMergeEntityOperation setTable(String table) {
            this.table = table;
            return this;
        }

        public Entity getEntity() {
            return entity;
        }

        public InsertOrMergeEntityOperation setEntity(Entity entity) {
            this.entity = entity;
            return this;
        }
    }

    public static class DeleteEntityOperation extends Operation {
        private String table;
        private String partitionKey;
        private String rowKey;

        public String getTable() {
            return table;
        }

        public DeleteEntityOperation setTable(String table) {
            this.table = table;
            return this;
        }

        public String getPartitionKey() {
            return partitionKey;
        }

        public DeleteEntityOperation setPartitionKey(String partitionKey) {
            this.partitionKey = partitionKey;
            return this;
        }

        public String getRowKey() {
            return rowKey;
        }

        public DeleteEntityOperation setRowKey(String rowKey) {
            this.rowKey = rowKey;
            return this;
        }
    }
}
