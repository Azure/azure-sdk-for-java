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

import static org.junit.Assert.*;

import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.NoSuchPaddingException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.BaseEncoding;

import com.microsoft.azure.keyvault.cryptography.SymmetricKey;
import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.DictionaryKeyResolver;
import com.microsoft.azure.storage.ResultContinuation;
import com.microsoft.azure.storage.ResultSegment;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.TestHelper;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.table.TableRequestOptions.EncryptionResolver;
import com.microsoft.azure.storage.table.TableRequestOptions.PropertyResolver;
import com.microsoft.azure.storage.table.TableTestHelper.Class1;
import com.microsoft.azure.storage.table.TableTestHelper.EncryptedClass1;
import com.microsoft.azure.storage.table.TableTestHelper.ComplexEntity;

public class TableEncryptionTests {
    CloudTable table = null;

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
    public void testTableOperationInsertDTEEncryption() throws InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, StorageException {
        doInsertDynamicTableEntityEncryptionSync(TablePayloadFormat.Json);
        doInsertDynamicTableEntityEncryptionSync(TablePayloadFormat.JsonNoMetadata);
        doInsertDynamicTableEntityEncryptionSync(TablePayloadFormat.JsonFullMetadata);
    }

    private void doInsertDynamicTableEntityEncryptionSync(TablePayloadFormat format) throws InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException, StorageException {
        this.table.getServiceClient().getDefaultRequestOptions().setTablePayloadFormat(format);

        // insert Entity
        DynamicTableEntity ent = new DynamicTableEntity(UUID.randomUUID().toString(), new Date().toString());
        ent.getProperties().put("foo2", new EntityProperty(Constants.EMPTY_STRING));
        ent.getProperties().put("foo", new EntityProperty("bar"));
        ent.getProperties().put("fooint", new EntityProperty(1234));

        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();

        // Create the resolver to be used for unwrapping.
        DictionaryKeyResolver resolver = new DictionaryKeyResolver();
        resolver.add(aesKey);

        TableRequestOptions options = new TableRequestOptions();
        options.setEncryptionPolicy(new TableEncryptionPolicy(aesKey, null));
        options.setEncryptionResolver(new EncryptionResolver() {
            public boolean encryptionResolver(String pk, String rk, String key) {
                if (key == "foo" || key == "foo2") {
                    return true;
                }

                return false;
            }
        });

        this.table.execute(TableOperation.insert(ent), options, null);

        // retrieve Entity
        TableRequestOptions retrieveOptions = new TableRequestOptions();
        retrieveOptions.setEncryptionPolicy(new TableEncryptionPolicy(null, resolver));
        retrieveOptions.setPropertyResolver(new PropertyResolver() {
            public EdmType propertyResolver(String pk, String rk, String key, String value) {
                if (key.equals("fooint")) {
                    return EdmType.INT32;
                }

                return EdmType.STRING;
            }
        });

        TableOperation operation = TableOperation.retrieve(ent.getPartitionKey(), ent.getRowKey(),
                DynamicTableEntity.class);
        TableResult result = this.table.execute(operation, retrieveOptions, null);

        DynamicTableEntity retrievedEntity = (DynamicTableEntity) result.getResult();
        assertNotNull(retrievedEntity);
        assertEquals(ent.getPartitionKey(), retrievedEntity.getPartitionKey());
        assertEquals(ent.getRowKey(), retrievedEntity.getRowKey());
        assertEquals(ent.getProperties().size(), retrievedEntity.getProperties().size());
        assertEquals(ent.getProperties().get("foo").getValueAsString(), 
                retrievedEntity.getProperties().get("foo").getValueAsString());
        assertEquals(ent.getProperties().get("foo2").getValueAsString(), 
                retrievedEntity.getProperties().get("foo2").getValueAsString());
        assertEquals(ent.getProperties().get("fooint").getValueAsInteger(),
                retrievedEntity.getProperties().get("fooint").getValueAsInteger());
    }

    @Test
    public void testTableOperationInsertPOCOEncryptionWithResolver() throws InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException, StorageException {
        doInsertPOCOEntityEncryptionWithResolver(TablePayloadFormat.Json);
        doInsertPOCOEntityEncryptionWithResolver(TablePayloadFormat.JsonNoMetadata);
        doInsertPOCOEntityEncryptionWithResolver(TablePayloadFormat.JsonFullMetadata);
    }

    private void doInsertPOCOEntityEncryptionWithResolver(TablePayloadFormat format) throws StorageException,
            InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        this.table.getServiceClient().getDefaultRequestOptions().setTablePayloadFormat(format);

        // insert Entity
        Class1 ent = new Class1(UUID.randomUUID().toString(), new Date().toString());
        ent.populate();

        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();

        // Create the resolver to be used for unwrapping.
        DictionaryKeyResolver resolver = new DictionaryKeyResolver();
        resolver.add(aesKey);

        TableRequestOptions insertOptions = new TableRequestOptions();
        insertOptions.setEncryptionPolicy(new TableEncryptionPolicy(aesKey, null));
        insertOptions.setEncryptionResolver(new EncryptionResolver() {
            public boolean encryptionResolver(String pk, String rk, String key) {
                if (key.equals("A") || key.equals("foo")) {
                    return true;
                }

                return false;
            }
        });

        this.table.execute(TableOperation.insert(ent), insertOptions, null);

        // retrieve Entity
        // No need for an encryption resolver while retrieving the entity.
        TableRequestOptions retrieveOptions = new TableRequestOptions();
        retrieveOptions.setEncryptionPolicy(new TableEncryptionPolicy(null, resolver));

        TableOperation operation = TableOperation.retrieve(ent.getPartitionKey(), ent.getRowKey(), Class1.class);
        TableResult result = this.table.execute(operation, retrieveOptions, null);

