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

package com.microsoft.azure.storage.table;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.UUID;

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageErrorCodeStrings;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageExtendedErrorInformation;
import com.microsoft.azure.storage.core.ExecutionEngine;
import com.microsoft.azure.storage.core.RequestLocationMode;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.StorageRequest;
import com.microsoft.azure.storage.core.Utility;

/**
 * A class which represents a batch operation. A batch operation is a collection of table operations which are executed
 * by the Storage Service REST API as a single atomic operation, by invoking an <a
 * href="http://msdn.microsoft.com/en-us/library/azure/dd894038.aspx">Entity Group Transaction</a>.
 * <p>
 * A batch operation may contain up to 100 individual table operations, with the requirement that each operation entity
 * must have same partition key. A batch with a retrieve operation cannot contain any other operations. Note that the
 * total payload of a batch operation is limited to 4MB.
 */
public class TableBatchOperation extends ArrayList<TableOperation> {

    private static final long serialVersionUID = -1192644463287355790L;
    private boolean hasQuery = false;
    private String partitionKey = null;
    private boolean containsWrites = false;

    /**
     * Adds the table operation at the specified index in the batch operation <code>ArrayList</code>.
     * 
     * @param index
     *            An <code>int</code> which represents the index in the batch operation <code>ArrayList</code> to add
     *            the table operation at.
     * @param element
     *            The {@link TableOperation} to add to the batch operation.
     */
    @Override
    public void add(final int index, final TableOperation element) {
        Utility.assertNotNull("element", element);

        this.checkSingleQueryPerBatch(element, this.size());

        if (element.getOperationType() == TableOperationType.RETRIEVE) {
            this.lockToPartitionKey(((QueryTableOperation) element).getPartitionKey());
        }
        else {
            this.lockToPartitionKey(element.getEntity().getPartitionKey());
        }

        super.add(index, element);
    }

    /**
     * Adds the table operation to the batch operation <code>ArrayList</code>.
     * 
     * @param element
     *            The {@link TableOperation} to add to the batch operation.
     * @return
     *         <code>true</code> if the operation was added successfully.
     */
    @Override
    public boolean add(final TableOperation element) {
        Utility.assertNotNull("element", element);

        this.checkSingleQueryPerBatch(element, this.size());

        if (element.getOperationType() == TableOperationType.RETRIEVE) {
            // Query operation
            this.lockToPartitionKey(((QueryTableOperation) element).getPartitionKey());
        }
        else {
            this.lockToPartitionKey(element.getEntity().getPartitionKey());
        }

        return super.add(element);
    }

    /**
     * Adds the collection of table operations to the batch operation <code>ArrayList</code> starting at the specified
     * index.
     * 
     * @param index
     *            An <code>int</code> which represents the index in the batch operation <code>ArrayList</code> to add
     *            the table operation at.
     * @param c
     *            A <code>java.util.Collection</code> of {@link TableOperation} objects to add to the batch operation.
     * @return
     *         <code>true</code> if the operations were added successfully.
     */
    @Override
    public boolean addAll(final int index, final java.util.Collection<? extends TableOperation> c) {
        int size = this.size();
        for (final TableOperation operation : c) {
            Utility.assertNotNull("operation", operation);
            this.checkSingleQueryPerBatch(operation, size);

            if (operation.getEntity() == null) {
                // Query operation
                this.lockToPartitionKey(((QueryTableOperation) operation).getPartitionKey());
            }
            else {
                this.lockToPartitionKey(operation.getEntity().getPartitionKey());
            }

            size++;
        }

        return super.addAll(index, c);
    }

    /**
     * Adds the collection of table operations to the batch operation <code>ArrayList</code>.
     * 
     * @param c
     *            A <code>java.util.Collection</code> of {@link TableOperation} objects to add to the batch operation.
     * @return
     *         <code>true</code> if the operations were added successfully.
     */
    @Override
    public boolean addAll(final java.util.Collection<? extends TableOperation> c) {
        int size = this.size();
        for (final TableOperation operation : c) {
            Utility.assertNotNull("operation", operation);
            this.checkSingleQueryPerBatch(operation, size);

            if (operation.getEntity() == null) {
                // Query operation
                this.lockToPartitionKey(((QueryTableOperation) operation).getPartitionKey());
            }
            else {
                this.lockToPartitionKey(operation.getEntity().getPartitionKey());
            }

            size++;
        }

        return super.addAll(c);
    }

    /**
     * Clears all table operations from the batch operation.
     */
    @Override
    public void clear() {
        super.clear();
        checkResetEntityLocks();
    }

    /**
     * Adds a table operation to delete the specified entity to the batch operation.
     * 
     * @param entity
     *            The {@link TableEntity} to delete.
     */
    public void delete(final TableEntity entity) {
        this.lockToPartitionKey(entity.getPartitionKey());
        this.add(TableOperation.delete(entity));
    }

