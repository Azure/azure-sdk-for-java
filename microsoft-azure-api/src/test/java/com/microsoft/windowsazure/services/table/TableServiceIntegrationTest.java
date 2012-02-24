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
package com.microsoft.windowsazure.services.table;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.services.core.Configuration;
import com.microsoft.windowsazure.services.core.ExponentialRetryPolicy;
import com.microsoft.windowsazure.services.core.RetryPolicyFilter;
import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.table.models.BatchOperations;
import com.microsoft.windowsazure.services.table.models.BatchResult;
import com.microsoft.windowsazure.services.table.models.BatchResult.DeleteEntity;
import com.microsoft.windowsazure.services.table.models.BatchResult.InsertEntity;
import com.microsoft.windowsazure.services.table.models.BatchResult.UpdateEntity;
import com.microsoft.windowsazure.services.table.models.DeleteEntityOptions;
import com.microsoft.windowsazure.services.table.models.EdmType;
import com.microsoft.windowsazure.services.table.models.Entity;
import com.microsoft.windowsazure.services.table.models.Filter;
import com.microsoft.windowsazure.services.table.models.GetEntityResult;
import com.microsoft.windowsazure.services.table.models.GetTableResult;
import com.microsoft.windowsazure.services.table.models.InsertEntityResult;
import com.microsoft.windowsazure.services.table.models.Query;
import com.microsoft.windowsazure.services.table.models.QueryEntitiesOptions;
import com.microsoft.windowsazure.services.table.models.QueryEntitiesResult;
import com.microsoft.windowsazure.services.table.models.QueryTablesOptions;
import com.microsoft.windowsazure.services.table.models.QueryTablesResult;
import com.microsoft.windowsazure.services.table.models.ServiceProperties;
import com.microsoft.windowsazure.services.table.models.TableEntry;

public class TableServiceIntegrationTest extends IntegrationTestBase {
    private static final String testTablesPrefix = "sdktest";
    private static final String createableTablesPrefix = "csdktest";
    private static String TEST_TABLE_1;
    private static String TEST_TABLE_2;
    private static String TEST_TABLE_3;
    private static String TEST_TABLE_4;
    private static String TEST_TABLE_5;
    private static String TEST_TABLE_6;
    private static String TEST_TABLE_7;
    private static String TEST_TABLE_8;
    private static String CREATABLE_TABLE_1;
    private static String CREATABLE_TABLE_2;
    //private static String CREATABLE_TABLE_3;
    private static String[] creatableTables;
    private static String[] testTables;

    @BeforeClass
    public static void setup() throws Exception {
        //System.setProperty("http.proxyHost", "127.0.0.1");
        //System.setProperty("http.proxyPort", "8888");

        // Setup container names array (list of container names used by
        // integration tests)
        testTables = new String[10];
        for (int i = 0; i < testTables.length; i++) {
            testTables[i] = String.format("%s%d", testTablesPrefix, i + 1);
        }

        creatableTables = new String[10];
        for (int i = 0; i < creatableTables.length; i++) {
            creatableTables[i] = String.format("%s%d", createableTablesPrefix, i + 1);
        }

        TEST_TABLE_1 = testTables[0];
        TEST_TABLE_2 = testTables[1];
        TEST_TABLE_3 = testTables[2];
        TEST_TABLE_4 = testTables[3];
        TEST_TABLE_5 = testTables[4];
        TEST_TABLE_6 = testTables[5];
        TEST_TABLE_7 = testTables[6];
        TEST_TABLE_8 = testTables[7];

        CREATABLE_TABLE_1 = creatableTables[0];
        CREATABLE_TABLE_2 = creatableTables[1];
        //CREATABLE_TABLE_3 = creatableTables[2];

        // Create all test containers and their content
        Configuration config = createConfiguration();
        TableContract service = TableService.create(config);

        deleteAllTables(service, testTables);
        deleteAllTables(service, creatableTables);
        createTables(service, testTablesPrefix, testTables);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        Configuration config = createConfiguration();
        TableContract service = TableService.create(config);

        deleteTables(service, testTablesPrefix, testTables);
        deleteTables(service, createableTablesPrefix, creatableTables);
    }