        Class1 retrievedEntity = (Class1) result.getResult();
        assertNotNull(retrievedEntity);
        assertEquals(ent.getPartitionKey(), retrievedEntity.getPartitionKey());
        assertEquals(ent.getRowKey(), retrievedEntity.getRowKey());
        retrievedEntity.validate();
    }

    @Test
    public void testTableOperationInsertPOCOEncryptionWithAttributes() throws InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException, StorageException {
        doInsertPOCOEntityEncryptionWithAttributes(TablePayloadFormat.Json);
        doInsertPOCOEntityEncryptionWithAttributes(TablePayloadFormat.JsonNoMetadata);
        doInsertPOCOEntityEncryptionWithAttributes(TablePayloadFormat.JsonFullMetadata);
    }

    private void doInsertPOCOEntityEncryptionWithAttributes(TablePayloadFormat format) throws StorageException,
            InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        this.table.getServiceClient().getDefaultRequestOptions().setTablePayloadFormat(format);

        // insert Entity
        EncryptedClass1 ent = new EncryptedClass1(UUID.randomUUID().toString(), new Date().toString() + format);
        ent.populate();

        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();

        // Create the resolver to be used for unwrapping.
        DictionaryKeyResolver resolver = new DictionaryKeyResolver();
        resolver.add(aesKey);

        TableRequestOptions insertOptions = new TableRequestOptions();
        insertOptions.setEncryptionPolicy(new TableEncryptionPolicy(aesKey, null));
        this.table.execute(TableOperation.insert(ent), insertOptions, null);

        // retrieve Entity
        // No need for an encryption resolver while retrieving the entity.
        TableRequestOptions retrieveOptions = new TableRequestOptions();
        retrieveOptions.setEncryptionPolicy(new TableEncryptionPolicy(null, resolver));

        TableOperation operation = TableOperation.retrieve(ent.getPartitionKey(), ent.getRowKey(),
                EncryptedClass1.class);
        TableResult result = this.table.execute(operation, retrieveOptions, null);

        EncryptedClass1 retrievedEntity = (EncryptedClass1) result.getResult();
        assertNotNull(retrievedEntity);
        assertEquals(ent.getPartitionKey(), retrievedEntity.getPartitionKey());
        assertEquals(ent.getRowKey(), retrievedEntity.getRowKey());
        retrievedEntity.validate();
    }
    
    @Test
    public void testTableOperationInsertPOCOEncryptionFailsWithClass() throws InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException, StorageException {
        doInsertPOCOEncryptionFailsWithClass(TablePayloadFormat.Json);
        doInsertPOCOEncryptionFailsWithClass(TablePayloadFormat.JsonNoMetadata);
        doInsertPOCOEncryptionFailsWithClass(TablePayloadFormat.JsonFullMetadata);
    }

    private void doInsertPOCOEncryptionFailsWithClass(TablePayloadFormat format)
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, StorageException {
        this.table.getServiceClient().getDefaultRequestOptions().setTablePayloadFormat(format);

        // insert Entity
        EncryptedClass1 ent = new EncryptedClass1(UUID.randomUUID().toString(), new Date().toString());
        ent.populate();

        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();

        // Create the resolver to be used for unwrapping.
        DictionaryKeyResolver resolver = new DictionaryKeyResolver();
        resolver.add(aesKey);

        TableRequestOptions insertOptions = new TableRequestOptions();
        insertOptions.setEncryptionPolicy(new TableEncryptionPolicy(aesKey, null));
        insertOptions.setEncryptionResolver(new EncryptionResolver() {
            public boolean encryptionResolver(String pk, String rk, String key) {
                if (key.equals("B")) {
                    return true;
                }

                return false;
            }
        });

        // Since we have specified attributes and resolver, properties A, B and foo will be encrypted.
        this.table.execute(TableOperation.insert(ent), insertOptions, null);

        // retrieve entity without decryption and confirm that all 3 properties were encrypted.
        // No need for an encryption resolver while retrieving the entity.
        TableOperation operation = TableOperation.retrieve(ent.getPartitionKey(), ent.getRowKey(),
                EncryptedClass1.class);
        TableResult result = this.table.execute(operation, null, null);
        EncryptedClass1 retrievedEntity = result.getResultAsType();
        assertNotNull(retrievedEntity);
        assertNotNull(retrievedEntity.getC());
        
        // Assert that we ignore encrypted entities due to bad type, except in no metadata which
        // assumes string
        if (format == TablePayloadFormat.JsonNoMetadata) {
            assertNotNull(retrievedEntity.getB());
        }
        else {
            assertNull(retrievedEntity.getB());
        }
    }

    @Test
    public void testTableOperationInsertPOCOEncryptionWithAttributesAndResolver() throws InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException, StorageException {
        doInsertPOCOEntityEncryptionWithAttributesAndResolver(TablePayloadFormat.Json);
        doInsertPOCOEntityEncryptionWithAttributesAndResolver(TablePayloadFormat.JsonNoMetadata);
        doInsertPOCOEntityEncryptionWithAttributesAndResolver(TablePayloadFormat.JsonFullMetadata);
    }

    private void doInsertPOCOEntityEncryptionWithAttributesAndResolver(TablePayloadFormat format)
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, StorageException {
        this.table.getServiceClient().getDefaultRequestOptions().setTablePayloadFormat(format);

        // insert Entity
        EncryptedClass1 ent = new EncryptedClass1(UUID.randomUUID().toString(), new Date().toString());
        ent.populate();

        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();

        // Create the resolver to be used for unwrapping.
        DictionaryKeyResolver resolver = new DictionaryKeyResolver();
        resolver.add(aesKey);

        TableRequestOptions insertOptions = new TableRequestOptions();
        insertOptions.setEncryptionPolicy(new TableEncryptionPolicy(aesKey, null));
        insertOptions.setEncryptionResolver(new EncryptionResolver() {
            public boolean encryptionResolver(String pk, String rk, String key) {
                if (key.equals("B")) {
                    return true;
                }

                return false;
            }
        });

        // Since we have specified attributes and resolver, properties A, B and foo will be encrypted.
        this.table.execute(TableOperation.insert(ent), insertOptions, null);

        // retrieve entity without decryption and confirm that all 3 properties were encrypted.
        // No need for an encryption resolver while retrieving the entity.
        TableOperation operation = TableOperation.retrieve(ent.getPartitionKey(), ent.getRowKey(), DynamicTableEntity.class);
        TableResult result = this.table.execute(operation, null, null);

        DynamicTableEntity retrievedDynamicEntity = (DynamicTableEntity) result.getResult();
        assertNotNull(retrievedDynamicEntity);
        assertEquals(ent.getPartitionKey(), retrievedDynamicEntity.getPartitionKey());
        assertEquals(ent.getRowKey(), retrievedDynamicEntity.getRowKey());
        if (format == TablePayloadFormat.JsonNoMetadata)
        {
            assertEquals(EdmType.STRING, retrievedDynamicEntity.getProperties().get("A").getEdmType());
            assertEquals(EdmType.STRING, retrievedDynamicEntity.getProperties().get("B").getEdmType());
        }
        else
        {
            assertEquals(EdmType.BINARY, retrievedDynamicEntity.getProperties().get("A").getEdmType());
            assertEquals(EdmType.BINARY, retrievedDynamicEntity.getProperties().get("B").getEdmType());
        }

        // retrieve entity and decrypt.
        TableRequestOptions retrieveOptions = new TableRequestOptions();
        retrieveOptions.setEncryptionPolicy(new TableEncryptionPolicy(null, resolver));

        operation = TableOperation.retrieve(ent.getPartitionKey(), ent.getRowKey(), EncryptedClass1.class);
        result = this.table.execute(operation, retrieveOptions, null);

        EncryptedClass1 retrievedEntity = (EncryptedClass1) result.getResult();
        assertNotNull(retrievedEntity);
        assertEquals(ent.getPartitionKey(), retrievedEntity.getPartitionKey());
        assertEquals(ent.getRowKey(), retrievedEntity.getRowKey());
        retrievedEntity.validate();
    }

    @Test
    public void testTableQueryPOCOProjectionEncryption() throws StorageException, InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException {
        // insert Entity
        EncryptedClass1 ent1 = new EncryptedClass1(UUID.randomUUID().toString(), new Date().toString());
        ent1.populate();

        EncryptedClass1 ent2 = new EncryptedClass1(UUID.randomUUID().toString(), new Date().toString());
        ent2.populate();

        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();

        TableRequestOptions options = new TableRequestOptions();
        options.setEncryptionPolicy(new TableEncryptionPolicy(aesKey, null));
        this.table.execute(TableOperation.insert(ent1), options, null);
        this.table.execute(TableOperation.insert(ent2), options, null);

        // Query with different payload formats.
        doTableQueryPOCOProjectionEncryption(TablePayloadFormat.Json, aesKey);
        doTableQueryPOCOProjectionEncryption(TablePayloadFormat.JsonNoMetadata, aesKey);
        doTableQueryPOCOProjectionEncryption(TablePayloadFormat.JsonFullMetadata, aesKey);
    }

    private void doTableQueryPOCOProjectionEncryption(TablePayloadFormat format, SymmetricKey aesKey) {
        this.table.getServiceClient().getDefaultRequestOptions().setTablePayloadFormat(format);

        // Create the resolver to be used for unwrapping.
        DictionaryKeyResolver resolver = new DictionaryKeyResolver();
        resolver.add(aesKey);

        TableRequestOptions options = new TableRequestOptions();
        options.setEncryptionPolicy(new TableEncryptionPolicy(null, resolver));

        TableQuery<EncryptedClass1> query = TableQuery.from(EncryptedClass1.class).select(new String[] { "A", "C" });

        for (EncryptedClass1 ent : this.table.execute(query, options, null)) {
            assertNotNull(ent.getPartitionKey());
            assertNotNull(ent.getRowKey());
            assertNotNull(ent.getTimestamp());

            assertEquals(ent.getA(), "foo_A");
            assertNull(ent.getB());
            assertEquals(ent.getC(), "foo_C");
            assertNull(ent.getD());
        }
    }

    @Test
    public void testTableQueryDTEProjectionEncryption() throws StorageException, InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException {
        // insert Entity
        DynamicTableEntity ent1 = new DynamicTableEntity(UUID.randomUUID().toString(), new Date().toString());
        ent1.getProperties().put("A", new EntityProperty(Constants.EMPTY_STRING));
        ent1.getProperties().put("B", new EntityProperty("b"));

        DynamicTableEntity ent2 = new DynamicTableEntity(UUID.randomUUID().toString(), new Date().toString());
        ent2.getProperties().put("A", new EntityProperty("a"));
        ent2.getProperties().put("B", new EntityProperty("b"));

        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();

        TableRequestOptions options = new TableRequestOptions();
        options.setEncryptionPolicy(new TableEncryptionPolicy(aesKey, null));
        options.setEncryptionResolver(new EncryptionResolver() {
            public boolean encryptionResolver(String pk, String rk, String key) {
                if (key.equals("A")) {
                    return true;
                }

                return false;
            }
        });

        this.table.execute(TableOperation.insert(ent1), options, null);
        this.table.execute(TableOperation.insert(ent2), options, null);

        // Query with different payload formats.
        doTableQueryDTEProjectionEncryption(TablePayloadFormat.Json, aesKey);
        doTableQueryDTEProjectionEncryption(TablePayloadFormat.JsonNoMetadata, aesKey);
        doTableQueryDTEProjectionEncryption(TablePayloadFormat.JsonFullMetadata, aesKey);
    }

    private void doTableQueryDTEProjectionEncryption(TablePayloadFormat format, SymmetricKey aesKey) {
        this.table.getServiceClient().getDefaultRequestOptions().setTablePayloadFormat(format);

        // Create the resolver to be used for unwrapping.
        DictionaryKeyResolver resolver = new DictionaryKeyResolver();
        resolver.add(aesKey);

        TableRequestOptions options = new TableRequestOptions();
        options.setEncryptionPolicy(new TableEncryptionPolicy(null, resolver));

        TableQuery<DynamicTableEntity> query = TableQuery.from(DynamicTableEntity.class).select(new String[] { "A" });

        for (DynamicTableEntity ent : this.table.execute(query, options, null)) {
            assertNotNull(ent.getPartitionKey());
            assertNotNull(ent.getRowKey());
            assertNotNull(ent.getTimestamp());

            assertTrue(ent.getProperties().get("A").getValueAsString().equals("a")
                    || ent.getProperties().get("A").getValueAsString().equals(Constants.EMPTY_STRING));
        }
        
        // Test to make sure that we don't specify encryption columns when there aren't any columns specified at all.
        query = TableQuery.from(DynamicTableEntity.class);

        for (DynamicTableEntity ent : this.table.execute(query, options, null)) {
            assertNotNull(ent.getPartitionKey());
            assertNotNull(ent.getRowKey());
            assertNotNull(ent.getTimestamp());

            assertTrue(ent.getProperties().get("A").getValueAsString().equals("a")
                    || ent.getProperties().get("A").getValueAsString().equals(Constants.EMPTY_STRING));
        }
    }

    @Test
    public void testTableOperationReplaceEncryption() throws InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, StorageException {
        doTableOperationReplaceEncryption(TablePayloadFormat.Json);
        doTableOperationReplaceEncryption(TablePayloadFormat.JsonNoMetadata);
        doTableOperationReplaceEncryption(TablePayloadFormat.JsonFullMetadata);
    }

    private void doTableOperationReplaceEncryption(TablePayloadFormat format) throws InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException, StorageException {
        this.table.getServiceClient().getDefaultRequestOptions().setTablePayloadFormat(format);

        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();

        TableRequestOptions options = new TableRequestOptions();
        options.setEncryptionPolicy(new TableEncryptionPolicy(aesKey, null));
        options.setEncryptionResolver(new EncryptionResolver() {
            public boolean encryptionResolver(String pk, String rk, String key) {
                if (key.equals("A") || key.equals("B")) {
                    return true;
                }

                return false;
            }
        });

        // insert Entity
        DynamicTableEntity baseEntity = new DynamicTableEntity("test", "foo" + format.toString());
        baseEntity.getProperties().put("A", new EntityProperty("a"));
        this.table.execute(TableOperation.insert(baseEntity), options, null);

        // ReplaceEntity
        DynamicTableEntity replaceEntity = new DynamicTableEntity(baseEntity.getPartitionKey(), baseEntity.getRowKey());
        replaceEntity.setEtag(baseEntity.getEtag());
        replaceEntity.getProperties().put("B", new EntityProperty("b"));
        this.table.execute(TableOperation.replace(replaceEntity), options, null);

        // retrieve Entity & Verify Contents
        // Create the resolver to be used for unwrapping.
        DictionaryKeyResolver resolver = new DictionaryKeyResolver();
        resolver.add(aesKey);

        TableRequestOptions retrieveOptions = new TableRequestOptions();
        retrieveOptions.setEncryptionPolicy(new TableEncryptionPolicy(null, resolver));
        TableResult result = this.table
                .execute(TableOperation.retrieve(baseEntity.getPartitionKey(), baseEntity.getRowKey(),
                        DynamicTableEntity.class), retrieveOptions, null);
        DynamicTableEntity retrievedEntity = (DynamicTableEntity) result.getResult();

        assertNotNull(retrievedEntity);
        assertEquals(replaceEntity.getProperties().size(), retrievedEntity.getProperties().size());
        assertEquals(replaceEntity.getProperties().get("B").getValueAsString(),
                retrievedEntity.getProperties().get("B").getValueAsString());
    }

    @Test
    public void testTableBatchinsertOrReplaceEncryption() throws InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, StorageException {
        doTableBatchInsertOrReplaceEncryption(TablePayloadFormat.Json);
        doTableBatchInsertOrReplaceEncryption(TablePayloadFormat.JsonNoMetadata);
        doTableBatchInsertOrReplaceEncryption(TablePayloadFormat.JsonFullMetadata);
    }

    private void doTableBatchInsertOrReplaceEncryption(TablePayloadFormat format) throws InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException, StorageException {
        this.table.getServiceClient().getDefaultRequestOptions().setTablePayloadFormat(format);

        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();

        TableRequestOptions options = new TableRequestOptions();
        options.setEncryptionPolicy(new TableEncryptionPolicy(aesKey, null));
        options.setEncryptionResolver(new EncryptionResolver() {
            public boolean encryptionResolver(String pk, String rk, String key) {
                if (key.equals("A") || key.equals("B")) {
                    return true;
                }

                return false;
            }
        });

        // insert Or Replace with no pre-existing entity
        DynamicTableEntity insertOrReplaceEntity = new DynamicTableEntity("insertOrReplace entity", "foo"
                + format.toString());
        insertOrReplaceEntity.getProperties().put("A", new EntityProperty("a"));

        TableBatchOperation batch = new TableBatchOperation();
        batch.insertOrReplace(insertOrReplaceEntity);
        this.table.execute(batch, options, null);

        // retrieve Entity & Verify Contents
        // Create the resolver to be used for unwrapping.
        DictionaryKeyResolver resolver = new DictionaryKeyResolver();
        resolver.add(aesKey);

        TableRequestOptions retrieveOptions = new TableRequestOptions();
        retrieveOptions.setEncryptionPolicy(new TableEncryptionPolicy(null, resolver));

        TableResult result = this.table.execute(TableOperation.retrieve(insertOrReplaceEntity.getPartitionKey(),
                insertOrReplaceEntity.getRowKey(), DynamicTableEntity.class), retrieveOptions, null);
        DynamicTableEntity retrievedEntity = (DynamicTableEntity) result.getResult();
        assertNotNull(retrievedEntity);
        assertEquals(insertOrReplaceEntity.getProperties().size(), retrievedEntity.getProperties().size());

        DynamicTableEntity replaceEntity = new DynamicTableEntity(insertOrReplaceEntity.getPartitionKey(),
                insertOrReplaceEntity.getRowKey());
        replaceEntity.getProperties().put("B", new EntityProperty("b"));

        TableBatchOperation batch2 = new TableBatchOperation();
        batch2.insertOrReplace(replaceEntity);
        this.table.execute(batch2, options, null);

        // retrieve Entity & Verify Contents
        result = this.table.execute(TableOperation.retrieve(insertOrReplaceEntity.getPartitionKey(),
                insertOrReplaceEntity.getRowKey(), DynamicTableEntity.class), retrieveOptions, null);
        retrievedEntity = (DynamicTableEntity) result.getResult();
        assertNotNull(retrievedEntity);
        assertEquals(1, retrievedEntity.getProperties().size());
        assertEquals(replaceEntity.getProperties().get("B").getValueAsString(), 
                retrievedEntity.getProperties().get("B").getValueAsString());
    }

    @Test
    public void testTableBatchRetrieveEncryptedEntity() throws InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, StorageException {
        doTableBatchRetrieveEncryptedEntitySync(TablePayloadFormat.Json);
        doTableBatchRetrieveEncryptedEntitySync(TablePayloadFormat.JsonNoMetadata);
        doTableBatchRetrieveEncryptedEntitySync(TablePayloadFormat.JsonFullMetadata);
    }

    private void doTableBatchRetrieveEncryptedEntitySync(TablePayloadFormat format) throws StorageException,
            InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        this.table.getServiceClient().getDefaultRequestOptions().setTablePayloadFormat(format);

        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();

        TableRequestOptions options = new TableRequestOptions();
        options.setEncryptionPolicy(new TableEncryptionPolicy(aesKey, null));
        options.setEncryptionResolver(new EncryptionResolver() {
            public boolean encryptionResolver(String pk, String rk, String key) {
                if (key.equals("A") || key.equals("B")) {
                    return true;
                }

                return false;
            }
        });

        // add insert
        DynamicTableEntity sendEnt = generateRandomEntity(UUID.randomUUID().toString());

        // generate a set of properties for all supported Types
        sendEnt.setProperties(new ComplexEntity().writeEntity(null));
        sendEnt.getProperties().put("foo", new EntityProperty("bar"));

        TableBatchOperation batch = new TableBatchOperation();
        TableOperation retrieve = TableOperation.retrieve(sendEnt.getPartitionKey(), sendEnt.getRowKey(),
                DynamicTableEntity.class);
        batch.add(retrieve);

        // not found
        ArrayList<TableResult> results = this.table.execute(batch, options, null);
        assertEquals(results.size(), 1);
        assertEquals(results.get(0).getHttpStatusCode(), HttpURLConnection.HTTP_NOT_FOUND);
        assertNull(results.get(0).getResult());
        assertNull(results.get(0).getEtag());

        // insert entity
        this.table.execute(TableOperation.insert(sendEnt), options, null);

        // Create the resolver to be used for unwrapping.
        DictionaryKeyResolver resolver = new DictionaryKeyResolver();
        resolver.add(aesKey);

        TableRequestOptions retrieveOptions = new TableRequestOptions();
        retrieveOptions.setEncryptionPolicy(new TableEncryptionPolicy(null, resolver));

        // Success
        results = this.table.execute(batch, retrieveOptions, null);
        assertEquals(results.size(), 1);
        assertEquals(results.get(0).getHttpStatusCode(), HttpURLConnection.HTTP_OK);

        DynamicTableEntity retrievedEntity = (DynamicTableEntity) results.get(0).getResult();

        // Validate entity
        assertEquals(sendEnt.getProperties().get("foo").getValueAsString(), 
                retrievedEntity.getProperties().get("foo").getValueAsString());
    }

    @Test
    public void testTableOperationValidateEncryption() throws InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, StorageException {
        doTableOperationValidateEncryption(TablePayloadFormat.Json);
        doTableOperationValidateEncryption(TablePayloadFormat.JsonNoMetadata);
        doTableOperationValidateEncryption(TablePayloadFormat.JsonFullMetadata);
    }

    private void doTableOperationValidateEncryption(TablePayloadFormat format) throws StorageException,
            InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        this.table.getServiceClient().getDefaultRequestOptions().setTablePayloadFormat(format);

        // insert Entity
        DynamicTableEntity ent = new DynamicTableEntity(UUID.randomUUID().toString(), new Date().toString());
        ent.getProperties().put("encprop", new EntityProperty(Constants.EMPTY_STRING));
        ent.getProperties().put("encprop2", new EntityProperty(Constants.EMPTY_STRING));
        ent.getProperties().put("encprop3", new EntityProperty("bar"));
        ent.getProperties().put("notencprop", new EntityProperty(1234));

        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();

        TableRequestOptions uploadOptions = new TableRequestOptions();
        uploadOptions.setEncryptionPolicy(new TableEncryptionPolicy(aesKey, null));
        uploadOptions.setEncryptionResolver(new EncryptionResolver() {
            public boolean encryptionResolver(String pk, String rk, String key) {
                if (key.startsWith("encprop")) {
                    return true;
                }

                return false;
            }
        });
        uploadOptions.setPropertyResolver(new PropertyResolver() {
            public EdmType propertyResolver(String pk, String rk, String key, String value) {
                if (key == "notencprop") {
                    return EdmType.INT32;
                }

                return EdmType.STRING;
            }
        });

        this.table.execute(TableOperation.insert(ent), uploadOptions, null);

        TableRequestOptions downloadOptions = new TableRequestOptions();
        downloadOptions.setPropertyResolver(new PropertyResolver() {
            public EdmType propertyResolver(String pk, String rk, String key, String value) {
                if (key == "notencprop") {
                    return EdmType.INT32;
                }

                return EdmType.STRING;
            }
        });

        // retrieve Entity without decrypting
        TableOperation operation = TableOperation.retrieve(ent.getPartitionKey(), ent.getRowKey(),
                DynamicTableEntity.class);
        TableResult result = this.table.execute(operation, downloadOptions, null);

        DynamicTableEntity retrievedEntity = (DynamicTableEntity) result.getResult();
        assertNotNull(retrievedEntity);
        assertEquals(ent.getPartitionKey(), retrievedEntity.getPartitionKey());
        assertEquals(ent.getRowKey(), retrievedEntity.getRowKey());

        // Properties having the same value should be encrypted to different values.
        if (format == TablePayloadFormat.JsonNoMetadata)
        {
            // With DTE and Json no metadata, if an encryption policy is not set, the client lib just reads the byte arrays as strings.
            assertFalse(ent.getProperties().get("encprop").getValueAsString()
                    .equals(retrievedEntity.getProperties().get("encprop").getValueAsString()));
        }
        else
        {
            assertFalse(retrievedEntity.getProperties().get("encprop").getValueAsByteArray()
                    .equals(retrievedEntity.getProperties().get("encprop2").getValueAsByteArray()));
            assertFalse(ent.getProperties().get("encprop").getEdmType()
                    .equals(retrievedEntity.getProperties().get("encprop").getEdmType()));
            assertFalse(ent.getProperties().get("encprop2").getEdmType()
                    .equals(retrievedEntity.getProperties().get("encprop2").getEdmType()));
            assertFalse(ent.getProperties().get("encprop3").getEdmType()
                    .equals(retrievedEntity.getProperties().get("encprop3").getEdmType()));
        }

        assertEquals(ent.getProperties().get("notencprop").getValueAsInteger(),
                retrievedEntity.getProperties().get("notencprop").getValueAsInteger());
    }

    @Test
    public void testTableEncryptingUnsupportedPropertiesShouldThrow() throws InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException {
        // insert Entity
        DynamicTableEntity ent = new DynamicTableEntity(UUID.randomUUID().toString(), new Date().toString());
        ent.getProperties().put("foo2", new EntityProperty(Constants.EMPTY_STRING));
        ent.getProperties().put("fooint", new EntityProperty(1234));

        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();

        TableRequestOptions options = new TableRequestOptions();
        options.setEncryptionPolicy(new TableEncryptionPolicy(aesKey, null));
        options.setEncryptionResolver(new EncryptionResolver() {
            public boolean encryptionResolver(String pk, String rk, String key) {
                if (key.startsWith("foo")) {
                    return true;
                }

                return false;
            }
        });

        try {
            this.table.execute(TableOperation.insert(ent), options, null);
            fail("Encrypting non-String properties should fail");
        }
        catch (StorageException e) {
            assertEquals(IllegalArgumentException.class, e.getCause().getClass());
        }

        ent.getProperties().remove("fooint");
        ent.getProperties().put("foo", null);

        try {
            this.table.execute(TableOperation.insert(ent), options, null);
            fail("Encrypting null properties should fail");
        }
        catch (StorageException e) {
            assertEquals(IllegalArgumentException.class, e.getCause().getClass());
        }
    }
    
    @Test
    public void testTableEncryptionValidateSwappingPropertiesThrows() throws StorageException, InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException {
        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();
        
        TableRequestOptions options = new TableRequestOptions();
        options.setEncryptionPolicy(new TableEncryptionPolicy(aesKey, null));
        options.setEncryptionResolver(new EncryptionResolver() {
            public boolean encryptionResolver(String pk, String rk, String key) {
                if (key == "Prop1")
                {
                    return true;
                }

                return false;
            }
        });

        // Insert Entities
        DynamicTableEntity baseEntity1 = new DynamicTableEntity("test1", "foo1");
        baseEntity1.getProperties().put("Prop1", new EntityProperty("Value1"));
        this.table.execute(TableOperation.insert(baseEntity1), options, null);

        DynamicTableEntity baseEntity2 = new DynamicTableEntity("test1", "foo2");
        baseEntity2.getProperties().put("Prop1", new EntityProperty("Value2"));
        this.table.execute(TableOperation.insert(baseEntity2), options, null);

        // Retrieve entity1 (Do not set encryption policy)
        TableResult result = this.table.execute(TableOperation.retrieve(baseEntity1.getPartitionKey(), 
                baseEntity1.getRowKey(), DynamicTableEntity.class));
        DynamicTableEntity retrievedEntity = (DynamicTableEntity) result.getResult();

        // Replace entity2 with encrypted entity1's properties (Do not set encryption policy).
        DynamicTableEntity replaceEntity = new DynamicTableEntity(baseEntity2.getPartitionKey(), 
                baseEntity2.getRowKey(), baseEntity2.getEtag(), retrievedEntity.getProperties());
        this.table.execute(TableOperation.replace(replaceEntity));

        // Try to retrieve entity2
        // Create the resolver to be used for unwrapping.
        DictionaryKeyResolver resolver = new DictionaryKeyResolver();
        resolver.add(aesKey);

        TableRequestOptions retrieveOptions = new TableRequestOptions();
        retrieveOptions.setEncryptionPolicy(new TableEncryptionPolicy(null, resolver));

        try
        {
            result = this.table.execute(TableOperation.retrieve(baseEntity2.getPartitionKey(), baseEntity2.getRowKey(), 
                    DynamicTableEntity.class), retrieveOptions, null);
            fail();
        }
        catch (StorageException ex)
        {
            assertEquals(BadPaddingException.class, ex.getCause().getClass());
        }
    }
    
    @Test
    public void testTableOperationEncryptionWithStrictMode() throws StorageException, InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException {
        // Insert Entity
        DynamicTableEntity ent = new DynamicTableEntity(UUID.randomUUID().toString(), new Date().toString());
        ent.getProperties().put("foo2", new EntityProperty(Constants.EMPTY_STRING));
        ent.getProperties().put("foo", new EntityProperty("bar"));
        ent.getProperties().put("fooint", new EntityProperty(1234));

        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();

        // Create the resolver to be used for unwrapping.
        DictionaryKeyResolver resolver = new DictionaryKeyResolver();
        resolver.add(aesKey);

        TableRequestOptions options = new TableRequestOptions();
        options.setEncryptionPolicy(new TableEncryptionPolicy(aesKey, null));
        options.setEncryptionResolver(new EncryptionResolver() {
            public boolean encryptionResolver(String pk, String rk, String key) {
                if (key == "foo" || key == "foo2") {
                    return true;
                }

                return false;
            }
        });
        options.setRequireEncryption(true);

        this.table.execute(TableOperation.insert(ent), options, null);

        // Insert an entity when RequireEncryption is set to true but no policy is specified. This should throw.
        options.setEncryptionPolicy(null);

        try {
            this.table.execute(TableOperation.insert(ent), options, null);
            fail("Not specifying a policy when RequireEncryption is set to true should throw.");
        }
        catch (IllegalArgumentException ex) {
        }

        // Retrieve Entity
        TableRequestOptions retrieveOptions = new TableRequestOptions();
        retrieveOptions.setEncryptionPolicy(new TableEncryptionPolicy(null, resolver));
        retrieveOptions.setPropertyResolver(new PropertyResolver() {
            public EdmType propertyResolver(String pk, String rk, String key, String value) {
                if (key == "fooint") {
                    return EdmType.INT32;
                }

                return EdmType.STRING;
            }
        });
        retrieveOptions.setRequireEncryption(true);

        TableOperation operation = TableOperation.retrieve(ent.getPartitionKey(), ent.getRowKey(),
                DynamicTableEntity.class);
        TableResult result = this.table.execute(operation, retrieveOptions, null);
        DynamicTableEntity retrievedEntity = (DynamicTableEntity) result.getResult();

        // Replace entity with plain text.
        ent.setEtag(retrievedEntity.getEtag());
        this.table.execute(TableOperation.replace(ent));

        // Retrieve with RequireEncryption flag but no metadata on the service. This should throw.
        try {
            this.table.execute(operation, retrieveOptions, null);
            fail("Retrieving with RequireEncryption set to true and no metadata on the service should fail.");
        }
        catch (StorageException ex) {
        }

        // Set RequireEncryption flag to true and retrieve.
        retrieveOptions.setRequireEncryption(false);
        result = this.table.execute(operation, retrieveOptions, null);
    }

    @Test
    public void testTableQueryEncryptionMixedMode() throws StorageException, InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException {
        // insert Entity
        EncryptedClass1 ent1 = new EncryptedClass1(UUID.randomUUID().toString(), new Date().toString());
        ent1.populate();

        EncryptedClass1 ent2 = new EncryptedClass1(UUID.randomUUID().toString(), new Date().toString());
        ent2.populate();

        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();

        TableRequestOptions options = new TableRequestOptions();
        options.setEncryptionPolicy(new TableEncryptionPolicy(aesKey, null));

        // Insert an encrypted entity.
        this.table.execute(TableOperation.insert(ent1), options, null);

        // Insert a non-encrypted entity.
        this.table.execute(TableOperation.insert(ent2), null, null);

        // Create the resolver to be used for unwrapping.
        DictionaryKeyResolver resolver = new DictionaryKeyResolver();
        resolver.add(aesKey);

        options = new TableRequestOptions();
        options.setEncryptionPolicy(new TableEncryptionPolicy(null, resolver));

        // Set RequireEncryption to false and query. This will succeed.
        options.setRequireEncryption(false);
        TableQuery<EncryptedClass1> query = TableQuery.from(EncryptedClass1.class);
        this.table.execute(query, options, null).iterator().next();

        // Set RequireEncryption to true and query. This will fail because it can't find the metadata for the second enctity on the server.
        options.setRequireEncryption(true);
        try {
            this.table.execute(query, options, null).iterator().next();
            fail("All entities retrieved should be encrypted when RequireEncryption is set to true.");
        }
        catch (NoSuchElementException ex) {
            assertEquals(StorageException.class, ex.getCause().getClass());
        }
    }
    
    @Test
    public void testTableOperationEncryptionWithStrictModeOnMerge() throws InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, StorageException {
        // Insert Entity
        DynamicTableEntity ent = new DynamicTableEntity(UUID.randomUUID().toString(), new Date().toString()); 
        ent.getProperties().put("foo2", new EntityProperty(Constants.EMPTY_STRING));
        ent.getProperties().put("foo", new EntityProperty("bar"));
        ent.getProperties().put("fooint", new EntityProperty(1234));
        ent.setEtag("*");

        TableRequestOptions options = new TableRequestOptions();
        options.setRequireEncryption(true);

        try
        {
            this.table.execute(TableOperation.merge(ent), options, null);
            fail("Merge with RequireEncryption on should fail.");
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(), SR.ENCRYPTION_POLICY_MISSING_IN_STRICT_MODE);
        }

        try
        {
            this.table.execute(TableOperation.insertOrMerge(ent), options, null);
            fail("InsertOrMerge with RequireEncryption on should fail.");
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(), SR.ENCRYPTION_POLICY_MISSING_IN_STRICT_MODE);
        }

        // Create the Key to be used for wrapping.
        SymmetricKey aesKey = TestHelper.getSymmetricKey();
        options.setEncryptionPolicy(new TableEncryptionPolicy(aesKey, null));

        try
        {
            this.table.execute(TableOperation.merge(ent), options, null);
            fail("Merge with an EncryptionPolicy should fail.");
        }
        catch (StorageException ex)
        {
            assertEquals(ex.getMessage(), SR.ENCRYPTION_NOT_SUPPORTED_FOR_OPERATION);
        }

        try
        {
            this.table.execute(TableOperation.insertOrMerge(ent), options, null);
            fail("InsertOrMerge with an EncryptionPolicy should fail.");
        }
        catch (StorageException ex)
        {
            assertEquals(ex.getMessage(), SR.ENCRYPTION_NOT_SUPPORTED_FOR_OPERATION);
        }
    }
    
    @Test
    public void testTableOperationsIgnoreEncryption() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, URISyntaxException, StorageException {
        SymmetricKey aesKey = TestHelper.getSymmetricKey();
        TableRequestOptions options = new TableRequestOptions();
        options.setEncryptionPolicy(new TableEncryptionPolicy(aesKey, null));
        options.setRequireEncryption(true);
        
        CloudTableClient tableClient = TableTestHelper.createCloudTableClient();
        CloudTable testTable = TableTestHelper.getRandomTableReference();
        
        try
        {
            // Check Create()
            testTable.create(options, null);
            assertTrue("Table failed to be created when encryption policy was supplied.", testTable.exists());

            // Check Exists()
            assertTrue("Table.Exists() failed when encryption policy was supplied.", testTable.exists(options, null));

            // Check ListTables()
            for (String tableName : tableClient.listTables(testTable.getName(), options, null))
            {
                assertEquals("ListTables failed when an encryption policy was specified.", testTable.getName(), tableName);
            }
            
            // Check ListTablesSegmented()
            for (String tableName : this.listAllTables(tableClient, testTable.getName(), options))
            {
                assertEquals("ListTables failed when an encryption policy was specified.", testTable.getName(), tableName);
            }

            // Check Get and Set Permissions
            TablePermissions permissions = testTable.downloadPermissions();
            String policyName = "samplePolicy";
            SharedAccessTablePolicy tempPolicy = new SharedAccessTablePolicy();
            tempPolicy.setPermissionsFromString("r");
            tempPolicy.setSharedAccessExpiryTime(new Date());
            permissions.getSharedAccessPolicies().put(policyName, tempPolicy);
            testTable.uploadPermissions(permissions, options, null);
            assertTrue(testTable.downloadPermissions().getSharedAccessPolicies().containsKey(policyName));
            assertTrue(testTable.downloadPermissions(options, null).getSharedAccessPolicies().containsKey(policyName));

            // Check Delete
            testTable.delete(options, null);
            assertFalse(testTable.exists());
        }
        finally
        {
            testTable.deleteIfExists();
        }
    }    
    
    @Test
    public void testCrossPlatformCompatibility() throws StorageException, URISyntaxException {
        CloudTable testTable = TableTestHelper.getRandomTableReference();

        try
        {
            testTable.createIfNotExists();
            
            // Hard code some sample data, then see if we can decrypt it.
            // This key is used only for test, do not use to encrypt any sensitive data.
            SymmetricKey sampleKEK = new SymmetricKey("key1", BaseEncoding.base64().decode("rFz7+tv4hRiWdWUJMFlxl1xxtU/qFUeTriGaxwEcxjU="));

            // This data here was created using Fiddler to capture the .NET library uploading an encrypted entity, encrypted with the specified KEK and CEK.
            // Note that this data is lacking the library information in the KeyWrappingMetadata.
            DynamicTableEntity dteNetOld = new DynamicTableEntity("pk", "netUp");
            dteNetOld.getProperties().put("sampleProp", new EntityProperty(BaseEncoding.base64().decode("27cLSlSFqy9C0xUCr57XAA==")));
            dteNetOld.getProperties().put("sampleProp2", new EntityProperty(BaseEncoding.base64().decode("pZR6Ln/DwbwyyOCEezL/hg==")));
            dteNetOld.getProperties().put("sampleProp3", new EntityProperty(BaseEncoding.base64().decode("JOix4N8eX/WuCtIvlD2QxQ==")));
            dteNetOld.getProperties().put("_ClientEncryptionMetadata1", new EntityProperty("{\"WrappedContentKey\":{\"KeyId\":\"key1\",\"EncryptedKey\":\"pwSKxpJkwCS2zCaykh0m8e4OApeLuQ4FiahZ9zdwxaLL1HsWqQ4DSw==\",\"Algorithm\":\"A256KW\"},\"EncryptionAgent\":{\"Protocol\":\"1.0\",\"EncryptionAlgorithm\":\"AES_CBC_256\"},\"ContentEncryptionIV\":\"obTAQcYeFQ3IU7Jfcema7Q==\",\"KeyWrappingMetadata\":{}}"));
            dteNetOld.getProperties().put("_ClientEncryptionMetadata2", new EntityProperty(BaseEncoding.base64().decode("MWA7LlvXSJnKhf8f7MVhfjWECkxrCyCXGIlYY6ucpr34IVDU7fN6IHvKxV15WiXp")));

            testTable.execute(TableOperation.insert(dteNetOld));

            // This data here was created using Fiddler to capture the Java library uploading an encrypted entity, encrypted with the specified KEK and CEK.
            // Note that this data is lacking the KeyWrappingMetadata.  It also constructs an IV with PK + RK + column name.
            DynamicTableEntity dteJavaOld = new DynamicTableEntity("pk", "javaUp");
            dteJavaOld.getProperties().put("sampleProp", new EntityProperty(BaseEncoding.base64().decode("sa3bCvXq79ImSPveChS+cg==")));
            dteJavaOld.getProperties().put("sampleProp2", new EntityProperty(BaseEncoding.base64().decode("KXjuBNn9DesCmMcdVpamJw==")));
            dteJavaOld.getProperties().put("sampleProp3", new EntityProperty(BaseEncoding.base64().decode("wykVEni1rV+H6oNjoNml6A==")));
            dteJavaOld.getProperties().put("_ClientEncryptionMetadata1", new EntityProperty("{\"WrappedContentKey\":{\"KeyId\":\"key1\",\"EncryptedKey\":\"2F4rIuDmGPgEmhpvTtE7x6281BetKz80EsgRwGxTjL8rRt7Z7GrOgg==\",\"Algorithm\":\"A256KW\"},\"EncryptionAgent\":{\"Protocol\":\"1.0\",\"EncryptionAlgorithm\":\"AES_CBC_256\"},\"ContentEncryptionIV\":\"8st/uXffG+6DxBhw4D1URw==\"}"));
            dteJavaOld.getProperties().put("_ClientEncryptionMetadata2", new EntityProperty(BaseEncoding.base64().decode("WznUoytxkvl9KhZ4mNlqkBvRTUHN/D5IgJmNl7kQBOtFBOSgZZrTfZXKH8GjmvKA")));

            testTable.execute(TableOperation.insert(dteJavaOld));
            
            TableEncryptionPolicy policy = new TableEncryptionPolicy(sampleKEK, null);
            TableRequestOptions options = new TableRequestOptions();
            options.setEncryptionPolicy(policy);
            options.setEncryptionResolver(new EncryptionResolver() {
                public boolean encryptionResolver(String pk, String rk, String key) {
                    return true;
                }
            });

            for (DynamicTableEntity dte : testTable.execute(TableQuery.from(DynamicTableEntity.class), options, null))
            {
                assertTrue("String not properly decoded.", dte.getProperties().get("sampleProp").getValueAsString().equals("sampleValue"));
                assertTrue("String not properly decoded.", dte.getProperties().get("sampleProp2").getValueAsString().equals("sampleValue"));
                assertTrue("String not properly decoded.", dte.getProperties().get("sampleProp3").getValueAsString().equals("sampleValue"));
                assertEquals("Incorrect number or properties", dte.getProperties().size(), 3);
            }
        }
        finally
        {
            if (testTable != null) {
                testTable.deleteIfExists();
            }
        }
    }
    
    private ArrayList<String> listAllTables(CloudTableClient tableClient, String prefix, TableRequestOptions options) throws StorageException
    {
        ResultContinuation token = null;
        ArrayList<String> tables = new ArrayList<String>();
        
        do
        {
            ResultSegment<String> tableSegment = tableClient.listTablesSegmented(prefix, null, token, options, null);
            tables.addAll(tableSegment.getResults());
            token = tableSegment.getContinuationToken();
        } while (token != null);
        return tables;
    }
    
    private static DynamicTableEntity generateRandomEntity(String pk) {
        DynamicTableEntity ent = new DynamicTableEntity();
        ent.getProperties().put("foo", new EntityProperty("bar"));

        ent.setPartitionKey(pk);
        ent.setRowKey(UUID.randomUUID().toString());
        return ent;
    }
}