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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.microsoft.windowsazure.storage.StorageException;

/**
 * Table Serializer Tests
 */
@RunWith(Parameterized.class)
public class TableSerializerTests extends TableTestBase {

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
    public TableSerializerTests(TablePayloadFormat format, boolean usePropertyResolver) {
        this.options = new TableRequestOptions();
        this.options.setTablePayloadFormat(format);
        this.usePropertyResolver = usePropertyResolver;
    }

    @Test
    public void testComplexEntityInsert() throws IOException, URISyntaxException, StorageException {
        ComplexEntity ref = new ComplexEntity();
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());
        ref.populateEntity();

        if (this.usePropertyResolver) {
            options.setPropertyResolver(ref);
        }

        tClient.execute(testSuiteTableName, TableOperation.insert(ref), options, null);

        TableResult res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), ComplexEntity.class), options, null);

        ComplexEntity retrievedComplexRef = res.getResultAsType();
        ref.assertEquality(retrievedComplexRef);
    }

    @Test
    public void testDoubles() throws StorageException {
        StrangeDoubles ref = new StrangeDoubles();
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());
        ref.populateEntity();

        if (this.usePropertyResolver) {
            options.setPropertyResolver(ref);
        }

        // try with pojo
        tClient.execute(testSuiteTableName, TableOperation.insert(ref), options, null);

        TableResult res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), StrangeDoubles.class), options, null);

        StrangeDoubles retrievedComplexRef = res.getResultAsType();
        ref.assertEquality(retrievedComplexRef);
    }

    @Test
    public void testIgnoreAnnotation() throws IOException, URISyntaxException, StorageException {
        // Ignore On Getter
        IgnoreOnGetter ignoreGetter = new IgnoreOnGetter();
        ignoreGetter.setPartitionKey("jxscl_odata");
        ignoreGetter.setRowKey(UUID.randomUUID().toString());
        ignoreGetter.setIgnoreString("ignore data");

        if (this.usePropertyResolver) {
            options.setPropertyResolver(ignoreGetter);
        }

        tClient.execute(testSuiteTableName, TableOperation.insert(ignoreGetter));

        TableResult res = tClient
                .execute(testSuiteTableName, TableOperation.retrieve(ignoreGetter.getPartitionKey(),
                        ignoreGetter.getRowKey(), IgnoreOnGetter.class), options, null);

        IgnoreOnGetter retrievedIgnoreG = res.getResultAsType();
        assertEquals(retrievedIgnoreG.getIgnoreString(), null);

        // Ignore On Setter
        IgnoreOnSetter ignoreSetter = new IgnoreOnSetter();
        ignoreSetter.setPartitionKey("jxscl_odata");
        ignoreSetter.setRowKey(UUID.randomUUID().toString());
        ignoreSetter.setIgnoreString("ignore data");

        tClient.execute(testSuiteTableName, TableOperation.insert(ignoreSetter), options, null);

        res = tClient
                .execute(testSuiteTableName, TableOperation.retrieve(ignoreSetter.getPartitionKey(),
                        ignoreSetter.getRowKey(), IgnoreOnSetter.class), options, null);

        IgnoreOnSetter retrievedIgnoreS = res.getResultAsType();
        assertEquals(retrievedIgnoreS.getIgnoreString(), null);

        // Ignore On Getter AndSetter
        IgnoreOnGetterAndSetter ignoreGetterSetter = new IgnoreOnGetterAndSetter();
        ignoreGetterSetter.setPartitionKey("jxscl_odata");
        ignoreGetterSetter.setRowKey(UUID.randomUUID().toString());
        ignoreGetterSetter.setIgnoreString("ignore data");

        tClient.execute(testSuiteTableName, TableOperation.insert(ignoreGetterSetter), options, null);

        res = tClient.execute(testSuiteTableName, TableOperation.retrieve(ignoreGetterSetter.getPartitionKey(),
                ignoreGetterSetter.getRowKey(), IgnoreOnGetterAndSetter.class), options, null);

        IgnoreOnGetterAndSetter retrievedIgnoreGS = res.getResultAsType();
        assertEquals(retrievedIgnoreGS.getIgnoreString(), null);
    }

    @Test
    public void testNulls() throws IOException, URISyntaxException, StorageException {
        ComplexEntity ref = new ComplexEntity();
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());
        ref.populateEntity();

        if (this.usePropertyResolver) {
            options.setPropertyResolver(ref);
        }

        // Binary object
        ref.setBinary(null);

        tClient.execute(testSuiteTableName, TableOperation.insert(ref));
        TableResult res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), ComplexEntity.class), options, null);
        ref = res.getResultAsType();

        assertNull("Binary should be null", ref.getBinary());

        // Bool
        ref.setBool(null);
        tClient.execute(testSuiteTableName, TableOperation.replace(ref), options, null);

        res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), ComplexEntity.class), options, null);

        ref = res.getResultAsType();

        assertNull("Bool should be null", ref.getBool());

        // Date
        ref.setDateTime(null);
        tClient.execute(testSuiteTableName, TableOperation.replace(ref), options, null);

        res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), ComplexEntity.class), options, null);

        ref = res.getResultAsType();

        assertNull("Date should be null", ref.getDateTime());

        // Double
        ref.setDouble(null);
        tClient.execute(testSuiteTableName, TableOperation.replace(ref), options, null);

        res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), ComplexEntity.class), options, null);

        ref = res.getResultAsType();

        assertNull("Double should be null", ref.getDouble());

        // UUID
        ref.setGuid(null);
        tClient.execute(testSuiteTableName, TableOperation.replace(ref), options, null);

        res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), ComplexEntity.class), options, null);

        ref = res.getResultAsType();

        assertNull("UUID should be null", ref.getGuid());

        // Int32
        ref.setInt32(null);
        tClient.execute(testSuiteTableName, TableOperation.replace(ref), options, null);

        res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), ComplexEntity.class), options, null);

        ref = res.getResultAsType();

        assertNull("Int32 should be null", ref.getInt32());

        // Int64
        ref.setInt64(null);
        tClient.execute(testSuiteTableName, TableOperation.replace(ref), options, null);

        res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), ComplexEntity.class), options, null);

        ref = res.getResultAsType();

        assertNull("Int64 should be null", ref.getInt64());

        // String
        ref.setString(null);
        tClient.execute(testSuiteTableName, TableOperation.replace(ref), options, null);

        res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), ComplexEntity.class), options, null);

        ref = res.getResultAsType();

        assertNull("String should be null", ref.getString());
    }

    @Test
    public void testStoreAsAnnotation() throws IOException, URISyntaxException, StorageException {
        StoreAsEntity ref = new StoreAsEntity();
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());
        ref.setStoreAsString("StoreAsOverride Data");
        ref.populateEntity();

        if (this.usePropertyResolver) {
            options.setPropertyResolver(ref);
        }

        tClient.execute(testSuiteTableName, TableOperation.insert(ref), options, null);

        TableResult res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), StoreAsEntity.class), options, null);

        StoreAsEntity retrievedStoreAsRef = res.getResultAsType();
        assertEquals(retrievedStoreAsRef.getStoreAsString(), ref.getStoreAsString());

        // Same query with a class without the storeAs annotation
        res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), ComplexEntity.class), options, null);

        ComplexEntity retrievedComplexRef = res.getResultAsType();
        assertEquals(retrievedComplexRef.getString(), ref.getStoreAsString());

        tClient.execute(testSuiteTableName, TableOperation.delete(retrievedComplexRef), options, null);
    }

    @Test
    public void testInvalidStoreAsAnnotation() throws IOException, URISyntaxException, StorageException {
        InvalidStoreAsEntity ref = new InvalidStoreAsEntity();
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());
        ref.setStoreAsString("StoreAsOverride Data");
        ref.populateEntity();

        if (this.usePropertyResolver) {
            options.setPropertyResolver(ref);
        }

        tClient.execute(testSuiteTableName, TableOperation.insert(ref), options, null);

        TableResult res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), InvalidStoreAsEntity.class), options,
                null);

        InvalidStoreAsEntity retrievedStoreAsRef = res.getResultAsType();
        assertEquals(retrievedStoreAsRef.getStoreAsString(), null);
    }

    @Test
    public void whitespaceTest() throws StorageException {
        Class1 ref = new Class1();

        ref.setA("B    ");
        ref.setB("    A   ");
        ref.setC(" ");
        ref.setD(new byte[] { 0, 1, 2 });
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());

        if (this.usePropertyResolver) {
            options.setPropertyResolver(ref);
        }

        tClient.execute(testSuiteTableName, TableOperation.insert(ref), options, null);

        TableResult res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), Class1.class), options, null);

        assertEquals(((Class1) res.getResult()).getA(), ref.getA());
    }

    @Test
    public void whitespaceOnEmptyKeysTest() throws StorageException {
        Class1 ref = new Class1();

        ref.setA("B    ");
        ref.setB("    A   ");
        ref.setC(" ");
        ref.setD(new byte[] { 0, 1, 2 });
        ref.setPartitionKey("");
        ref.setRowKey("");

        if (this.usePropertyResolver) {
            options.setPropertyResolver(ref);
        }

        tClient.execute(testSuiteTableName, TableOperation.insert(ref), options, null);

        TableResult res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), Class1.class), options, null);

        assertEquals(((Class1) res.getResult()).getA(), ref.getA());

        tClient.execute(testSuiteTableName, TableOperation.delete(ref), options, null);
    }

    @Test
    public void newLineTest() throws StorageException {
        Class1 ref = new Class1();

        ref.setA("B    ");
        ref.setB("    A   ");
        ref.setC("\r\n");
        ref.setD(new byte[] { 0, 1, 2 });
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());

        if (this.usePropertyResolver) {
            options.setPropertyResolver(ref);
        }

        tClient.execute(testSuiteTableName, TableOperation.insert(ref), options, null);

        TableResult res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), Class1.class), options, null);

        assertEquals(((Class1) res.getResult()).getA(), ref.getA());
    }
}
