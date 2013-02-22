/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.table.models;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.windowsazure.services.table.TableContract;

/**
 * Represents the collection of table operations that may be sent as a single batch transaction with a
 * {@link TableContract#batch(BatchOperations)} or {@link TableContract#batch(BatchOperations, TableServiceOptions)}
 * request. A batch transaction is executed by the Storage Service REST API as a single atomic operation, by invoking an
 * <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd894038.aspx">Entity Group Transaction</a>.
 * <p>
 * A batch operation may contain up to 100 individual table operations, with the requirement that the target entity of
 * each operation must have same partition key. A batch with a query operation cannot contain any other operations. Note
 * that the total payload of a batch operation is limited to 4MB.
 * <p>
 * The semantics for entity group transactions are defined by the <a
 * href="http://msdn.microsoft.com/en-us/library/dd744839.aspx">WCF Data Services Batching Operations</a>. The WCF Data
 * Services specification defines the following concepts for batch requests:
 * <ul>
 * <li>A <em>change set</em> is a group of one or more insert, update, or delete operations.</li>
 * <li>A <em>batch</em> is a container of operations, including one or more change sets and query operations.</li>
 * </ul>
 * <p>
 * The Table service supports a subset of the functionality defined by WCF Data Services:
 * <ul>
 * <li>The Table service supports only a single change set within a batch. The change set can include multiple insert,
 * update, and delete operations. If a batch includes more than one change set, the first change set will be processed
 * by the service, and additional change sets will be rejected with status code 400 (Bad Request). <br>
 * <br>
 * <strong>Important:</strong> Multiple operations against a single entity are not permitted within a change set.<br>
 * <br>
 * </li>
 * <li>Note that a query operation is not permitted within a batch that contains insert, update, or delete operations;
 * it must be submitted singly in the batch.</li>
 * <li>Operations within a change set are processed atomically; that is, all operations in the change set either succeed
 * or fail. Operations are processed in the order they are specified in the change set.</li>
 * <li>The Table service does not support linking operations in a change set.</li>
 * <li>The Table service supports a maximum of 100 operations in a change set.</li>
 * </ul>
 * <p>
 * An individual request within the change set is identical to a request made when that operation is being called by
 * itself.
 * <p>
 * To specify an update, merge, or delete operation only succeeds when the entity has not changed since it was last seen
 * by the client, include the entities' ETag value in the {@link Entity} instance passed to the operation in the change
 * set.
 */
public class BatchOperations {
    private List<Operation> operations = new ArrayList<Operation>();

    /**
     * Gets the collection of table operations in the batch.
     * 
     * @return
     *         A {@link java.util.List} of {@link Operation} instances representing the table operations in the batch.
     */
    public List<Operation> getOperations() {
        return operations;
    }

    /**
     * Sets the collection of table operations in the batch.
     * 
     * @param operations
     *            A {@link java.util.List} of {@link Operation} instances representing the table operations in the
     *            batch.
     */
    public void setOperations(List<Operation> operations) {
        this.operations = operations;
    }

    /**
     * Adds an insert entity operation to the collection of table operations in the batch.
     * <p>
     * The <em>table</em> parameter must refer to the same table as all other operations in the batch, and the
     * <em>entity</em> parameter must have the same partition key as all other entities in the batch.
     * 
     * @param table
     *            A {@link String} containing the name of the table to insert the entity into.
     * @param entity
     *            The {@link Entity} instance to insert into the table.
     * @return
     *         A reference to this {@link BatchOperations} instance.
     */
    public BatchOperations addInsertEntity(String table, Entity entity) {
        this.operations.add(new InsertEntityOperation().setTable(table).setEntity(entity));
        return this;
    }

    /**
     * Adds an update entity operation to the collection of table operations in the batch. An update operation replaces
     * an existing entity with with the same primary key as the <em>entity</em> parameter.
     * <p>
     * The <em>table</em> parameter must refer to the same table as all other operations in the batch, and the
     * <em>entity</em> parameter must have the same partition key as all other entities in the batch.
     * 
     * @param table
     *            A {@link String} containing the name of the table to update the entity in.
     * @param entity
     *            The {@link Entity} instance to update in the table.
     * @return
     *         A reference to this {@link BatchOperations} instance.
     */
    public BatchOperations addUpdateEntity(String table, Entity entity) {
        this.operations.add(new UpdateEntityOperation().setTable(table).setEntity(entity));
        return this;
    }

