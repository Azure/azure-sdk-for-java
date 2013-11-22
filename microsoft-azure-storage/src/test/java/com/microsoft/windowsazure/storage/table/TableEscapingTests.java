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

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.microsoft.windowsazure.storage.StorageException;

/**
 * Table Escaping Tests
 */
@RunWith(Parameterized.class)
public class TableEscapingTests extends TableTestBase {

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
    public TableEscapingTests(TablePayloadFormat format, boolean usePropertyResolver) {
        this.options = TableRequestOptions.applyDefaults(null, TableTestBase.tClient);
        this.options.setTablePayloadFormat(format);
        this.usePropertyResolver = usePropertyResolver;
    }

    @Test
    public void emptyString() throws StorageException {
        doEscapeTest("", false, true);
    }

    @Test
    public void emptyStringBatch() throws StorageException {
        doEscapeTest("", true, true);
    }

    @Test
    public void randomChars() throws StorageException {
        doEscapeTest("!$'\"()*+,;=", false);
    }

    @Test
    public void randomCharsBatch() throws StorageException {
        doEscapeTest("!$'\"()*+,;=", true);
    }

    @Test
    @Ignore
    public void percent25() throws StorageException {
        // Disabled Until Double Percent decoding issue is fixed for single entity operations
        // doEscapeTest("foo%25", false, true);
    }

    @Test
    public void percent25Batch() throws StorageException {
        doEscapeTest("foo%25", true, true);
    }

    @Test
    public void regularPKInQuery() throws StorageException {
        doQueryEscapeTest("data");
    }

    @Test
    public void specialChars() throws StorageException {
        doEscapeTest("\\ // @ ? <?", true);
    }

    @Test
    public void specialCharsBatch() throws StorageException {
        doEscapeTest("\\ // @ ? <?", true);
    }

    @Test
    public void unicode() throws StorageException {
        doEscapeTest("\u00A9\u770b\u5168\u90e8", false, true);
        doEscapeTest("char中文test", false, true);
        doEscapeTest("charä¸­æ–‡test", false, true);
        doEscapeTest("世界你好", false, true);
    }

    @Test
    public void unicodeBatch() throws StorageException {
        doEscapeTest("\u00A9\u770b\u5168\u90e8", true, true);
        doEscapeTest("char中文test", true, true);
        doEscapeTest("charä¸­æ–‡test", true, true);
        doEscapeTest("世界你好", true, true);
    }

    @Test
    public void unicodeInQuery() throws StorageException {
        doQueryEscapeTest("char中文test");
        doQueryEscapeTest("charä¸­æ–‡test");
        doQueryEscapeTest("世界你好");
        doQueryEscapeTest("\u00A9\u770b\u5168\u90e8");
    }

    @Test
    public void whiteSpaceOnly() throws StorageException {
        doEscapeTest("     ", false, true);
    }

    @Test
    public void whiteSpaceOnlyBatch() throws StorageException {
        doEscapeTest("     ", true, true);
    }

    @Test
    public void whiteSpaceOnlyInQuery() throws StorageException {
        doQueryEscapeTest("     ");
    }

    @Test
    public void xmlTest() throws StorageException {
        doEscapeTest("</>", false);
        doEscapeTest("<tag>", false);
        doEscapeTest("</entry>", false);
        doEscapeTest("!<", false);
        doEscapeTest("<!%^&j", false);
    }

    @Test
    public void xmlTestBatch() throws StorageException {
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
        Class1 ref = new Class1();
        ref.setA(data);
        ref.setPartitionKey(includeInKey ? "temp" + data : "temp");
        ref.setRowKey(UUID.randomUUID().toString());

        if (this.usePropertyResolver) {
            options.setPropertyResolver(ref);
        }

        if (useBatch) {
            TableBatchOperation batch = new TableBatchOperation();
            batch.insert(ref);
            tClient.execute(testSuiteTableName, batch, options, null);
        }
        else {
            tClient.execute(testSuiteTableName, TableOperation.insert(ref), options, null);
        }

        TableResult res = null;

        if (useBatch) {
            TableBatchOperation batch = new TableBatchOperation();
            batch.retrieve(ref.getPartitionKey(), ref.getRowKey(), Class1.class);
            res = tClient.execute(testSuiteTableName, batch, options, null).get(0);
        }
        else {
            res = tClient.execute(testSuiteTableName,
                    TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), Class1.class), options, null);
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
            tClient.execute(testSuiteTableName, batch, options, null);
        }
        else {
            tClient.execute(testSuiteTableName, TableOperation.merge(ref), options, null);
        }

        if (useBatch) {
            TableBatchOperation batch = new TableBatchOperation();
            batch.retrieve(ref.getPartitionKey(), ref.getRowKey(), Class1.class);
            res = tClient.execute(testSuiteTableName, batch, options, null).get(0);
        }
        else {
            res = tClient.execute(testSuiteTableName,
                    TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), Class1.class), options, null);
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
            tClient.execute(testSuiteTableName, batch, options, null);
        }
        else {
            tClient.execute(testSuiteTableName, TableOperation.replace(ref), options, null);
        }

        if (useBatch) {
            TableBatchOperation batch = new TableBatchOperation();
            batch.retrieve(ref.getPartitionKey(), ref.getRowKey(), Class1.class);
            res = tClient.execute(testSuiteTableName, batch, options, null).get(0);
        }
        else {
            res = tClient.execute(testSuiteTableName,
                    TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), Class1.class), options, null);
        }

        retObj = res.getResultAsType();
        assertEquals(ref.getA(), retObj.getA());
        assertEquals(ref.getB(), retObj.getB());
        assertEquals(ref.getC(), retObj.getC());

        if (useBatch) {
            TableBatchOperation batch = new TableBatchOperation();
            batch.delete(retObj);
            res = tClient.execute(testSuiteTableName, batch, options, null).get(0);
        }
        else {
            res = tClient.execute(testSuiteTableName, TableOperation.delete(retObj), options, null);
        }
    }

    private void doQueryEscapeTest(String data) throws StorageException {
        Class1 ref = new Class1();
        ref.setA(data);
        ref.setPartitionKey(UUID.randomUUID().toString());
        ref.setRowKey("foo");

        if (this.usePropertyResolver) {
            options.setPropertyResolver(ref);
        }

        tClient.execute(testSuiteTableName, TableOperation.insert(ref), options, null);
        TableQuery<Class1> query = TableQuery.from(testSuiteTableName, Class1.class).where(
                String.format("(PartitionKey eq '%s') and (A eq '%s')", ref.getPartitionKey(), data));

        int count = 0;

        for (Class1 ent : tClient.execute(query, options, null)) {
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
