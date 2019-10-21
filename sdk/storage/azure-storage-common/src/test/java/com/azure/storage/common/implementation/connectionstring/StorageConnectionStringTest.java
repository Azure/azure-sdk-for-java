// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.connectionstring;

import com.azure.core.util.logging.ClientLogger;
import org.junit.Assert;
import org.junit.Test;

public class StorageConnectionStringTest {
    private final ClientLogger logger = new ClientLogger(StorageConnectionStringTest.class);
    private static final String ACCOUNT_NAME_VALUE = "contoso";
    private static final String ACCOUNT_KEY_VALUE =
            "95o6TL9jkIjNr6HurD6Xa+zLQ+PX9/VWR8fI2ofHatbrUb8kRJ75B6enwRU3q1OP8fmjghaoxdqnwhN7m3pZow==";
    private static final String SAS_TOKEN =
            "sv=2015-07-08&sig=iCvQmdZngZNW%2F4vw43j6%2BVz6fndHF5LI639QJba4r8o%3D&spr=https"
                    + "&st=2016-04-12T03%3A24%3A31Z"
                    + "&se=2016-04-13T03%3A29%3A31Z&srt=s&ss=bf&sp=rwl";
    private static final String CHINA_CLOUD_ENDPOINT_SUFFIX = "core.chinacloudapi.cn";

    @Test
    public void sasToken() {
        final String blobEndpointStr = "https://storagesample.blob.core.windows.net";
        final String fileEndpointStr = "https://storagesample.file.core.windows.net";

        final String connectionString = String.format("BlobEndpoint=%s;FileEndpoint=%s;SharedAccessSignature=%s;",
                blobEndpointStr,
                fileEndpointStr,
                SAS_TOKEN);

        StorageConnectionString storageConnectionString = StorageConnectionString.create(connectionString, logger);
        Assert.assertNotNull(storageConnectionString);
        StorageEndpoint blobEndpoint = storageConnectionString.getBlobEndpoint();
        Assert.assertNotNull(blobEndpoint);
        Assert.assertNotNull(blobEndpoint.getPrimaryUri());
        Assert.assertTrue(blobEndpoint.getPrimaryUri().equalsIgnoreCase(blobEndpointStr));

        StorageEndpoint fileEndpoint = storageConnectionString.getFileEndpoint();
        Assert.assertNotNull(fileEndpoint);
        Assert.assertNotNull(fileEndpoint.getPrimaryUri());
        Assert.assertTrue(fileEndpoint.getPrimaryUri().equalsIgnoreCase(fileEndpointStr));

        Assert.assertNull(storageConnectionString.getQueueEndpoint());
        Assert.assertNull(storageConnectionString.getTableEndpoint());

        StorageAuthenticationSettings authSettings
                = storageConnectionString.getStorageAuthSettings();
        Assert.assertNotNull(authSettings);
        Assert.assertEquals(StorageAuthenticationSettings.Type.SAS_TOKEN,
                authSettings.getType());
        Assert.assertNotNull(authSettings.getSasToken());
        Assert.assertTrue(authSettings.getSasToken().equalsIgnoreCase(SAS_TOKEN));
        Assert.assertNull(storageConnectionString.getAccountName());
    }

    @Test
    public void accountNameKey() {
        final String connectionString = String.format("DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;",
                ACCOUNT_NAME_VALUE,
                ACCOUNT_KEY_VALUE);

        StorageConnectionString storageConnectionString = StorageConnectionString.create(connectionString, logger);
        Assert.assertNotNull(storageConnectionString);

        StorageEndpoint blobEndpoint = storageConnectionString.getBlobEndpoint();
        Assert.assertNotNull(blobEndpoint);
        Assert.assertNotNull(blobEndpoint.getPrimaryUri());
        Assert.assertTrue(blobEndpoint.getPrimaryUri()
                .equalsIgnoreCase(String.format("https://%s.blob.core.windows.net", ACCOUNT_NAME_VALUE)));

        StorageEndpoint fileEndpoint = storageConnectionString.getFileEndpoint();
        Assert.assertNotNull(fileEndpoint);
        Assert.assertNotNull(fileEndpoint.getPrimaryUri());
        Assert.assertTrue(fileEndpoint.getPrimaryUri()
                .equalsIgnoreCase(String.format("https://%s.file.core.windows.net", ACCOUNT_NAME_VALUE)));

        StorageEndpoint queueEndpoint = storageConnectionString.getQueueEndpoint();
        Assert.assertNotNull(queueEndpoint);
        Assert.assertNotNull(queueEndpoint.getPrimaryUri());
        Assert.assertTrue(queueEndpoint.getPrimaryUri()
                .equalsIgnoreCase(String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME_VALUE)));

