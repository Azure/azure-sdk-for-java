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
package com.microsoft.windowsazure.services.table.client;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;

import com.microsoft.windowsazure.services.core.storage.StorageException;

/**
 * Table Serializer Tests
 */
public class TableSerializerTests extends TableTestBase {
    @Test
    public void testComplexEntityInsert() throws IOException, URISyntaxException, StorageException {
        ComplexEntity ref = new ComplexEntity();
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());
        ref.populateEntity();

        tClient.execute(testSuiteTableName, TableOperation.insert(ref));

        TableResult res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), ComplexEntity.class));

        ComplexEntity retrievedComplexRef = res.getResultAsType();
        ref.assertEquality(retrievedComplexRef);
    }

    @Test
    public void testIgnoreAnnotation() throws IOException, URISyntaxException, StorageException {
        // Ignore On Getter
        IgnoreOnGetter ignoreGetter = new IgnoreOnGetter();
        ignoreGetter.setPartitionKey("jxscl_odata");
        ignoreGetter.setRowKey(UUID.randomUUID().toString());
        ignoreGetter.setIgnoreString("ignore data");

        tClient.execute(testSuiteTableName, TableOperation.insert(ignoreGetter));

        TableResult res = tClient
                .execute(testSuiteTableName, TableOperation.retrieve(ignoreGetter.getPartitionKey(),
                        ignoreGetter.getRowKey(), IgnoreOnGetter.class));

        IgnoreOnGetter retrievedIgnoreG = res.getResultAsType();
        Assert.assertEquals(retrievedIgnoreG.getIgnoreString(), null);

        // Ignore On Setter
        IgnoreOnSetter ignoreSetter = new IgnoreOnSetter();
        ignoreSetter.setPartitionKey("jxscl_odata");
        ignoreSetter.setRowKey(UUID.randomUUID().toString());
        ignoreSetter.setIgnoreString("ignore data");

        tClient.execute(testSuiteTableName, TableOperation.insert(ignoreSetter));

        res = tClient
                .execute(testSuiteTableName, TableOperation.retrieve(ignoreSetter.getPartitionKey(),
                        ignoreSetter.getRowKey(), IgnoreOnSetter.class));

        IgnoreOnSetter retrievedIgnoreS = res.getResultAsType();
        Assert.assertEquals(retrievedIgnoreS.getIgnoreString(), null);

        // Ignore On Getter AndSetter
        IgnoreOnGetterAndSetter ignoreGetterSetter = new IgnoreOnGetterAndSetter();
        ignoreGetterSetter.setPartitionKey("jxscl_odata");
        ignoreGetterSetter.setRowKey(UUID.randomUUID().toString());
        ignoreGetterSetter.setIgnoreString("ignore data");

        tClient.execute(testSuiteTableName, TableOperation.insert(ignoreGetterSetter));

        res = tClient.execute(testSuiteTableName, TableOperation.retrieve(ignoreGetterSetter.getPartitionKey(),
                ignoreGetterSetter.getRowKey(), IgnoreOnGetterAndSetter.class));

        IgnoreOnGetterAndSetter retrievedIgnoreGS = res.getResultAsType();
        Assert.assertEquals(retrievedIgnoreGS.getIgnoreString(), null);
    }

    @Test
    public void testNulls() throws IOException, URISyntaxException, StorageException {
        ComplexEntity ref = new ComplexEntity();
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());
        ref.populateEntity();

        // Binary object
        ref.setBinary(null);

        tClient.execute(testSuiteTableName, TableOperation.insert(ref));
        TableResult res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), ComplexEntity.class));
        ref = res.getResultAsType();

        Assert.assertNull("Binary should be null", ref.getBinary());

        // Bool
        ref.setBool(null);
        tClient.execute(testSuiteTableName, TableOperation.replace(ref));

        res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), ComplexEntity.class));

        ref = res.getResultAsType();

        Assert.assertNull("Bool should be null", ref.getBool());

        // Date
        ref.setDateTime(null);
        tClient.execute(testSuiteTableName, TableOperation.replace(ref));

        res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), ComplexEntity.class));

        ref = res.getResultAsType();

        Assert.assertNull("Date should be null", ref.getDateTime());

        // Double
        ref.setDouble(null);
        tClient.execute(testSuiteTableName, TableOperation.replace(ref));

        res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), ComplexEntity.class));

        ref = res.getResultAsType();

        Assert.assertNull("Double should be null", ref.getDouble());

        // UUID
        ref.setGuid(null);
        tClient.execute(testSuiteTableName, TableOperation.replace(ref));

        res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), ComplexEntity.class));

        ref = res.getResultAsType();

        Assert.assertNull("UUID should be null", ref.getGuid());

        // Int32
        ref.setInt32(null);
        tClient.execute(testSuiteTableName, TableOperation.replace(ref));

        res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), ComplexEntity.class));

        ref = res.getResultAsType();

        Assert.assertNull("Int32 should be null", ref.getInt32());

        // Int64
        ref.setInt64(null);
        tClient.execute(testSuiteTableName, TableOperation.replace(ref));

        res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), ComplexEntity.class));

        ref = res.getResultAsType();

        Assert.assertNull("Int64 should be null", ref.getInt64());

        // String
        ref.setString(null);
        tClient.execute(testSuiteTableName, TableOperation.replace(ref));

        res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), ComplexEntity.class));

        ref = res.getResultAsType();

        Assert.assertNull("String should be null", ref.getString());
    }

    @Test
    public void testStoreAsAnnotation() throws IOException, URISyntaxException, StorageException {
        StoreAsEntity ref = new StoreAsEntity();
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());
        ref.setStoreAsString("StoreAsOverride Data");
        ref.populateEntity();

        tClient.execute(testSuiteTableName, TableOperation.insert(ref));

        TableResult res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), StoreAsEntity.class));

        StoreAsEntity retrievedStoreAsRef = res.getResultAsType();
        Assert.assertEquals(retrievedStoreAsRef.getStoreAsString(), ref.getStoreAsString());

        // Same query with a class without the storeAs annotation
        res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), ComplexEntity.class));

        ComplexEntity retrievedComplexRef = res.getResultAsType();
        Assert.assertEquals(retrievedComplexRef.getString(), ref.getStoreAsString());

        tClient.execute(testSuiteTableName, TableOperation.delete(retrievedComplexRef));
    }

    @Test
    public void testInvalidStoreAsAnnotation() throws IOException, URISyntaxException, StorageException {
        InvalidStoreAsEntity ref = new InvalidStoreAsEntity();
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());
        ref.setStoreAsString("StoreAsOverride Data");
        ref.populateEntity();

        tClient.execute(testSuiteTableName, TableOperation.insert(ref));

        TableResult res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), InvalidStoreAsEntity.class));

        InvalidStoreAsEntity retrievedStoreAsRef = res.getResultAsType();
        Assert.assertEquals(retrievedStoreAsRef.getStoreAsString(), null);
    }

    @Test
    public void whitespaceTest() throws StorageException {
        class1 ref = new class1();

        ref.setA("B    ");
        ref.setB("    A   ");
        ref.setC(" ");
        ref.setD(new byte[] { 0, 1, 2 });
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());

        tClient.execute(testSuiteTableName, TableOperation.insert(ref));

        TableResult res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), class1.class));

        Assert.assertEquals(((class1) res.getResult()).getA(), ref.getA());
    }

    @Test
    public void whitespaceOnEmptyKeysTest() throws StorageException {
        class1 ref = new class1();

        ref.setA("B    ");
        ref.setB("    A   ");
        ref.setC(" ");
        ref.setD(new byte[] { 0, 1, 2 });
        ref.setPartitionKey("");
        ref.setRowKey("");

        tClient.execute(testSuiteTableName, TableOperation.insert(ref));

        TableResult res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), class1.class));

        Assert.assertEquals(((class1) res.getResult()).getA(), ref.getA());
    }

    @Test
    public void newLineTest() throws StorageException {
        class1 ref = new class1();

        ref.setA("B    ");
        ref.setB("    A   ");
        ref.setC("\r\n");
        ref.setD(new byte[] { 0, 1, 2 });
        ref.setPartitionKey("jxscl_odata");
        ref.setRowKey(UUID.randomUUID().toString());

        tClient.execute(testSuiteTableName, TableOperation.insert(ref));

        TableResult res = tClient.execute(testSuiteTableName,
                TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), class1.class));

        Assert.assertEquals(((class1) res.getResult()).getA(), ref.getA());
    }
}
