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

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.TestRunners.CloudTests;
import com.microsoft.azure.storage.TestRunners.DevFabricTests;
import com.microsoft.azure.storage.TestRunners.DevStoreTests;
import com.microsoft.azure.storage.table.TableTestHelper.Class1;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.URISyntaxException;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Table Escaping Tests
 */
@Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
public class TableEscapingTests {

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
    public void testEmptyString() throws StorageException {
        doEscapeTest("", false, true);
    }

    @Test
    public void testEmptyStringBatch() throws StorageException {
        doEscapeTest("", true, true);
    }

    @Test
    public void testRandomChars() throws StorageException {
        doEscapeTest("!$'\"()*+,;=", false);
    }

    @Test
    public void testRandomCharsBatch() throws StorageException {
        doEscapeTest("!$'\"()*+,;=", true);
    }

    @Test
    public void testPercent25() throws StorageException {
        doEscapeTest("foo%25", false, true);
    }

    @Test
    public void testPercent25Batch() throws StorageException {
        doEscapeTest("foo%25", true, true);
    }

    @Test
    public void testRegularPKInQuery() throws StorageException {
        doQueryEscapeTest("data");
    }

    @Test
    public void testSpecialChars() throws StorageException {
        doEscapeTest("\\ // @ ? <?", true);
    }

    @Test
    public void testSpecialCharsBatch() throws StorageException {
        doEscapeTest("\\ // @ ? <?", true);
    }

    @Test
    public void testUnicode() throws StorageException {
        doEscapeTest("\u00A9\u770b\u5168\u90e8", false, true);
        doEscapeTest("char中文test", false, true);
        doEscapeTest("charä¸­æ–‡test", false, true);
        doEscapeTest("世界你好", false, true);
    }

    @Test
    public void testUnicodeBatch() throws StorageException {
        doEscapeTest("\u00A9\u770b\u5168\u90e8", true, true);
        doEscapeTest("char中文test", true, true);
        doEscapeTest("charä¸­æ–‡test", true, true);
        doEscapeTest("世界你好", true, true);
    }

    @Test
    public void testUnicodeInQuery() throws StorageException {
        doQueryEscapeTest("char中文test");
        doQueryEscapeTest("charä¸­æ–‡test");
        doQueryEscapeTest("世界你好");
        doQueryEscapeTest("\u00A9\u770b\u5168\u90e8");
    }

    @Test
    public void testWhiteSpaceOnly() throws StorageException {
        doEscapeTest("     ", false, true);
    }

    @Test
    public void testWhiteSpaceOnlyBatch() throws StorageException {
        doEscapeTest("     ", true, true);
    }

    @Test
    public void testWhiteSpaceOnlyInQuery() throws StorageException {
        doQueryEscapeTest("     ");
    }

    @Test
    public void testXmlTest() throws StorageException {
        doEscapeTest("</>", false);
        doEscapeTest("<tag>", false);
        doEscapeTest("</entry>", false);
        doEscapeTest("!<", false);
        doEscapeTest("<!%^&j", false);
    }

    @Test
    public void testXmlTestBatch() throws StorageException {
        doEscapeTest("</>", false);
        doEscapeTest("<tag>", false);
        doEscapeTest("</entry>", false);
        doEscapeTest("!<", false);
        doEscapeTest("<!%^&j", false);
    }

    private void doEscapeTest(String data, boolean useBatch) throws StorageException {
        doEscapeTest(data, useBatch, false);
    }

    private void doEscapeTest(String data, boolean useBatch, boolean includeInKey) throws StorageException {
        TableRequestOptions options = new TableRequestOptions();

        options.setTablePayloadFormat(TablePayloadFormat.JsonFullMetadata);
        doEscapeTestHelper(data, useBatch, includeInKey, options);

        options.setTablePayloadFormat(TablePayloadFormat.Json);
        doEscapeTestHelper(data, useBatch, includeInKey, options);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        doEscapeTestHelper(data, useBatch, includeInKey, options);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        options.setPropertyResolver(new Class1());
        doEscapeTestHelper(data, useBatch, includeInKey, options);
    }

