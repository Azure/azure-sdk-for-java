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

import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.TestRunners.CloudTests;
import com.microsoft.azure.storage.TestRunners.DevFabricTests;
import com.microsoft.azure.storage.TestRunners.DevStoreTests;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.table.TableRequestOptions.PropertyResolver;
import com.microsoft.azure.storage.table.TableTestHelper.Class1;
import com.microsoft.azure.storage.table.TableTestHelper.Class2;
import com.microsoft.azure.storage.table.TableTestHelper.EmptyClass;
import com.microsoft.azure.storage.table.TableTestHelper.EmptyClassDynamic;
import com.microsoft.azure.storage.table.TableTestHelper.class1class2PropertyResolver;

import static org.junit.Assert.*;

/**
 * Table Operation Tests
 */
@Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
public class TableOperationTests {

    private CloudTable table;

    @Before
    public void tableTestMethodSetUp() throws URISyntaxException, StorageException {
        this.table = TableTestHelper.getRandomTableReference();
        this.table.createIfNotExists();
    }

    @After
    public void tableTestMethodTearDown() throws StorageException {
        this.table.deleteIfExists();
    }

    @Test
    public void testPropertyCacheDisable() {
        try {
            TableServiceEntity.getReflectedEntityCache().put(this.getClass(), new HashMap<String, PropertyPair>());

            TableServiceEntity.setReflectedEntityCacheDisabled(true);
            assertEquals(true, TableServiceEntity.isReflectedEntityCacheDisabled());
            assertTrue(TableServiceEntity.getReflectedEntityCache().isEmpty());

            TableServiceEntity.setReflectedEntityCacheDisabled(false);
            assertEquals(false, TableServiceEntity.isReflectedEntityCacheDisabled());
        }
        finally {

            TableServiceEntity.setReflectedEntityCacheDisabled(false);
        }
    }

    @Test
    public void testRetrieveWithNullResolver() {
        try {
            TableOperation.retrieve("foo", "blah", (EntityResolver<?>) null);
        }
        catch (IllegalArgumentException ex) {
            assertEquals(String.format(SR.ARGUMENT_NULL, SR.QUERY_REQUIRES_VALID_CLASSTYPE_OR_RESOLVER),
                    ex.getMessage());
        }
    }