    /**
     * Adds a table operation to insert the specified entity to the batch operation.
     * 
     * @param entity
     *            The {@link TableEntity} to insert.
     */
    public void insert(final TableEntity entity) {
        insert(entity, false);
    }

    /**
     * Adds a table operation to insert the specified entity to the batch operation.
     * 
     * @param entity
     *            The {@link TableEntity} to insert.
     * @param echoContent
     *            The boolean representing whether the message payload should be returned in the response.
     */
    public void insert(final TableEntity entity, boolean echoContent) {
        this.lockToPartitionKey(entity.getPartitionKey());
        this.add(TableOperation.insert(entity, echoContent));
    }

    /**
     * Adds a table operation to insert or merge the specified entity to the batch operation.
     * 
     * @param entity
     *            The {@link TableEntity} to insert if not found or to merge if it exists.
     */
    public void insertOrMerge(final TableEntity entity) {
        this.lockToPartitionKey(entity.getPartitionKey());
        this.add(TableOperation.insertOrMerge(entity));
    }

    /**
     * Adds a table operation to insert or replace the specified entity to the batch operation.
     * 
     * @param entity
     *            The {@link TableEntity} to insert if not found or to replace if it exists.
     */
    public void insertOrReplace(final TableEntity entity) {
        this.lockToPartitionKey(entity.getPartitionKey());
        this.add(TableOperation.insertOrReplace(entity));
    }

    /**
     * Adds a table operation to merge the specified entity to the batch operation.
     * 
     * @param entity
     *            The {@link TableEntity} to merge.
     */
    public void merge(final TableEntity entity) {
        this.lockToPartitionKey(entity.getPartitionKey());
        this.add(TableOperation.merge(entity));
    }

    /**
     * Adds a table operation to retrieve an entity of the specified class type with the specified PartitionKey and
     * RowKey to the batch operation.
     * 
     * @param partitionKey
     *            A <code>String</code> containing the PartitionKey of the entity to retrieve.
     * @param rowKey
     *            A <code>String</code> containing the RowKey of the entity to retrieve.
     * @param clazzType
     *            The class of the {@link TableEntity} type for the entity to retrieve.
     */
    public void retrieve(final String partitionKey, final String rowKey, final Class<? extends TableEntity> clazzType) {
        this.lockToPartitionKey(partitionKey);
        this.add(TableOperation.retrieve(partitionKey, rowKey, clazzType));
    }

    /**
     * Adds a table operation to retrieve an entity of the specified class type with the specified PartitionKey and
     * RowKey to the batch operation.
     * 
     * @param partitionKey
     *            A <code>String</code> containing the PartitionKey of the entity to retrieve.
     * @param rowKey
     *            A <code>String</code> containing the RowKey of the entity to retrieve.
     * @param resolver
     *            The {@link EntityResolver} implementation to project the entity to retrieve as a particular type in
     *            the result.
     */
    public void retrieve(final String partitionKey, final String rowKey, final EntityResolver<?> resolver) {
        this.lockToPartitionKey(partitionKey);
        this.add(TableOperation.retrieve(partitionKey, rowKey, resolver));
    }

    /**
     * Removes the table operation at the specified index from the batch operation.
     * 
     * @param index
     *            An <code>int</code> which represents the index in the <code>ArrayList</code> of the table operation to
     *            remove from the batch operation.
     */
    @Override
    public TableOperation remove(int index) {
        TableOperation op = super.remove(index);
        checkResetEntityLocks();
        return op;
    }

    /**
     * Removes the specified <code>Object</code> from the batch operation.
     * 
     * @param o
     *            The <code>Object</code> to remove from the batch operation.
     * @return
     *         <code>true</code> if the object was removed successfully.
     */
    @Override
    public boolean remove(Object o) {
        boolean ret = super.remove(o);
        checkResetEntityLocks();
        return ret;
    }

    /**
     * Removes all elements of the specified collection from the batch operation.
     * 
     * @param c
     *            The collection of elements to remove from the batch operation.
     * @return
     *         <code>true</code> if the objects in the collection were removed successfully.
     */
    @Override
    public boolean removeAll(java.util.Collection<?> c) {
        boolean ret = super.removeAll(c);
        checkResetEntityLocks();
        return ret;
    }

    /**
     * Adds a table operation to replace the specified entity to the batch operation.
     * 
     * @param entity
     *            The {@link TableEntity} to replace.
     */
    public void replace(final TableEntity entity) {
        this.lockToPartitionKey(entity.getPartitionKey());
        this.add(TableOperation.replace(entity));
    }

    /**
     * Reserved for internal use. Clears internal fields when the batch operation is empty.
     */
    private void checkResetEntityLocks() {
        if (this.size() == 0) {
            this.partitionKey = null;
            this.hasQuery = false;
            this.containsWrites = false;
        }
    }

