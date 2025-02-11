// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.connectionstring;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.SasImplUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.azure.storage.common.FakeCredentialInTest.FAKE_SIGNATURE_PLACEHOLDER;
import static com.azure.storage.common.FakeCredentialInTest.WELL_KNOWN_ACCOUNT_KEY_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StorageConnectionStringTest {
    private static final ClientLogger LOGGER = new ClientLogger(StorageConnectionStringTest.class);
    private static final String ACCOUNT_NAME_VALUE = "contoso";
    private static final String SAS_TOKEN = "sv=2015-07-08&sig=" + FAKE_SIGNATURE_PLACEHOLDER
        + "&spr=https&st=2016-04-12T03%3A24%3A31Z&se=2016-04-13T03%3A29%3A31Z&srt=s&ss=bf&sp=rwl";
    private static final String CHINA_CLOUD_ENDPOINT_SUFFIX = "core.chinacloudapi.cn";

    @Test
    public void sasToken() {
        final String blobEndpointStr = "https://storagesample.blob.core.windows.net";
        final String fileEndpointStr = "https://storagesample.file.core.windows.net";

        final String connectionString = String.format("BlobEndpoint=%s;FileEndpoint=%s;SharedAccessSignature=%s;",
            blobEndpointStr, fileEndpointStr, SAS_TOKEN);

        StorageConnectionString storageConnectionString = StorageConnectionString.create(connectionString, LOGGER);
        assertNotNull(storageConnectionString);
        StorageEndpoint blobEndpoint = storageConnectionString.getBlobEndpoint();
        assertNotNull(blobEndpoint);
        assertNotNull(blobEndpoint.getPrimaryUri());
        assertTrue(blobEndpoint.getPrimaryUri().equalsIgnoreCase(blobEndpointStr));

        StorageEndpoint fileEndpoint = storageConnectionString.getFileEndpoint();
        assertNotNull(fileEndpoint);
        assertNotNull(fileEndpoint.getPrimaryUri());
        assertTrue(fileEndpoint.getPrimaryUri().equalsIgnoreCase(fileEndpointStr));

        assertNull(storageConnectionString.getQueueEndpoint());
        assertNull(storageConnectionString.getTableEndpoint());

        StorageAuthenticationSettings authSettings = storageConnectionString.getStorageAuthSettings();
        assertNotNull(authSettings);
        assertEquals(StorageAuthenticationSettings.Type.SAS_TOKEN, authSettings.getType());
        assertNotNull(authSettings.getSasToken());
        assertSasTokensEqual(authSettings.getSasToken(), SAS_TOKEN);
        assertNull(storageConnectionString.getAccountName());
    }

    @Test
    public void sasTokenWithQuestionMark() {
        final String blobEndpointStr = "https://storagesample.blob.core.windows.net";
        final String fileEndpointStr = "https://storagesample.file.core.windows.net";

        final String connectionString = String.format("BlobEndpoint=%s;FileEndpoint=%s;SharedAccessSignature=%s;",
            blobEndpointStr, fileEndpointStr, "?" + SAS_TOKEN);

        StorageConnectionString storageConnectionString = StorageConnectionString.create(connectionString, LOGGER);
        assertNotNull(storageConnectionString);
        StorageEndpoint blobEndpoint = storageConnectionString.getBlobEndpoint();
        assertNotNull(blobEndpoint);
        assertNotNull(blobEndpoint.getPrimaryUri());
        assertTrue(blobEndpoint.getPrimaryUri().equalsIgnoreCase(blobEndpointStr));

        StorageEndpoint fileEndpoint = storageConnectionString.getFileEndpoint();
        assertNotNull(fileEndpoint);
        assertNotNull(fileEndpoint.getPrimaryUri());
        assertTrue(fileEndpoint.getPrimaryUri().equalsIgnoreCase(fileEndpointStr));

        assertNull(storageConnectionString.getQueueEndpoint());
        assertNull(storageConnectionString.getTableEndpoint());

        StorageAuthenticationSettings authSettings = storageConnectionString.getStorageAuthSettings();
        assertNotNull(authSettings);
        assertEquals(StorageAuthenticationSettings.Type.SAS_TOKEN, authSettings.getType());
        assertNotNull(authSettings.getSasToken());
        assertSasTokensEqual(authSettings.getSasToken(), SAS_TOKEN);
        assertNull(storageConnectionString.getAccountName());
    }

    private static void assertSasTokensEqual(String left, String right) {
        Map<String, String[]> leftMap = SasImplUtils.parseQueryString(left);
        Map<String, String[]> rightMap = SasImplUtils.parseQueryString(right);

        assertEquals(leftMap.keySet(), rightMap.keySet());
        for (String key : leftMap.keySet()) {
            Set<String> leftValues = new HashSet<>(Arrays.asList(leftMap.get(key)));
            Set<String> rightValues = new HashSet<>(Arrays.asList(rightMap.get(key)));
            assertEquals(leftValues, rightValues);
        }
    }

    @Test
    public void accountNameKey() {
        final String connectionString = String.format("DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;",
            ACCOUNT_NAME_VALUE, WELL_KNOWN_ACCOUNT_KEY_VALUE);

        StorageConnectionString storageConnectionString = StorageConnectionString.create(connectionString, LOGGER);
        assertNotNull(storageConnectionString);

        StorageEndpoint blobEndpoint = storageConnectionString.getBlobEndpoint();
        assertNotNull(blobEndpoint);
        assertNotNull(blobEndpoint.getPrimaryUri());
        assertTrue(
            blobEndpoint.getPrimaryUri().equalsIgnoreCase("https://" + ACCOUNT_NAME_VALUE + ".blob.core.windows.net"));

        StorageEndpoint fileEndpoint = storageConnectionString.getFileEndpoint();
        assertNotNull(fileEndpoint);
        assertNotNull(fileEndpoint.getPrimaryUri());
        assertTrue(
            fileEndpoint.getPrimaryUri().equalsIgnoreCase("https://" + ACCOUNT_NAME_VALUE + ".file.core.windows.net"));

        StorageEndpoint queueEndpoint = storageConnectionString.getQueueEndpoint();
        assertNotNull(queueEndpoint);
        assertNotNull(queueEndpoint.getPrimaryUri());
        assertTrue(queueEndpoint.getPrimaryUri()
            .equalsIgnoreCase(String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME_VALUE)));

        StorageEndpoint tableEndpoint = storageConnectionString.getTableEndpoint();
        assertNotNull(tableEndpoint);
        assertNotNull(tableEndpoint.getPrimaryUri());
        assertTrue(tableEndpoint.getPrimaryUri()
            .equalsIgnoreCase(String.format("https://%s.table.core.windows.net", ACCOUNT_NAME_VALUE)));

        StorageAuthenticationSettings authSettings = storageConnectionString.getStorageAuthSettings();
        assertNotNull(authSettings);
        assertEquals(StorageAuthenticationSettings.Type.ACCOUNT_NAME_KEY, authSettings.getType());
        assertNotNull(authSettings.getAccount());
        assertNotNull(authSettings.getAccount().getName());
        assertNotNull(authSettings.getAccount().getAccessKey());
        assertEquals(ACCOUNT_NAME_VALUE, authSettings.getAccount().getName());
        assertEquals(WELL_KNOWN_ACCOUNT_KEY_VALUE, authSettings.getAccount().getAccessKey());
    }

    @Test
    public void customEndpointSuffix() {
        final String connectionString
            = String.format("DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=%s",
                ACCOUNT_NAME_VALUE, WELL_KNOWN_ACCOUNT_KEY_VALUE, CHINA_CLOUD_ENDPOINT_SUFFIX);

        StorageConnectionString storageConnectionString = StorageConnectionString.create(connectionString, LOGGER);
        assertNotNull(storageConnectionString);

        StorageEndpoint blobEndpoint = storageConnectionString.getBlobEndpoint();
        assertNotNull(blobEndpoint);
        assertNotNull(blobEndpoint.getPrimaryUri());
        assertTrue(blobEndpoint.getPrimaryUri()
            .equalsIgnoreCase(String.format("https://%s.blob.%s", ACCOUNT_NAME_VALUE, CHINA_CLOUD_ENDPOINT_SUFFIX)));

        StorageEndpoint fileEndpoint = storageConnectionString.getFileEndpoint();
        assertNotNull(fileEndpoint);
        assertNotNull(fileEndpoint.getPrimaryUri());
        assertTrue(fileEndpoint.getPrimaryUri()
            .equalsIgnoreCase(String.format("https://%s.file.%s", ACCOUNT_NAME_VALUE, CHINA_CLOUD_ENDPOINT_SUFFIX)));

        StorageEndpoint queueEndpoint = storageConnectionString.getQueueEndpoint();
        assertNotNull(queueEndpoint);
        assertNotNull(queueEndpoint.getPrimaryUri());
        assertTrue(queueEndpoint.getPrimaryUri()
            .equalsIgnoreCase(String.format("https://%s.queue.%s", ACCOUNT_NAME_VALUE, CHINA_CLOUD_ENDPOINT_SUFFIX)));

        StorageEndpoint tableEndpoint = storageConnectionString.getTableEndpoint();
        assertNotNull(tableEndpoint);
        assertNotNull(tableEndpoint.getPrimaryUri());
        assertTrue(tableEndpoint.getPrimaryUri()
            .equalsIgnoreCase(String.format("https://%s.table.%s", ACCOUNT_NAME_VALUE, CHINA_CLOUD_ENDPOINT_SUFFIX)));

        StorageAuthenticationSettings authSettings = storageConnectionString.getStorageAuthSettings();
        assertNotNull(authSettings);
        assertEquals(StorageAuthenticationSettings.Type.ACCOUNT_NAME_KEY, authSettings.getType());
        assertNotNull(authSettings.getAccount());
        assertNotNull(authSettings.getAccount().getName());
        assertNotNull(authSettings.getAccount().getAccessKey());
        assertEquals(ACCOUNT_NAME_VALUE, authSettings.getAccount().getName());
        assertEquals(WELL_KNOWN_ACCOUNT_KEY_VALUE, authSettings.getAccount().getAccessKey());
    }

    @Test
    public void explicitEndpointsAndAccountName() {
        final String blobEndpointStr = "https://storagesample.blob.core.windows.net";
        final String fileEndpointStr = "https://storagesample.file.core.windows.net";
        final String connectionString = String.format("BlobEndpoint=%s;FileEndpoint=%s;AccountName=%s;AccountKey=%s",
            blobEndpointStr, fileEndpointStr, ACCOUNT_NAME_VALUE, WELL_KNOWN_ACCOUNT_KEY_VALUE);

        StorageConnectionString storageConnectionString = StorageConnectionString.create(connectionString, LOGGER);
        assertNotNull(storageConnectionString);
        StorageEndpoint blobEndpoint = storageConnectionString.getBlobEndpoint();
        assertNotNull(blobEndpoint);
        assertNotNull(blobEndpoint.getPrimaryUri());
        assertTrue(blobEndpoint.getPrimaryUri().equalsIgnoreCase(blobEndpointStr));

        StorageEndpoint fileEndpoint = storageConnectionString.getFileEndpoint();
        assertNotNull(fileEndpoint);
        assertNotNull(fileEndpoint.getPrimaryUri());
        assertTrue(fileEndpoint.getPrimaryUri().equalsIgnoreCase(fileEndpointStr));

        StorageEndpoint queueEndpoint = storageConnectionString.getQueueEndpoint();
        assertNotNull(queueEndpoint);
        assertNotNull(queueEndpoint.getPrimaryUri());
        assertTrue(queueEndpoint.getPrimaryUri()
            .equalsIgnoreCase("https://" + ACCOUNT_NAME_VALUE + ".queue.core.windows.net"));

        StorageEndpoint tableEndpoint = storageConnectionString.getTableEndpoint();
        assertNotNull(tableEndpoint);
        assertNotNull(tableEndpoint.getPrimaryUri());
        assertTrue(tableEndpoint.getPrimaryUri()
            .equalsIgnoreCase(String.format("https://%s.table.core.windows.net", ACCOUNT_NAME_VALUE)));

        StorageAuthenticationSettings authSettings = storageConnectionString.getStorageAuthSettings();
        assertNotNull(authSettings);
        assertEquals(StorageAuthenticationSettings.Type.ACCOUNT_NAME_KEY, authSettings.getType());
        assertNotNull(authSettings.getAccount());
        assertNotNull(authSettings.getAccount().getName());
        assertNotNull(authSettings.getAccount().getAccessKey());
        assertEquals(ACCOUNT_NAME_VALUE, authSettings.getAccount().getName());
        assertEquals(WELL_KNOWN_ACCOUNT_KEY_VALUE, authSettings.getAccount().getAccessKey());
    }

    @Test
    public void explicitEndpointsAndAnonymousAccess() {
        final String blobEndpointStr = "https://storagesample.blob.core.windows.net";
        final String fileEndpointStr = "https://storagesample.file.core.windows.net";

        String connectionString = String.format("BlobEndpoint=%s;FileEndpoint=%s;", blobEndpointStr, fileEndpointStr);

        StorageConnectionString storageConnectionString = StorageConnectionString.create(connectionString, LOGGER);
        assertNotNull(storageConnectionString);
        StorageEndpoint blobEndpoint = storageConnectionString.getBlobEndpoint();
        assertNotNull(blobEndpoint);
        assertNotNull(blobEndpoint.getPrimaryUri());
        assertTrue(blobEndpoint.getPrimaryUri().equalsIgnoreCase(blobEndpointStr));

        StorageEndpoint fileEndpoint = storageConnectionString.getFileEndpoint();
        assertNotNull(fileEndpoint);
        assertNotNull(fileEndpoint.getPrimaryUri());
        assertTrue(fileEndpoint.getPrimaryUri().equalsIgnoreCase(fileEndpointStr));

        StorageEndpoint queueEndpoint = storageConnectionString.getQueueEndpoint();
        assertNull(queueEndpoint);

        StorageEndpoint tableEndpoint = storageConnectionString.getTableEndpoint();
        assertNull(tableEndpoint);

        StorageAuthenticationSettings authSettings = storageConnectionString.getStorageAuthSettings();
        assertNotNull(authSettings);
        assertEquals(StorageAuthenticationSettings.Type.NONE, authSettings.getType());
        assertNull(authSettings.getAccount());
        assertNull(authSettings.getSasToken());
    }

    @Test
    public void skipEmptyEntries() {
        // connection string with empty entries (;; after protocol)
        final String connectionString
            = String.format("DefaultEndpointsProtocol=https;;;AccountName=%s;AccountKey=%s; EndpointSuffix=%s",
                ACCOUNT_NAME_VALUE, WELL_KNOWN_ACCOUNT_KEY_VALUE, CHINA_CLOUD_ENDPOINT_SUFFIX);

        StorageConnectionString.create(connectionString, LOGGER);
    }

    @Test
    public void nullOrEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            StorageConnectionString.create(null, LOGGER);
            StorageConnectionString.create("", LOGGER);
        });
    }

    @Test
    public void missingEqualDelimiter() {
        // A connection string with missing equal symbol between AccountKey and it's value
        final String connectionString
            = String.format("DefaultEndpointsProtocol=https;AccountName=%s;AccountKey%s;EndpointSuffix=%s",
                ACCOUNT_NAME_VALUE, WELL_KNOWN_ACCOUNT_KEY_VALUE, CHINA_CLOUD_ENDPOINT_SUFFIX);
        assertThrows(IllegalArgumentException.class, () -> StorageConnectionString.create(connectionString, LOGGER));
    }

    @Test
    public void missingKey() {
        // A connection string with missing 'AccountName' key for it's value
        final String connectionString
            = String.format("DefaultEndpointsProtocol=https;=%s;AccountKey=%s;EndpointSuffix=%s", ACCOUNT_NAME_VALUE,
                WELL_KNOWN_ACCOUNT_KEY_VALUE, CHINA_CLOUD_ENDPOINT_SUFFIX);
        assertThrows(IllegalArgumentException.class, () -> StorageConnectionString.create(connectionString, LOGGER));
    }

    @Test
    public void missingValue() {
        // A connection string with missing value for 'AccountName' key
        final String connectionString
            = String.format("DefaultEndpointsProtocol=https;AccountName=;AccountKey%s;EndpointSuffix=%s",
                WELL_KNOWN_ACCOUNT_KEY_VALUE, CHINA_CLOUD_ENDPOINT_SUFFIX);
        assertThrows(IllegalArgumentException.class, () -> StorageConnectionString.create(connectionString, LOGGER));
    }

    @Test
    public void missingKeyValue() {
        // a connection string with key and value missing for equal (=) delimiter
        final String connectionString
            = String.format("DefaultEndpointsProtocol=https;=;AccountName=%s;AccountKey%s;EndpointSuffix=%s",
                ACCOUNT_NAME_VALUE, WELL_KNOWN_ACCOUNT_KEY_VALUE, CHINA_CLOUD_ENDPOINT_SUFFIX);
        assertThrows(IllegalArgumentException.class, () -> StorageConnectionString.create(connectionString, LOGGER));
    }

    @Test
    public void missingAccountKey() {
        final String connectionString
            = String.format("DefaultEndpointsProtocol=https;AccountName=%s;%s;EndpointSuffix=%s", ACCOUNT_NAME_VALUE,
                WELL_KNOWN_ACCOUNT_KEY_VALUE, CHINA_CLOUD_ENDPOINT_SUFFIX);
        assertThrows(IllegalArgumentException.class, () -> StorageConnectionString.create(connectionString, LOGGER));
    }

    @Test
    public void sasTokenAccountKeyMutuallyExclusive() {
        final String blobEndpointStr = "https://storagesample.blob.core.windows.net";
        final String fileEndpointStr = "https://storagesample.file.core.windows.net";

        final String connectionString
            = String.format("BlobEndpoint=%s;FileEndpoint=%s;SharedAccessSignature=%s;AccountName=%s;AccountKey=%s;",
                blobEndpointStr, fileEndpointStr, SAS_TOKEN, ACCOUNT_NAME_VALUE, WELL_KNOWN_ACCOUNT_KEY_VALUE);
        assertThrows(IllegalArgumentException.class, () -> StorageConnectionString.create(connectionString, LOGGER));
    }

    @Test
    public void sasTokenAllowedWithAccountName() {
        final String blobEndpointStr = "https://storagesample.blob.core.windows.net";
        final String fileEndpointStr = "https://storagesample.file.core.windows.net";

        final String connectionString
            = String.format("BlobEndpoint=%s;FileEndpoint=%s;SharedAccessSignature=%s;AccountName=%s;", blobEndpointStr,
                fileEndpointStr, SAS_TOKEN, ACCOUNT_NAME_VALUE);
        StorageConnectionString.create(connectionString, LOGGER);
    }

    @Test
    public void overrideDefaultProtocolToHttp() {
        final String connectionString
            = String.format("DefaultEndpointsProtocol=http;AccountName=%s;AccountKey=%s;EndpointSuffix=%s",
                ACCOUNT_NAME_VALUE, WELL_KNOWN_ACCOUNT_KEY_VALUE, CHINA_CLOUD_ENDPOINT_SUFFIX);

        StorageConnectionString storageConnectionString = StorageConnectionString.create(connectionString, LOGGER);
        assertNotNull(storageConnectionString);

        StorageEndpoint blobEndpoint = storageConnectionString.getBlobEndpoint();
        assertNotNull(blobEndpoint);
        assertNotNull(blobEndpoint.getPrimaryUri());
        assertTrue(blobEndpoint.getPrimaryUri()
            .equalsIgnoreCase(String.format("http://%s.blob.%s", // http protocol
                ACCOUNT_NAME_VALUE, CHINA_CLOUD_ENDPOINT_SUFFIX)));
    }
}