    private static void createTables(TableContract service, String prefix, String[] list) throws Exception {
        // Retry creating every table as long as we get "409 - Table being deleted" error
        service = service.withFilter(new RetryPolicyFilter(new ExponentialRetryPolicy(new int[] { 409 })));

        Set<String> containers = queryTables(service, prefix);
        for (String item : list) {
            if (!containers.contains(item)) {
                service.createTable(item);
            }
        }
    }

    private static void deleteTables(TableContract service, String prefix, String[] list) throws Exception {
        Set<String> containers = queryTables(service, prefix);
        for (String item : list) {
            if (containers.contains(item)) {
                service.deleteTable(item);
            }
        }
    }

    private static void deleteAllTables(TableContract service, String[] list) throws Exception {
        for (String item : list) {
            try {
                service.deleteTable(item);
            }
            catch (ServiceException e) {
                // Ignore
            }
        }
    }

    private static Set<String> queryTables(TableContract service, String prefix) throws Exception {
        HashSet<String> result = new HashSet<String>();
        QueryTablesResult list = service.queryTables(new QueryTablesOptions().setPrefix(prefix));
        for (TableEntry item : list.getTables()) {
            result.add(item.getName());
        }
        return result;
    }

    @Test
    public void getServicePropertiesWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        TableContract service = TableService.create(config);

        // Don't run this test with emulator, as v1.6 doesn't support this method
        if (isRunningWithEmulator(config)) {
            return;
        }

        // Act
        ServiceProperties props = service.getServiceProperties().getValue();