    /**
     * Adds a merge entity operation to the collection of table operations in the batch. A merge operation replaces
     * and inserts properties in an existing entity with with the same primary key as the <em>entity</em> parameter.
     * <p>
     * The <em>table</em> parameter must refer to the same table as all other operations in the batch, and the
     * <em>entity</em> parameter must have the same partition key as all other entities in the batch.
     * 
     * @param table
     *            A {@link String} containing the name of the table to merge the entity in.
     * @param entity
     *            The {@link Entity} instance to merge in the table.
     * @return
     *         A reference to this {@link BatchOperations} instance.
     */
    public BatchOperations addMergeEntity(String table, Entity entity) {
        this.operations.add(new MergeEntityOperation().setTable(table).setEntity(entity));
        return this;
    }

    /**
     * Adds an insert or replace entity operation to the collection of table operations in the batch. An insert or
     * replace operation replaces an existing entity with with the same primary key as the <em>entity</em> parameter, or
     * inserts the entity if no matching entity exists in the table.
     * <p>
     * The <em>table</em> parameter must refer to the same table as all other operations in the batch, and the
     * <em>entity</em> parameter must have the same partition key as all other entities in the batch.
     * 
     * @param table
     *            A {@link String} containing the name of the table to insert or replace the entity in.
     * @param entity
     *            The {@link Entity} instance to insert or replace in the table.
     * @return
     *         A reference to this {@link BatchOperations} instance.
     */
    public BatchOperations addInsertOrReplaceEntity(String table, Entity entity) {
        this.operations.add(new InsertOrReplaceEntityOperation().setTable(table).setEntity(entity));
        return this;
    }

    /**
     * Adds an insert or merge entity operation to the collection of table operations in the batch. An insert or
     * merge operation replaces and inserts properties in an existing entity with with the same primary key as the
     * <em>entity</em> parameter, or inserts the entity if no matching entity exists in the table.
     * <p>
     * The <em>table</em> parameter must refer to the same table as all other operations in the batch, and the
     * <em>entity</em> parameter must have the same partition key as all other entities in the batch.
     * 
     * @param table
     *            A {@link String} containing the name of the table to insert or replace the entity in.
     * @param entity
     *            The {@link Entity} instance to insert or replace in the table.
     * @return
     *         A reference to this {@link BatchOperations} instance.
     */
    public BatchOperations addInsertOrMergeEntity(String table, Entity entity) {
        this.operations.add(new InsertOrMergeEntityOperation().setTable(table).setEntity(entity));
        return this;
    }

    /**
     * Adds a delete entity operation to the collection of table operations in the batch. The delete operation removes
     * the entity with with the specified partition key, row key, and ETag from the table.
     * <p>
     * The <em>table</em> parameter must refer to the same table as all other operations in the batch, and the
     * <em>partitionKey</em> parameter must be the same partition key in all other operations in the batch.
     * 
     * @param table
     *            A {@link String} containing the name of the table to delete the entity in.
     * @param partitionKey
     *            A {@link String} containing the partition key of the entity to delete.
     * @param rowKey
     *            A {@link String} containing the row key of the entity to delete.
     * @param etag
     *            A {@link String} containing the ETag value of the entity to delete.
     * @return
     *         A reference to this {@link BatchOperations} instance.
     */
    public BatchOperations addDeleteEntity(String table, String partitionKey, String rowKey, String etag) {
        this.operations.add(new DeleteEntityOperation().setTable(table).setPartitionKey(partitionKey).setRowKey(rowKey)
                .setEtag(etag));
        return this;
    }

    /**
     * The abstract base class for all batch operations.
     */
    public static abstract class Operation {
    }

    /**
     * Represents the parameters needed for an insert entity batch operation.
     */
    public static class InsertEntityOperation extends Operation {
        private String table;
        private Entity entity;

        /**
         * Gets the table name parameter for the insert entity batch operation set in this {@link InsertEntityOperation}
         * instance.
         * 
         * @return
         *         A {@link String} containing the name of the table that contains the entity to insert.
         */
        public String getTable() {
            return table;
        }

        /**
         * Sets the table name parameter for the insert entity batch operation. Note that this value must be the same
         * for
         * all operations in the batch.
         * 
         * @param table
         *            A {@link String} containing the name of the table that contains the entity to insert.
         * @return
         *         A reference to this {@link InsertEntityOperation} instance.
         */
        public InsertEntityOperation setTable(String table) {
            this.table = table;
            return this;
        }

        /**
         * Gets the entity parameter for the insert entity batch operation set in this {@link InsertEntityOperation}
         * instance.
         * 
         * @return
         *         The {@link Entity} instance containing the entity data to be inserted.
         */
        public Entity getEntity() {
            return entity;
        }

        /**
         * Sets the entity parameter for the insert entity batch operation. Note that the partition key value
         * in the entity must be the same for all operations in the batch.
         * 
         * @param entity
         *            The {@link Entity} instance containing the entity data to be inserted.
         * @return
         *         A reference to this {@link InsertEntityOperation} instance.
         */
        public InsertEntityOperation setEntity(Entity entity) {
            this.entity = entity;
            return this;
        }
    }