    private void doEscapeTestHelper(String data, boolean useBatch, boolean includeInKey, TableRequestOptions options)
            throws StorageException {
        Class1 ref = new Class1();
        ref.setA(data);
        ref.setPartitionKey(includeInKey ? "temp" + data : "temp");
        ref.setRowKey(UUID.randomUUID().toString());

        if (useBatch) {
            TableBatchOperation batch = new TableBatchOperation();
            batch.insert(ref);
            this.table.execute(batch, options, null);
        }
        else {
            this.table.execute(TableOperation.insert(ref), options, null);
        }

        TableResult res = null;

        if (useBatch) {
            TableBatchOperation batch = new TableBatchOperation();
            batch.retrieve(ref.getPartitionKey(), ref.getRowKey(), Class1.class);
            res = this.table.execute(batch, options, null).get(0);
        }
        else {
            res = this.table.execute(TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), Class1.class),
                    options, null);
        }

        Class1 retObj = res.getResultAsType();
        assertEquals(ref.getA(), retObj.getA());
        assertEquals(ref.getPartitionKey(), retObj.getPartitionKey());

        ref.setEtag(retObj.getEtag());
        ref.setB(data);

        // Merge
        if (useBatch) {
            TableBatchOperation batch = new TableBatchOperation();
            batch.merge(ref);
            this.table.execute(batch, options, null);
        }
        else {
            this.table.execute(TableOperation.merge(ref), options, null);
        }

        if (useBatch) {
            TableBatchOperation batch = new TableBatchOperation();
            batch.retrieve(ref.getPartitionKey(), ref.getRowKey(), Class1.class);
            res = this.table.execute(batch, options, null).get(0);
        }
        else {
            res = this.table.execute(TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), Class1.class),
                    options, null);
        }

        retObj = res.getResultAsType();
        assertEquals(ref.getA(), retObj.getA());
        assertEquals(ref.getB(), retObj.getB());

        // Replace
        ref.setEtag(retObj.getEtag());
        ref.setC(data);

        if (useBatch) {
            TableBatchOperation batch = new TableBatchOperation();
            batch.replace(ref);
            this.table.execute(batch, options, null);
        }
        else {
            this.table.execute(TableOperation.replace(ref), options, null);
        }

        if (useBatch) {
            TableBatchOperation batch = new TableBatchOperation();
            batch.retrieve(ref.getPartitionKey(), ref.getRowKey(), Class1.class);
            res = this.table.execute(batch, options, null).get(0);
        }
        else {
            res = this.table.execute(TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), Class1.class),
                    options, null);
        }

        retObj = res.getResultAsType();
        assertEquals(ref.getA(), retObj.getA());
        assertEquals(ref.getB(), retObj.getB());
        assertEquals(ref.getC(), retObj.getC());

        if (useBatch) {
            TableBatchOperation batch = new TableBatchOperation();
            batch.delete(retObj);
            res = this.table.execute(batch, options, null).get(0);
        }
        else {
            res = this.table.execute(TableOperation.delete(retObj), options, null);
        }
    }

    private void doQueryEscapeTest(String data) throws StorageException {
        TableRequestOptions options = new TableRequestOptions();

        options.setTablePayloadFormat(TablePayloadFormat.JsonFullMetadata);
        doQueryEscapeTestHelper(data, options);

        options.setTablePayloadFormat(TablePayloadFormat.Json);
        doQueryEscapeTestHelper(data, options);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        doQueryEscapeTestHelper(data, options);

        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
        options.setPropertyResolver(new Class1());
        doQueryEscapeTestHelper(data, options);
    }

    private void doQueryEscapeTestHelper(String data, TableRequestOptions options) throws StorageException {
        Class1 ref = new Class1();
        ref.setA(data);
        ref.setPartitionKey(UUID.randomUUID().toString());
        ref.setRowKey("foo");

        this.table.execute(TableOperation.insert(ref), options, null);
        TableQuery<Class1> query = TableQuery.from(Class1.class).where(
                String.format("(PartitionKey eq '%s') and (A eq '%s')", ref.getPartitionKey(), data));

        int count = 0;

        for (Class1 ent : this.table.execute(query, options, null)) {
            count++;
            assertEquals(ent.getA(), ref.getA());
            assertEquals(ent.getB(), ref.getB());
            assertEquals(ent.getC(), ref.getC());
            assertEquals(ent.getPartitionKey(), ref.getPartitionKey());
            assertEquals(ent.getRowKey(), ref.getRowKey());
        }

        assertEquals(count, 1);
    }
}
