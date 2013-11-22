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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.microsoft.windowsazure.storage.StorageException;
import com.microsoft.windowsazure.storage.core.SR;
import com.microsoft.windowsazure.storage.table.TableRequestOptions.PropertyResolver;

/**
 * Table Operation Tests
 */
@RunWith(Parameterized.class)
public class TableOperationTests extends TableTestBase {

    private final TableRequestOptions options;
    private final boolean usePropertyResolver;

    /**
     * These parameters are passed to the constructor at the start of each test run. This includes TablePayloadFormat,
     * and if that format is JsonNoMetadata, whether or not to use a PropertyResolver
     * 
     * @return the parameters pass to the constructor
     */
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { TablePayloadFormat.AtomPub, false }, // AtomPub
                { TablePayloadFormat.JsonFullMetadata, false }, // Json Full Metadata
                { TablePayloadFormat.Json, false }, // Json Minimal Metadata
                { TablePayloadFormat.JsonNoMetadata, false }, // Json No Metadata without PropertyResolver 
                { TablePayloadFormat.JsonNoMetadata, true } // Json No Metadata with PropertyResolver
                });
    }

    /**
     * Takes a parameter from @Parameters to use for this run of the tests.
     * 
     * @param format
     *            The {@link TablePaylodFormat} to use for this test run
     * @param usePropertyResolver
     *            Whether or not to use a property resolver, applicable only for <code>JsonNoMetadata</code> format
     */
    public TableOperationTests(TablePayloadFormat format, boolean usePropertyResolver) {
        this.options = TableRequestOptions.applyDefaults(null, TableTestBase.tClient);
        this.options.setTablePayloadFormat(format);
        this.usePropertyResolver = usePropertyResolver;
    }

    @Test
    public void testDelete() throws StorageException {
        Class1 ref = new Class1();
        ref.setA("foo_A");
        ref.setB("foo_B");
        ref.setC("foo_C");
        ref.setD(new byte[] { 0, 1, 2 });
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());

        if (this.usePropertyResolver) {
            options.setPropertyResolver(ref);
        }

        TableOperation op = TableOperation.insert(ref);

        tClient.execute(testSuiteTableName, op, options, null);
        tClient.execute(testSuiteTableName, TableOperation.delete(ref), options, null);

        TableResult res2 = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), Class1.class), options, null);

        assertTrue(res2.getResult() == null);
    }

    @Test
    public void testDeleteFail() throws StorageException {
        Class1 ref = new Class1();
        ref.setA("foo_A");
        ref.setB("foo_B");
        ref.setC("foo_C");
        ref.setD(new byte[] { 0, 1, 2 });
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());

        if (this.usePropertyResolver) {
            options.setPropertyResolver(ref);
        }

        tClient.execute(testSuiteTableName, TableOperation.insert(ref), options, null);
        String oldEtag = ref.getEtag();

        // update entity
        ref.setA("updated");
        tClient.execute(testSuiteTableName, TableOperation.replace(ref), options, null);

        ref.setEtag(oldEtag);

        try {
            tClient.execute(testSuiteTableName, TableOperation.delete(ref), options, null);
            fail();
        }
        catch (TableServiceException ex) {
            assertEquals(ex.getMessage(), "Precondition Failed");
            assertTrue(ex.getExtendedErrorInformation().getErrorMessage()
                    .startsWith("The update condition specified in the request was not satisfied."));
            assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "UpdateConditionNotSatisfied");
        }

        TableResult res2 = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), Class1.class), options, null);

        ref = res2.getResultAsType();
        // actually delete it
        tClient.execute(testSuiteTableName, TableOperation.delete(ref), options, null);

        // now try to delete it and fail
        try {
            tClient.execute(testSuiteTableName, TableOperation.delete(ref), options, null);
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
        Class1 ref = new Class1();
        ref.setA("foo_A");
        ref.setB("foo_B");
        ref.setC("foo_C");
        ref.setD(new byte[] { 0, 1, 2 });
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());

        if (this.usePropertyResolver) {
            options.setPropertyResolver(ref);
        }

        TableResult res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), Class1.class), options, null);

        assertNull(res.getResult());
        assertEquals(res.getHttpStatusCode(), HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    public void testInsertOrMerge() throws StorageException {
        Class1 baseEntity = new Class1();
        baseEntity.setA("foo_A");
        baseEntity.setB("foo_B");
        baseEntity.setC("foo_C");
        baseEntity.setD(new byte[] { 0, 1, 2 });
        baseEntity.setPartitionKey("jxscl_odata");
        baseEntity.setRowKey(UUID.randomUUID().toString());

        TableRequestOptions options1 = new TableRequestOptions(options);;
        if (this.usePropertyResolver) {
            options1.setPropertyResolver(baseEntity);
        }

        Class2 secondEntity = new Class2();
        secondEntity.setL("foo_L");
        secondEntity.setM("foo_M");
        secondEntity.setN("foo_N");
        secondEntity.setO("foo_O");
        secondEntity.setPartitionKey(baseEntity.getPartitionKey());
        secondEntity.setRowKey(baseEntity.getRowKey());
        secondEntity.setEtag(baseEntity.getEtag());

        TableRequestOptions options2 = new TableRequestOptions(options);
        if (this.usePropertyResolver) {
            PropertyResolver resolver = new class1class2PropertyResolver();
            options2.setPropertyResolver(resolver);
        }

        // Insert or merge Entity - ENTITY DOES NOT EXIST NOW.
        TableResult insertResult = tClient.execute(testSuiteTableName, TableOperation.insertOrMerge(baseEntity),
                options1, null);

        assertEquals(insertResult.getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
        assertNotNull(insertResult.getEtag());

        // Insert or replace Entity - ENTITY EXISTS -> WILL MERGE
        tClient.execute(testSuiteTableName, TableOperation.insertOrMerge(secondEntity), options2, null);

        // Retrieve entity
        TableResult queryResult = tClient
                .execute(testSuiteTableName, TableOperation.retrieve(baseEntity.getPartitionKey(),
                        baseEntity.getRowKey(), DynamicTableEntity.class), options2, null);

        DynamicTableEntity retrievedEntity = queryResult.<DynamicTableEntity> getResultAsType();

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
        Class1 baseEntity = new Class1();
        baseEntity.setA("foo_A");
        baseEntity.setB("foo_B");
        baseEntity.setC("foo_C");
        baseEntity.setD(new byte[] { 0, 1, 2 });
        baseEntity.setPartitionKey("jxscl_odata");
        baseEntity.setRowKey(UUID.randomUUID().toString());

        TableRequestOptions options1 = new TableRequestOptions(options);;
        if (this.usePropertyResolver) {
            options1.setPropertyResolver(baseEntity);
        }

        Class2 secondEntity = new Class2();
        secondEntity.setL("foo_L");
        secondEntity.setM("foo_M");
        secondEntity.setN("foo_N");
        secondEntity.setO("foo_O");
        secondEntity.setPartitionKey(baseEntity.getPartitionKey());
        secondEntity.setRowKey(baseEntity.getRowKey());
        secondEntity.setEtag(baseEntity.getEtag());

        TableRequestOptions options2 = new TableRequestOptions(options);
        if (this.usePropertyResolver) {
            options2.setPropertyResolver(secondEntity);
        }

        // Insert or replace Entity - ENTITY DOES NOT EXIST NOW.
        TableResult insertResult = tClient.execute(testSuiteTableName, TableOperation.insertOrReplace(baseEntity),
                options1, null);

        assertEquals(insertResult.getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
        assertNotNull(insertResult.getEtag());

        // Insert or replace Entity - ENTITY EXISTS -> WILL REPLACE
        tClient.execute(testSuiteTableName, TableOperation.insertOrReplace(secondEntity), options2, null);

        // Retrieve entity
        TableResult queryResult = tClient
                .execute(testSuiteTableName, TableOperation.retrieve(baseEntity.getPartitionKey(),
                        baseEntity.getRowKey(), DynamicTableEntity.class), options2, null);

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
        // Insert base entity
        Class1 baseEntity = new Class1();
        baseEntity.setA("foo_A");
        baseEntity.setB("foo_B");
        baseEntity.setC("foo_C");
        baseEntity.setD(new byte[] { 0, 1, 2 });
        baseEntity.setPartitionKey("jxscl_odata");
        baseEntity.setRowKey(UUID.randomUUID().toString());

        TableRequestOptions options1 = new TableRequestOptions(options);;
        if (this.usePropertyResolver) {
            options1.setPropertyResolver(baseEntity);
        }

        tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity), options1, null);

        Class2 secondEntity = new Class2();
        secondEntity.setL("foo_L");
        secondEntity.setM("foo_M");
        secondEntity.setN("foo_N");
        secondEntity.setO("foo_O");
        secondEntity.setPartitionKey(baseEntity.getPartitionKey());
        secondEntity.setRowKey(baseEntity.getRowKey());
        secondEntity.setEtag(baseEntity.getEtag());

        TableRequestOptions options2 = new TableRequestOptions(options);
        if (this.usePropertyResolver) {
            PropertyResolver resolver = new class1class2PropertyResolver();
            options2.setPropertyResolver(resolver);
        }

        TableResult mergeResult = tClient.execute(testSuiteTableName, TableOperation.merge(secondEntity), options2,
                null);

        assertEquals(mergeResult.getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
        assertNotNull(mergeResult.getEtag());

        TableResult res2 = tClient.execute(testSuiteTableName, TableOperation.retrieve(secondEntity.getPartitionKey(),
                secondEntity.getRowKey(), DynamicTableEntity.class), options2, null);
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
    public void testMergeFail() throws StorageException {
        // Insert base entity
        Class1 baseEntity = new Class1();
        baseEntity.setA("foo_A");
        baseEntity.setB("foo_B");
        baseEntity.setC("foo_C");
        baseEntity.setD(new byte[] { 0, 1, 2 });
        baseEntity.setPartitionKey("jxscl_odata");
        baseEntity.setRowKey(UUID.randomUUID().toString());

        TableRequestOptions options1 = new TableRequestOptions(options);;
        if (this.usePropertyResolver) {
            options1.setPropertyResolver(baseEntity);
        }

        tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity), options1, null);

        Class2 secondEntity = new Class2();
        secondEntity.setL("foo_L");
        secondEntity.setM("foo_M");
        secondEntity.setN("foo_N");
        secondEntity.setO("foo_O");
        secondEntity.setPartitionKey(baseEntity.getPartitionKey());
        secondEntity.setRowKey(baseEntity.getRowKey());
        secondEntity.setEtag(baseEntity.getEtag());
        String oldEtag = baseEntity.getEtag();

        TableRequestOptions options2 = new TableRequestOptions(options);
        if (this.usePropertyResolver) {
            PropertyResolver resolver = new class1class2PropertyResolver();
            options2.setPropertyResolver(resolver);
        }

        tClient.execute(testSuiteTableName, TableOperation.merge(secondEntity), options2, null);

        secondEntity.setEtag(oldEtag);
        secondEntity.setL("updated");
        try {
            tClient.execute(testSuiteTableName, TableOperation.merge(secondEntity), options2, null);
            fail();
        }
        catch (TableServiceException ex) {
            assertEquals(ex.getMessage(), "Precondition Failed");
            assertTrue(ex.getExtendedErrorInformation().getErrorMessage()
                    .startsWith("The update condition specified in the request was not satisfied."));
            assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "UpdateConditionNotSatisfied");
        }

        // retrieve entity
        TableResult queryResult = tClient
                .execute(testSuiteTableName, TableOperation.retrieve(baseEntity.getPartitionKey(),
                        baseEntity.getRowKey(), DynamicTableEntity.class), options2, null);

        DynamicTableEntity retrievedEntity = queryResult.getResultAsType();
        tClient.execute(testSuiteTableName, TableOperation.delete(retrievedEntity), options2, null);

        try {
            tClient.execute(testSuiteTableName, TableOperation.merge(secondEntity), options2, null);
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
    public void testRetrieveWithoutResolver() throws StorageException {
        Class1 ref = new Class1();
        ref.setA("foo_A");
        ref.setB("foo_B");
        ref.setC("foo_C");
        ref.setD(new byte[] { 0, 1, 2 });
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());

        if (this.usePropertyResolver) {
            options.setPropertyResolver(ref);
        }

        // with cache on
        tClient.execute(testSuiteTableName, TableOperation.insert(ref), options, null);

        TableResult res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), Class1.class), options, null);

        Class1 retrievedEnt = res.getResultAsType();

        assertEquals(ref.getA(), retrievedEnt.getA());
        assertTrue(Arrays.equals(ref.getD(), retrievedEnt.getD()));

        // with cache off
        TableServiceEntity.setReflectedEntityCacheDisabled(true);
        try {
            res = tClient.execute(testSuiteTableName,
                    TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), Class1.class), options, null);

            retrievedEnt = res.getResultAsType();

            assertEquals(ref.getA(), retrievedEnt.getA());
            assertTrue(Arrays.equals(ref.getD(), retrievedEnt.getD()));
        }
        finally {

            TableServiceEntity.setReflectedEntityCacheDisabled(false);
        }
    }

    @Test
    public void testRetrieveWithResolver() throws StorageException {
        Class1 ref = new Class1();
        ref.setA("foo_A");
        ref.setB("foo_B");
        ref.setC("foo_C");
        ref.setD(new byte[] { 0, 1, 2 });
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());

        if (this.usePropertyResolver) {
            options.setPropertyResolver(ref);
        }

        // with cache on
        TableOperation op = TableOperation.insert(ref);

        tClient.execute(testSuiteTableName, op, options, null);

        TableResult res4 = tClient.execute(testSuiteTableName,
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
            res4 = tClient.execute(testSuiteTableName,
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

    @Test
    public void testRetrieveWithNullResolver() throws StorageException {
        try {
            TableOperation.retrieve("foo", "blah", (EntityResolver<?>) null);
        }
        catch (IllegalArgumentException ex) {
            assertEquals(ex.getMessage(),
                    String.format(SR.ARGUMENT_NULL_OR_EMPTY, SR.QUERY_REQUIRES_VALID_CLASSTYPE_OR_RESOLVER));
        }
    }

    @Test
    public void testInsertFail() throws StorageException {
        Class1 ref = new Class1();
        ref.setA("foo_A");
        ref.setB("foo_B");
        ref.setC("foo_C");
        ref.setD(new byte[] { 0, 1, 2 });
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());

        if (this.usePropertyResolver) {
            options.setPropertyResolver(ref);
        }

        TableOperation op = TableOperation.insert(ref);

        tClient.execute(testSuiteTableName, op, options, null);
        try {
            tClient.execute(testSuiteTableName, op, options, null);
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
    public void testReplace() throws StorageException {
        Class1 baseEntity = new Class1();
        baseEntity.setA("foo_A");
        baseEntity.setB("foo_B");
        baseEntity.setC("foo_C");
        baseEntity.setD(new byte[] { 0, 1, 2 });
        baseEntity.setPartitionKey("jxscl_odata");
        baseEntity.setRowKey(UUID.randomUUID().toString());

        if (this.usePropertyResolver) {
            options.setPropertyResolver(baseEntity);
        }

        // Insert entity
        tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity), options, null);

        TableResult queryResult = tClient
                .execute(testSuiteTableName, TableOperation.retrieve(baseEntity.getPartitionKey(),
                        baseEntity.getRowKey(), DynamicTableEntity.class), options, null);
        // Retrieve entity
        DynamicTableEntity retrievedEntity = queryResult.<DynamicTableEntity> getResultAsType();
        assertNotNull("Property D", retrievedEntity.getProperties().get("D"));
        assertTrue(Arrays.equals(baseEntity.getD(), retrievedEntity.getProperties().get("D").getValueAsByteArray()));

        // Remove property and update
        retrievedEntity.getProperties().remove("D");

        TableResult replaceResult = tClient.execute(testSuiteTableName, TableOperation.replace(retrievedEntity),
                options, null);

        assertEquals(replaceResult.getHttpStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
        assertNotNull(replaceResult.getEtag());

        // Retrieve Entity
        queryResult = tClient
                .execute(testSuiteTableName, TableOperation.retrieve(baseEntity.getPartitionKey(),
                        baseEntity.getRowKey(), DynamicTableEntity.class), options, null);

        retrievedEntity = queryResult.<DynamicTableEntity> getResultAsType();

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
    public void testReplaceFail() throws StorageException {
        Class1 baseEntity = new Class1();
        baseEntity.setA("foo_A");
        baseEntity.setB("foo_B");
        baseEntity.setC("foo_C");
        baseEntity.setD(new byte[] { 0, 1, 2 });
        baseEntity.setPartitionKey("jxscl_odata");
        baseEntity.setRowKey(UUID.randomUUID().toString());

        if (this.usePropertyResolver) {
            options.setPropertyResolver(baseEntity);
        }

        // Insert entity
        tClient.execute(testSuiteTableName, TableOperation.insert(baseEntity), options, null);

        String oldEtag = baseEntity.getEtag();

        TableResult queryResult = tClient
                .execute(testSuiteTableName, TableOperation.retrieve(baseEntity.getPartitionKey(),
                        baseEntity.getRowKey(), DynamicTableEntity.class), options, null);

        // Retrieve entity
        DynamicTableEntity retrievedEntity = queryResult.<DynamicTableEntity> getResultAsType();
        assertNotNull("Property D", retrievedEntity.getProperties().get("D"));
        assertTrue(Arrays.equals(baseEntity.getD(), retrievedEntity.getProperties().get("D").getValueAsByteArray()));

        // Remove property and update
        retrievedEntity.getProperties().remove("D");

        tClient.execute(testSuiteTableName, TableOperation.replace(retrievedEntity), options, null);

        retrievedEntity.setEtag(oldEtag);

        try {
            tClient.execute(testSuiteTableName, TableOperation.replace(retrievedEntity), options, null);
            fail();
        }
        catch (TableServiceException ex) {
            assertEquals(ex.getMessage(), "Precondition Failed");
            assertTrue(ex.getExtendedErrorInformation().getErrorMessage()
                    .startsWith("The update condition specified in the request was not satisfied."));
            assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "UpdateConditionNotSatisfied");
        }

        // delete entity
        queryResult = tClient
                .execute(testSuiteTableName, TableOperation.retrieve(baseEntity.getPartitionKey(),
                        baseEntity.getRowKey(), DynamicTableEntity.class), options, null);

        tClient.execute(testSuiteTableName, TableOperation.delete((DynamicTableEntity) queryResult.getResultAsType()),
                options, null);

        try {
            tClient.execute(testSuiteTableName, TableOperation.replace(retrievedEntity), options, null);
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
    public void testInsert() throws StorageException {
        Class1 ref = new Class1();
        ref.setA("foo_A");
        ref.setB("foo_B");
        ref.setC("foo_C");
        // 1mb right here
        ref.setD(new byte[1024]);
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey("echo_default" + UUID.randomUUID().toString());

        if (this.usePropertyResolver) {
            options.setPropertyResolver(ref);
        }

        TableResult res = tClient.execute(testSuiteTableName, TableOperation.insert(ref), options, null);
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, res.getHttpStatusCode());

        ref.setRowKey("echo" + UUID.randomUUID().toString());
        res = tClient.execute(testSuiteTableName, TableOperation.insert(ref, true), options, null);
        assertEquals(HttpURLConnection.HTTP_CREATED, res.getHttpStatusCode());

        ref.setRowKey("echo_off" + UUID.randomUUID().toString());
        res = tClient.execute(testSuiteTableName, TableOperation.insert(ref, false), options, null);
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, res.getHttpStatusCode());
    }

    @Test
    public void testInsertEmptyEntity() throws StorageException {
        EmptyClass ref = new EmptyClass();
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey("echo_default" + UUID.randomUUID().toString());

        if (this.usePropertyResolver) {
            options.setPropertyResolver(ref);
        }

        TableResult res = tClient.execute(testSuiteTableName, TableOperation.insert(ref), options, null);
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, res.getHttpStatusCode());

        EmptyClassDynamic refDynamic = new EmptyClassDynamic();
        refDynamic.setPartitionKey("jxscl_odata");
        refDynamic.setRowKey("echo_default" + UUID.randomUUID().toString());

        if (this.usePropertyResolver) {
            options.setPropertyResolver(refDynamic);
        }

        res = tClient.execute(testSuiteTableName, TableOperation.insert(refDynamic), options, null);
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, res.getHttpStatusCode());
    }

    @Test
    public void testInsertEntityWithoutPartitionKeyRowKey() throws StorageException {
        EmptyClass ref = new EmptyClass();
        ref.setPartitionKey("jxscl_odata");

        if (this.usePropertyResolver) {
            options.setPropertyResolver(ref);
        }

        try {
            tClient.execute(testSuiteTableName, TableOperation.insert(ref), options, null);
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
            tClient.execute(testSuiteTableName, TableOperation.insert(ref), options, null);
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
    public void testInsertEntityOver1MB() throws StorageException {
        Class1 ref = new Class1();
        ref.setA("foo_A");
        ref.setB("foo_B");
        ref.setC("foo_C");
        // 1mb right here
        ref.setD(new byte[1024 * 1024]);
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());

        if (this.usePropertyResolver) {
            options.setPropertyResolver(ref);
        }

        try {
            tClient.execute(testSuiteTableName, TableOperation.insert(ref), options, null);
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
    public void testInsertEntityWithPropertyMoreThan255chars() throws StorageException {
        DynamicTableEntity ref = new DynamicTableEntity();

        String propName = "";
        for (int m = 0; m < 256; m++) {
            propName = propName.concat("a");
        }

        ref.getProperties().put(propName, new EntityProperty("test"));
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());

        try {
            tClient.execute(testSuiteTableName, TableOperation.insert(ref), options, null);
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
    public void testInsertEntityWithNumericProperty() throws StorageException {
        DynamicTableEntity ref = new DynamicTableEntity();

        String propName = "";
        for (int m = 0; m < 255; m++) {
            propName = propName.concat(Integer.toString(m % 9));
        }

        ref.getProperties().put(propName, new EntityProperty("test"));
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());

        try {
            tClient.execute(testSuiteTableName, TableOperation.insert(ref), options, null);
            fail();
        }
        catch (TableServiceException ex) {
            // OData handles AtomPub and Json differently when properties start with a number. 
            // Hence, a different error code is returned. This may be fixed later.
            if (options.getTablePayloadFormat() == TablePayloadFormat.AtomPub) {
                assertEquals(ex.getMessage(), "Bad Request");
                assertTrue(ex.getExtendedErrorInformation().getErrorMessage()
                        .startsWith("One of the request inputs is not valid."));
                assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "InvalidInput");
            }
            else {
                assertEquals(ex.getMessage(), "Bad Request");
                assertTrue(ex.getExtendedErrorInformation().getErrorMessage()
                        .startsWith("The property name is invalid."));
                assertEquals(ex.getExtendedErrorInformation().getErrorCode(), "PropertyNameInvalid");
            }
        }
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
}