    /**
     * Represents the parameters needed for an update entity batch operation.
     */
    public static class UpdateEntityOperation extends Operation {
        private String table;
        private Entity entity;

        /**
         * Gets the table name parameter for the update entity batch operation set in this {@link UpdateEntityOperation}
         * instance.
         * 
         * @return
         *         A {@link String} containing the name of the table that contains the entity to update.
         */
        public String getTable() {
            return table;
        }

        /**
         * Sets the table name parameter for the update entity batch operation. Note that this value must be the same
         * for
         * all operations in the batch.
         * 
         * @param table
         *            A {@link String} containing the name of the table that contains the entity to update.
         * @return
         *         A reference to this {@link UpdateEntityOperation} instance.
         */
        public UpdateEntityOperation setTable(String table) {
            this.table = table;
            return this;
        }

        /**
         * Gets the entity parameter for the update entity batch operation set in this {@link UpdateEntityOperation}
         * instance.
         * 
         * @return
         *         The {@link Entity} instance containing the entity data to be updated.
         */
        public Entity getEntity() {
            return entity;
        }

        /**
         * Sets the entity parameter for the update entity batch operation. Note that the partition key value
         * in the entity must be the same for all operations in the batch.
         * 
         * @param entity
         *            The {@link Entity} instance containing the entity data to be updated.
         * @return
         *         A reference to this {@link UpdateEntityOperation} instance.
         */
        public UpdateEntityOperation setEntity(Entity entity) {
            this.entity = entity;
            return this;
        }
    }

    /**
     * Represents the parameters needed for a merge entity batch operation.
     */
    public static class MergeEntityOperation extends Operation {
        private String table;
        private Entity entity;

        /**
         * Gets the table name parameter for the merge entity batch operation set in this {@link MergeEntityOperation}
         * instance.
         * 
         * @return
         *         A {@link String} containing the name of the table that contains the entity to merge.
         */
        public String getTable() {
            return table;
        }

        /**
         * Sets the table name parameter for the merge entity batch operation. Note that this value must be the same for
         * all operations in the batch.
         * 
         * @param table
         *            A {@link String} containing the name of the table that contains the entity to merge.
         * @return
         *         A reference to this {@link MergeEntityOperation} instance.
         */
        public MergeEntityOperation setTable(String table) {
            this.table = table;
            return this;
        }

        /**
         * Gets the entity parameter for the merge entity batch operation set in this {@link MergeEntityOperation}
         * instance.
         * 
         * @return
         *         The {@link Entity} instance containing the entity data to be merged.
         */
        public Entity getEntity() {
            return entity;
        }

        /**
         * Sets the entity parameter for the merge entity batch operation. Note that the partition key value
         * in the entity must be the same for all operations in the batch.
         * 
         * @param entity
         *            The {@link Entity} instance containing the entity data to be merged.
         * @return
         *         A reference to this {@link MergeEntityOperation} instance.
         */
        public MergeEntityOperation setEntity(Entity entity) {
            this.entity = entity;
            return this;
        }
    }

    /**
     * Represents the parameters needed for an insert or replace entity batch operation.
     */
    public static class InsertOrReplaceEntityOperation extends Operation {
        private String table;
        private Entity entity;

        /**
         * Gets the table name parameter for the insert or replace entity batch operation set in this
         * {@link InsertOrReplaceEntityOperation} instance.
         * 
         * @return
         *         A {@link String} containing the name of the table that contains the entity to insert or replace.
         */
        public String getTable() {
            return table;
        }

        /**
         * Sets the table name parameter for the insert or replace entity batch operation. Note that this value must be
         * the same for all operations in the batch.
         * 
         * @param table
         *            A {@link String} containing the name of the table that contains the entity to insert or replace.
         * @return
         *         A reference to this {@link InsertOrReplaceEntityOperation} instance.
         */
        public InsertOrReplaceEntityOperation setTable(String table) {
            this.table = table;
            return this;
        }

        /**
         * Gets the entity parameter for the insert or replace entity batch operation set in this
         * {@link InsertOrReplaceEntityOperation} instance.
         * 
         * @return
         *         The {@link Entity} instance containing the entity data to be inserted or replaced.
         */
        public Entity getEntity() {
            return entity;
        }

        /**
         * Sets the entity parameter for the insert or replace entity batch operation. Note that the partition key value
         * in the entity must be the same for all operations in the batch.
         * 
         * @param entity
         *            The {@link Entity} instance containing the entity data to be inserted or replaced.
         * @return
         *         A reference to this {@link InsertOrReplaceEntityOperation} instance.
         */
        public InsertOrReplaceEntityOperation setEntity(Entity entity) {
            this.entity = entity;
            return this;
        }
    }

