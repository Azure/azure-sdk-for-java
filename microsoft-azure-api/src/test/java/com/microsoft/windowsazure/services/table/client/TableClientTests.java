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

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.Test;

import com.microsoft.windowsazure.services.core.storage.ResultSegment;
import com.microsoft.windowsazure.services.core.storage.StorageException;

/**
 * Table Client Tests
 */
public class TableClientTests extends TableTestBase {
    @Test
    public void listTablesSegmented() throws IOException, URISyntaxException, StorageException {
        String tableBaseName = generateRandomTableName();
        ArrayList<String> tables = new ArrayList<String>();
        for (int m = 0; m < 20; m++) {
            String name = String.format("%s%s", tableBaseName, new DecimalFormat("#0000").format(m));
            tClient.createTable(name);
            tables.add(name);
        }

        try {
            int currTable = 0;
            ResultSegment<String> segment1 = tClient.listTablesSegmented(tableBaseName, 5, null, null, null);
            Assert.assertEquals(5, segment1.getLength());
            for (String s : segment1.getResults()) {
                Assert.assertEquals(s,
                        String.format("%s%s", tableBaseName, new DecimalFormat("#0000").format(currTable)));
                currTable++;
            }

            ResultSegment<String> segment2 = tClient.listTablesSegmented(tableBaseName, 5,
                    segment1.getContinuationToken(), null, null);
            Assert.assertEquals(5, segment2.getLength());
            for (String s : segment2.getResults()) {
                Assert.assertEquals(s,
                        String.format("%s%s", tableBaseName, new DecimalFormat("#0000").format(currTable)));
                currTable++;
            }

            ResultSegment<String> segment3 = tClient.listTablesSegmented(tableBaseName, 5,
                    segment2.getContinuationToken(), null, null);
            Assert.assertEquals(5, segment3.getLength());
            for (String s : segment3.getResults()) {
                Assert.assertEquals(s,
                        String.format("%s%s", tableBaseName, new DecimalFormat("#0000").format(currTable)));
                currTable++;
            }
        }
        finally {
            for (String s : tables) {
                tClient.deleteTable(s);
            }
        }
    }

    @Test
    public void listTablesSegmentedNoPrefix() throws IOException, URISyntaxException, StorageException {
        String tableBaseName = generateRandomTableName();
        ArrayList<String> tables = new ArrayList<String>();
        for (int m = 0; m < 20; m++) {
            String name = String.format("%s%s", tableBaseName, new DecimalFormat("#0000").format(m));
            tClient.createTable(name);
            tables.add(name);
        }

        try {
            int currTable = 0;
            ResultSegment<String> segment1 = tClient.listTablesSegmented(null, 5, null, null, null);
            Assert.assertEquals(5, segment1.getLength());
            for (String s : segment1.getResults()) {
                if (s.startsWith(tableBaseName)) {
                    Assert.assertEquals(s,
                            String.format("%s%s", tableBaseName, new DecimalFormat("#0000").format(currTable)));
                    currTable++;
                }
            }

            ResultSegment<String> segment2 = tClient.listTablesSegmented(null, 5, segment1.getContinuationToken(),
                    null, null);
            Assert.assertEquals(5, segment2.getLength());
            for (String s : segment2.getResults()) {
                if (s.startsWith(tableBaseName)) {
                    Assert.assertEquals(s,
                            String.format("%s%s", tableBaseName, new DecimalFormat("#0000").format(currTable)));
                    currTable++;
                }
            }

            ResultSegment<String> segment3 = tClient.listTablesSegmented(null, 5, segment2.getContinuationToken(),
                    null, null);
            Assert.assertEquals(5, segment3.getLength());
            for (String s : segment3.getResults()) {
                if (s.startsWith(tableBaseName)) {
                    Assert.assertEquals(s,
                            String.format("%s%s", tableBaseName, new DecimalFormat("#0000").format(currTable)));
                    currTable++;
                }

            }
        }
        finally {
            for (String s : tables) {
                tClient.deleteTable(s);
            }
        }
    }