    /**
     * Reserved for internal use. Verifies that the batch operation either contains no retrieve operations, or contains
     * only a single retrieve operation.
     * 
     * @param op
     *            The {@link TableOperation} to be added if the verification succeeds.
     */
    private void checkSingleQueryPerBatch(final TableOperation op, final int size) {
        // if this has a query then no other operations can be added.
        if (this.hasQuery) {
            throw new IllegalArgumentException(SR.RETRIEVE_MUST_BE_ONLY_OPERATION_IN_BATCH);
        }

        if (op.getOperationType() == TableOperationType.RETRIEVE) {
            if (size > 0) {
                throw new IllegalArgumentException(SR.RETRIEVE_MUST_BE_ONLY_OPERATION_IN_BATCH);
            }
            else {
                this.hasQuery = true;
            }
        }

        this.containsWrites = op.getOperationType() != TableOperationType.RETRIEVE;
    }

    /**
     * Reserved for internal use. Verifies that the specified PartitionKey value matches the value in the batch
     * operation.
     * 
     * @param partitionKey
     *            The <code>String</code> containing the PartitionKey value to check.
     */
    private void lockToPartitionKey(final String partitionKey) {
        if (this.partitionKey == null) {
            this.partitionKey = partitionKey;
        }
        else {
            if (partitionKey.length() != partitionKey.length() || !this.partitionKey.equals(partitionKey)) {
                throw new IllegalArgumentException(SR.OPS_IN_BATCH_MUST_HAVE_SAME_PARTITION_KEY);
            }
        }
    }

