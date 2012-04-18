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

import java.util.Iterator;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;

import com.microsoft.windowsazure.services.core.storage.StorageException;

/**
 * Table Escaping Tests
 */
public class TableEscapingTests extends TableTestBase {
    @Test
    public void emptyString() throws StorageException {
        doEscapeTest("", false);
    }

    @Test
    public void emptyStringBatch() throws StorageException {
        doEscapeTest("", true);
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
        doEscapeTest("\u00A9\u770b\u5168\u90e8", false);
        doEscapeTest("char中文test", false);
        doEscapeTest("charä¸­æ–‡test", false);
        doEscapeTest("世界你好", false);
    }

    @Test
    public void unicodeBatch() throws StorageException {
        doEscapeTest("\u00A9\u770b\u5168\u90e8", true);
        doEscapeTest("char中文test", true);
        doEscapeTest("charä¸­æ–‡test", true);
        doEscapeTest("世界你好", true);
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
        doEscapeTest("     ", false);
    }

    @Test
    public void whiteSpaceOnlyBatch() throws StorageException {
        doEscapeTest("     ", true);
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
        class1 ref = new class1();

        ref.setA(data);
        ref.setPartitionKey("temp");
        ref.setRowKey(UUID.randomUUID().toString());
        if (useBatch) {
            TableBatchOperation batch = new TableBatchOperation();
            batch.insert(ref);
            tClient.execute(testSuiteTableName, batch);
        }
        else {
            tClient.execute(testSuiteTableName, TableOperation.insert(ref));
        }

        TableResult res = null;

        if (useBatch) {
            TableBatchOperation batch = new TableBatchOperation();
            batch.retrieve(ref.getPartitionKey(), ref.getRowKey(), class1.class);
            res = tClient.execute(testSuiteTableName, batch).get(0);
        }
        else {
            res = tClient.execute(testSuiteTableName,
                    TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), class1.class));
        }

        class1 retObj = res.getResultAsType();
        Assert.assertEquals(ref.getA(), retObj.getA());

        ref.setEtag(retObj.getEtag());
        ref.setB(data);

        // Merge
        if (useBatch) {
            TableBatchOperation batch = new TableBatchOperation();
            batch.merge(ref);
            tClient.execute(testSuiteTableName, batch);
        }
        else {
            tClient.execute(testSuiteTableName, TableOperation.merge(ref));
        }

        if (useBatch) {
            TableBatchOperation batch = new TableBatchOperation();
            batch.retrieve(ref.getPartitionKey(), ref.getRowKey(), class1.class);
            res = tClient.execute(testSuiteTableName, batch).get(0);
        }
        else {
            res = tClient.execute(testSuiteTableName,
                    TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), class1.class));
        }

        retObj = res.getResultAsType();
        Assert.assertEquals(ref.getA(), retObj.getA());
        Assert.assertEquals(ref.getB(), retObj.getB());

        // Replace
        ref.setEtag(retObj.getEtag());
        ref.setC(data);

        if (useBatch) {
            TableBatchOperation batch = new TableBatchOperation();
            batch.replace(ref);
            tClient.execute(testSuiteTableName, batch);
        }
        else {
            tClient.execute(testSuiteTableName, TableOperation.replace(ref));
        }

        if (useBatch) {
            TableBatchOperation batch = new TableBatchOperation();
            batch.retrieve(ref.getPartitionKey(), ref.getRowKey(), class1.class);
            res = tClient.execute(testSuiteTableName, batch).get(0);
        }
        else {
            res = tClient.execute(testSuiteTableName,
                    TableOperation.retrieve(ref.getPartitionKey(), ref.getRowKey(), class1.class));
        }

        retObj = res.getResultAsType();
        Assert.assertEquals(ref.getA(), retObj.getA());
        Assert.assertEquals(ref.getB(), retObj.getB());
        Assert.assertEquals(ref.getC(), retObj.getC());
    }

    private void doQueryEscapeTest(String data) throws StorageException {
        class1 ref = new class1();

        ref.setA(data);
        ref.setPartitionKey(UUID.randomUUID().toString());
        ref.setRowKey("foo");

        tClient.execute(testSuiteTableName, TableOperation.insert(ref));
        TableQuery<class1> query = TableQuery.from(testSuiteTableName, class1.class).where(
                String.format("(PartitionKey eq '%s') and (A eq '%s')", ref.getPartitionKey(), data));

        int count = 0;

        for (Iterator<class1> iterator = tClient.execute(query); iterator.hasNext();) {
            class1 ent = iterator.next();
            count++;
            Assert.assertEquals(ent.getA(), ref.getA());
            Assert.assertEquals(ent.getB(), ref.getB());
            Assert.assertEquals(ent.getC(), ref.getC());
            Assert.assertEquals(ent.getPartitionKey(), ref.getPartitionKey());
            Assert.assertEquals(ent.getRowKey(), ref.getRowKey());
        }

        Assert.assertEquals(count, 1);
    }
}