    @Test
    public void listTablesWithIterator() throws IOException, URISyntaxException, StorageException {
        String tableBaseName = generateRandomTableName();
        ArrayList<String> tables = new ArrayList<String>();
        for (int m = 0; m < 20; m++) {
            String name = String.format("%s%s", tableBaseName, new DecimalFormat("#0000").format(m));
            tClient.createTable(name);
            tables.add(name);
        }

        try {
            // With prefix
            int currTable = 0;
            for (String s : tClient.listTables(tableBaseName, null, null)) {
                Assert.assertEquals(s,
                        String.format("%s%s", tableBaseName, new DecimalFormat("#0000").format(currTable)));
                currTable++;
            }

            Assert.assertEquals(20, currTable);

            // Without prefix
            currTable = 0;
            for (String s : tClient.listTables()) {
                if (s.startsWith(tableBaseName)) {
                    currTable++;
                }
            }

            Assert.assertEquals(20, currTable);
        }
        finally {
            for (String s : tables) {
                tClient.deleteTable(s);
            }
        }
    }

    @Test
    public void tableCreateAndAttemptCreateOnceExists() throws StorageException {
        String tableName = generateRandomTableName();
        try {
            tClient.createTable(tableName);
            Assert.assertTrue(tClient.doesTableExist(tableName));

            // Should fail as it already exists
            try {
                tClient.createTable(tableName);
                fail();
            }
            catch (StorageException ex) {
                Assert.assertEquals(ex.getErrorCode(), "TableAlreadyExists");
            }
        }
        finally {
            // cleanup
            tClient.deleteTableIfExists(tableName);
        }
    }

    @Test
    public void tableCreateExistsAndDelete() throws StorageException {
        String tableName = generateRandomTableName();
        try {
            Assert.assertTrue(tClient.createTableIfNotExists(tableName));
            Assert.assertTrue(tClient.doesTableExist(tableName));
            Assert.assertTrue(tClient.deleteTableIfExists(tableName));
        }
        finally {
            // cleanup
            tClient.deleteTableIfExists(tableName);
        }
    }

    @Test
    public void tableCreateIfNotExists() throws StorageException {
        String tableName = generateRandomTableName();
        try {
            Assert.assertTrue(tClient.createTableIfNotExists(tableName));
            Assert.assertTrue(tClient.doesTableExist(tableName));
            Assert.assertFalse(tClient.createTableIfNotExists(tableName));
        }
        finally {
            // cleanup
            tClient.deleteTableIfExists(tableName);
        }
    }

    @Test
    public void tableDeleteIfExists() throws StorageException {
        String tableName = generateRandomTableName();

        Assert.assertFalse(tClient.deleteTableIfExists(tableName));

        tClient.createTable(tableName);
        Assert.assertTrue(tClient.doesTableExist(tableName));
        Assert.assertTrue(tClient.deleteTableIfExists(tableName));
        Assert.assertFalse(tClient.deleteTableIfExists(tableName));
    }

    @Test
    public void tableDeleteWhenExistAndNotExists() throws StorageException {
        String tableName = generateRandomTableName();
        try {
            // Should fail as it doesnt already exists
            try {
                tClient.deleteTable(tableName);
                fail();
            }
            catch (StorageException ex) {
                Assert.assertEquals(ex.getMessage(), "Not Found");
            }

            tClient.createTable(tableName);
            Assert.assertTrue(tClient.doesTableExist(tableName));
            tClient.deleteTable(tableName);
            Assert.assertFalse(tClient.doesTableExist(tableName));
        }
        finally {
            tClient.deleteTableIfExists(tableName);
        }
    }

    @Test
    public void tableDoesTableExist() throws StorageException {
        String tableName = generateRandomTableName();
        try {
            Assert.assertFalse(tClient.doesTableExist(tableName));
            Assert.assertTrue(tClient.createTableIfNotExists(tableName));
            Assert.assertTrue(tClient.doesTableExist(tableName));
        }
        finally {
            // cleanup
            tClient.deleteTableIfExists(tableName);
        }
    }
}
