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
import com.microsoft.windowsazure.services.table.models.EdmType;
import com.microsoft.windowsazure.services.table.models.Entity;
import com.microsoft.windowsazure.services.table.models.GetTableResult;
import com.microsoft.windowsazure.services.table.models.InsertEntityResult;
import com.microsoft.windowsazure.services.table.models.ListTablesOptions;
import com.microsoft.windowsazure.services.table.models.QueryTablesResult;
import com.microsoft.windowsazure.services.table.models.ServiceProperties;
import com.microsoft.windowsazure.services.table.models.TableEntry;

public class TableServiceIntegrationTest extends IntegrationTestBase {
    private static final String testTablesPrefix = "sdktest";
    private static final String createableTablesPrefix = "csdktest";
    private static String TEST_TABLE_1;
    private static String TEST_TABLE_2;
    //private static String TEST_TABLE_3;
    //private static String TEST_TABLE_4;
    private static String CREATABLE_TABLE_1;
    private static String CREATABLE_TABLE_2;
    //private static String CREATABLE_TABLE_3;
    private static String[] creatableTables;
    private static String[] testTables;

    @BeforeClass
    public static void setup() throws Exception {
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
        //TEST_TABLE_3 = testTables[2];
        //TEST_TABLE_4 = testTables[3];

        CREATABLE_TABLE_1 = creatableTables[0];
        CREATABLE_TABLE_2 = creatableTables[1];
        //CREATABLE_TABLE_3 = creatableTables[2];

        // Create all test containers and their content
        Configuration config = createConfiguration();
        TableContract service = TableService.create(config);

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
        Set<String> containers = listTables(service, prefix);
        for (String item : list) {
            if (!containers.contains(item)) {
                service.createTable(item);
            }
        }
    }

    private static void deleteTables(TableContract service, String prefix, String[] list) throws Exception {
        Set<String> containers = listTables(service, prefix);
        for (String item : list) {
            if (containers.contains(item)) {
                service.deleteTable(item);
            }
        }
    }

    private static Set<String> listTables(TableContract service, String prefix) throws Exception {
        HashSet<String> result = new HashSet<String>();
        QueryTablesResult list = service.listTables(new ListTablesOptions().setPrefix(prefix));
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
    public void listTablesWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        TableContract service = TableService.create(config);

        // Act
        QueryTablesResult result = service.listTables();

        // Assert
        assertNotNull(result);
    }

    @Test
    public void queryTablesWithPrefixWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        TableContract service = TableService.create(config);

        // Act
        QueryTablesResult result = service.listTables(new ListTablesOptions().setPrefix(testTablesPrefix));

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

        // Act
        InsertEntityResult result = service.insertEntity(
                TEST_TABLE_2,
                new Entity().setPartitionKey("001").setRowKey("002").setProperty("test", EdmType.BOOLEAN, true)
                        .setProperty("test2", EdmType.STRING, "value").setProperty("test3", EdmType.INT32, 3)
                        .setProperty("test4", EdmType.INT64, 12345678901L)
                        .setProperty("test5", EdmType.DATETIME, new Date()));

        // Assert
        assertNotNull(result);
        assertNotNull(result.getEntity());

        assertEquals("001", result.getEntity().getPartitionKey());
        assertEquals("002", result.getEntity().getRowKey());
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

        // Act
        InsertEntityResult result = service.insertEntity(
                TEST_TABLE_2,
                new Entity().setPartitionKey("001").setRowKey("updateEntityWorks")
                        .setProperty("test", EdmType.BOOLEAN, true).setProperty("test2", EdmType.STRING, "value")
                        .setProperty("test3", EdmType.INT32, 3).setProperty("test4", EdmType.INT64, 12345678901L)
                        .setProperty("test5", EdmType.DATETIME, new Date()));

        result.getEntity().setProperty("test4", EdmType.INT32, 5);
        service.updateEntity(TEST_TABLE_2, result.getEntity());

        // Assert
    }
}