    @Test
    public void testEntityWithSingleQuote() throws StorageException {
        TableRequestOptions options = new TableRequestOptions();
        options.setTablePayloadFormat(TablePayloadFormat.Json);

        EmptyClass ref = new EmptyClass();
        ref.setPartitionKey("partition'key");
        ref.setRowKey("row'key");

        this.table.execute(TableOperation.insert(ref), options, null);
        this.table.execute(TableOperation.merge(ref), options, null);
        this.table.execute(TableOperation.insertOrReplace(ref), options, null);
        this.table.execute(TableOperation.insertOrMerge(ref), options, null);
        this.table.execute(TableOperation.replace(ref), options, null);
        this.table.execute(TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), EmptyClass.class), options, null);
        this.table.execute(TableOperation.delete(ref), options, null);
    }

    @Test
    public void testInsertEntityWithoutPartitionKeyRowKey() {
        TableRequestOptions options = new TableRequestOptions();
        options.setTablePayloadFormat(TablePayloadFormat.Json);

        EmptyClass ref = new EmptyClass();
        ref.setPartitionKey("jxscl_odata");

        try {
            this.table.execute(TableOperation.insert(ref), options, null);
            fail("Inserts should not allow null row keys.");
        }
        catch (IllegalArgumentException e) {
            // continue, this is appropriate
        }
        catch (Exception e) {
            fail("Inserts with null row key should fail with an IllegalArgumentException thrown by assert not null.");
        }

        ref.setPartitionKey(null);
        ref.setRowKey(UUID.randomUUID().toString());
        try {
            this.table.execute(TableOperation.insert(ref), options, null);
            fail("Inserts should not allow null partition keys.");
        }
        catch (IllegalArgumentException e) {
            // continue, this is appropriate
        }
        catch (Exception e) {
            fail("Inserts with null partition key should fail with an IllegalArgumentException thrown by assert not null.");
        }
    }

    @Test
    public void testInsertEntityWithPropertyMoreThan255chars() throws StorageException {
        TableRequestOptions options = new TableRequestOptions();
        options.setTablePayloadFormat(TablePayloadFormat.Json);

        DynamicTableEntity ref = new DynamicTableEntity();

        String propName = "";
        for (int m = 0; m < 256; m++) {
            propName = propName.concat("a");
        }

        ref.getProperties().put(propName, new EntityProperty("test"));
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());

        try {
            this.table.execute(TableOperation.insert(ref), options, null);
            fail();
        }
        catch (TableServiceException ex) {
            assertEquals(ex.getMessage(), "Bad Request");
            assertTrue(ex.getExtendedErrorInformation().getErrorMessage()
                    .startsWith("The property name exceeds the maximum allowed length (255)."));
            assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "PropertyNameTooLong");
        }
    }

    @Test
    public void testInsertEntityOver1MB() throws StorageException {
        TableRequestOptions options = new TableRequestOptions();
        options.setTablePayloadFormat(TablePayloadFormat.Json);

        Class1 ref = new Class1();
        ref.setA("foo_A");
        ref.setB("foo_B");
        ref.setC("foo_C");
        // 1mb right here
        ref.setD(new byte[1024 * 1024]);
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());

        try {
            this.table.execute(TableOperation.insert(ref), options, null);
            fail();
        }
        catch (TableServiceException ex) {
            assertEquals(ex.getMessage(), "Bad Request");
            assertTrue(ex.getExtendedErrorInformation().getErrorMessage()
                    .startsWith("The entity is larger than the maximum allowed size (1MB)."));
            assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "EntityTooLarge");
        }
    }

    @Test
    public void testInsertEntityWithNumericProperty() throws StorageException {
        TableRequestOptions options = new TableRequestOptions();
        options.setTablePayloadFormat(TablePayloadFormat.Json);

        DynamicTableEntity ref = new DynamicTableEntity();

        String propName = "";
        for (int m = 0; m < 255; m++) {
            propName = propName.concat(Integer.toString(m % 9));
        }

        ref.getProperties().put(propName, new EntityProperty("test"));
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());

        try {
            this.table.execute(TableOperation.insert(ref), options, null);
            fail();
        }
        catch (TableServiceException ex) {
            assertEquals(ex.getMessage(), "Bad Request");
            assertTrue(ex.getExtendedErrorInformation().getErrorMessage().startsWith("The property name is invalid."));
            assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "PropertyNameInvalid");
        }
    }

    @Test
    public void testDeleteFail() throws StorageException {
        TableRequestOptions options = new TableRequestOptions();
        options.setTablePayloadFormat(TablePayloadFormat.Json);

        Class1 ref = new Class1();
        ref.setA("foo_A");
        ref.setB("foo_B");
        ref.setC("foo_C");
        ref.setD(new byte[]{0, 1, 2});
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());

        this.table.execute(TableOperation.insert(ref), options, null);
        String oldEtag = ref.getEtag();

        // update entity
        ref.setA("updated");
        this.table.execute(TableOperation.replace(ref), options, null);

        ref.setEtag(oldEtag);

        try {
            this.table.execute(TableOperation.delete(ref), options, null);
            fail();
        }
        catch (TableServiceException ex) {
            assertEquals(ex.getMessage(), "Precondition Failed");
            assertTrue(ex.getExtendedErrorInformation().getErrorMessage()
                    .startsWith("The update condition specified in the request was not satisfied."));
            assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "UpdateConditionNotSatisfied");
        }

        TableResult res2 = this.table.execute(
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), Class1.class), options, null);

        ref = res2.getResultAsType();
        // actually delete it
        this.table.execute(TableOperation.delete(ref), options, null);

        // now try to delete it and fail
        try {
            this.table.execute(TableOperation.delete(ref), options, null);
            fail();
        }
        catch (TableServiceException ex) {
            assertEquals(ex.getMessage(), "Not Found");
            assertTrue(ex.getExtendedErrorInformation().getErrorMessage()
                    .startsWith("The specified resource does not exist."));
            assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "ResourceNotFound");
        }
    }

    @Test
    public void testMergeFail() throws StorageException {
        TableRequestOptions options = new TableRequestOptions();
        options.setTablePayloadFormat(TablePayloadFormat.Json);

        // Insert base entity
        Class1 baseEntity = new Class1();
        baseEntity.setA("foo_A");
        baseEntity.setB("foo_B");
        baseEntity.setC("foo_C");
        baseEntity.setD(new byte[]{0, 1, 2});
        baseEntity.setPartitionKey("jxscl_odata");
        baseEntity.setRowKey(UUID.randomUUID().toString());

        this.table.execute(TableOperation.insert(baseEntity), options, null);

        Class2 secondEntity = new Class2();
        secondEntity.setL("foo_L");
        secondEntity.setM("foo_M");
        secondEntity.setN("foo_N");
        secondEntity.setO("foo_O");
        secondEntity.setPartitionKey(baseEntity.getPartitionKey());
        secondEntity.setRowKey(baseEntity.getRowKey());
        secondEntity.setEtag(baseEntity.getEtag());
        String oldEtag = baseEntity.getEtag();

        this.table.execute(TableOperation.merge(secondEntity), options, null);

        secondEntity.setEtag(oldEtag);
        secondEntity.setL("updated");
        try {
            this.table.execute(TableOperation.merge(secondEntity), options, null);
            fail();
        }
        catch (TableServiceException ex) {
            assertEquals(ex.getMessage(), "Precondition Failed");
            assertTrue(ex.getExtendedErrorInformation().getErrorMessage()
                    .startsWith("The update condition specified in the request was not satisfied."));
            assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "UpdateConditionNotSatisfied");
        }

        // retrieve entity
        TableResult queryResult = this.table
                .execute(TableOperation.retrieve(baseEntity.getPartitionKey(), baseEntity.getRowKey(),
                        DynamicTableEntity.class), options, null);

        DynamicTableEntity retrievedEntity = queryResult.getResultAsType();
        this.table.execute(TableOperation.delete(retrievedEntity), options, null);

        try {
            this.table.execute(TableOperation.merge(secondEntity), options, null);
            fail();
        }
        catch (TableServiceException ex) {
            assertEquals(ex.getMessage(), "Not Found");
            assertTrue(ex.getExtendedErrorInformation().getErrorMessage()
                    .startsWith("The specified resource does not exist."));
            assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "ResourceNotFound");
        }
    }

    @Test
    public void testInsertFail() throws StorageException {
        TableRequestOptions options = new TableRequestOptions();
        options.setTablePayloadFormat(TablePayloadFormat.Json);

        Class1 ref = new Class1();
        ref.setA("foo_A");
        ref.setB("foo_B");
        ref.setC("foo_C");
        ref.setD(new byte[]{0, 1, 2});
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());

        TableOperation op = TableOperation.insert(ref);

        this.table.execute(op, options, null);
        try {
            this.table.execute(op, options, null);
            fail();
        }
        catch (TableServiceException ex) {
            assertEquals(ex.getMessage(), "Conflict");
            assertTrue(ex.getExtendedErrorInformation().getErrorMessage()
                    .startsWith("The specified entity already exists"));
            assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "EntityAlreadyExists");
        }
    }

    @Test
    public void testReplaceFail() throws StorageException {
        TableRequestOptions options = new TableRequestOptions();
        options.setTablePayloadFormat(TablePayloadFormat.Json);

        Class1 baseEntity = new Class1();
        baseEntity.setA("foo_A");
        baseEntity.setB("foo_B");
        baseEntity.setC("foo_C");
        baseEntity.setD(new byte[]{0, 1, 2});
        baseEntity.setPartitionKey("jxscl_odata");
        baseEntity.setRowKey(UUID.randomUUID().toString());

        // Insert entity
        this.table.execute(TableOperation.insert(baseEntity), options, null);

        String oldEtag = baseEntity.getEtag();

        TableResult queryResult = this.table
                .execute(TableOperation.retrieve(baseEntity.getPartitionKey(), baseEntity.getRowKey(),
                        DynamicTableEntity.class), options, null);

        // Retrieve entity
        DynamicTableEntity retrievedEntity = queryResult.<DynamicTableEntity>getResultAsType();
        assertNotNull("Property D", retrievedEntity.getProperties().get("D"));
        assertTrue(Arrays.equals(baseEntity.getD(), retrievedEntity.getProperties().get("D").getValueAsByteArray()));

        // Remove property and update
        retrievedEntity.getProperties().remove("D");

        this.table.execute(TableOperation.replace(retrievedEntity), options, null);

        retrievedEntity.setEtag(oldEtag);

        try {
            this.table.execute(TableOperation.replace(retrievedEntity), options, null);
            fail();
        }
        catch (TableServiceException ex) {
            assertEquals(ex.getMessage(), "Precondition Failed");
            assertTrue(ex.getExtendedErrorInformation().getErrorMessage()
                    .startsWith("The update condition specified in the request was not satisfied."));
            assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "UpdateConditionNotSatisfied");
        }

        // delete entity
        queryResult = this.table
                .execute(TableOperation.retrieve(baseEntity.getPartitionKey(), baseEntity.getRowKey(),
                        DynamicTableEntity.class), options, null);

        this.table.execute(TableOperation.delete((DynamicTableEntity) queryResult.getResultAsType()), options, null);

        try {
            this.table.execute(TableOperation.replace(retrievedEntity), options, null);
            fail();
        }
        catch (TableServiceException ex) {
            assertEquals(ex.getMessage(), "Not Found");
            assertTrue(ex.getExtendedErrorInformation().getErrorMessage()
                    .startsWith("The specified resource does not exist."));
            assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "ResourceNotFound");
        }
    }

    @Test
    public void testEmptyRetrieve() throws StorageException {
        TableRequestOptions options = new TableRequestOptions();
        options.setTablePayloadFormat(TablePayloadFormat.Json);
        
        Class1 ref = new Class1();
        ref.setA("foo_A");
        ref.setB("foo_B");
        ref.setC("foo_C");
        ref.setD(new byte[]{0, 1, 2});
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());

        TableResult res = this.table.execute(
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), Class1.class), options, null);

        assertNull(res.getResult());
        assertEquals(res.getHttpStatusCode(), HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    public void testInsertEmptyEntity() throws StorageException {
        TableRequestOptions options = new TableRequestOptions();
        options.setTablePayloadFormat(TablePayloadFormat.Json);

        EmptyClass ref = new EmptyClass();
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey("echo_default" + UUID.randomUUID().toString());

        TableResult res = this.table.execute(TableOperation.insert(ref), options, null);
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, res.getHttpStatusCode());

        EmptyClassDynamic refDynamic = new EmptyClassDynamic();
        refDynamic.setPartitionKey("jxscl_odata");
        refDynamic.setRowKey("echo_default" + UUID.randomUUID().toString());

        res = this.table.execute(TableOperation.insert(refDynamic), options, null);
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, res.getHttpStatusCode());
    }

    @Test
    public void testDelete() throws StorageException {
        TableRequestOptions options = new TableRequestOptions();

        options.setTablePayloadFormat(TablePayloadFormat.JsonFullMetadata);
        testDelete(options);

        options.setTablePayloadFormat(TablePayloadFormat.Json);
        testDelete(options);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        testDelete(options);
    }

    private void testDelete(TableRequestOptions options) throws StorageException {
        Class1 ref = new Class1();
        ref.setA("foo_A");
        ref.setB("foo_B");
        ref.setC("foo_C");
        ref.setD(new byte[]{0, 1, 2});
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());

        TableOperation op = TableOperation.insert(ref);

        this.table.execute(op, options, null);
        this.table.execute(TableOperation.delete(ref), options, null);

        TableResult res2 = this.table.execute(
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), Class1.class), options, null);

        assertTrue(res2.getResult() == null);
    }

    @Test
    public void testInsertOrMerge() throws StorageException {
        TableRequestOptions options = new TableRequestOptions();
        
        options.setTablePayloadFormat(TablePayloadFormat.JsonFullMetadata);
        testInsertOrMerge(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.Json);
        testInsertOrMerge(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        testInsertOrMerge(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        testInsertOrMerge(options, true);
    }

    private void testInsertOrMerge(TableRequestOptions options, boolean usePropertyResolver) throws StorageException {
        Class1 baseEntity = new Class1();
        baseEntity.setA("foo_A");
        baseEntity.setB("foo_B");
        baseEntity.setC("foo_C");
        baseEntity.setD(new byte[]{0, 1, 2});
        baseEntity.setPartitionKey("jxscl_odata");
        baseEntity.setRowKey(UUID.randomUUID().toString());

        Class2 secondEntity = new Class2();
        secondEntity.setL("foo_L");
        secondEntity.setM("foo_M");
        secondEntity.setN("foo_N");
        secondEntity.setO("foo_O");
        secondEntity.setPartitionKey(baseEntity.getPartitionKey());
        secondEntity.setRowKey(baseEntity.getRowKey());
        secondEntity.setEtag(baseEntity.getEtag());

        // Insert or merge Entity - ENTITY DOES NOT EXIST NOW.
        TableResult insertResult = this.table.execute(TableOperation.insertOrMerge(baseEntity), options, null);

        assertEquals(insertResult.getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
        assertNotNull(insertResult.getEtag());

        // Insert or replace Entity - ENTITY EXISTS -> WILL MERGE
        this.table.execute(TableOperation.insertOrMerge(secondEntity), options, null);

        // Retrieve entity
        if (usePropertyResolver) {
            PropertyResolver resolver = new class1class2PropertyResolver();
            options.setPropertyResolver(resolver);
        }

        TableResult queryResult = this.table
                .execute(TableOperation.retrieve(baseEntity.getPartitionKey(), baseEntity.getRowKey(),
                        DynamicTableEntity.class), options, null);

        DynamicTableEntity retrievedEntity = queryResult.<DynamicTableEntity>getResultAsType();

        assertNotNull("Property A", retrievedEntity.getProperties().get("A"));
        assertEquals(baseEntity.getA(), retrievedEntity.getProperties().get("A").getValueAsString());

        assertNotNull("Property B", retrievedEntity.getProperties().get("B"));
        assertEquals(baseEntity.getB(), retrievedEntity.getProperties().get("B").getValueAsString());

        assertNotNull("Property C", retrievedEntity.getProperties().get("C"));
        assertEquals(baseEntity.getC(), retrievedEntity.getProperties().get("C").getValueAsString());

        assertNotNull("Property D", retrievedEntity.getProperties().get("D"));
        assertTrue(Arrays.equals(baseEntity.getD(), retrievedEntity.getProperties().get("D").getValueAsByteArray()));

        // Validate New properties exist
        assertNotNull("Property L", retrievedEntity.getProperties().get("L"));
        assertEquals(secondEntity.getL(), retrievedEntity.getProperties().get("L").getValueAsString());

        assertNotNull("Property M", retrievedEntity.getProperties().get("M"));
        assertEquals(secondEntity.getM(), retrievedEntity.getProperties().get("M").getValueAsString());

        assertNotNull("Property N", retrievedEntity.getProperties().get("N"));
        assertEquals(secondEntity.getN(), retrievedEntity.getProperties().get("N").getValueAsString());

        assertNotNull("Property O", retrievedEntity.getProperties().get("O"));
        assertEquals(secondEntity.getO(), retrievedEntity.getProperties().get("O").getValueAsString());
    }

    @Test
    public void testInsertOrReplace() throws StorageException {
        TableRequestOptions options = new TableRequestOptions();

        options.setTablePayloadFormat(TablePayloadFormat.JsonFullMetadata);
        testInsertOrReplace(options);

        options.setTablePayloadFormat(TablePayloadFormat.Json);
        testInsertOrReplace(options);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        testInsertOrReplace(options);
    }

    private void testInsertOrReplace(TableRequestOptions options) throws StorageException {
        Class1 baseEntity = new Class1();
        baseEntity.setA("foo_A");
        baseEntity.setB("foo_B");
        baseEntity.setC("foo_C");
        baseEntity.setD(new byte[]{0, 1, 2});
        baseEntity.setPartitionKey("jxscl_odata");
        baseEntity.setRowKey(UUID.randomUUID().toString());

        Class2 secondEntity = new Class2();
        secondEntity.setL("foo_L");
        secondEntity.setM("foo_M");
        secondEntity.setN("foo_N");
        secondEntity.setO("foo_O");
        secondEntity.setPartitionKey(baseEntity.getPartitionKey());
        secondEntity.setRowKey(baseEntity.getRowKey());
        secondEntity.setEtag(baseEntity.getEtag());

        // Insert or replace Entity - ENTITY DOES NOT EXIST NOW.
        TableResult insertResult = this.table.execute(TableOperation.insertOrReplace(baseEntity), options, null);

        assertEquals(insertResult.getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
        assertNotNull(insertResult.getEtag());

        // Insert or replace Entity - ENTITY EXISTS -> WILL REPLACE
        this.table.execute(TableOperation.insertOrReplace(secondEntity), options, null);

        // Retrieve entity
        TableResult queryResult = this.table
                .execute(TableOperation.retrieve(baseEntity.getPartitionKey(), baseEntity.getRowKey(),
                        DynamicTableEntity.class), options, null);

        DynamicTableEntity retrievedEntity = queryResult.getResultAsType();

        // Validate old properties dont exist
        assertTrue(retrievedEntity.getProperties().get("A") == null);
        assertTrue(retrievedEntity.getProperties().get("B") == null);
        assertTrue(retrievedEntity.getProperties().get("C") == null);
        assertTrue(retrievedEntity.getProperties().get("D") == null);

        // Validate New properties exist
        assertNotNull("Property L", retrievedEntity.getProperties().get("L"));
        assertEquals(secondEntity.getL(), retrievedEntity.getProperties().get("L").getValueAsString());

        assertNotNull("Property M", retrievedEntity.getProperties().get("M"));
        assertEquals(secondEntity.getM(), retrievedEntity.getProperties().get("M").getValueAsString());

        assertNotNull("Property N", retrievedEntity.getProperties().get("N"));
        assertEquals(secondEntity.getN(), retrievedEntity.getProperties().get("N").getValueAsString());

        assertNotNull("Property O", retrievedEntity.getProperties().get("O"));
        assertEquals(secondEntity.getO(), retrievedEntity.getProperties().get("O").getValueAsString());
    }

    @Test
    public void testMerge() throws StorageException {
        TableRequestOptions options = new TableRequestOptions();

        options.setTablePayloadFormat(TablePayloadFormat.JsonFullMetadata);
        testMerge(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.Json);
        testMerge(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        testMerge(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        testMerge(options, true);
    }

    private void testMerge(TableRequestOptions options, boolean usePropertyResolver) throws StorageException {
        // Insert base entity
        Class1 baseEntity = new Class1();
        baseEntity.setA("foo_A");
        baseEntity.setB("foo_B");
        baseEntity.setC("foo_C");
        baseEntity.setD(new byte[]{0, 1, 2});
        baseEntity.setPartitionKey("jxscl_odata");
        baseEntity.setRowKey(UUID.randomUUID().toString());

        this.table.execute(TableOperation.insert(baseEntity), options, null);

        Class2 secondEntity = new Class2();
        secondEntity.setL("foo_L");
        secondEntity.setM("foo_M");
        secondEntity.setN("foo_N");
        secondEntity.setO("foo_O");
        secondEntity.setPartitionKey(baseEntity.getPartitionKey());
        secondEntity.setRowKey(baseEntity.getRowKey());
        secondEntity.setEtag(baseEntity.getEtag());

        TableResult mergeResult = this.table.execute(TableOperation.merge(secondEntity), options, null);

        assertEquals(mergeResult.getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
        assertNotNull(mergeResult.getEtag());

        // retrieve result
        if (usePropertyResolver) {
            PropertyResolver resolver = new class1class2PropertyResolver();
            options.setPropertyResolver(resolver);
        }

        TableResult res2 = this.table.execute(TableOperation.retrieve(secondEntity.getPartitionKey(),
                secondEntity.getRowKey(), DynamicTableEntity.class), options, null);
        DynamicTableEntity mergedEntity = (DynamicTableEntity) res2.getResult();

        assertNotNull("Property A", mergedEntity.getProperties().get("A"));
        assertEquals(baseEntity.getA(), mergedEntity.getProperties().get("A").getValueAsString());

        assertNotNull("Property B", mergedEntity.getProperties().get("B"));
        assertEquals(baseEntity.getB(), mergedEntity.getProperties().get("B").getValueAsString());

        assertNotNull("Property C", mergedEntity.getProperties().get("C"));
        assertEquals(baseEntity.getC(), mergedEntity.getProperties().get("C").getValueAsString());

        assertNotNull("Property D", mergedEntity.getProperties().get("D"));
        assertTrue(Arrays.equals(baseEntity.getD(), mergedEntity.getProperties().get("D").getValueAsByteArray()));

        assertNotNull("Property L", mergedEntity.getProperties().get("L"));
        assertEquals(secondEntity.getL(), mergedEntity.getProperties().get("L").getValueAsString());

        assertNotNull("Property M", mergedEntity.getProperties().get("M"));
        assertEquals(secondEntity.getM(), mergedEntity.getProperties().get("M").getValueAsString());

        assertNotNull("Property N", mergedEntity.getProperties().get("N"));
        assertEquals(secondEntity.getN(), mergedEntity.getProperties().get("N").getValueAsString());

        assertNotNull("Property O", mergedEntity.getProperties().get("O"));
        assertEquals(secondEntity.getO(), mergedEntity.getProperties().get("O").getValueAsString());
    }

    @Test
    public void testReplace() throws StorageException {
        TableRequestOptions options = new TableRequestOptions();

        options.setTablePayloadFormat(TablePayloadFormat.JsonFullMetadata);
        testReplace(options);

        options.setTablePayloadFormat(TablePayloadFormat.Json);
        testReplace(options);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        testReplace(options);
    }

    private void testReplace(TableRequestOptions options) throws StorageException {
        Class1 baseEntity = new Class1();
        baseEntity.setA("foo_A");
        baseEntity.setB("foo_B");
        baseEntity.setC("foo_C");
        baseEntity.setD(new byte[]{0, 1, 2});
        baseEntity.setPartitionKey("jxscl_odata");
        baseEntity.setRowKey(UUID.randomUUID().toString());

        // Insert entity
        this.table.execute(TableOperation.insert(baseEntity), options, null);

        TableResult queryResult = this.table
                .execute(TableOperation.retrieve(baseEntity.getPartitionKey(), baseEntity.getRowKey(),
                        DynamicTableEntity.class), options, null);
        // Retrieve entity
        DynamicTableEntity retrievedEntity = queryResult.<DynamicTableEntity>getResultAsType();
        assertNotNull("Property D", retrievedEntity.getProperties().get("D"));
        assertTrue(Arrays.equals(baseEntity.getD(), retrievedEntity.getProperties().get("D").getValueAsByteArray()));

        // Remove property and update
        retrievedEntity.getProperties().remove("D");

        TableResult replaceResult = this.table.execute(TableOperation.replace(retrievedEntity), options, null);

        assertEquals(replaceResult.getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
        assertNotNull(replaceResult.getEtag());

        // Retrieve Entity
        queryResult = this.table
                .execute(TableOperation.retrieve(baseEntity.getPartitionKey(), baseEntity.getRowKey(),
                        DynamicTableEntity.class), options, null);

        retrievedEntity = queryResult.<DynamicTableEntity>getResultAsType();

        // Validate
        assertNotNull("Property A", retrievedEntity.getProperties().get("A"));
        assertEquals(baseEntity.getA(), retrievedEntity.getProperties().get("A").getValueAsString());

        assertNotNull("Property B", retrievedEntity.getProperties().get("B"));
        assertEquals(baseEntity.getB(), retrievedEntity.getProperties().get("B").getValueAsString());

        assertNotNull("Property C", retrievedEntity.getProperties().get("C"));
        assertEquals(baseEntity.getC(), retrievedEntity.getProperties().get("C").getValueAsString());

        assertTrue(retrievedEntity.getProperties().get("D") == null);
    }

    @Test
    public void testInsert() throws StorageException {
        TableRequestOptions options = new TableRequestOptions();

        options.setTablePayloadFormat(TablePayloadFormat.JsonFullMetadata);
        testInsert(options);

        options.setTablePayloadFormat(TablePayloadFormat.Json);
        testInsert(options);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        testInsert(options);
    }

    private void testInsert(TableRequestOptions options) throws StorageException {
        Class1 ref = new Class1();
        ref.setA("foo_A");
        ref.setB("foo_B");
        ref.setC("foo_C");
        // 1mb right here
        ref.setD(new byte[1024]);
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey("echo_default" + UUID.randomUUID().toString());

        TableResult res = this.table.execute(TableOperation.insert(ref), options, null);
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, res.getHttpStatusCode());

        ref.setRowKey("echo" + UUID.randomUUID().toString());
        res = this.table.execute(TableOperation.insert(ref, true), options, null);
        assertEquals(HttpURLConnection.HTTP_CREATED, res.getHttpStatusCode());

        ref.setRowKey("echo_off" + UUID.randomUUID().toString());
        res = this.table.execute(TableOperation.insert(ref, false), options, null);
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, res.getHttpStatusCode());
    }

    @Test
    public void testRetrieveWithoutEntityResolver() throws StorageException {
        TableRequestOptions options = new TableRequestOptions();

        options.setTablePayloadFormat(TablePayloadFormat.JsonFullMetadata);
        testRetrieveWithoutEntityResolver(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.Json);
        testRetrieveWithoutEntityResolver(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        testRetrieveWithoutEntityResolver(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        testRetrieveWithoutEntityResolver(options, true);
    }

    private void testRetrieveWithoutEntityResolver(TableRequestOptions options, boolean usePropertyResolver)
            throws StorageException {
        Class1 ref = new Class1();
        ref.setA("foo_A");
        ref.setB("foo_B");
        ref.setC("foo_C");
        ref.setD(new byte[]{0, 1, 2});
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());

        if (usePropertyResolver) {
            options.setPropertyResolver(ref);
        }

        // with cache on
        this.table.execute(TableOperation.insert(ref), options, null);

        TableResult res = this.table.execute(
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), Class1.class), options, null);

        Class1 retrievedEnt = res.getResultAsType();

        assertEquals(ref.getA(), retrievedEnt.getA());
        assertTrue(Arrays.equals(ref.getD(), retrievedEnt.getD()));

        // with cache off
        TableServiceEntity.setReflectedEntityCacheDisabled(true);
        try {
            res = this.table.execute(TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), Class1.class),
                    options, null);

            retrievedEnt = res.getResultAsType();

            assertEquals(ref.getA(), retrievedEnt.getA());
            assertTrue(Arrays.equals(ref.getD(), retrievedEnt.getD()));
        }
        finally {
            TableServiceEntity.setReflectedEntityCacheDisabled(false);
        }
    }

    @Test
    public void testRetrieveWithEntityResolver() throws StorageException {
        TableRequestOptions options = new TableRequestOptions();

        options.setTablePayloadFormat(TablePayloadFormat.JsonFullMetadata);
        testRetrieveWithEntityResolver(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.Json);
        testRetrieveWithEntityResolver(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        testRetrieveWithEntityResolver(options, false);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        testRetrieveWithEntityResolver(options, true);
    }

    private void testRetrieveWithEntityResolver(TableRequestOptions options, boolean usePropertyResolver)
            throws StorageException {
        Class1 ref = new Class1();
        ref.setA("foo_A");
        ref.setB("foo_B");
        ref.setC("foo_C");
        ref.setD(new byte[]{0, 1, 2});
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());

        if (usePropertyResolver) {
            options.setPropertyResolver(ref);
        }

        // with cache on
        TableOperation op = TableOperation.insert(ref);

        this.table.execute(op, options, null);

        TableResult res4 = this.table.execute(
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), new EntityResolver<Class1>() {
                    @Override
                    public Class1 resolve(String partitionKey, String rowKey, Date timeStamp,
                                          HashMap<String, EntityProperty> properties, String etag) {
                        Class1 result = new Class1();
                        result.setA(properties.get("A").getValueAsString());
                        result.setD(properties.get("D").getValueAsByteArray());
                        return result;
                    }
                }), options, null);

        Class1 retrievedEnt = (Class1) res4.getResult();
        assertEquals(ref.getA(), retrievedEnt.getA());
        assertTrue(Arrays.equals(ref.getD(), retrievedEnt.getD()));

        // with cache off
        TableServiceEntity.setReflectedEntityCacheDisabled(true);
        try {
            res4 = this.table.execute(
                    TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), new EntityResolver<Class1>() {
                        @Override
                        public Class1 resolve(String partitionKey, String rowKey, Date timeStamp,
                                              HashMap<String, EntityProperty> properties, String etag) {
                            Class1 result = new Class1();
                            result.setA(properties.get("A").getValueAsString());
                            result.setD(properties.get("D").getValueAsByteArray());
                            return result;
                        }
                    }), options, null);

            retrievedEnt = (Class1) res4.getResult();
            assertEquals(ref.getA(), retrievedEnt.getA());
            assertTrue(Arrays.equals(ref.getD(), retrievedEnt.getD()));
        }
        finally {
            TableServiceEntity.setReflectedEntityCacheDisabled(false);
        }
    }
}
