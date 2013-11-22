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

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.microsoft.windowsazure.storage.StorageException;
import com.microsoft.windowsazure.storage.table.TableRequestOptions.PropertyResolver;

public class TableODataTests extends TableTestBase {

    TableRequestOptions options;
    DynamicTableEntity ent;

    @Before
    public void tableODataTestsBeforeMethod() throws StorageException {
        this.options = TableRequestOptions.applyDefaults(this.options, TableTestBase.tClient);
        this.options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);

        // Insert Entity
        ent = new DynamicTableEntity();
        ent.setPartitionKey("jxscl_odata");
        ent.setRowKey(UUID.randomUUID().toString());

        ent.getProperties().put("foo2", new EntityProperty("bar2"));
        ent.getProperties().put("foo", new EntityProperty("bar"));
        ent.getProperties().put("fooint", new EntityProperty(1234));

        tClient.execute(testSuiteTableName, TableOperation.insert(ent), options, null);
    }

    @After
    public void tableODataTestsAfterMethod() throws StorageException {
        tClient.execute(testSuiteTableName, TableOperation.delete(ent), options, null);
    }

    @Test
    public void tableOperationRetrieveJsonNoMetadataFail() throws StorageException {

        // set custom property resolver
        this.options.setPropertyResolver(new CustomPropertyResolver());

        try {
            tClient.execute(testSuiteTableName,
                    TableOperation.retrieve(ent.getPartitionKey(), ent.getRowKey(), Class1.class), options, null);
            fail("Invalid property resolver should throw");
        }
        catch (StorageException e) {
            assertEquals("Failed to parse property 'fooint' with value '1234' as type 'Edm.Guid'", e.getMessage());
        }
    }

    @Test
    public void tableOperationRetrieveJsonNoMetadataResolverFail() throws StorageException {

        // set custom property resolver which throws
        this.options.setPropertyResolver(new ThrowingPropertyResolver());

        try {
            tClient.execute(testSuiteTableName,
                    TableOperation.retrieve(ent.getPartitionKey(), ent.getRowKey(), Class1.class), options, null);
            fail("Invalid property resolver should throw");
        }
        catch (StorageException e) {
            assertEquals(
                    "The custom property resolver delegate threw an exception. Check the inner exception for more details.",
                    e.getMessage());
            assertTrue(e.getCause().getClass() == IllegalArgumentException.class);
        }
    }

    class CustomPropertyResolver implements PropertyResolver {
        @Override
        public EdmType propertyResolver(String pk, String rk, String key, String value) {
            if (key.equals("fooint")) {
                return EdmType.GUID;
            }

            return EdmType.STRING;
        }
    }

    class ThrowingPropertyResolver implements PropertyResolver {
        @Override
        public EdmType propertyResolver(String pk, String rk, String key, String value) {
            if (key.equals("fooint")) {
                throw new IllegalArgumentException();
            }

            return EdmType.STRING;
        }
    }
}