    /**
     * Reserved for internal use. Executes this batch operation on the specified table, using the specified
     * {@link TableRequestOptions} and {@link OperationContext}.
     * <p>
     * This method will invoke the Storage Service REST API to execute this batch operation, using the Table service
     * endpoint and storage account credentials in the {@link CloudTableClient} object.
     * 
     * @param client
     *            A {@link CloudTableClient} instance specifying the Table service endpoint and storage account
     *            credentials to use.
     * @param tableName
     *            A <code>String</code> containing the name of the table.
     * @param options
     *            A {@link TableRequestOptions} object that specifies execution options such as retry policy and timeout
     *            settings for the operation.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation.
     * 
     * @return
     *         An <code>ArrayList</code> of {@link TableResult} containing the results of executing the operation.
     * 
     * @throws StorageException
     *             if an error occurs in the storage operation.
     */
    protected ArrayList<TableResult> execute(final CloudTableClient client, final String tableName,
            final TableRequestOptions options, final OperationContext opContext) throws StorageException {

        Utility.assertNotNullOrEmpty(TableConstants.TABLE_NAME, tableName);

        if (this.size() == 0) {
            throw new IllegalArgumentException(SR.EMPTY_BATCH_NOT_ALLOWED);
        }

        return ExecutionEngine.executeWithRetry(client, this, this.executeImpl(client, tableName, options, opContext),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudTableClient, TableBatchOperation, ArrayList<TableResult>> executeImpl(
            final CloudTableClient client, final String tableName, final TableRequestOptions options,
            final OperationContext opContext) throws StorageException {
        final String batchID = String.format("batch_%s", UUID.randomUUID().toString());
        final String changeSet = String.format("changeset_%s", UUID.randomUUID().toString());
        ByteArrayOutputStream sendStream = new ByteArrayOutputStream();
        try {
            MimeHelper.writeBatchToStream(sendStream, options, tableName, client.getTransformedEndPoint(opContext)
                    .getPrimaryUri(), this, batchID, changeSet, opContext);
            final byte[] batchBytes = sendStream.toByteArray();
            final StorageRequest<CloudTableClient, TableBatchOperation, ArrayList<TableResult>> batchRequest = new StorageRequest<CloudTableClient, TableBatchOperation, ArrayList<TableResult>>(
                    options, client.getStorageUri()) {

                @Override
                public void setRequestLocationMode() {
                    this.setRequestLocationMode(TableBatchOperation.this.containsWrites ? RequestLocationMode.PRIMARY_ONLY
                            : RequestLocationMode.PRIMARY_OR_SECONDARY);
                }

                @Override
                public HttpURLConnection buildRequest(CloudTableClient client, TableBatchOperation batch,
                        OperationContext context) throws Exception {

                    this.setSendStream(new ByteArrayInputStream(batchBytes));
                    this.setLength((long) batchBytes.length);
                    return TableRequest.batch(client.getTransformedEndPoint(context).getUri(this.getCurrentLocation()),
                            options, null, context, batchID);
                }

                @Override
                public void signRequest(HttpURLConnection connection, CloudTableClient client, OperationContext context)
                        throws Exception {
                    StorageRequest.signTableRequest(connection, client, -1L, context);
                }

                @Override
                public ArrayList<TableResult> preProcessResponse(TableBatchOperation batch, CloudTableClient client,
                        OperationContext context) throws Exception {
                    if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_ACCEPTED) {
                        this.setNonExceptionedRetryableFailure(true);
                    }

                    return null;
                }

                @Override
                public ArrayList<TableResult> postProcessResponse(HttpURLConnection connection,
                        TableBatchOperation batch, CloudTableClient client, OperationContext context,
                        ArrayList<TableResult> storageObject) throws Exception {
                    final InputStream streamRef = connection.getInputStream();
                    ArrayList<MimePart> responseParts = null;

                    final String contentType = connection.getHeaderField(Constants.HeaderConstants.CONTENT_TYPE);

                    final String[] headerVals = contentType.split("multipart/mixed; boundary=");
                    if (headerVals == null || headerVals.length != 2) {
                        throw new StorageException(StorageErrorCodeStrings.OUT_OF_RANGE_INPUT, SR.INVALID_CONTENT_TYPE,
                                Constants.HeaderConstants.HTTP_UNUSED_306, null /* extendedErrorInfo */, null /* innerException */);
                    }

                    responseParts = MimeHelper.readBatchResponseStream(streamRef, headerVals[1], opContext,
                            options.getTablePayloadFormat());

                    final ArrayList<TableResult> result = new ArrayList<TableResult>();
                    for (int m = 0; m < batch.size(); m++) {
                        final TableOperation currOp = batch.get(m);
                        final MimePart currMimePart = responseParts.get(m);

                        boolean failFlag = false;

                        // Validate response
                        if (currOp.getOperationType() == TableOperationType.INSERT) {
                            if (currOp.getEchoContent()
                                    && currMimePart.httpStatusCode != HttpURLConnection.HTTP_CREATED) {
                                // Insert should receive created if echo content is on
                                failFlag = true;
                            }
                            else if (!currOp.getEchoContent()
                                    && currMimePart.httpStatusCode != HttpURLConnection.HTTP_NO_CONTENT) {
                                // Insert should receive no content if echo content is off
                                failFlag = true;
                            }
                        }
                        else if (currOp.getOperationType() == TableOperationType.RETRIEVE) {
                            if (currMimePart.httpStatusCode == HttpURLConnection.HTTP_NOT_FOUND) {
                                // Empty result
                                result.add(new TableResult(currMimePart.httpStatusCode));
                                return result;
                            }

                            // Point query should receive ok.
                            if (currMimePart.httpStatusCode != HttpURLConnection.HTTP_OK) {
                                failFlag = true;
                            }
                        }
                        else {
                            if (currMimePart.httpStatusCode != HttpURLConnection.HTTP_NO_CONTENT) {
                                // All others should receive no content. (delete, merge, upsert etc)
                                failFlag = true;
                            }
                        }

                        if (failFlag) {
                            throw new TableServiceException(currMimePart.httpStatusCode,
                                    currMimePart.httpStatusMessage, currOp, new StringReader(currMimePart.payload),
                                    options.getTablePayloadFormat());
                        }

                        ByteArrayInputStream byteStream = null;

                        if ((currOp.getOperationType() == TableOperationType.INSERT && currOp.getEchoContent())
                                || currOp.getOperationType() == TableOperationType.RETRIEVE) {
                            byteStream = new ByteArrayInputStream(currMimePart.payload.getBytes());
                        }

                        result.add(currOp.parseResponse(byteStream, currMimePart.httpStatusCode,
                                currMimePart.headers.get(TableConstants.HeaderConstants.ETAG), opContext, options));
                    }

                    return result;
                }

                @Override
                public StorageExtendedErrorInformation parseErrorDetails() {
                    return TableStorageErrorDeserializer.parseErrorDetails(this);
                }
            };

            return batchRequest;
        }
        catch (IOException e) {
            // The request was not even made. There was an error while trying to read the batch contents. Just throw.
            StorageException translatedException = StorageException.translateClientException(e);
            throw translatedException;
        }
        catch (URISyntaxException e) {
            // The request was not even made. There was an error while trying to read the batch contents. Just throw.
            StorageException translatedException = StorageException.translateClientException(e);
            throw translatedException;
        }
    }

    /**
     * Reserved for internal use. Removes all the table operations at indexes in the specified range from the batch
     * operation <code>ArrayList</code>.
     * 
     * @param fromIndex
     *            An <code>int</code> which represents the inclusive lower bound of the range of {@link TableOperation}
     *            objects to remove from the batch operation <code>ArrayList</code>.
     * @param toIndex
     *            An <code>int</code> which represents the exclusive upper bound of the range of {@link TableOperation}
     *            objects to remove from the batch operation <code>ArrayList</code>.
     */
    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        super.removeRange(fromIndex, toIndex);
        checkResetEntityLocks();
    }
}