    /**
     * Represents the parameters needed for an insert or merge entity batch operation.
     */
    public static class InsertOrMergeEntityOperation extends Operation {
        private String table;
        private Entity entity;

        /**
         * Gets the table name parameter for the insert or merge entity batch operation set in this
         * {@link InsertOrMergeEntityOperation} instance.
         * 
         * @return
         *         A {@link String} containing the name of the table that contains the entity to insert or merge.
         */
        public String getTable() {
            return table;
        }

        /**
         * Sets the table name parameter for the insert or merge entity batch operation. Note that this value must be
         * the same for all operations in the batch.
         * 
         * @param table
         *            A {@link String} containing the name of the table that contains the entity to insert or merge.
         * @return
         *         A reference to this {@link InsertOrMergeEntityOperation} instance.
         */
        public InsertOrMergeEntityOperation setTable(String table) {
            this.table = table;
            return this;
        }

        /**
         * Gets the entity parameter for the insert or merge entity batch operation set in this
         * {@link InsertOrMergeEntityOperation} instance.
         * 
         * @return
         *         The {@link Entity} instance containing the entity data to be inserted or merged.
         */
        public Entity getEntity() {
            return entity;
        }

        /**
         * Sets the entity parameter for the insert or merge entity batch operation. Note that the partition key value
         * in the entity must be the same for all operations in the batch.
         * 
         * @param entity
         *            The {@link Entity} instance containing the entity data to be inserted or merged.
         * @return
         *         A reference to this {@link InsertOrMergeEntityOperation} instance.
         */
        public InsertOrMergeEntityOperation setEntity(Entity entity) {
            this.entity = entity;
            return this;
        }
    }

    /**
     * Represents the parameters needed for a delete entity operation.
     */
    public static class DeleteEntityOperation extends Operation {
        private String table;
        private String partitionKey;
        private String rowKey;
        private String etag;

        /**
         * Gets the table name parameter for the delete entity batch operation set in this {@link DeleteEntityOperation}
         * instance.
         * 
         * @return
         *         A {@link String} containing the name of the table that contains the entity to delete.
         */
        public String getTable() {
            return table;
        }

        /**
         * Sets the table name parameter for the delete entity batch operation. Note that this value must be the same
         * for all operations in the batch.
         * 
         * @param table
         *            A {@link String} containing the name of the table that contains the entity to delete.
         * @return
         *         A reference to this {@link DeleteEntityOperation} instance.
         */
        public DeleteEntityOperation setTable(String table) {
            this.table = table;
            return this;
        }

        /**
         * Gets the partition key parameter for the delete entity batch operation set in this
         * {@link DeleteEntityOperation} instance.
         * 
         * @return
         *         A {@link String} containing the partition key value of the entity to delete.
         */
        public String getPartitionKey() {
            return partitionKey;
        }

        /**
         * Sets the partition key parameter for the delete entity batch operation. Note that this value must be the same
         * for all operations in the batch.
         * 
         * @param partitionKey
         *            A {@link String} containing the partition key value of the entity to delete.
         * @return
         *         A reference to this {@link DeleteEntityOperation} instance.
         */
        public DeleteEntityOperation setPartitionKey(String partitionKey) {
            this.partitionKey = partitionKey;
            return this;
        }

        /**
         * Gets the row key parameter for the delete entity batch operation set in this {@link DeleteEntityOperation}
         * instance.
         * 
         * @return
         *         A {@link String} containing the row key value of the entity to delete.
         */
        public String getRowKey() {
            return rowKey;
        }

        /**
         * Sets the row key parameter for the delete entity batch operation.
         * 
         * @param rowKey
         *            A {@link String} containing the row key value of the entity to delete.
         * @return
         *         A reference to this {@link DeleteEntityOperation} instance.
         */
        public DeleteEntityOperation setRowKey(String rowKey) {
            this.rowKey = rowKey;
            return this;
        }

        /**
         * Gets the ETag parameter for the delete entity batch operation set in this {@link DeleteEntityOperation}
         * instance.
         * 
         * @return
         *         A {@link String} containing the ETag value of the entity to delete.
         */
        public String getEtag() {
            return etag;
        }

        /**
         * Sets the ETag parameter for the delete entity batch operation.
         * 
         * @param etag
         *            A {@link String} containing the ETag value of the entity to delete.
         * @return
         *         A reference to this {@link DeleteEntityOperation} instance.
         */
        public DeleteEntityOperation setEtag(String etag) {
            this.etag = etag;
            return this;
        }
    }
}
