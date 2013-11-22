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
package com.microsoft.windowsazure.storage.table;

import static org.junit.Assert.*;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.microsoft.windowsazure.storage.LocationMode;
import com.microsoft.windowsazure.storage.RetryNoRetry;
import com.microsoft.windowsazure.storage.StorageErrorCodeStrings;
import com.microsoft.windowsazure.storage.StorageException;
import com.microsoft.windowsazure.storage.core.SR;

@RunWith(Parameterized.class)
public class TableBatchOperationTests extends TableTestBase {

    private final TableRequestOptions options;

    /**
     * These parameters are passed to the constructor at the start of each test run. This includes TablePayloadFormat.
     * 
     * @return the parameters pass to the constructor
     */
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { TablePayloadFormat.AtomPub }, // AtomPub
                { TablePayloadFormat.JsonFullMetadata }, // Json Full Metadata
                { TablePayloadFormat.Json }, // Json Minimal Metadata
                { TablePayloadFormat.JsonNoMetadata } // Json No Metadata without PropertyResolver 
                });
    }

    /**
     * Takes a parameter from @Parameters to use for this run of the tests.
     * 
     * @param format
     *            The {@link TablePaylodFormat} to use for this test run
     */
    public TableBatchOperationTests(TablePayloadFormat format) {
        this.options = TableRequestOptions.applyDefaults(null, tClient);
        this.options.setTablePayloadFormat(format);
    }

    @Test
    public void testBatchAddAll() throws StorageException {
        ArrayList<TableOperation> ops = allOpsList();

        TableBatchOperation batch = new TableBatchOperation();
        boolean added = batch.addAll(ops);
        assertTrue(added);

        ArrayList<TableResult> results = tClient.execute(testSuiteTableName, batch, options, null);
        assertEquals(8, results.size());

        Iterator<TableResult> iter = results.iterator();

        for (int i = 0; i < 7; i++) {
            assertEquals(HttpURLConnection.HTTP_NO_CONTENT, iter.next().getHttpStatusCode());
        }

        // test to make sure we can't now add a query with addAll()
        ops.clear();
        Class1 ref = generateRandomEntity("jxscl_odata");
        ops.add(TableOperation.retrieve(ref.partitionKey, ref.rowKey, ref.getClass()));
        try {
            batch.addAll(ops);
        }
        catch (Exception e) {
            assertEquals(SR.RETRIEVE_MUST_BE_ONLY_OPERATION_IN_BATCH, e.getMessage());
        }

        // test to make sure we can't now add an operation with a different partition key with addAll()
        ops.clear();
        ref.partitionKey = "jxscl_odata_different";
        ops.add(TableOperation.insert(ref));
        try {
            batch.addAll(ops);
        }
        catch (Exception e) {
            assertEquals(SR.OPS_IN_BATCH_MUST_HAVE_SAME_PARTITION_KEY, e.getMessage());
        }
    }

    @Test
    public void testBatchAddAllWithRetrieveShouldThrow() throws StorageException {
        ArrayList<TableOperation> ops = allOpsList();

        TableBatchOperation batch = new TableBatchOperation();

        {
            // Insert entity to retrieve
            Class1 baseEntity = generateRandomEntity("jxscl_odata");
            tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity), options, null);
            ops.add(TableOperation.retrieve(baseEntity.getPartitionKey(), baseEntity.getRowKey(), Class1.class));
        }

        try {
            batch.addAll(ops);
        }
        catch (IllegalArgumentException e) {
            assertEquals(SR.RETRIEVE_MUST_BE_ONLY_OPERATION_IN_BATCH, e.getMessage());
        }
    }

    @Test
    public void testBatchAddIndex() throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();

        // add a retrieve
        Class1 ref = generateRandomEntity("jxscl_odata");
        TableOperation queryOp = TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), ref.getClass());
        batch.add(0, queryOp);

        // remove the retrieve
        batch.remove(0);
        assertEquals(0, batch.size());

        // should be able to add an entity with a different partition key
        Class1 baseEntity = generateRandomEntity("jxscl_odata_2");
        TableOperation op = TableOperation.insert(baseEntity);
        batch.add(0, op);

        // should not be able to make a request to secondary as there are writes
        try {
            TableRequestOptions options = new TableRequestOptions(this.options);
            options.setLocationMode(LocationMode.SECONDARY_ONLY);
            options.setRetryPolicyFactory(new RetryNoRetry());
            tClient.execute(testSuiteTableName, batch, options, null);
            fail("Should not be able to make a request to secondary as there are writes.");
        }
        catch (StorageException e) {
            assertEquals(IllegalArgumentException.class, e.getCause().getClass());
            assertEquals(SR.PRIMARY_ONLY_COMMAND, e.getCause().getMessage());
        }

        // remove the insert
        batch.remove(0);
        assertEquals(0, batch.size());

        // insert an object and add a retrieve to the batch
        ref = generateRandomEntity("jxscl_odata");
        queryOp = TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), ref.getClass());
        batch.add(0, queryOp);

        // should be able to make a request to secondary as there are no writes
        try {
            TableRequestOptions options = new TableRequestOptions(this.options);
            options.setLocationMode(LocationMode.SECONDARY_ONLY);
            options.setRetryPolicyFactory(new RetryNoRetry());
            tClient.execute(testSuiteTableName, batch, options, null);
        }
        catch (StorageException e) {
            // it's okay if entity is not found - we just want to make sure the request hit secondary
            assertEquals("The specified resource does not exist.", e.getMessage());
            assertEquals(StorageErrorCodeStrings.RESOURCE_NOT_FOUND, e.getErrorCode());
        }
        catch (Exception e) {
            fail("Should be able to make a request to secondary as there are no writes.");
        }
    }

    @Test
    public void testBatchRemoveAll() throws StorageException {
        ArrayList<TableOperation> ops = allOpsList();

        TableBatchOperation batch = new TableBatchOperation();

        batch.addAll(ops);

        assertTrue(batch.removeAll(ops));
        assertEquals(0, batch.size());

        // should be able to add an entity with a different partition key
        Class1 baseEntity = generateRandomEntity("jxscl_odata_2");
        batch.insert(baseEntity);
    }

    @Test
    public void testBatchRemoveRange() throws StorageException {
        ArrayList<TableOperation> ops = allOpsList();

        TableBatchOperation batch = new TableBatchOperation();

        batch.addAll(ops);

        batch.removeRange(0, ops.size());
        assertEquals(0, batch.size());

        // should be able to add an entity with a different partition key
        Class1 baseEntity = generateRandomEntity("jxscl_odata_2");
        batch.insert(baseEntity);

        batch.removeRange(0, 1);

        batch.addAll(ops);
        batch.removeRange(0, ops.size() - 1);

        // should be not be able to add an entity with a different partition key
        baseEntity = generateRandomEntity("jxscl_odata_2");
        try {
            batch.insert(baseEntity);
        }
        catch (IllegalArgumentException e) {
            assertEquals(SR.OPS_IN_BATCH_MUST_HAVE_SAME_PARTITION_KEY, e.getMessage());
        }

        batch.removeRange(0, 1);

        // should be able to add a retrieve to the batch
        Class1 ref = generateRandomEntity("jxscl_odata");
        TableOperation queryOp = TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), ref.getClass());
        batch.add(queryOp);

        // should be able to make a request to secondary as there are no writes
        try {
            TableRequestOptions options = new TableRequestOptions(this.options);
            options.setLocationMode(LocationMode.SECONDARY_ONLY);
            options.setRetryPolicyFactory(new RetryNoRetry());
            tClient.execute(testSuiteTableName, batch, options, null);
        }
        catch (StorageException e) {
            assertEquals("The specified resource does not exist.", e.getMessage());
            assertEquals(StorageErrorCodeStrings.RESOURCE_NOT_FOUND, e.getErrorCode());
        }
    }

    @Test
    public void testBatchRemove() throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();

        // add a retrieve
        Class1 ref = generateRandomEntity("jxscl_odata");
        TableOperation queryOp = TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), ref.getClass());
        batch.add(queryOp);

        // remove the retrieve
        batch.remove(queryOp);
        assertEquals(0, batch.size());

        // should be able to add an entity with a different partition key
        Class1 baseEntity = generateRandomEntity("jxscl_odata_2");
        TableOperation op = TableOperation.insert(baseEntity);
        batch.add(op);

        // remove the insert
        batch.remove(op);
        assertEquals(0, batch.size());

        // should be able to add a retrieve to the batch
        ref = generateRandomEntity("jxscl_odata");
        queryOp = TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), ref.getClass());
        batch.add(queryOp);

        // should be able to make a request to secondary as there are no writes
        try {
            TableRequestOptions options = new TableRequestOptions(this.options);
            options.setLocationMode(LocationMode.SECONDARY_ONLY);
            options.setRetryPolicyFactory(new RetryNoRetry());
            tClient.execute(testSuiteTableName, batch, options, null);
        }
        catch (StorageException e) {
            assertEquals("The specified resource does not exist.", e.getMessage());
            assertEquals(StorageErrorCodeStrings.RESOURCE_NOT_FOUND, e.getErrorCode());
        }
    }

    @Test
    public void testBatchDelete() throws StorageException {
        Class1 ref = generateRandomEntity("jxscl_odata");

        // insert entity  
        tClient.execute(testSuiteTableName, TableOperation.insert(ref), options, null);

        TableBatchOperation batch = new TableBatchOperation();
        batch.delete(ref);

        ArrayList<TableResult> delResults = tClient.execute(testSuiteTableName, batch, options, null);
        for (TableResult r : delResults) {
            assertEquals(r.getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
        }

        try {
            tClient.execute(testSuiteTableName, batch, options, null);
            fail();
        }
        catch (StorageException ex) {
            assertEquals(ex.getHttpStatusCode(), HttpURLConnection.HTTP_NOT_FOUND);
        }
    }

    @Test
    public void testBatchDeleteFail() throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();

        // Insert entity to delete
        Class1 baseEntity = generateRandomEntity("jxscl_odata");
        tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity), options, null);

        Class1 updatedEntity = generateRandomEntity("jxscl_odata");
        updatedEntity.setPartitionKey(baseEntity.getPartitionKey());
        updatedEntity.setRowKey(baseEntity.getRowKey());
        updatedEntity.setEtag(baseEntity.getEtag());
        tClient.execute(testSuiteTableName, TableOperation.replace(updatedEntity), options, null);

        // add delete to fail
        batch.delete(baseEntity);

        try {
            tClient.execute(testSuiteTableName, batch, options, null);
        }
        catch (TableServiceException ex) {
            assertEquals(ex.getMessage(), "Precondition Failed");
            assertTrue(ex.getExtendedErrorInformation().getErrorMessage()
                    .startsWith("The update condition specified in the request was not satisfied."));
            assertEquals(ex.getErrorCode(), StorageErrorCodeStrings.UPDATE_CONDITION_NOT_SATISFIED);
        }
    }

    @Test
    public void testBatchEmptyQuery() throws StorageException {
        // insert entity
        Class1 ref = generateRandomEntity("jxscl_odata");

        TableBatchOperation batch = new TableBatchOperation();
        batch.retrieve(ref.getPartitionKey(), ref.getRowKey(), ref.getClass());

        ArrayList<TableResult> results = tClient.execute(testSuiteTableName, batch, options, null);

        assertEquals(results.size(), 1);
        assertNull(results.get(0).getResult());
        assertEquals(results.get(0).getHttpStatusCode(), HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    public void testBatchInsertFail() throws StorageException {
        // insert entity
        Class1 ref = generateRandomEntity("jxscl_odata");
        tClient.execute(testSuiteTableName, TableOperation.insert(ref), options, null);
        try {
            TableBatchOperation batch = new TableBatchOperation();
            batch.insert(ref);
            tClient.execute(testSuiteTableName, batch, options, null);
        }
        catch (TableServiceException ex) {
            assertEquals(ex.getMessage(), "Conflict");
            assertTrue(ex.getExtendedErrorInformation().getErrorMessage()
                    .startsWith("The specified entity already exists"));
            assertEquals(ex.getErrorCode(), StorageErrorCodeStrings.ENTITY_ALREADY_EXISTS);
        }
    }

    @Test
    public void testBatchLockToPartitionKey() throws StorageException {
        try {
            TableBatchOperation batch = new TableBatchOperation();
            batch.insert(generateRandomEntity("jxscl_odata"));
            batch.insert(generateRandomEntity("jxscl_odata2"));
        }
        catch (IllegalArgumentException ex) {
            assertEquals(ex.getMessage(), SR.OPS_IN_BATCH_MUST_HAVE_SAME_PARTITION_KEY);
        }
    }

    @Test
    public void testBatchMergeFail() throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();
        addInsertBatch(batch);

        // Insert entity to merge
        Class1 baseEntity = generateRandomEntity("jxscl_odata");
        tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity), options, null);

        Class1 updatedEntity = generateRandomEntity("jxscl_odata");
        updatedEntity.setPartitionKey(baseEntity.getPartitionKey());
        updatedEntity.setRowKey(baseEntity.getRowKey());
        updatedEntity.setEtag(baseEntity.getEtag());
        tClient.execute(testSuiteTableName, TableOperation.replace(updatedEntity), options, null);

        // add merge to fail
        addMergeToBatch(baseEntity, batch);

        try {
            tClient.execute(testSuiteTableName, batch, options, null);
        }
        catch (TableServiceException ex) {
            assertEquals(ex.getMessage(), "Precondition Failed");
            String errorAfterSemiColon = ex.getExtendedErrorInformation().getErrorMessage();
            errorAfterSemiColon = errorAfterSemiColon.substring(errorAfterSemiColon.indexOf(":") + 1);
            assertTrue(errorAfterSemiColon
                    .startsWith("The update condition specified in the request was not satisfied."));
            assertEquals(ex.getErrorCode(), StorageErrorCodeStrings.UPDATE_CONDITION_NOT_SATISFIED);
        }
    }

    @Test
    public void testBatchMultiQueryShouldThrow() throws StorageException {
        Class1 ref = generateRandomEntity("jxscl_odata");
        Class1 ref2 = generateRandomEntity("jxscl_odata");

        try {
            TableBatchOperation batch = new TableBatchOperation();
            batch.retrieve(ref.getPartitionKey(), ref.getRowKey(), ref.getClass());
            batch.retrieve(ref2.getPartitionKey(), ref2.getRowKey(), ref2.getClass());
        }
        catch (IllegalArgumentException ex) {
            assertEquals(ex.getMessage(),
                    "A batch transaction with a retrieve operation cannot contain any other operations.");
        }
    }

    @Test
    public void testBatchAddNullShouldThrow() throws StorageException {
        try {
            TableBatchOperation batch = new TableBatchOperation();
            batch.add(null);
        }
        catch (IllegalArgumentException ex) {
            assertEquals(ex.getMessage(), String.format(SR.ARGUMENT_NULL_OR_EMPTY, "element"));
        }
    }

    @Test
    public void testBatchRetrieveWithNullResolver() throws StorageException {
        try {
            TableBatchOperation batch = new TableBatchOperation();
            batch.retrieve("foo", "blah", (EntityResolver<?>) null);
        }
        catch (IllegalArgumentException ex) {
            assertEquals(ex.getMessage(),
                    String.format(SR.ARGUMENT_NULL_OR_EMPTY, SR.QUERY_REQUIRES_VALID_CLASSTYPE_OR_RESOLVER));
        }
    }

    @Test
    public void testBatchOver100Entities() throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();
        try {
            for (int m = 0; m < 101; m++) {
                batch.insert(generateRandomEntity("jxscl_odata"));
            }

            tClient.execute(testSuiteTableName, batch, options, null);
        }
        catch (TableServiceException ex) {
            assertEquals(ex.getMessage(), "Bad Request");
            String errorAfterSemiColon = ex.getExtendedErrorInformation().getErrorMessage();
            errorAfterSemiColon = errorAfterSemiColon.substring(errorAfterSemiColon.indexOf(":") + 1);
            assertTrue(errorAfterSemiColon.startsWith("One of the request inputs is not valid."));
            assertEquals(ex.getErrorCode(), StorageErrorCodeStrings.INVALID_INPUT);
        }
    }

    @Test
    public void testBatchRetrieve() throws StorageException {
        // insert entity
        Class1 ref = generateRandomEntity("jxscl_odata");
        tClient.execute(testSuiteTableName, TableOperation.insert(ref), options, null);

        TableBatchOperation batch = new TableBatchOperation();
        batch.retrieve(ref.getPartitionKey(), ref.getRowKey(), ref.getClass());

        ArrayList<TableResult> results = tClient.execute(testSuiteTableName, batch, options, null);
        assertEquals(results.size(), 1);

        assertEquals(results.get(0).getHttpStatusCode(), HttpURLConnection.HTTP_OK);
        Class1 retrievedRef = results.get(0).getResultAsType();

        assertEquals(ref.getA(), retrievedRef.getA());
        assertEquals(ref.getB(), retrievedRef.getB());
        assertEquals(ref.getC(), retrievedRef.getC());
        assertTrue(Arrays.equals(ref.getD(), retrievedRef.getD()));

        tClient.execute(testSuiteTableName, TableOperation.delete(ref), options, null);
    }

    @Test
    public void tableBatchRetrieveWithEntityResolver() throws StorageException {
        // insert entity
        Class1 randEnt = generateRandomEntity("jxscl_odata");
        tClient.execute(testSuiteTableName, TableOperation.insert(randEnt), options, null);

        TableBatchOperation batch = new TableBatchOperation();
        batch.retrieve(randEnt.getPartitionKey(), randEnt.getRowKey(), new EntityResolver<Class1>() {
            @Override
            public Class1 resolve(String partitionKey, String rowKey, Date timeStamp,
                    HashMap<String, EntityProperty> properties, String etag) {
                assertEquals(properties.size(), 4);
                Class1 ref = new Class1();
                ref.setA(properties.get("A").getValueAsString());
                ref.setB(properties.get("B").getValueAsString());
                ref.setC(properties.get("C").getValueAsString());
                ref.setD(properties.get("D").getValueAsByteArray());
                return ref;
            }
        });

        ArrayList<TableResult> results = tClient.execute(testSuiteTableName, batch, options, null);
        assertEquals(results.size(), 1);

        Class1 ent = (Class1) results.get(0).getResult();

        // Validate results
        assertEquals(ent.getA(), randEnt.getA());
        assertEquals(ent.getB(), randEnt.getB());
        assertEquals(ent.getC(), randEnt.getC());
        assertTrue(Arrays.equals(ent.getD(), randEnt.getD()));
    }

    @Test
    public void testBatchRetrieveAndOneMoreOperationShouldThrow() throws StorageException {
        Class1 ref2 = generateRandomEntity("jxscl_odata");

        try {
            TableBatchOperation batch = new TableBatchOperation();
            batch.insert(generateRandomEntity("jxscl_odata"));
            batch.retrieve(ref2.getPartitionKey(), ref2.getRowKey(), ref2.getClass());
        }
        catch (IllegalArgumentException ex) {
            assertEquals(ex.getMessage(),
                    "A batch transaction with a retrieve operation cannot contain any other operations.");
        }

        try {
            TableBatchOperation batch = new TableBatchOperation();
            batch.retrieve(ref2.getPartitionKey(), ref2.getRowKey(), ref2.getClass());
            batch.insert(generateRandomEntity("jxscl_odata"));
        }
        catch (IllegalArgumentException ex) {
            assertEquals(ex.getMessage(),
                    "A batch transaction with a retrieve operation cannot contain any other operations.");
        }
    }

    @Test
    public void testBatchReplaceFail() throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();

        // Insert entity to merge
        Class1 baseEntity = generateRandomEntity("jxscl_odata");
        tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity), options, null);

        Class1 updatedEntity = generateRandomEntity("jxscl_odata");
        updatedEntity.setPartitionKey(baseEntity.getPartitionKey());
        updatedEntity.setRowKey(baseEntity.getRowKey());
        updatedEntity.setEtag(baseEntity.getEtag());
        tClient.execute(testSuiteTableName, TableOperation.replace(updatedEntity), options, null);

        // add merge to fail
        addReplaceToBatch(baseEntity, batch);

        try {
            tClient.execute(testSuiteTableName, batch);
        }
        catch (TableServiceException ex) {
            assertEquals(ex.getMessage(), "Precondition Failed");
            assertTrue(ex.getExtendedErrorInformation().getErrorMessage()
                    .startsWith("The update condition specified in the request was not satisfied."));
            assertEquals(ex.getErrorCode(), StorageErrorCodeStrings.UPDATE_CONDITION_NOT_SATISFIED);
        }
    }

    @Test
    public void testBatchInsertEntityOver1MB() throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();
        Class1 bigEnt = new Class1();

        bigEnt.setA("foo_A");
        bigEnt.setB("foo_B");
        bigEnt.setC("foo_C");
        // 1mb right here
        bigEnt.setD(new byte[1024 * 1024]);
        bigEnt.setPartitionKey("jxscl_odata");
        bigEnt.setRowKey(UUID.randomUUID().toString());

        batch.insert(bigEnt);

        for (int m = 0; m < 3; m++) {
            Class1 ref = new Class1();
            ref.setA("foo_A");
            ref.setB("foo_B");
            ref.setC("foo_C");
            ref.setPartitionKey("jxscl_odata");
            ref.setRowKey(UUID.randomUUID().toString());
            batch.insert(ref);
        }

        try {
            tClient.execute(testSuiteTableName, batch, options, null);
            fail();
        }
        catch (TableServiceException ex) {
            assertEquals(ex.getMessage(), "Bad Request");
            String errorAfterSemiColon = ex.getExtendedErrorInformation().getErrorMessage();
            errorAfterSemiColon = errorAfterSemiColon.substring(errorAfterSemiColon.indexOf(":") + 1);
            assertTrue(errorAfterSemiColon.startsWith("The entity is larger than the maximum allowed size (1MB)."));
            assertEquals(ex.getErrorCode(), StorageErrorCodeStrings.ENTITY_TOO_LARGE);
        }
    }

    @Test
    public void testBatchInsertEntityWithPropertyMoreThan255chars() throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();
        DynamicTableEntity bigEnt = new DynamicTableEntity();

        String propName = "";
        for (int m = 0; m < 256; m++) {
            propName = propName.concat("a");
        }

        bigEnt.getProperties().put(propName, new EntityProperty("test"));
        bigEnt.setPartitionKey("jxscl_odata");
        bigEnt.setRowKey(UUID.randomUUID().toString());

        batch.insert(bigEnt);

        for (int m = 0; m < 3; m++) {
            Class1 ref = new Class1();
            ref.setA("foo_A");
            ref.setB("foo_B");
            ref.setC("foo_C");
            ref.setPartitionKey("jxscl_odata");
            ref.setRowKey(UUID.randomUUID().toString());
            batch.insert(ref);
        }

        try {
            tClient.execute(testSuiteTableName, batch, options, null);
            fail();
        }
        catch (TableServiceException ex) {
            assertEquals(ex.getMessage(), "Bad Request");
            String errorAfterSemiColon = ex.getExtendedErrorInformation().getErrorMessage();
            errorAfterSemiColon = errorAfterSemiColon.substring(errorAfterSemiColon.indexOf(":") + 1);
            assertTrue(errorAfterSemiColon.startsWith("The property name exceeds the maximum allowed length (255)."));
            assertEquals(ex.getErrorCode(), "PropertyNameTooLong");
        }
    }

    @Test
    public void testBatchSizeOver4mb() throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();
        byte[] datArr = new byte[1024 * 128];
        Random rand = new Random();
        rand.nextBytes(datArr);

        // Each entity is approx 128kb, meaning ~32 entities will result in a request over 4mb.
        try {
            for (int m = 0; m < 32; m++) {
                Class1 ref = new Class1();

                ref.setA("foo_A");
                ref.setB("foo_B");
                ref.setC("foo_C");
                ref.setD(datArr);
                ref.setPartitionKey("jxscl_odata");
                ref.setRowKey(UUID.randomUUID().toString());
                batch.insert(ref);
            }

            tClient.execute(testSuiteTableName, batch, options, null);
        }
        catch (StorageException ex) {
            assertEquals(ex.getHttpStatusCode(), HttpURLConnection.HTTP_ENTITY_TOO_LARGE);
            assertEquals(ex.getErrorCode(), StorageErrorCodeStrings.REQUEST_BODY_TOO_LARGE);
            assertTrue(ex.getMessage().startsWith(
                    "The request body is too large and exceeds the maximum permissible limit."));
        }
    }

    @Test
    public void testBatchWithAllOperations() throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();
        // insert
        addInsertBatch(batch);

        {
            // insert entity to delete
            Class1 delRef = generateRandomEntity("jxscl_odata");
            tClient.execute(testSuiteTableName, TableOperation.insert(delRef), options, null);
            batch.delete(delRef);
        }

        {
            // Insert entity to replace
            Class1 baseEntity = generateRandomEntity("jxscl_odata");
            tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity), options, null);
            addReplaceToBatch(baseEntity, batch);
        }

        {
            // Insert entity to insert or replace
            Class1 baseEntity = generateRandomEntity("jxscl_odata");
            tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity), options, null);
            addInsertOrReplaceToBatch(baseEntity, batch);
        }

        {
            // Insert entity to merge
            Class1 baseEntity = generateRandomEntity("jxscl_odata");
            tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity), options, null);
            addMergeToBatch(baseEntity, batch);
        }

        {
            // Insert entity to merge, no pre-esisting entity
            Class1 baseEntity = generateRandomEntity("jxscl_odata");
            tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity), options, null);
            addInsertOrMergeToBatch(baseEntity, batch);
        }

        ArrayList<TableResult> results = tClient.execute(testSuiteTableName, batch, options, null);
        assertEquals(results.size(), 6);

        Iterator<TableResult> iter = results.iterator();

        // insert
        assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);

        // delete
        assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);

        // replace
        assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);

        // insert or replace
        assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);

        // merge
        assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);

        // insert or merge
        assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);

    }

    @Test
    public void testBatchInsert() throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();

        // Add 3 inserts
        addInsertBatch(batch); // default echo content (true)
        addInsertBatch(batch, true); // set echo content to true
        addInsertBatch(batch, false); // set echo content to false

        // insert entity
        Class1 ref = generateRandomEntity("jxscl_odata");
        tClient.execute(testSuiteTableName, TableOperation.insert(ref), options, null);
        batch.delete(ref);

        ArrayList<TableResult> results = tClient.execute(testSuiteTableName, batch, options, null);
        assertEquals(results.size(), 4);

        Iterator<TableResult> iter = results.iterator();

        TableResult res = iter.next();
        assertEquals(res.getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);

        res = iter.next();
        assertEquals(res.getHttpStatusCode(), HttpURLConnection.HTTP_CREATED);

        res = iter.next();
        assertEquals(res.getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);

        // delete
        assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
    }

    @Test
    public void testBatchMerge() throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();
        addInsertBatch(batch);

        // insert entity to delete
        Class1 delRef = generateRandomEntity("jxscl_odata");
        tClient.execute(testSuiteTableName, TableOperation.insert(delRef));
        batch.delete(delRef);

        // Insert entity to merge
        Class1 baseEntity = generateRandomEntity("jxscl_odata");
        tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity), options, null);
        addMergeToBatch(baseEntity, batch);

        ArrayList<TableResult> results = tClient.execute(testSuiteTableName, batch);
        assertEquals(results.size(), 3);

        Iterator<TableResult> iter = results.iterator();

        // insert
        assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);

        // delete
        assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);

        // merge
        assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
    }

    @Test
    public void testBatchReplace() throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();
        addInsertBatch(batch);

        // insert entity to delete
        Class1 delRef = generateRandomEntity("jxscl_odata");
        tClient.execute(testSuiteTableName, TableOperation.insert(delRef), options, null);
        batch.delete(delRef);

        // Insert entity to replace
        Class1 baseEntity = generateRandomEntity("jxscl_odata");
        tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity), options, null);
        addReplaceToBatch(baseEntity, batch);

        ArrayList<TableResult> results = tClient.execute(testSuiteTableName, batch);
        assertEquals(results.size(), 3);

        Iterator<TableResult> iter = results.iterator();

        // insert
        assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);

        // delete
        assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);

        // replace
        assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
    }

    @Test
    public void testBatchInsertOrMerge() throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();
        addInsertBatch(batch);

        // insert entity to delete
        Class1 delRef = generateRandomEntity("jxscl_odata");
        tClient.execute(testSuiteTableName, TableOperation.insert(delRef), options, null);
        batch.delete(delRef);

        // Insert entity to merge
        Class1 baseEntity = generateRandomEntity("jxscl_odata");
        tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity), options, null);
        addInsertOrMergeToBatch(baseEntity, batch);

        ArrayList<TableResult> results = tClient.execute(testSuiteTableName, batch);
        assertEquals(results.size(), 3);

        Iterator<TableResult> iter = results.iterator();

        // insert
        assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);

        // delete
        assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);

        // merge
        assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
    }

    @Test
    public void testBatchInsertOrReplace() throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();
        addInsertBatch(batch);

        // insert entity to delete
        Class1 delRef = generateRandomEntity("jxscl_odata");
        tClient.execute(testSuiteTableName, TableOperation.insert(delRef), options, null);
        batch.delete(delRef);

        // Insert entity to replace
        Class1 baseEntity = generateRandomEntity("jxscl_odata");
        tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity), options, null);
        addInsertOrReplaceToBatch(baseEntity, batch);

        ArrayList<TableResult> results = tClient.execute(testSuiteTableName, batch, options, null);
        assertEquals(results.size(), 3);

        Iterator<TableResult> iter = results.iterator();

        // insert
        assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);

        // delete
        assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);

        // replace
        assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
    }

    @Test
    public void testEmptyBatch() throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();

        try {
            tClient.execute(testSuiteTableName, batch, options, null);
        }
        catch (IllegalArgumentException ex) {
            assertEquals(ex.getMessage(), "Cannot execute an empty batch operation.");
        }
    }

    @Test
    public void testInsertBatch1() throws StorageException {
        insertAndDeleteBatchWithX(1);
    }

    @Test
    public void testInsertBatch10() throws StorageException {
        insertAndDeleteBatchWithX(10);
    }

    @Test
    public void testInsertBatch100() throws StorageException {
        insertAndDeleteBatchWithX(100);
    }

    @Test
    public void testUpsertBatch1() throws StorageException {
        upsertAndDeleteBatchWithX(1);
    }

    @Test
    public void testUpsertBatch10() throws StorageException {
        upsertAndDeleteBatchWithX(10);
    }

    @Test
    public void testUpsertBatch100() throws StorageException {
        upsertAndDeleteBatchWithX(100);
    }

    private Class1 addInsertBatch(TableBatchOperation batch) {
        Class1 ref = generateRandomEntity("jxscl_odata");
        batch.insert(ref);
        return ref;
    }

    private Class1 addInsertBatch(TableBatchOperation batch, boolean echoContent) {
        Class1 ref = generateRandomEntity("jxscl_odata");
        batch.insert(ref, echoContent);
        return ref;
    }

    private Class2 addInsertOrMergeToBatch(Class1 baseEntity, TableBatchOperation batch) {
        Class2 secondEntity = createEntityToReplaceOrMerge(baseEntity);
        batch.insertOrMerge(secondEntity);
        return secondEntity;
    }

    private Class2 addInsertOrReplaceToBatch(Class1 baseEntity, TableBatchOperation batch) {
        Class2 secondEntity = createEntityToReplaceOrMerge(baseEntity);
        batch.insertOrReplace(secondEntity);
        return secondEntity;
    }

    private Class2 addMergeToBatch(Class1 baseEntity, TableBatchOperation batch) {
        Class2 secondEntity = createEntityToReplaceOrMerge(baseEntity);
        batch.merge(secondEntity);
        return secondEntity;
    }

    private Class2 addReplaceToBatch(Class1 baseEntity, TableBatchOperation batch) {
        Class2 secondEntity = createEntityToReplaceOrMerge(baseEntity);
        batch.replace(secondEntity);
        return secondEntity;
    }

    private void insertAndDeleteBatchWithX(int x) throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();
        for (int m = 0; m < x; m++) {
            addInsertBatch(batch);
        }

        TableBatchOperation delBatch = new TableBatchOperation();
        ArrayList<TableResult> results = tClient.execute(testSuiteTableName, batch);
        for (TableResult r : results) {
            assertEquals(r.getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
            delBatch.delete((Class1) r.getResult());
        }

        ArrayList<TableResult> delResults = tClient.execute(testSuiteTableName, delBatch);
        for (TableResult r : delResults) {
            assertEquals(r.getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
        }
    }

    private void upsertAndDeleteBatchWithX(int x) throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();
        for (int m = 0; m < x; m++) {
            addInsertOrMergeToBatch(generateRandomEntity("jxscl_odata"), batch);
        }

        TableBatchOperation delBatch = new TableBatchOperation();
        ArrayList<TableResult> results = tClient.execute(testSuiteTableName, batch, options, null);
        for (TableResult r : results) {
            assertEquals(r.getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
            delBatch.delete((Class2) r.getResult());
        }

        ArrayList<TableResult> delResults = tClient.execute(testSuiteTableName, delBatch, options, null);
        for (TableResult r : delResults) {
            assertEquals(r.getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
        }
    }

    private ArrayList<TableOperation> allOpsList() throws StorageException {
        ArrayList<TableOperation> ops = new ArrayList<TableOperation>();

        // insert
        ops.add(TableOperation.insert(generateRandomEntity("jxscl_odata")));

        {
            // Insert entity to delete
            Class1 delRef = generateRandomEntity("jxscl_odata");
            tClient.execute(testSuiteTableName, TableOperation.insert(delRef), options, null);
            ops.add(TableOperation.delete(delRef));
        }

        {
            // Insert entity to replace
            Class1 baseEntity = generateRandomEntity("jxscl_odata");
            tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity), options, null);
            ops.add(TableOperation.replace(createEntityToReplaceOrMerge(baseEntity)));
        }

        {
            // Insert entity to insert or replace
            Class1 baseEntity = generateRandomEntity("jxscl_odata");
            tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity), options, null);
            ops.add(TableOperation.insertOrReplace(createEntityToReplaceOrMerge(baseEntity)));
        }

        {
            // Insert or replace, no pre-existing entity
            Class1 baseEntity = generateRandomEntity("jxscl_odata");
            ops.add(TableOperation.insertOrReplace(createEntityToReplaceOrMerge(baseEntity)));
        }

        {
            // Insert entity to merge
            Class1 baseEntity = generateRandomEntity("jxscl_odata");
            tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity), options, null);
            ops.add(TableOperation.merge(createEntityToReplaceOrMerge(baseEntity)));
        }

        {
            // Insert entity to insert or merge
            Class1 baseEntity = generateRandomEntity("jxscl_odata");
            tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity), options, null);
            ops.add(TableOperation.insertOrMerge(baseEntity));
        }

        {
            // Insert or merge, no pre-existing entity
            Class1 baseEntity = generateRandomEntity("jxscl_odata");
            ops.add(TableOperation.insertOrMerge(baseEntity));
        }

        return ops;
    }

    private Class2 createEntityToReplaceOrMerge(Class1 baseEntity) {
        Class2 secondEntity = new Class2();
        secondEntity.setL("foo_L");
        secondEntity.setM("foo_M");
        secondEntity.setN("foo_N");
        secondEntity.setO("foo_O");
        secondEntity.setPartitionKey(baseEntity.getPartitionKey());
        secondEntity.setRowKey(baseEntity.getRowKey());
        secondEntity.setEtag(baseEntity.getEtag());
        return secondEntity;
    }
}