        // Assert
        assertNotNull(props);
        assertNotNull(props.getLogging());
        assertNotNull(props.getLogging().getRetentionPolicy());
        assertNotNull(props.getLogging().getVersion());
        assertNotNull(props.getMetrics().getRetentionPolicy());
        assertNotNull(props.getMetrics().getVersion());
    }

    @Test
    public void setServicePropertiesWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        TableContract service = TableService.create(config);

        // Don't run this test with emulator, as v1.6 doesn't support this method
        if (isRunningWithEmulator(config)) {
            return;
        }

        // Act
        ServiceProperties props = service.getServiceProperties().getValue();

        props.getLogging().setRead(true);
        service.setServiceProperties(props);

        props = service.getServiceProperties().getValue();

        // Assert
        assertNotNull(props);
        assertNotNull(props.getLogging());
        assertNotNull(props.getLogging().getRetentionPolicy());
        assertNotNull(props.getLogging().getVersion());
        assertTrue(props.getLogging().isRead());
        assertNotNull(props.getMetrics().getRetentionPolicy());
        assertNotNull(props.getMetrics().getVersion());
    }

    @Test
    public void createTablesWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        TableContract service = TableService.create(config);

        // Act
        Exception error;
        try {
            service.getTable(CREATABLE_TABLE_1);
            error = null;
        }
        catch (Exception e) {
            error = e;
        }
        service.createTable(CREATABLE_TABLE_1);
        GetTableResult result = service.getTable(CREATABLE_TABLE_1);

        // Assert
        assertNotNull(error);
        assertNotNull(result);
    }

    @Test
    public void deleteTablesWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        TableContract service = TableService.create(config);

        // Act
        service.createTable(CREATABLE_TABLE_2);
        GetTableResult result = service.getTable(CREATABLE_TABLE_2);

        service.deleteTable(CREATABLE_TABLE_2);
        Exception error;
        try {
            service.getTable(CREATABLE_TABLE_2);
            error = null;
        }
        catch (Exception e) {
            error = e;
        }

        // Assert
        assertNotNull(error);
        assertNotNull(result);
    }

    @Test
    public void queryTablesWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        TableContract service = TableService.create(config);

        // Act
        QueryTablesResult result = service.queryTables();

        // Assert
        assertNotNull(result);
    }

    @Test
    public void queryTablesWithPrefixWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        TableContract service = TableService.create(config);

        // Act
        QueryTablesResult result = service.queryTables(new QueryTablesOptions().setPrefix(testTablesPrefix));

        // Assert
        assertNotNull(result);
    }

    @Test
    public void getTableWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        TableContract service = TableService.create(config);

        // Act
        GetTableResult result = service.getTable(TEST_TABLE_1);

        // Assert
        assertNotNull(result);
    }

    @Test
    public void insertEntityWorks() throws Exception {
        System.out.println("insertEntityWorks()");

        // Arrange
        Configuration config = createConfiguration();
        TableContract service = TableService.create(config);
        Entity entity = new Entity().setPartitionKey("001").setRowKey("insertEntityWorks")
                .setProperty("test", EdmType.BOOLEAN, true).setProperty("test2", EdmType.STRING, "value")
                .setProperty("test3", EdmType.INT32, 3).setProperty("test4", EdmType.INT64, 12345678901L)
                .setProperty("test5", EdmType.DATETIME, new Date());

        // Act
        InsertEntityResult result = service.insertEntity(TEST_TABLE_2, entity);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getEntity());

        assertEquals("001", result.getEntity().getPartitionKey());
        assertEquals("insertEntityWorks", result.getEntity().getRowKey());
        assertNotNull(result.getEntity().getTimestamp());
        assertNotNull(result.getEntity().getEtag());

        assertNotNull(result.getEntity().getProperty("test"));
        assertEquals(true, result.getEntity().getProperty("test").getValue());

        assertNotNull(result.getEntity().getProperty("test2"));
        assertEquals("value", result.getEntity().getProperty("test2").getValue());

        assertNotNull(result.getEntity().getProperty("test3"));
        assertEquals(3, result.getEntity().getProperty("test3").getValue());

        assertNotNull(result.getEntity().getProperty("test4"));
        assertEquals(12345678901L, result.getEntity().getProperty("test4").getValue());

        assertNotNull(result.getEntity().getProperty("test5"));
        assertTrue(result.getEntity().getProperty("test5").getValue() instanceof Date);
    }

    @Test
    public void updateEntityWorks() throws Exception {
        System.out.println("updateEntityWorks()");

        // Arrange
        Configuration config = createConfiguration();
        TableContract service = TableService.create(config);
        Entity entity = new Entity().setPartitionKey("001").setRowKey("updateEntityWorks")
                .setProperty("test", EdmType.BOOLEAN, true).setProperty("test2", EdmType.STRING, "value")
                .setProperty("test3", EdmType.INT32, 3).setProperty("test4", EdmType.INT64, 12345678901L)
                .setProperty("test5", EdmType.DATETIME, new Date());

        // Act
        InsertEntityResult result = service.insertEntity(TEST_TABLE_2, entity);
        result.getEntity().setProperty("test4", EdmType.INT32, 5);
        service.updateEntity(TEST_TABLE_2, result.getEntity());

        // Assert
    }

    @Test
    public void insertOrReplaceEntityWorks() throws Exception {
        System.out.println("insertOrReplaceEntityWorks()");

        // Arrange
        Configuration config = createConfiguration();
        TableContract service = TableService.create(config);
        Entity entity = new Entity().setPartitionKey("001").setRowKey("insertOrReplaceEntityWorks")
                .setProperty("test", EdmType.BOOLEAN, true).setProperty("test2", EdmType.STRING, "value")
                .setProperty("test3", EdmType.INT32, 3).setProperty("test4", EdmType.INT64, 12345678901L)
                .setProperty("test5", EdmType.DATETIME, new Date());

        // Act
        service.insertOrReplaceEntity(TEST_TABLE_2, entity);
        entity.setProperty("test4", EdmType.INT32, 5);
        entity.setProperty("test6", EdmType.INT32, 6);
        service.insertOrReplaceEntity(TEST_TABLE_2, entity);

        // Assert
    }

    @Test
    public void insertOrMergeEntityWorks() throws Exception {
        System.out.println("insertOrMergeEntityWorks()");

        // Arrange
        Configuration config = createConfiguration();
        TableContract service = TableService.create(config);
        Entity entity = new Entity().setPartitionKey("001").setRowKey("insertOrMergeEntityWorks")
                .setProperty("test", EdmType.BOOLEAN, true).setProperty("test2", EdmType.STRING, "value")
                .setProperty("test3", EdmType.INT32, 3).setProperty("test4", EdmType.INT64, 12345678901L)
                .setProperty("test5", EdmType.DATETIME, new Date());

        // Act
        service.insertOrMergeEntity(TEST_TABLE_2, entity);
        entity.setProperty("test4", EdmType.INT32, 5);
        entity.setProperty("test6", EdmType.INT32, 6);
        service.insertOrMergeEntity(TEST_TABLE_2, entity);

        // Assert
    }

    @Test
    public void mergeEntityWorks() throws Exception {
        System.out.println("mergeEntityWorks()");

        // Arrange
        Configuration config = createConfiguration();
        TableContract service = TableService.create(config);
        Entity entity = new Entity().setPartitionKey("001").setRowKey("mergeEntityWorks")
                .setProperty("test", EdmType.BOOLEAN, true).setProperty("test2", EdmType.STRING, "value")
                .setProperty("test3", EdmType.INT32, 3).setProperty("test4", EdmType.INT64, 12345678901L)
                .setProperty("test5", EdmType.DATETIME, new Date());

        // Act
        InsertEntityResult result = service.insertEntity(TEST_TABLE_2, entity);

        result.getEntity().setProperty("test4", EdmType.INT32, 5);
        result.getEntity().setProperty("test6", EdmType.INT32, 6);
        service.mergeEntity(TEST_TABLE_2, result.getEntity());

        // Assert
    }

    @Test
    public void deleteEntityWorks() throws Exception {
        System.out.println("deleteEntityWorks()");

        // Arrange
        Configuration config = createConfiguration();
        TableContract service = TableService.create(config);
        Entity entity = new Entity().setPartitionKey("001").setRowKey("deleteEntityWorks")
                .setProperty("test", EdmType.BOOLEAN, true).setProperty("test2", EdmType.STRING, "value")
                .setProperty("test3", EdmType.INT32, 3).setProperty("test4", EdmType.INT64, 12345678901L)
                .setProperty("test5", EdmType.DATETIME, new Date());

        // Act
        InsertEntityResult result = service.insertEntity(TEST_TABLE_2, entity);

        service.deleteEntity(TEST_TABLE_2, result.getEntity().getPartitionKey(), result.getEntity().getRowKey());

        // Assert
    }

    @Test
    public void deleteEntityWithETagWorks() throws Exception {
        System.out.println("deleteEntityWithETagWorks()");

        // Arrange
        Configuration config = createConfiguration();
        TableContract service = TableService.create(config);
        Entity entity = new Entity().setPartitionKey("001").setRowKey("deleteEntityWithETagWorks")
                .setProperty("test", EdmType.BOOLEAN, true).setProperty("test2", EdmType.STRING, "value")
                .setProperty("test3", EdmType.INT32, 3).setProperty("test4", EdmType.INT64, 12345678901L)
                .setProperty("test5", EdmType.DATETIME, new Date());

        // Act
        InsertEntityResult result = service.insertEntity(TEST_TABLE_2, entity);

        service.deleteEntity(TEST_TABLE_2, result.getEntity().getPartitionKey(), result.getEntity().getRowKey(),
                new DeleteEntityOptions().setEtag(result.getEntity().getEtag()));

        // Assert
    }

    @Test
    public void getEntityWorks() throws Exception {
        System.out.println("getEntityWorks()");

        // Arrange
        Configuration config = createConfiguration();
        TableContract service = TableService.create(config);
        Entity entity = new Entity().setPartitionKey("001").setRowKey("getEntityWorks")
                .setProperty("test", EdmType.BOOLEAN, true).setProperty("test2", EdmType.STRING, "value")
                .setProperty("test3", EdmType.INT32, 3).setProperty("test4", EdmType.INT64, 12345678901L)
                .setProperty("test5", EdmType.DATETIME, new Date());

        // Act
        InsertEntityResult insertResult = service.insertEntity(TEST_TABLE_2, entity);
        GetEntityResult result = service.getEntity(TEST_TABLE_2, insertResult.getEntity().getPartitionKey(),
                insertResult.getEntity().getRowKey());

        // Assert
        assertNotNull(result);
        assertNotNull(result.getEntity());

        assertEquals("001", result.getEntity().getPartitionKey());
        assertEquals("getEntityWorks", result.getEntity().getRowKey());
        assertNotNull(result.getEntity().getTimestamp());
        assertNotNull(result.getEntity().getEtag());

        assertNotNull(result.getEntity().getProperty("test"));
        assertEquals(true, result.getEntity().getProperty("test").getValue());

        assertNotNull(result.getEntity().getProperty("test2"));
        assertEquals("value", result.getEntity().getProperty("test2").getValue());

        assertNotNull(result.getEntity().getProperty("test3"));
        assertEquals(3, result.getEntity().getProperty("test3").getValue());

        assertNotNull(result.getEntity().getProperty("test4"));
        assertEquals(12345678901L, result.getEntity().getProperty("test4").getValue());

        assertNotNull(result.getEntity().getProperty("test5"));
        assertTrue(result.getEntity().getProperty("test5").getValue() instanceof Date);
    }

    @Test
    public void queryEntitiesWorks() throws Exception {
        System.out.println("queryEntitiesWorks()");

        // Arrange
        Configuration config = createConfiguration();
        TableContract service = TableService.create(config);
        Entity entity = new Entity().setPartitionKey("001").setRowKey("queryEntitiesWorks")
                .setProperty("test", EdmType.BOOLEAN, true).setProperty("test2", EdmType.STRING, "value")
                .setProperty("test3", EdmType.INT32, 3).setProperty("test4", EdmType.INT64, 12345678901L)
                .setProperty("test5", EdmType.DATETIME, new Date());

        // Act
        service.insertEntity(TEST_TABLE_3, entity);
        QueryEntitiesResult result = service.queryEntities(TEST_TABLE_3);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getEntities());
        assertEquals(1, result.getEntities().size());

        assertNotNull(result.getEntities().get(0));

        assertEquals("001", result.getEntities().get(0).getPartitionKey());
        assertEquals("queryEntitiesWorks", result.getEntities().get(0).getRowKey());
        assertNotNull(result.getEntities().get(0).getTimestamp());
        assertNotNull(result.getEntities().get(0).getEtag());

        assertNotNull(result.getEntities().get(0).getProperty("test"));
        assertEquals(true, result.getEntities().get(0).getProperty("test").getValue());

        assertNotNull(result.getEntities().get(0).getProperty("test2"));
        assertEquals("value", result.getEntities().get(0).getProperty("test2").getValue());

        assertNotNull(result.getEntities().get(0).getProperty("test3"));
        assertEquals(3, result.getEntities().get(0).getProperty("test3").getValue());

        assertNotNull(result.getEntities().get(0).getProperty("test4"));
        assertEquals(12345678901L, result.getEntities().get(0).getProperty("test4").getValue());

        assertNotNull(result.getEntities().get(0).getProperty("test5"));
        assertTrue(result.getEntities().get(0).getProperty("test5").getValue() instanceof Date);
    }

    @Test
    public void queryEntitiesWithPaginationWorks() throws Exception {
        System.out.println("queryEntitiesWithPaginationWorks()");

        // Arrange
        Configuration config = createConfiguration();
        TableContract service = TableService.create(config);
        String table = TEST_TABLE_4;
        int numberOfEntries = 20;
        for (int i = 0; i < numberOfEntries; i++) {
            Entity entity = new Entity().setPartitionKey("001").setRowKey("queryEntitiesWithPaginationWorks-" + i)
                    .setProperty("test", EdmType.BOOLEAN, true).setProperty("test2", EdmType.STRING, "value")
                    .setProperty("test3", EdmType.INT32, 3).setProperty("test4", EdmType.INT64, 12345678901L)
                    .setProperty("test5", EdmType.DATETIME, new Date());

            service.insertEntity(table, entity);
        }

        // Act
        int entryCount = 0;
        String nextPartitionKey = null;
        String nextRowKey = null;
        while (true) {
            QueryEntitiesResult result = service.queryEntities(table,
                    new QueryEntitiesOptions().setNextPartitionKey(nextPartitionKey).setNextRowKey(nextRowKey));

            entryCount += result.getEntities().size();

            if (nextPartitionKey == null)
                break;

            nextPartitionKey = result.getNextPartitionKey();
            nextRowKey = result.getNextRowKey();
        }

        // Assert
        assertEquals(numberOfEntries, entryCount);
    }

    @Test
    public void queryEntitiesWithFilterWorks() throws Exception {
        System.out.println("queryEntitiesWithFilterWorks()");

        // Arrange
        Configuration config = createConfiguration();
        TableContract service = TableService.create(config);
        String table = TEST_TABLE_5;
        int numberOfEntries = 5;
        for (int i = 0; i < numberOfEntries; i++) {
            Entity entity = new Entity().setPartitionKey("001").setRowKey("queryEntitiesWithFilterWorks-" + i)
                    .setProperty("test", EdmType.BOOLEAN, true).setProperty("test2", EdmType.STRING, "value")
                    .setProperty("test3", EdmType.INT32, 3).setProperty("test4", EdmType.INT64, 12345678901L)
                    .setProperty("test5", EdmType.DATETIME, new Date());

            service.insertEntity(table, entity);
        }

        {
            // Act
            QueryEntitiesResult result = service.queryEntities(table,
                    new QueryEntitiesOptions().setQuery(new Query().setFilter(Filter.eq(Filter.litteral("RowKey"),
                            Filter.constant("queryEntitiesWithFilterWorks-3")))));

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getEntities().size());
            assertEquals("queryEntitiesWithFilterWorks-3", result.getEntities().get(0).getRowKey());
        }

        {
            // Act
            QueryEntitiesResult result = service.queryEntities(table, new QueryEntitiesOptions().setQuery(new Query()
                    .setFilter(Filter.rawString("RowKey eq 'queryEntitiesWithFilterWorks-3'"))));

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getEntities().size());
            assertEquals("queryEntitiesWithFilterWorks-3", result.getEntities().get(0).getRowKey());
        }
    }

    @Test
    public void batchInsertWorks() throws Exception {
        System.out.println("batchInsertWorks()");

        // Arrange
        Configuration config = createConfiguration();
        TableContract service = TableService.create(config);
        String table = TEST_TABLE_6;
        String partitionKey = "001";

        // Act
        Entity entity = new Entity().setPartitionKey(partitionKey).setRowKey("batchInsertWorks")
                .setProperty("test", EdmType.BOOLEAN, true).setProperty("test2", EdmType.STRING, "value")
                .setProperty("test3", EdmType.INT32, 3).setProperty("test4", EdmType.INT64, 12345678901L)
                .setProperty("test5", EdmType.DATETIME, new Date());

        BatchResult result = service.batch(new BatchOperations().addInsertEntity(table, entity));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getEntries().size());
        assertEquals(InsertEntity.class, result.getEntries().get(0).getClass());
    }

    @Test
    public void batchUpdateWorks() throws Exception {
        System.out.println("batchUpdateWorks()");

        // Arrange
        Configuration config = createConfiguration();
        TableContract service = TableService.create(config);
        String table = TEST_TABLE_6;
        String partitionKey = "001";
        Entity entity = new Entity().setPartitionKey(partitionKey).setRowKey("batchUpdateWorks")
                .setProperty("test", EdmType.BOOLEAN, true).setProperty("test2", EdmType.STRING, "value")
                .setProperty("test3", EdmType.INT32, 3).setProperty("test4", EdmType.INT64, 12345678901L)
                .setProperty("test5", EdmType.DATETIME, new Date());
        entity = service.insertEntity(table, entity).getEntity();

        // Act
        entity.setProperty("test", EdmType.BOOLEAN, false);
        BatchResult result = service.batch(new BatchOperations().addUpdateEntity(table, entity));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getEntries().size());
        assertEquals(UpdateEntity.class, result.getEntries().get(0).getClass());
    }

    @Test
    public void batchMergeWorks() throws Exception {
        System.out.println("batchMergeWorks()");

        // Arrange
        Configuration config = createConfiguration();
        TableContract service = TableService.create(config);
        String table = TEST_TABLE_6;
        String partitionKey = "001";
        Entity entity = new Entity().setPartitionKey(partitionKey).setRowKey("batchMergeWorks")
                .setProperty("test", EdmType.BOOLEAN, true).setProperty("test2", EdmType.STRING, "value")
                .setProperty("test3", EdmType.INT32, 3).setProperty("test4", EdmType.INT64, 12345678901L)
                .setProperty("test5", EdmType.DATETIME, new Date());
        entity = service.insertEntity(table, entity).getEntity();

        // Act
        BatchResult result = service.batch(new BatchOperations().addMergeEntity(table, entity));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getEntries().size());
        assertEquals(UpdateEntity.class, result.getEntries().get(0).getClass());
    }

    @Test
    public void batchInsertOrReplaceWorks() throws Exception {
        System.out.println("batchInsertOrReplaceWorks()");

        // Arrange
        Configuration config = createConfiguration();
        TableContract service = TableService.create(config);
        String table = TEST_TABLE_6;
        String partitionKey = "001";

        // Act
        Entity entity = new Entity().setPartitionKey(partitionKey).setRowKey("batchInsertOrReplaceWorks")
                .setProperty("test", EdmType.BOOLEAN, true).setProperty("test2", EdmType.STRING, "value")
                .setProperty("test3", EdmType.INT32, 3).setProperty("test4", EdmType.INT64, 12345678901L)
                .setProperty("test5", EdmType.DATETIME, new Date());

        BatchResult result = service.batch(new BatchOperations().addInsertOrReplaceEntity(table, entity));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getEntries().size());
        assertEquals(UpdateEntity.class, result.getEntries().get(0).getClass());
    }

    @Test
    public void batchInsertOrMergeWorks() throws Exception {
        System.out.println("batchInsertOrMergeWorks()");

        // Arrange
        Configuration config = createConfiguration();
        TableContract service = TableService.create(config);
        String table = TEST_TABLE_6;
        String partitionKey = "001";

        // Act
        Entity entity = new Entity().setPartitionKey(partitionKey).setRowKey("batchInsertOrMergeWorks")
                .setProperty("test", EdmType.BOOLEAN, true).setProperty("test2", EdmType.STRING, "value")
                .setProperty("test3", EdmType.INT32, 3).setProperty("test4", EdmType.INT64, 12345678901L)
                .setProperty("test5", EdmType.DATETIME, new Date());

        BatchResult result = service.batch(new BatchOperations().addInsertOrMergeEntity(table, entity));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getEntries().size());
        assertEquals(UpdateEntity.class, result.getEntries().get(0).getClass());
    }

    @Test
    public void batchDeleteWorks() throws Exception {
        System.out.println("batchDeleteWorks()");

        // Arrange
        Configuration config = createConfiguration();
        TableContract service = TableService.create(config);
        String table = TEST_TABLE_6;
        String partitionKey = "001";
        Entity entity = new Entity().setPartitionKey(partitionKey).setRowKey("batchDeleteWorks")
                .setProperty("test", EdmType.BOOLEAN, true).setProperty("test2", EdmType.STRING, "value")
                .setProperty("test3", EdmType.INT32, 3).setProperty("test4", EdmType.INT64, 12345678901L)
                .setProperty("test5", EdmType.DATETIME, new Date());
        entity = service.insertEntity(table, entity).getEntity();

        // Act
        BatchResult result = service.batch(new BatchOperations().addDeleteEntity(table, entity.getPartitionKey(),
                entity.getRowKey(), entity.getEtag()));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getEntries().size());
        assertEquals(DeleteEntity.class, result.getEntries().get(0).getClass());
    }

    @Test
    public void batchLotsOfInsertsWorks() throws Exception {
        System.out.println("batchMultipleWorks()");

        // Arrange
        Configuration config = createConfiguration();
        TableContract service = TableService.create(config);
        String table = TEST_TABLE_7;
        String partitionKey = "001";
        int insertCount = 100;

        // Act
        BatchOperations batchOperations = new BatchOperations();
        for (int i = 0; i < insertCount; i++) {

            Entity entity = new Entity().setPartitionKey(partitionKey).setRowKey("batchWorks-" + i)
                    .setProperty("test", EdmType.BOOLEAN, true).setProperty("test2", EdmType.STRING, "value")
                    .setProperty("test3", EdmType.INT32, 3).setProperty("test4", EdmType.INT64, 12345678901L)
                    .setProperty("test5", EdmType.DATETIME, new Date());

            batchOperations.addInsertEntity(table, entity);
        }
        BatchResult result = service.batch(batchOperations);

        // Assert
        assertNotNull(result);
        assertEquals(insertCount, result.getEntries().size());
        for (int i = 0; i < insertCount; i++) {
            assertEquals(InsertEntity.class, result.getEntries().get(i).getClass());

            Entity entity = ((InsertEntity) result.getEntries().get(i)).getEntity();

            assertEquals("001", entity.getPartitionKey());
            assertEquals("batchWorks-" + i, entity.getRowKey());
            assertNotNull(entity.getTimestamp());
            assertNotNull(entity.getEtag());

            assertNotNull(entity.getProperty("test"));
            assertEquals(true, entity.getProperty("test").getValue());

            assertNotNull(entity.getProperty("test2"));
            assertEquals("value", entity.getProperty("test2").getValue());

            assertNotNull(entity.getProperty("test3"));
            assertEquals(3, entity.getProperty("test3").getValue());

            assertNotNull(entity.getProperty("test4"));
            assertEquals(12345678901L, entity.getProperty("test4").getValue());

            assertNotNull(entity.getProperty("test5"));
            assertTrue(entity.getProperty("test5").getValue() instanceof Date);
        }
    }

    @Test
    public void batchAllOperationsTogetherWorks() throws Exception {
        System.out.println("batchAllOperationsWorks()");

        // Arrange
        Configuration config = createConfiguration();
        TableContract service = TableService.create(config);
        String table = TEST_TABLE_8;
        String partitionKey = "001";

        // Insert a few entities to allow updating them in batch
        Entity entity1 = new Entity().setPartitionKey(partitionKey).setRowKey("batchAllOperationsWorks-" + 1)
                .setProperty("test", EdmType.BOOLEAN, true).setProperty("test2", EdmType.STRING, "value")
                .setProperty("test3", EdmType.INT32, 3).setProperty("test4", EdmType.INT64, 12345678901L)
                .setProperty("test5", EdmType.DATETIME, new Date());

        entity1 = service.insertEntity(table, entity1).getEntity();

        Entity entity2 = new Entity().setPartitionKey(partitionKey).setRowKey("batchAllOperationsWorks-" + 2)
                .setProperty("test", EdmType.BOOLEAN, true).setProperty("test2", EdmType.STRING, "value")
                .setProperty("test3", EdmType.INT32, 3).setProperty("test4", EdmType.INT64, 12345678901L)
                .setProperty("test5", EdmType.DATETIME, new Date());

        entity2 = service.insertEntity(table, entity2).getEntity();

        Entity entity3 = new Entity().setPartitionKey(partitionKey).setRowKey("batchAllOperationsWorks-" + 3)
                .setProperty("test", EdmType.BOOLEAN, true).setProperty("test2", EdmType.STRING, "value")
                .setProperty("test3", EdmType.INT32, 3).setProperty("test4", EdmType.INT64, 12345678901L)
                .setProperty("test5", EdmType.DATETIME, new Date());

        entity3 = service.insertEntity(table, entity3).getEntity();

        Entity entity4 = new Entity().setPartitionKey(partitionKey).setRowKey("batchAllOperationsWorks-" + 4)
                .setProperty("test", EdmType.BOOLEAN, true).setProperty("test2", EdmType.STRING, "value")
                .setProperty("test3", EdmType.INT32, 3).setProperty("test4", EdmType.INT64, 12345678901L)
                .setProperty("test5", EdmType.DATETIME, new Date());

        entity4 = service.insertEntity(table, entity4).getEntity();

        // Act
        BatchOperations batchOperations = new BatchOperations();

        Entity entity = new Entity().setPartitionKey(partitionKey).setRowKey("batchAllOperationsWorks")
                .setProperty("test", EdmType.BOOLEAN, true).setProperty("test2", EdmType.STRING, "value")
                .setProperty("test3", EdmType.INT32, 3).setProperty("test4", EdmType.INT64, 12345678901L)
                .setProperty("test5", EdmType.DATETIME, new Date());
        batchOperations.addInsertEntity(table, entity);

        batchOperations.addDeleteEntity(table, entity1.getPartitionKey(), entity1.getRowKey(), entity1.getEtag());

        batchOperations.addUpdateEntity(table, entity2.setProperty("test", EdmType.INT32, 5));
        batchOperations.addMergeEntity(table, entity3.setProperty("test", EdmType.INT32, 5));
        batchOperations.addInsertOrReplaceEntity(table, entity4.setProperty("test", EdmType.INT32, 5));

        Entity entity5 = new Entity().setPartitionKey(partitionKey).setRowKey("batchAllOperationsWorks-" + 5)
                .setProperty("test", EdmType.BOOLEAN, true).setProperty("test2", EdmType.STRING, "value")
                .setProperty("test3", EdmType.INT32, 3).setProperty("test4", EdmType.INT64, 12345678901L)
                .setProperty("test5", EdmType.DATETIME, new Date());
        batchOperations.addInsertOrMergeEntity(table, entity5);

        BatchResult result = service.batch(batchOperations);

        // Assert
        assertNotNull(result);
        assertEquals(batchOperations.getOperations().size(), result.getEntries().size());
        assertEquals(InsertEntity.class, result.getEntries().get(0).getClass());
        assertEquals(DeleteEntity.class, result.getEntries().get(1).getClass());
        assertEquals(UpdateEntity.class, result.getEntries().get(2).getClass());
        assertEquals(UpdateEntity.class, result.getEntries().get(3).getClass());
        assertEquals(UpdateEntity.class, result.getEntries().get(4).getClass());
        assertEquals(UpdateEntity.class, result.getEntries().get(5).getClass());
    }
}
