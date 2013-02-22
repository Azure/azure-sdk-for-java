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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;

import com.microsoft.windowsazure.services.core.storage.StorageException;

/**
 * Table Operation Tests
 */
public class TableOperationTests extends TableTestBase {
    @Test
    public void delete() throws StorageException {
        class1 ref = new class1();

        ref.setA("foo_A");
        ref.setB("foo_B");
        ref.setC("foo_C");
        ref.setD(new byte[] { 0, 1, 2 });
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());

        TableOperation op = TableOperation.insert(ref);

        tClient.execute(testSuiteTableName, op);
        tClient.execute(testSuiteTableName, TableOperation.delete(ref));

        TableResult res2 = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), class1.class));

        Assert.assertTrue(res2.getResult() == null);
    }

    @Test
    public void deleteFail() throws StorageException {
        class1 ref = new class1();

        ref.setA("foo_A");
        ref.setB("foo_B");
        ref.setC("foo_C");
        ref.setD(new byte[] { 0, 1, 2 });
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());

        tClient.execute(testSuiteTableName, TableOperation.insert(ref));
        String oldEtag = ref.getEtag();

        // update entity
        ref.setA("updated");
        tClient.execute(testSuiteTableName, TableOperation.replace(ref));

        ref.setEtag(oldEtag);

        try {
            tClient.execute(testSuiteTableName, TableOperation.delete(ref));
            fail();
        }
        catch (TableServiceException ex) {
            Assert.assertEquals(ex.getMessage(), "Precondition Failed");
            Assert.assertTrue(ex.getExtendedErrorInformation().getErrorMessage()
                    .startsWith("The update condition specified in the request was not satisfied."));
            Assert.assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "UpdateConditionNotSatisfied");
        }

        TableResult res2 = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), class1.class));

        ref = res2.getResultAsType();
        // actually delete it
        tClient.execute(testSuiteTableName, TableOperation.delete(ref));

        // now try to delete it and fail
        try {
            tClient.execute(testSuiteTableName, TableOperation.delete(ref));
            fail();
        }
        catch (TableServiceException ex) {
            Assert.assertEquals(ex.getMessage(), "Not Found");
            Assert.assertTrue(ex.getExtendedErrorInformation().getErrorMessage()
                    .startsWith("The specified resource does not exist."));
            Assert.assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "ResourceNotFound");
        }
    }

    @Test
    public void emptyRetrieve() throws StorageException {
        class1 ref = new class1();
        ref.setA("foo_A");
        ref.setB("foo_B");
        ref.setC("foo_C");
        ref.setD(new byte[] { 0, 1, 2 });
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());

        TableResult res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), class1.class));

        Assert.assertNull(res.getResult());
        Assert.assertEquals(res.getHttpStatusCode(), HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    public void insertOrMerge() throws StorageException {
        class1 baseEntity = new class1();
        baseEntity.setA("foo_A");
        baseEntity.setB("foo_B");
        baseEntity.setC("foo_C");
        baseEntity.setD(new byte[] { 0, 1, 2 });
        baseEntity.setPartitionKey("jxscl_odata");
        baseEntity.setRowKey(UUID.randomUUID().toString());

        class2 secondEntity = new class2();
        secondEntity.setL("foo_L");
        secondEntity.setM("foo_M");
        secondEntity.setN("foo_N");
        secondEntity.setO("foo_O");
        secondEntity.setPartitionKey(baseEntity.getPartitionKey());
        secondEntity.setRowKey(baseEntity.getRowKey());
        secondEntity.setEtag(baseEntity.getEtag());

        // Insert or merge Entity - ENTITY DOES NOT EXIST NOW.
        TableResult insertResult = tClient.execute(testSuiteTableName, TableOperation.insertOrMerge(baseEntity));

        Assert.assertEquals(insertResult.getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
        Assert.assertNotNull(insertResult.getEtag());

        // Insert or replace Entity - ENTITY EXISTS -> WILL REPLACE
        tClient.execute(testSuiteTableName, TableOperation.insertOrMerge(secondEntity));

        // Retrieve entity
        TableResult queryResult = tClient
                .execute(testSuiteTableName, TableOperation.retrieve(baseEntity.getPartitionKey(),
                        baseEntity.getRowKey(), DynamicTableEntity.class));

        DynamicTableEntity retrievedEntity = queryResult.<DynamicTableEntity> getResultAsType();

        Assert.assertNotNull("Property A", retrievedEntity.getProperties().get("A"));
        Assert.assertEquals(baseEntity.getA(), retrievedEntity.getProperties().get("A").getValueAsString());

        Assert.assertNotNull("Property B", retrievedEntity.getProperties().get("B"));
        Assert.assertEquals(baseEntity.getB(), retrievedEntity.getProperties().get("B").getValueAsString());

        Assert.assertNotNull("Property C", retrievedEntity.getProperties().get("C"));
        Assert.assertEquals(baseEntity.getC(), retrievedEntity.getProperties().get("C").getValueAsString());

        Assert.assertNotNull("Property D", retrievedEntity.getProperties().get("D"));
        Assert.assertTrue(Arrays.equals(baseEntity.getD(), retrievedEntity.getProperties().get("D")
                .getValueAsByteArray()));

        // Validate New properties exist
        Assert.assertNotNull("Property L", retrievedEntity.getProperties().get("L"));
        Assert.assertEquals(secondEntity.getL(), retrievedEntity.getProperties().get("L").getValueAsString());

        Assert.assertNotNull("Property M", retrievedEntity.getProperties().get("M"));
        Assert.assertEquals(secondEntity.getM(), retrievedEntity.getProperties().get("M").getValueAsString());

        Assert.assertNotNull("Property N", retrievedEntity.getProperties().get("N"));
        Assert.assertEquals(secondEntity.getN(), retrievedEntity.getProperties().get("N").getValueAsString());

        Assert.assertNotNull("Property O", retrievedEntity.getProperties().get("O"));
        Assert.assertEquals(secondEntity.getO(), retrievedEntity.getProperties().get("O").getValueAsString());
    }

    @Test
    public void insertOrReplace() throws StorageException {
        class1 baseEntity = new class1();
        baseEntity.setA("foo_A");
        baseEntity.setB("foo_B");
        baseEntity.setC("foo_C");
        baseEntity.setD(new byte[] { 0, 1, 2 });
        baseEntity.setPartitionKey("jxscl_odata");
        baseEntity.setRowKey(UUID.randomUUID().toString());

        class2 secondEntity = new class2();
        secondEntity.setL("foo_L");
        secondEntity.setM("foo_M");
        secondEntity.setN("foo_N");
        secondEntity.setO("foo_O");
        secondEntity.setPartitionKey(baseEntity.getPartitionKey());
        secondEntity.setRowKey(baseEntity.getRowKey());
        secondEntity.setEtag(baseEntity.getEtag());

        // Insert or replace Entity - ENTITY DOES NOT EXIST NOW.
        TableResult insertResult = tClient.execute(testSuiteTableName, TableOperation.insertOrReplace(baseEntity));

        Assert.assertEquals(insertResult.getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
        Assert.assertNotNull(insertResult.getEtag());

        // Insert or replace Entity - ENTITY EXISTS -> WILL REPLACE
        tClient.execute(testSuiteTableName, TableOperation.insertOrReplace(secondEntity));

        // Retrieve entity
        TableResult queryResult = tClient
                .execute(testSuiteTableName, TableOperation.retrieve(baseEntity.getPartitionKey(),
                        baseEntity.getRowKey(), DynamicTableEntity.class));

        DynamicTableEntity retrievedEntity = queryResult.getResultAsType();

        // Validate old properties dont exist
        Assert.assertTrue(retrievedEntity.getProperties().get("A") == null);
        Assert.assertTrue(retrievedEntity.getProperties().get("B") == null);
        Assert.assertTrue(retrievedEntity.getProperties().get("C") == null);
        Assert.assertTrue(retrievedEntity.getProperties().get("D") == null);

        // Validate New properties exist
        Assert.assertNotNull("Property L", retrievedEntity.getProperties().get("L"));
        Assert.assertEquals(secondEntity.getL(), retrievedEntity.getProperties().get("L").getValueAsString());

        Assert.assertNotNull("Property M", retrievedEntity.getProperties().get("M"));
        Assert.assertEquals(secondEntity.getM(), retrievedEntity.getProperties().get("M").getValueAsString());

        Assert.assertNotNull("Property N", retrievedEntity.getProperties().get("N"));
        Assert.assertEquals(secondEntity.getN(), retrievedEntity.getProperties().get("N").getValueAsString());

        Assert.assertNotNull("Property O", retrievedEntity.getProperties().get("O"));
        Assert.assertEquals(secondEntity.getO(), retrievedEntity.getProperties().get("O").getValueAsString());
    }

    @Test
    public void merge() throws StorageException {
        // Insert base entity
        class1 baseEntity = new class1();
        baseEntity.setA("foo_A");
        baseEntity.setB("foo_B");
        baseEntity.setC("foo_C");
        baseEntity.setD(new byte[] { 0, 1, 2 });
        baseEntity.setPartitionKey("jxscl_odata");
        baseEntity.setRowKey(UUID.randomUUID().toString());

        tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity));

        class2 secondEntity = new class2();
        secondEntity.setL("foo_L");
        secondEntity.setM("foo_M");
        secondEntity.setN("foo_N");
        secondEntity.setO("foo_O");
        secondEntity.setPartitionKey(baseEntity.getPartitionKey());
        secondEntity.setRowKey(baseEntity.getRowKey());
        secondEntity.setEtag(baseEntity.getEtag());

        TableResult mergeResult = tClient.execute(testSuiteTableName, TableOperation.merge(secondEntity));

        Assert.assertEquals(mergeResult.getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
        Assert.assertNotNull(mergeResult.getEtag());

        TableResult res2 = tClient.execute(testSuiteTableName, TableOperation.retrieve(secondEntity.getPartitionKey(),
                secondEntity.getRowKey(), DynamicTableEntity.class));
        DynamicTableEntity mergedEntity = (DynamicTableEntity) res2.getResult();

        Assert.assertNotNull("Property A", mergedEntity.getProperties().get("A"));
        Assert.assertEquals(baseEntity.getA(), mergedEntity.getProperties().get("A").getValueAsString());

        Assert.assertNotNull("Property B", mergedEntity.getProperties().get("B"));
        Assert.assertEquals(baseEntity.getB(), mergedEntity.getProperties().get("B").getValueAsString());

        Assert.assertNotNull("Property C", mergedEntity.getProperties().get("C"));
        Assert.assertEquals(baseEntity.getC(), mergedEntity.getProperties().get("C").getValueAsString());

        Assert.assertNotNull("Property D", mergedEntity.getProperties().get("D"));
        Assert.assertTrue(Arrays.equals(baseEntity.getD(), mergedEntity.getProperties().get("D").getValueAsByteArray()));

        Assert.assertNotNull("Property L", mergedEntity.getProperties().get("L"));
        Assert.assertEquals(secondEntity.getL(), mergedEntity.getProperties().get("L").getValueAsString());

        Assert.assertNotNull("Property M", mergedEntity.getProperties().get("M"));
        Assert.assertEquals(secondEntity.getM(), mergedEntity.getProperties().get("M").getValueAsString());

        Assert.assertNotNull("Property N", mergedEntity.getProperties().get("N"));
        Assert.assertEquals(secondEntity.getN(), mergedEntity.getProperties().get("N").getValueAsString());

        Assert.assertNotNull("Property O", mergedEntity.getProperties().get("O"));
        Assert.assertEquals(secondEntity.getO(), mergedEntity.getProperties().get("O").getValueAsString());
    }

    @Test
    public void mergeFail() throws StorageException {
        // Insert base entity
        class1 baseEntity = new class1();
        baseEntity.setA("foo_A");
        baseEntity.setB("foo_B");
        baseEntity.setC("foo_C");
        baseEntity.setD(new byte[] { 0, 1, 2 });
        baseEntity.setPartitionKey("jxscl_odata");
        baseEntity.setRowKey(UUID.randomUUID().toString());

        tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity));

        class2 secondEntity = new class2();
        secondEntity.setL("foo_L");
        secondEntity.setM("foo_M");
        secondEntity.setN("foo_N");
        secondEntity.setO("foo_O");
        secondEntity.setPartitionKey(baseEntity.getPartitionKey());
        secondEntity.setRowKey(baseEntity.getRowKey());
        secondEntity.setEtag(baseEntity.getEtag());
        String oldEtag = baseEntity.getEtag();

        tClient.execute(testSuiteTableName, TableOperation.merge(secondEntity));

        secondEntity.setEtag(oldEtag);
        secondEntity.setL("updated");
        try {
            tClient.execute(testSuiteTableName, TableOperation.merge(secondEntity));
            fail();
        }
        catch (TableServiceException ex) {
            Assert.assertEquals(ex.getMessage(), "Precondition Failed");
            Assert.assertTrue(ex.getExtendedErrorInformation().getErrorMessage()
                    .startsWith("The update condition specified in the request was not satisfied."));
            Assert.assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "UpdateConditionNotSatisfied");
        }

        // delete entity
        TableResult queryResult = tClient
                .execute(testSuiteTableName, TableOperation.retrieve(baseEntity.getPartitionKey(),
                        baseEntity.getRowKey(), DynamicTableEntity.class));

        DynamicTableEntity retrievedEntity = queryResult.getResultAsType();
        tClient.execute(testSuiteTableName, TableOperation.delete(retrievedEntity));

        try {
            tClient.execute(testSuiteTableName, TableOperation.merge(secondEntity));
            fail();
        }
        catch (TableServiceException ex) {
            Assert.assertEquals(ex.getMessage(), "Not Found");
            Assert.assertTrue(ex.getExtendedErrorInformation().getErrorMessage()
                    .startsWith("The specified resource does not exist."));
            Assert.assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "ResourceNotFound");
        }
    }

    @Test
    public void retrieveWithoutResolver() throws StorageException {
        class1 ref = new class1();

        ref.setA("foo_A");
        ref.setB("foo_B");
        ref.setC("foo_C");
        ref.setD(new byte[] { 0, 1, 2 });
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());

        tClient.execute(testSuiteTableName, TableOperation.insert(ref));

        TableResult res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), class1.class));

        @SuppressWarnings("unused")
        class1 retrievedEnt = res.getResultAsType();

        Assert.assertEquals(((class1) res.getResult()).getA(), ref.getA());
    }

    @Test
    public void retrieveWithResolver() throws StorageException {
        class1 ref = new class1();
        ref.setA("foo_A");
        ref.setB("foo_B");
        ref.setC("foo_C");
        ref.setD(new byte[] { 0, 1, 2 });
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());

        TableOperation op = TableOperation.insert(ref);

        tClient.execute(testSuiteTableName, op);

        TableResult res4 = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), new EntityResolver<String>() {
                    @Override
                    public String resolve(String partitionKey, String rowKey, Date timeStamp,
                            HashMap<String, EntityProperty> properties, String etag) {
                        return properties.get("A").getValueAsString();
                    }
                }));

        Assert.assertEquals(res4.getResult().toString(), ref.getA());
    }

    @Test
    public void retrieveWithNullResolver() throws StorageException {
        try {
            TableOperation.retrieve("foo", "blah", (EntityResolver<?>) null);
        }
        catch (IllegalArgumentException ex) {
            Assert.assertEquals(ex.getMessage(), "Query requires a valid class type or resolver.");
        }
    }

    @Test
    public void insertFail() throws StorageException {
        class1 ref = new class1();
        ref.setA("foo_A");
        ref.setB("foo_B");
        ref.setC("foo_C");
        ref.setD(new byte[] { 0, 1, 2 });
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());

        TableOperation op = TableOperation.insert(ref);

        tClient.execute(testSuiteTableName, op);
        try {
            tClient.execute(testSuiteTableName, op);
            fail();
        }
        catch (TableServiceException ex) {
            Assert.assertEquals(ex.getMessage(), "Conflict");
            Assert.assertTrue(ex.getExtendedErrorInformation().getErrorMessage()
                    .startsWith("The specified entity already exists"));
            Assert.assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "EntityAlreadyExists");
        }
    }

    @Test
    public void replace() throws StorageException {
        class1 baseEntity = new class1();
        baseEntity.setA("foo_A");
        baseEntity.setB("foo_B");
        baseEntity.setC("foo_C");
        baseEntity.setD(new byte[] { 0, 1, 2 });
        baseEntity.setPartitionKey("jxscl_odata");
        baseEntity.setRowKey(UUID.randomUUID().toString());

        // Insert entity
        tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity));

        TableResult queryResult = tClient
                .execute(testSuiteTableName, TableOperation.retrieve(baseEntity.getPartitionKey(),
                        baseEntity.getRowKey(), DynamicTableEntity.class));
        // Retrieve entity
        DynamicTableEntity retrievedEntity = queryResult.<DynamicTableEntity> getResultAsType();
        Assert.assertNotNull("Property D", retrievedEntity.getProperties().get("D"));
        Assert.assertTrue(Arrays.equals(baseEntity.getD(), retrievedEntity.getProperties().get("D")
                .getValueAsByteArray()));

        // Remove property and update
        retrievedEntity.getProperties().remove("D");

        TableResult replaceResult = tClient.execute(testSuiteTableName, TableOperation.replace(retrievedEntity));

        Assert.assertEquals(replaceResult.getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
        Assert.assertNotNull(replaceResult.getEtag());

        // Retrieve Entity
        queryResult = tClient
                .execute(testSuiteTableName, TableOperation.retrieve(baseEntity.getPartitionKey(),
                        baseEntity.getRowKey(), DynamicTableEntity.class));

        retrievedEntity = queryResult.<DynamicTableEntity> getResultAsType();

        // Validate
        Assert.assertNotNull("Property A", retrievedEntity.getProperties().get("A"));
        Assert.assertEquals(baseEntity.getA(), retrievedEntity.getProperties().get("A").getValueAsString());

        Assert.assertNotNull("Property B", retrievedEntity.getProperties().get("B"));
        Assert.assertEquals(baseEntity.getB(), retrievedEntity.getProperties().get("B").getValueAsString());

        Assert.assertNotNull("Property C", retrievedEntity.getProperties().get("C"));
        Assert.assertEquals(baseEntity.getC(), retrievedEntity.getProperties().get("C").getValueAsString());

        Assert.assertTrue(retrievedEntity.getProperties().get("D") == null);
    }

    @Test
    public void replaceFail() throws StorageException {
        class1 baseEntity = new class1();
        baseEntity.setA("foo_A");
        baseEntity.setB("foo_B");
        baseEntity.setC("foo_C");
        baseEntity.setD(new byte[] { 0, 1, 2 });
        baseEntity.setPartitionKey("jxscl_odata");
        baseEntity.setRowKey(UUID.randomUUID().toString());

        // Insert entity
        tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity));

        String oldEtag = baseEntity.getEtag();

        TableResult queryResult = tClient
                .execute(testSuiteTableName, TableOperation.retrieve(baseEntity.getPartitionKey(),
                        baseEntity.getRowKey(), DynamicTableEntity.class));

        // Retrieve entity
        DynamicTableEntity retrievedEntity = queryResult.<DynamicTableEntity> getResultAsType();
        Assert.assertNotNull("Property D", retrievedEntity.getProperties().get("D"));
        Assert.assertTrue(Arrays.equals(baseEntity.getD(), retrievedEntity.getProperties().get("D")
                .getValueAsByteArray()));

        // Remove property and update
        retrievedEntity.getProperties().remove("D");

        tClient.execute(testSuiteTableName, TableOperation.replace(retrievedEntity));

        retrievedEntity.setEtag(oldEtag);

        try {
            tClient.execute(testSuiteTableName, TableOperation.replace(retrievedEntity));
            fail();
        }
        catch (TableServiceException ex) {
            Assert.assertEquals(ex.getMessage(), "Precondition Failed");
            Assert.assertTrue(ex.getExtendedErrorInformation().getErrorMessage()
                    .startsWith("The update condition specified in the request was not satisfied."));
            Assert.assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "UpdateConditionNotSatisfied");
        }

        // delete entity
        queryResult = tClient
                .execute(testSuiteTableName, TableOperation.retrieve(baseEntity.getPartitionKey(),
                        baseEntity.getRowKey(), DynamicTableEntity.class));

        tClient.execute(testSuiteTableName, TableOperation.delete((DynamicTableEntity) queryResult.getResultAsType()));

        try {
            tClient.execute(testSuiteTableName, TableOperation.replace(retrievedEntity));
            fail();
        }
        catch (TableServiceException ex) {
            Assert.assertEquals(ex.getMessage(), "Not Found");
            Assert.assertTrue(ex.getExtendedErrorInformation().getErrorMessage()
                    .startsWith("The specified resource does not exist."));
            Assert.assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "ResourceNotFound");
        }
    }

    @Test
    public void insertEntityOver1MB() throws StorageException {
        class1 ref = new class1();

        ref.setA("foo_A");
        ref.setB("foo_B");
        ref.setC("foo_C");
        // 1mb right here
        ref.setD(new byte[1024 * 1024]);
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());

        try {
            tClient.execute(testSuiteTableName, TableOperation.insert(ref));
            fail();
        }
        catch (TableServiceException ex) {
            Assert.assertEquals(ex.getMessage(), "Bad Request");
            Assert.assertTrue(ex.getExtendedErrorInformation().getErrorMessage()
                    .startsWith("The entity is larger than allowed by the Table Service."));
            Assert.assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "EntityTooLarge");
        }
    }

    @Test
    public void insertEntityWithPropertyMoreThan255chars() throws StorageException {
        DynamicTableEntity ref = new DynamicTableEntity();

        String propName = "";
        for (int m = 0; m < 255; m++) {
            propName.concat(Integer.toString(m % 9));
        }

        ref.getProperties().put(propName, new EntityProperty("test"));
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());

        try {
            tClient.execute(testSuiteTableName, TableOperation.insert(ref));
            fail();
        }
        catch (TableServiceException ex) {
            Assert.assertEquals(ex.getMessage(), "Bad Request");
            Assert.assertTrue(ex.getExtendedErrorInformation().getErrorMessage()
                    .startsWith("One of the request inputs is not valid."));
            Assert.assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "InvalidInput");
        }
    }
}
