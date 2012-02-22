/**
 * Copyright 2011 Microsoft Corporation
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
package com.microsoft.windowsazure.services.table.client;

import static org.junit.Assert.*;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;

import com.microsoft.windowsazure.services.core.storage.StorageException;

public class TableBatchOperationTests extends TableTestBase {
    @Test
    public void batchDelete() throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();

        // insert entity
        class1 ref = generateRandomEnitity("jxscl_odata");
        tClient.execute(testSuiteTableName, TableOperation.insert(ref));
        batch.delete(ref);

        ArrayList<TableResult> delResults = tClient.execute(testSuiteTableName, batch);
        for (TableResult r : delResults) {
            Assert.assertEquals(r.getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
        }

        try {
            tClient.execute(testSuiteTableName, batch);
            fail();
        }
        catch (StorageException ex) {
            Assert.assertEquals(ex.getHttpStatusCode(), HttpURLConnection.HTTP_NOT_FOUND);
        }
    }

    @Test
    public void batchDeleteFail() throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();

        // Insert entity to delete
        class1 baseEntity = generateRandomEnitity("jxscl_odata");
        tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity));

        class1 updatedEntity = generateRandomEnitity("jxscl_odata");
        updatedEntity.setPartitionKey(baseEntity.getPartitionKey());
        updatedEntity.setRowKey(baseEntity.getRowKey());
        updatedEntity.setEtag(baseEntity.getEtag());
        tClient.execute(testSuiteTableName, TableOperation.replace(updatedEntity));

        // add delete to fail
        batch.delete(baseEntity);

        try {
            @SuppressWarnings("unused")
            ArrayList<TableResult> results = tClient.execute(testSuiteTableName, batch);
        }
        catch (TableServiceException ex) {
            Assert.assertEquals(ex.getMessage(), "Precondition Failed");
            Assert.assertTrue(ex.getExtendedErrorInformation().getErrorMessage()
                    .startsWith("The update condition specified in the request was not satisfied."));
            Assert.assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "UpdateConditionNotSatisfied");
        }
    }

    @Test
    public void batchEmptyQuery() throws StorageException {
        // insert entity
        class1 ref = generateRandomEnitity("jxscl_odata");

        TableBatchOperation batch = new TableBatchOperation();
        batch.retrieve(ref.getPartitionKey(), ref.getRowKey(), ref.getClass());

        ArrayList<TableResult> results = tClient.execute(testSuiteTableName, batch);

        Assert.assertEquals(results.size(), 1);
        Assert.assertNull(results.get(0).getResult());
        Assert.assertEquals(results.get(0).getHttpStatusCode(), HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    public void batchInsertFail() throws StorageException {
        // insert entity
        class1 ref = generateRandomEnitity("jxscl_odata");
        tClient.execute(testSuiteTableName, TableOperation.insert(ref));
        try {
            TableBatchOperation batch = new TableBatchOperation();
            batch.insert(ref);
            tClient.execute(testSuiteTableName, batch);
        }
        catch (TableServiceException ex) {
            Assert.assertEquals(ex.getMessage(), "Conflict");
            Assert.assertTrue(ex.getExtendedErrorInformation().getErrorMessage()
                    .startsWith("The specified entity already exists"));
            Assert.assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "EntityAlreadyExists");
        }
    }

    @Test
    public void batchLockToPartitionKey() throws StorageException {
        try {
            TableBatchOperation batch = new TableBatchOperation();
            batch.insert(generateRandomEnitity("jxscl_odata"));
            batch.insert(generateRandomEnitity("jxscl_odata2"));
        }
        catch (IllegalArgumentException ex) {
            Assert.assertEquals(ex.getMessage(), "All entities in a given batch must have the same partition key.");
        }
    }

    @Test
    public void batchMergeFail() throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();
        addInsertBatch(batch);

        // Insert entity to merge
        class1 baseEntity = generateRandomEnitity("jxscl_odata");
        tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity));

        class1 updatedEntity = generateRandomEnitity("jxscl_odata");
        updatedEntity.setPartitionKey(baseEntity.getPartitionKey());
        updatedEntity.setRowKey(baseEntity.getRowKey());
        updatedEntity.setEtag(baseEntity.getEtag());
        tClient.execute(testSuiteTableName, TableOperation.replace(updatedEntity));

        // add merge to fail
        addMergeToBatch(baseEntity, batch);

        try {
            @SuppressWarnings("unused")
            ArrayList<TableResult> results = tClient.execute(testSuiteTableName, batch);
        }
        catch (TableServiceException ex) {
            Assert.assertEquals(ex.getMessage(), "Precondition Failed");
            String errorAfterSemiColon = ex.getExtendedErrorInformation().getErrorMessage();
            errorAfterSemiColon = errorAfterSemiColon.substring(errorAfterSemiColon.indexOf(":") + 1);
            Assert.assertTrue(errorAfterSemiColon
                    .startsWith("The update condition specified in the request was not satisfied."));
            Assert.assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "UpdateConditionNotSatisfied");
        }
    }

    @Test
    public void batchMultiQueryShouldThrow() throws StorageException {
        class1 ref = generateRandomEnitity("jxscl_odata");
        class1 ref2 = generateRandomEnitity("jxscl_odata");

        try {
            TableBatchOperation batch = new TableBatchOperation();
            batch.retrieve(ref.getPartitionKey(), ref.getRowKey(), ref.getClass());
            batch.retrieve(ref2.getPartitionKey(), ref2.getRowKey(), ref2.getClass());
        }
        catch (IllegalArgumentException ex) {
            Assert.assertEquals(ex.getMessage(),
                    "A batch transaction with a retrieve operation cannot contain any other operations.");
        }
    }

    @Test
    public void batchAddNullShouldThrow() throws StorageException {
        try {
            TableBatchOperation batch = new TableBatchOperation();
            batch.add(null);
        }
        catch (IllegalArgumentException ex) {
            Assert.assertEquals(ex.getMessage(), "element");
        }
    }

    @Test
    public void batchRetrieveWithNullResolver() throws StorageException {
        try {
            TableBatchOperation batch = new TableBatchOperation();
            batch.retrieve("foo", "blah", (EntityResolver<?>) null);
        }
        catch (IllegalArgumentException ex) {
            Assert.assertEquals(ex.getMessage(), "Query requires a valid class type or resolver.");
        }
    }

    @Test
    public void batchOver100Entities() throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();
        try {
            for (int m = 0; m < 101; m++) {
                batch.insert(generateRandomEnitity("jxscl_odata"));
            }

            tClient.execute(testSuiteTableName, batch);
        }
        catch (TableServiceException ex) {
            Assert.assertEquals(ex.getMessage(), "Bad Request");
            String errorAfterSemiColon = ex.getExtendedErrorInformation().getErrorMessage();
            errorAfterSemiColon = errorAfterSemiColon.substring(errorAfterSemiColon.indexOf(":") + 1);
            Assert.assertTrue(errorAfterSemiColon.startsWith("One of the request inputs is not valid."));
            Assert.assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "InvalidInput");
        }
    }

    @Test
    public void batchQuery() throws StorageException {
        // insert entity
        class1 ref = generateRandomEnitity("jxscl_odata");
        tClient.execute(testSuiteTableName, TableOperation.insert(ref));

        TableBatchOperation batch = new TableBatchOperation();

        batch.retrieve(ref.getPartitionKey(), ref.getRowKey(), ref.getClass());

        ArrayList<TableResult> results = tClient.execute(testSuiteTableName, batch);
        Assert.assertEquals(results.size(), 1);

        Assert.assertEquals(results.get(0).getHttpStatusCode(), HttpURLConnection.HTTP_OK);
        class1 retrievedRef = results.get(0).getResultAsType();

        Assert.assertEquals(ref.getA(), retrievedRef.getA());
        Assert.assertEquals(ref.getB(), retrievedRef.getB());
        Assert.assertEquals(ref.getC(), retrievedRef.getC());
        Assert.assertTrue(Arrays.equals(ref.getD(), retrievedRef.getD()));

        tClient.execute(testSuiteTableName, TableOperation.delete(ref));
    }

    @Test
    public void batchQueryAndOneMoreOperationShouldThrow() throws StorageException {
        class1 ref2 = generateRandomEnitity("jxscl_odata");

        try {
            TableBatchOperation batch = new TableBatchOperation();
            batch.insert(generateRandomEnitity("jxscl_odata"));
            batch.retrieve(ref2.getPartitionKey(), ref2.getRowKey(), ref2.getClass());
        }
        catch (IllegalArgumentException ex) {
            Assert.assertEquals(ex.getMessage(),
                    "A batch transaction with a retrieve operation cannot contain any other operations.");
        }

        try {
            TableBatchOperation batch = new TableBatchOperation();
            batch.retrieve(ref2.getPartitionKey(), ref2.getRowKey(), ref2.getClass());
            batch.insert(generateRandomEnitity("jxscl_odata"));
        }
        catch (IllegalArgumentException ex) {
            Assert.assertEquals(ex.getMessage(),
                    "A batch transaction with a retrieve operation cannot contain any other operations.");
        }
    }

    @Test
    public void batchReplaceFail() throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();

        // Insert entity to merge
        class1 baseEntity = generateRandomEnitity("jxscl_odata");
        tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity));

        class1 updatedEntity = generateRandomEnitity("jxscl_odata");
        updatedEntity.setPartitionKey(baseEntity.getPartitionKey());
        updatedEntity.setRowKey(baseEntity.getRowKey());
        updatedEntity.setEtag(baseEntity.getEtag());
        tClient.execute(testSuiteTableName, TableOperation.replace(updatedEntity));

        // add merge to fail
        addReplaceToBatch(baseEntity, batch);

        try {
            @SuppressWarnings("unused")
            ArrayList<TableResult> results = tClient.execute(testSuiteTableName, batch);
        }
        catch (TableServiceException ex) {
            Assert.assertEquals(ex.getMessage(), "Precondition Failed");
            String errorAfterSemiColon = ex.getExtendedErrorInformation().getErrorMessage();
            errorAfterSemiColon = errorAfterSemiColon.substring(errorAfterSemiColon.indexOf(":") + 1);
            Assert.assertTrue(errorAfterSemiColon
                    .startsWith("The condition specified using HTTP conditional header(s) is not met."));
            Assert.assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "ConditionNotMet");
        }
    }

    @Test
    public void batchInsertEntityOver1MB() throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();
        class1 bigEnt = new class1();

        bigEnt.setA("foo_A");
        bigEnt.setB("foo_B");
        bigEnt.setC("foo_C");
        // 1mb right here
        bigEnt.setD(new byte[1024 * 1024]);
        bigEnt.setPartitionKey("jxscl_odata");
        bigEnt.setRowKey(UUID.randomUUID().toString());

        batch.insert(bigEnt);

        for (int m = 0; m < 3; m++) {
            class1 ref = new class1();
            ref.setA("foo_A");
            ref.setB("foo_B");
            ref.setC("foo_C");
            ref.setPartitionKey("jxscl_odata");
            ref.setRowKey(UUID.randomUUID().toString());
            batch.insert(ref);
        }

        try {
            tClient.execute(testSuiteTableName, batch);
            fail();
        }
        catch (TableServiceException ex) {
            Assert.assertEquals(ex.getMessage(), "Bad Request");
            String errorAfterSemiColon = ex.getExtendedErrorInformation().getErrorMessage();
            errorAfterSemiColon = errorAfterSemiColon.substring(errorAfterSemiColon.indexOf(":") + 1);
            Assert.assertTrue(errorAfterSemiColon.startsWith("The entity is larger than allowed by the Table Service."));
            Assert.assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "EntityTooLarge");
        }
    }

    @Test
    public void batchInsertEntityWithPropertyMoreThan255chars() throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();
        DynamicTableEntity bigEnt = new DynamicTableEntity();

        String propName = "";
        for (int m = 0; m < 255; m++) {
            propName.concat(Integer.toString(m % 9));
        }

        bigEnt.getProperties().put(propName, new EntityProperty("test"));
        bigEnt.setPartitionKey("jxscl_odata");
        bigEnt.setRowKey(UUID.randomUUID().toString());

        batch.insert(bigEnt);

        for (int m = 0; m < 3; m++) {
            class1 ref = new class1();
            ref.setA("foo_A");
            ref.setB("foo_B");
            ref.setC("foo_C");
            ref.setPartitionKey("jxscl_odata");
            ref.setRowKey(UUID.randomUUID().toString());
            batch.insert(ref);
        }

        try {
            tClient.execute(testSuiteTableName, batch);
            fail();
        }
        catch (TableServiceException ex) {
            Assert.assertEquals(ex.getMessage(), "Bad Request");
            String errorAfterSemiColon = ex.getExtendedErrorInformation().getErrorMessage();
            errorAfterSemiColon = errorAfterSemiColon.substring(errorAfterSemiColon.indexOf(":") + 1);
            Assert.assertTrue(errorAfterSemiColon.startsWith("One of the request inputs is not valid."));
            Assert.assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "InvalidInput");
        }
    }

    @Test
    public void batchSizeOver4mb() throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();
        byte[] datArr = new byte[1024 * 128];
        Random rand = new Random();
        rand.nextBytes(datArr);

        // Each entity is approx 128kb, meaning ~32 entities will result in a request over 4mb.
        try {
            for (int m = 0; m < 32; m++) {
                class1 ref = new class1();

                ref.setA("foo_A");
                ref.setB("foo_B");
                ref.setC("foo_C");
                ref.setD(datArr);
                ref.setPartitionKey("jxscl_odata");
                ref.setRowKey(UUID.randomUUID().toString());
                batch.insert(ref);
            }

            tClient.execute(testSuiteTableName, batch);
        }
        catch (TableServiceException ex) {
            Assert.assertEquals(ex.getMessage(), "Bad Request");
            String errorAfterSemiColon = ex.getExtendedErrorInformation().getErrorMessage();
            Assert.assertTrue(errorAfterSemiColon
                    .startsWith("The content length for the requested operation has exceeded the limit."));
            Assert.assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "ContentLengthExceeded");
        }
    }

    @Test
    public void batchWithAllOperations() throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();
        // insert
        addInsertBatch(batch);

        {
            // insert entity to delete
            class1 delRef = generateRandomEnitity("jxscl_odata");
            tClient.execute(testSuiteTableName, TableOperation.insert(delRef));
            batch.delete(delRef);
        }

        {
            // Insert entity to replace
            class1 baseEntity = generateRandomEnitity("jxscl_odata");
            tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity));
            addReplaceToBatch(baseEntity, batch);
        }

        {
            // Insert entity to insert or replace
            class1 baseEntity = generateRandomEnitity("jxscl_odata");
            tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity));
            addInsertOrReplaceToBatch(baseEntity, batch);
        }

        {
            // Insert entity to merge
            class1 baseEntity = generateRandomEnitity("jxscl_odata");
            tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity));
            addMergeToBatch(baseEntity, batch);
        }

        {
            // Insert entity to merge, no pre-esisting entity
            class1 baseEntity = generateRandomEnitity("jxscl_odata");
            tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity));
            addInsertOrMergeToBatch(baseEntity, batch);
        }

        ArrayList<TableResult> results = tClient.execute(testSuiteTableName, batch);
        Assert.assertEquals(results.size(), 6);

        Iterator<TableResult> iter = results.iterator();

        // insert
        Assert.assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_CREATED);

        // delete
        Assert.assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);

        // replace
        Assert.assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);

        // insert or replace
        Assert.assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);

        // merge
        Assert.assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);

        // insert or merge
        Assert.assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);

    }

    @Test
    public void batchInsert() throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();

        // Add 3 inserts
        for (int m = 0; m < 3; m++) {
            addInsertBatch(batch);
        }

        // insert entity
        class1 ref = generateRandomEnitity("jxscl_odata");
        tClient.execute(testSuiteTableName, TableOperation.insert(ref));
        batch.delete(ref);

        ArrayList<TableResult> results = tClient.execute(testSuiteTableName, batch);
        Assert.assertEquals(results.size(), 4);

        Iterator<TableResult> iter = results.iterator();

        Assert.assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_CREATED);
        Assert.assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_CREATED);
        Assert.assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_CREATED);

        // delete
        Assert.assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
    }

    @Test
    public void batchMerge() throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();
        addInsertBatch(batch);

        // insert entity to delete
        class1 delRef = generateRandomEnitity("jxscl_odata");
        tClient.execute(testSuiteTableName, TableOperation.insert(delRef));
        batch.delete(delRef);

        // Insert entity to merge
        class1 baseEntity = generateRandomEnitity("jxscl_odata");
        tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity));
        addMergeToBatch(baseEntity, batch);

        ArrayList<TableResult> results = tClient.execute(testSuiteTableName, batch);
        Assert.assertEquals(results.size(), 3);

        Iterator<TableResult> iter = results.iterator();

        // insert
        Assert.assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_CREATED);

        // delete
        Assert.assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);

        // merge
        Assert.assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
    }

    @Test
    public void batchReplace() throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();
        addInsertBatch(batch);

        // insert entity to delete
        class1 delRef = generateRandomEnitity("jxscl_odata");
        tClient.execute(testSuiteTableName, TableOperation.insert(delRef));
        batch.delete(delRef);

        // Insert entity to replace
        class1 baseEntity = generateRandomEnitity("jxscl_odata");
        tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity));
        addReplaceToBatch(baseEntity, batch);

        ArrayList<TableResult> results = tClient.execute(testSuiteTableName, batch);
        Assert.assertEquals(results.size(), 3);

        Iterator<TableResult> iter = results.iterator();

        // insert
        Assert.assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_CREATED);

        // delete
        Assert.assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);

        // replace
        Assert.assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
    }

    @Test
    public void batchInsertOrMerge() throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();
        addInsertBatch(batch);

        // insert entity to delete
        class1 delRef = generateRandomEnitity("jxscl_odata");
        tClient.execute(testSuiteTableName, TableOperation.insert(delRef));
        batch.delete(delRef);

        // Insert entity to merge
        class1 baseEntity = generateRandomEnitity("jxscl_odata");
        tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity));
        addInsertOrMergeToBatch(baseEntity, batch);

        ArrayList<TableResult> results = tClient.execute(testSuiteTableName, batch);
        Assert.assertEquals(results.size(), 3);

        Iterator<TableResult> iter = results.iterator();

        // insert
        Assert.assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_CREATED);

        // delete
        Assert.assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);

        // merge
        Assert.assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
    }

    @Test
    public void batchInsertOrReplace() throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();
        addInsertBatch(batch);

        // insert entity to delete
        class1 delRef = generateRandomEnitity("jxscl_odata");
        tClient.execute(testSuiteTableName, TableOperation.insert(delRef));
        batch.delete(delRef);

        // Insert entity to replace
        class1 baseEntity = generateRandomEnitity("jxscl_odata");
        tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity));
        addInsertOrReplaceToBatch(baseEntity, batch);

        ArrayList<TableResult> results = tClient.execute(testSuiteTableName, batch);
        Assert.assertEquals(results.size(), 3);

        Iterator<TableResult> iter = results.iterator();

        // insert
        Assert.assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_CREATED);

        // delete
        Assert.assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);

        // replace
        Assert.assertEquals(iter.next().getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
    }

    @Test
    public void emptyBatch() throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();

        try {
            tClient.execute(testSuiteTableName, batch);
        }
        catch (IllegalArgumentException ex) {
            Assert.assertEquals(ex.getMessage(), "Cannot Execute an empty batch operation");
        }
    }

    @Test
    public void insertBatch1() throws StorageException {
        insertAndDeleteBatchWithX(1);
    }

    @Test
    public void insertBatch10() throws StorageException {
        insertAndDeleteBatchWithX(10);
    }

    @Test
    public void insertBatch100() throws StorageException {
        insertAndDeleteBatchWithX(100);
    }

    @Test
    public void upsertBatch1() throws StorageException {
        upsertAndDeleteBatchWithX(1);
    }

    @Test
    public void upsertBatch10() throws StorageException {
        upsertAndDeleteBatchWithX(10);
    }

    @Test
    public void upsertBatch100() throws StorageException {
        upsertAndDeleteBatchWithX(100);
    }

    private class1 addInsertBatch(TableBatchOperation batch) {
        class1 ref = generateRandomEnitity("jxscl_odata");
        batch.insert(ref);
        return ref;
    }

    private class2 addInsertOrMergeToBatch(class1 baseEntity, TableBatchOperation batch) {
        class2 secondEntity = new class2();
        secondEntity.setL("foo_L");
        secondEntity.setM("foo_M");
        secondEntity.setN("foo_N");
        secondEntity.setO("foo_O");
        secondEntity.setPartitionKey(baseEntity.getPartitionKey());
        secondEntity.setRowKey(baseEntity.getRowKey());
        secondEntity.setEtag(baseEntity.getEtag());

        batch.insertOrMerge(secondEntity);
        return secondEntity;
    }

    private class2 addInsertOrReplaceToBatch(class1 baseEntity, TableBatchOperation batch) {
        class2 secondEntity = new class2();
        secondEntity.setL("foo_L");
        secondEntity.setM("foo_M");
        secondEntity.setN("foo_N");
        secondEntity.setO("foo_O");
        secondEntity.setPartitionKey(baseEntity.getPartitionKey());
        secondEntity.setRowKey(baseEntity.getRowKey());
        secondEntity.setEtag(baseEntity.getEtag());

        batch.insertOrReplace(secondEntity);
        return secondEntity;
    }

    private class2 addMergeToBatch(class1 baseEntity, TableBatchOperation batch) {
        class2 secondEntity = new class2();
        secondEntity.setL("foo_L");
        secondEntity.setM("foo_M");
        secondEntity.setN("foo_N");
        secondEntity.setO("foo_O");
        secondEntity.setPartitionKey(baseEntity.getPartitionKey());
        secondEntity.setRowKey(baseEntity.getRowKey());
        secondEntity.setEtag(baseEntity.getEtag());

        batch.merge(secondEntity);
        return secondEntity;
    }

    private class2 addReplaceToBatch(class1 baseEntity, TableBatchOperation batch) {
        class2 secondEntity = new class2();
        secondEntity.setL("foo_L");
        secondEntity.setM("foo_M");
        secondEntity.setN("foo_N");
        secondEntity.setO("foo_O");
        secondEntity.setPartitionKey(baseEntity.getPartitionKey());
        secondEntity.setRowKey(baseEntity.getRowKey());
        secondEntity.setEtag(baseEntity.getEtag());

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
            Assert.assertEquals(r.getHttpStatusCode(), HttpURLConnection.HTTP_CREATED);
            delBatch.delete((class1) r.getResult());
        }

        ArrayList<TableResult> delResults = tClient.execute(testSuiteTableName, delBatch);
        for (TableResult r : delResults) {
            Assert.assertEquals(r.getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
        }
    }

    private void upsertAndDeleteBatchWithX(int x) throws StorageException {
        TableBatchOperation batch = new TableBatchOperation();
        for (int m = 0; m < x; m++) {
            addInsertOrMergeToBatch(generateRandomEnitity("jxscl_odata"), batch);
        }

        TableBatchOperation delBatch = new TableBatchOperation();
        ArrayList<TableResult> results = tClient.execute(testSuiteTableName, batch);
        for (TableResult r : results) {
            Assert.assertEquals(r.getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
            delBatch.delete((class2) r.getResult());
        }

        ArrayList<TableResult> delResults = tClient.execute(testSuiteTableName, delBatch);
        for (TableResult r : delResults) {
            Assert.assertEquals(r.getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
        }
    }
}