        StorageEndpoint tableEndpoint = storageConnectionString.getTableEndpoint();
        Assert.assertNotNull(tableEndpoint);
        Assert.assertNotNull(tableEndpoint.getPrimaryUri());
        Assert.assertTrue(tableEndpoint.getPrimaryUri()
                .equalsIgnoreCase(String.format("https://%s.table.core.windows.net", ACCOUNT_NAME_VALUE)));

        StorageAuthenticationSettings authSettings = storageConnectionString.getStorageAuthSettings();
        Assert.assertNotNull(authSettings);
        Assert.assertEquals(StorageAuthenticationSettings.Type.ACCOUNT_NAME_KEY,
                authSettings.getType());
        Assert.assertNotNull(authSettings.getAccount());
        Assert.assertNotNull(authSettings.getAccount().getName());
        Assert.assertNotNull(authSettings.getAccount().getAccessKey());
        Assert.assertTrue(authSettings.getAccount().getName().equals(ACCOUNT_NAME_VALUE));
        Assert.assertTrue(authSettings.getAccount().getAccessKey().equals(ACCOUNT_KEY_VALUE));
    }

    @Test
    public void customEndpointSuffix() {
        final String connectionString =
                String.format("DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=%s",
                        ACCOUNT_NAME_VALUE,
                        ACCOUNT_KEY_VALUE,
                        CHINA_CLOUD_ENDPOINT_SUFFIX);

        StorageConnectionString storageConnectionString = StorageConnectionString.create(connectionString, logger);
        Assert.assertNotNull(storageConnectionString);

        StorageEndpoint blobEndpoint = storageConnectionString.getBlobEndpoint();
        Assert.assertNotNull(blobEndpoint);
        Assert.assertNotNull(blobEndpoint.getPrimaryUri());
        Assert.assertTrue(blobEndpoint.getPrimaryUri()
                .equalsIgnoreCase(String.format("https://%s.blob.%s",
                        ACCOUNT_NAME_VALUE,
                        CHINA_CLOUD_ENDPOINT_SUFFIX)));

        StorageEndpoint fileEndpoint = storageConnectionString.getFileEndpoint();
        Assert.assertNotNull(fileEndpoint);
        Assert.assertNotNull(fileEndpoint.getPrimaryUri());
        Assert.assertTrue(fileEndpoint.getPrimaryUri()
                .equalsIgnoreCase(String.format("https://%s.file.%s",
                        ACCOUNT_NAME_VALUE,
                        CHINA_CLOUD_ENDPOINT_SUFFIX)));

        StorageEndpoint queueEndpoint = storageConnectionString.getQueueEndpoint();
        Assert.assertNotNull(queueEndpoint);
        Assert.assertNotNull(queueEndpoint.getPrimaryUri());
        Assert.assertTrue(queueEndpoint.getPrimaryUri()
                .equalsIgnoreCase(String.format("https://%s.queue.%s",
                        ACCOUNT_NAME_VALUE,
                        CHINA_CLOUD_ENDPOINT_SUFFIX)));

        StorageEndpoint tableEndpoint = storageConnectionString.getTableEndpoint();
        Assert.assertNotNull(tableEndpoint);
        Assert.assertNotNull(tableEndpoint.getPrimaryUri());
        Assert.assertTrue(tableEndpoint.getPrimaryUri()
                .equalsIgnoreCase(String.format("https://%s.table.%s",
                        ACCOUNT_NAME_VALUE,
                        CHINA_CLOUD_ENDPOINT_SUFFIX)));

        StorageAuthenticationSettings authSettings =
                storageConnectionString.getStorageAuthSettings();
        Assert.assertNotNull(authSettings);
        Assert.assertEquals(StorageAuthenticationSettings.Type.ACCOUNT_NAME_KEY,
                authSettings.getType());
        Assert.assertNotNull(authSettings.getAccount());
        Assert.assertNotNull(authSettings.getAccount().getName());
        Assert.assertNotNull(authSettings.getAccount().getAccessKey());
        Assert.assertTrue(authSettings.getAccount().getName().equals(ACCOUNT_NAME_VALUE));
        Assert.assertTrue(authSettings.getAccount().getAccessKey().equals(ACCOUNT_KEY_VALUE));
    }

    @Test
    public void explicitEndpointsAndAccountName() {
        final String blobEndpointStr = "https://storagesample.blob.core.windows.net";
        final String fileEndpointStr = "https://storagesample.file.core.windows.net";
        final String connectionString = String.format("BlobEndpoint=%s;FileEndpoint=%s;AccountName=%s;AccountKey=%s",
                blobEndpointStr,
                fileEndpointStr,
                ACCOUNT_NAME_VALUE,
                ACCOUNT_KEY_VALUE);

        StorageConnectionString storageConnectionString = StorageConnectionString.create(connectionString, logger);
        Assert.assertNotNull(storageConnectionString);
        StorageEndpoint blobEndpoint = storageConnectionString.getBlobEndpoint();
        Assert.assertNotNull(blobEndpoint);
        Assert.assertNotNull(blobEndpoint.getPrimaryUri());
        Assert.assertTrue(blobEndpoint.getPrimaryUri().equalsIgnoreCase(blobEndpointStr));

        StorageEndpoint fileEndpoint = storageConnectionString.getFileEndpoint();
        Assert.assertNotNull(fileEndpoint);
        Assert.assertNotNull(fileEndpoint.getPrimaryUri());
        Assert.assertTrue(fileEndpoint.getPrimaryUri().equalsIgnoreCase(fileEndpointStr));

        StorageEndpoint queueEndpoint = storageConnectionString.getQueueEndpoint();
        Assert.assertNotNull(queueEndpoint);
        Assert.assertNotNull(queueEndpoint.getPrimaryUri());
        Assert.assertTrue(queueEndpoint.getPrimaryUri()
                .equalsIgnoreCase(String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME_VALUE)));

        StorageEndpoint tableEndpoint = storageConnectionString.getTableEndpoint();
        Assert.assertNotNull(tableEndpoint);
        Assert.assertNotNull(tableEndpoint.getPrimaryUri());
        Assert.assertTrue(tableEndpoint.getPrimaryUri()
                .equalsIgnoreCase(String.format("https://%s.table.core.windows.net", ACCOUNT_NAME_VALUE)));

        StorageAuthenticationSettings authSettings =
                storageConnectionString.getStorageAuthSettings();
        Assert.assertNotNull(authSettings);
        Assert.assertEquals(StorageAuthenticationSettings.Type.ACCOUNT_NAME_KEY,
                authSettings.getType());
        Assert.assertNotNull(authSettings.getAccount());
        Assert.assertNotNull(authSettings.getAccount().getName());
        Assert.assertNotNull(authSettings.getAccount().getAccessKey());
        Assert.assertTrue(authSettings.getAccount().getName().equals(ACCOUNT_NAME_VALUE));
        Assert.assertTrue(authSettings.getAccount().getAccessKey().equals(ACCOUNT_KEY_VALUE));
    }

    @Test
    public void explicitEndpointsAndAnonymousAccess() {
        final String blobEndpointStr = "https://storagesample.blob.core.windows.net";
        final String fileEndpointStr = "https://storagesample.file.core.windows.net";

        final String connectionString = String.format("BlobEndpoint=%s;FileEndpoint=%s;",
                blobEndpointStr,
                fileEndpointStr);

        StorageConnectionString storageConnectionString = StorageConnectionString.create(connectionString, logger);
        Assert.assertNotNull(storageConnectionString);
        StorageEndpoint blobEndpoint = storageConnectionString.getBlobEndpoint();
        Assert.assertNotNull(blobEndpoint);
        Assert.assertNotNull(blobEndpoint.getPrimaryUri());
        Assert.assertTrue(blobEndpoint.getPrimaryUri().equalsIgnoreCase(blobEndpointStr));

        StorageEndpoint fileEndpoint = storageConnectionString.getFileEndpoint();
        Assert.assertNotNull(fileEndpoint);
        Assert.assertNotNull(fileEndpoint.getPrimaryUri());
        Assert.assertTrue(fileEndpoint.getPrimaryUri().equalsIgnoreCase(fileEndpointStr));

        StorageEndpoint queueEndpoint = storageConnectionString.getQueueEndpoint();
        Assert.assertNull(queueEndpoint);

        StorageEndpoint tableEndpoint = storageConnectionString.getTableEndpoint();
        Assert.assertNull(tableEndpoint);

        StorageAuthenticationSettings authSettings
                = storageConnectionString.getStorageAuthSettings();
        Assert.assertNotNull(authSettings);
        Assert.assertEquals(StorageAuthenticationSettings.Type.NONE,
                authSettings.getType());
        Assert.assertNull(authSettings.getAccount());
        Assert.assertNull(authSettings.getSasToken());
    }

    @Test
    public void skipEmptyEntries() {
        // connection string with empty entries (;; after protocol)
        final String connectionString =
                String.format("DefaultEndpointsProtocol=https;;;AccountName=%s;AccountKey=%s; EndpointSuffix=%s",
                ACCOUNT_NAME_VALUE, ACCOUNT_KEY_VALUE, CHINA_CLOUD_ENDPOINT_SUFFIX);

        StorageConnectionString.create(connectionString, logger);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullOrEmpty() {
        StorageConnectionString.create(null, logger);
        StorageConnectionString.create("", logger);
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingEqualDelimiter() {
        // A connection string with missing equal symbol between AccountKey and it's value
        final String connectionString =
                String.format("DefaultEndpointsProtocol=https;AccountName=%s;AccountKey%s;EndpointSuffix=%s",
                        ACCOUNT_NAME_VALUE, ACCOUNT_KEY_VALUE, CHINA_CLOUD_ENDPOINT_SUFFIX);
        StorageConnectionString.create(connectionString, logger);
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingKey() {
        // A connection string with missing 'AccountName' key for it's value
        final String connectionString =
                String.format("DefaultEndpointsProtocol=https;=%s;AccountKey=%s;EndpointSuffix=%s",
                        ACCOUNT_NAME_VALUE, ACCOUNT_KEY_VALUE, CHINA_CLOUD_ENDPOINT_SUFFIX);
        StorageConnectionString.create(connectionString, logger);
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingValue() {
        // A connection string with missing value for 'AccountName' key
        final String connectionString =
                String.format("DefaultEndpointsProtocol=https;AccountName=;AccountKey%s;EndpointSuffix=%s",
                        ACCOUNT_KEY_VALUE, CHINA_CLOUD_ENDPOINT_SUFFIX);
        StorageConnectionString.create(connectionString, logger);
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingKeyValue() {
        // a connection string with key and value missing for equal (=) delimiter
        final String connectionString =
                String.format("DefaultEndpointsProtocol=https;=;AccountName=%s;AccountKey%s;EndpointSuffix=%s",
                        ACCOUNT_NAME_VALUE, ACCOUNT_KEY_VALUE, CHINA_CLOUD_ENDPOINT_SUFFIX);
        StorageConnectionString.create(connectionString, logger);
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingAccountKey() {
        final String connectionString =
                String.format("DefaultEndpointsProtocol=https;AccountName=%s;%s;EndpointSuffix=%s",
                        ACCOUNT_NAME_VALUE, ACCOUNT_KEY_VALUE, CHINA_CLOUD_ENDPOINT_SUFFIX);
        StorageConnectionString.create(connectionString, logger);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sasTokenAccountKeyMutuallyExclusive() {
        final String blobEndpointStr = "https://storagesample.blob.core.windows.net";
        final String fileEndpointStr = "https://storagesample.file.core.windows.net";

        final String connectionString =
                String.format("BlobEndpoint=%s;FileEndpoint=%s;SharedAccessSignature=%s;AccountName=%s;AccountKey=%s;",
                        blobEndpointStr,
                        fileEndpointStr,
                        SAS_TOKEN,
                        ACCOUNT_NAME_VALUE,
                        ACCOUNT_KEY_VALUE);
        StorageConnectionString.create(connectionString, logger);
    }

    @Test
    public void sasTokenAllowedWithAccountName() {
        final String blobEndpointStr = "https://storagesample.blob.core.windows.net";
        final String fileEndpointStr = "https://storagesample.file.core.windows.net";

        final String connectionString =
                String.format("BlobEndpoint=%s;FileEndpoint=%s;SharedAccessSignature=%s;AccountName=%s;",
                        blobEndpointStr,
                        fileEndpointStr,
                        SAS_TOKEN,
                        ACCOUNT_NAME_VALUE,
                        ACCOUNT_KEY_VALUE);
        StorageConnectionString.create(connectionString, logger);
    }

    @Test
    public void overrideDefaultProtocolToHttp() {
        final String connectionString =
                String.format("DefaultEndpointsProtocol=http;AccountName=%s;AccountKey=%s;EndpointSuffix=%s",
                        ACCOUNT_NAME_VALUE,
                        ACCOUNT_KEY_VALUE,
                        CHINA_CLOUD_ENDPOINT_SUFFIX);

        StorageConnectionString storageConnectionString = StorageConnectionString.create(connectionString, logger);
        Assert.assertNotNull(storageConnectionString);

        StorageEndpoint blobEndpoint = storageConnectionString.getBlobEndpoint();
        Assert.assertNotNull(blobEndpoint);
        Assert.assertNotNull(blobEndpoint.getPrimaryUri());
        Assert.assertTrue(blobEndpoint.getPrimaryUri()
                .equalsIgnoreCase(String.format("http://%s.blob.%s", // http protocol
                        ACCOUNT_NAME_VALUE,
                        CHINA_CLOUD_ENDPOINT_SUFFIX)));
    }
}
