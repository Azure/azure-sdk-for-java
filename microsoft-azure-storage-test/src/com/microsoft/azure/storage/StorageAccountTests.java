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
package com.microsoft.azure.storage;

import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.core.Base64;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.CloudFileShare;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueClient;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.TestRunners.CloudTests;
import com.microsoft.azure.storage.TestRunners.DevFabricTests;
import com.microsoft.azure.storage.TestRunners.DevStoreTests;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Locale;
import java.util.UUID;

import static org.junit.Assert.*;


@Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
public class StorageAccountTests {

    public static final String ACCOUNT_NAME = UUID.randomUUID().toString();
    public static final String ACCOUNT_KEY = Base64.encode(UUID.randomUUID().toString().getBytes());

    @Test
    public void testStorageCredentialsAnonymous() throws URISyntaxException, StorageException {
        StorageCredentials cred = StorageCredentialsAnonymous.ANONYMOUS;

        assertNull(cred.getAccountName());

        URI testUri = new URI("http://test/abc?querya=1");
        assertEquals(testUri, cred.transformUri(testUri));
    }

    @Test
    public void testStorageCredentialsSharedKey() throws URISyntaxException, StorageException {
        StorageCredentialsAccountAndKey cred = new StorageCredentialsAccountAndKey(ACCOUNT_NAME, ACCOUNT_KEY);

        assertEquals(ACCOUNT_NAME, cred.getAccountName());

        URI testUri = new URI("http://test/abc?querya=1");
        assertEquals(testUri, cred.transformUri(testUri));

        assertEquals(ACCOUNT_KEY, cred.exportBase64EncodedKey());
        byte[] dummyKey = { 0, 1, 2 };
        String base64EncodedDummyKey = Base64.encode(dummyKey);
        cred = new StorageCredentialsAccountAndKey(ACCOUNT_NAME, base64EncodedDummyKey);
        assertEquals(base64EncodedDummyKey, cred.exportBase64EncodedKey());

        dummyKey[0] = 3;
        base64EncodedDummyKey = Base64.encode(dummyKey);
        cred = new StorageCredentialsAccountAndKey(ACCOUNT_NAME, base64EncodedDummyKey);
        assertEquals(base64EncodedDummyKey, cred.exportBase64EncodedKey());
    }

    @Test
    public void testStorageCredentialsSharedKeyUpdateKey() throws URISyntaxException, StorageException {
        StorageCredentialsAccountAndKey cred = new StorageCredentialsAccountAndKey(ACCOUNT_NAME, ACCOUNT_KEY);
        assertEquals(ACCOUNT_KEY, cred.exportBase64EncodedKey());

        // Validate update with byte array
        byte[] dummyKey = { 0, 1, 2 };
        cred.updateKey(dummyKey);
        String base64EncodedDummyKey = Base64.encode(dummyKey);
        assertEquals(base64EncodedDummyKey, cred.exportBase64EncodedKey());

        // Validate update with string
        dummyKey[0] = 3;
        base64EncodedDummyKey = Base64.encode(dummyKey);
        cred.updateKey(base64EncodedDummyKey);
        assertEquals(base64EncodedDummyKey, cred.exportBase64EncodedKey());
    }

    @Test
    public void testStorageCredentialsSAS() throws URISyntaxException, StorageException {
        String token = "?sig=1&sp=abcde&api-version=" + Constants.HeaderConstants.TARGET_STORAGE_VERSION;
        StorageCredentialsSharedAccessSignature cred = new StorageCredentialsSharedAccessSignature(token);
        assertNull(cred.getAccountName());

        URI testUri = new URI("http://test/abc" + token);
        TestHelper.assertURIsEqual(testUri, cred.transformUri(testUri), true);

        testUri = new URI("http://test/abc?query=a&query2=b");
        URI expectedUri = new URI("http://test/abc?sig=1&query=a&sp=abcde&query2=b&api-version="
                + Constants.HeaderConstants.TARGET_STORAGE_VERSION);
        TestHelper.assertURIsEqual(expectedUri, cred.transformUri(testUri), true);
    }

    @Test
    public void testStorageCredentialsEmptyKeyValue() throws URISyntaxException, InvalidKeyException {
        String emptyKeyValueAsString = "";
        String emptyKeyConnectionString = String.format(Locale.US,
                "DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=", ACCOUNT_NAME);

        try {
            new StorageCredentialsAccountAndKey(ACCOUNT_NAME, emptyKeyValueAsString);
            fail("Did not hit expected exception");
        }
        catch (IllegalArgumentException ex) {
            assertEquals(SR.INVALID_KEY, ex.getMessage());
        }

        try {
            CloudStorageAccount.parse(emptyKeyConnectionString);
            fail("Did not hit expected exception");
        }
        catch (IllegalArgumentException ex) {
            assertEquals(SR.INVALID_CONNECTION_STRING, ex.getMessage());
        }

        try {
            byte[] emptyKeyValueAsByteArray = new byte[0];
            new StorageCredentialsAccountAndKey(ACCOUNT_NAME, emptyKeyValueAsByteArray);
            fail("Did not hit expected exception");
        }
        catch (IllegalArgumentException ex) {
            assertEquals(SR.INVALID_KEY, ex.getMessage());
        }
    }

    @Test
    public void testStorageCredentialsNullKeyValue() {
        String nullKeyValueAsString = null;

        try {
            new StorageCredentialsAccountAndKey(ACCOUNT_NAME, nullKeyValueAsString);
            fail("Did not hit expected exception");
        }
        catch (IllegalArgumentException ex) {
            assertEquals(SR.STRING_NOT_VALID, ex.getMessage());
        }

        StorageCredentialsAccountAndKey credentials2 = new StorageCredentialsAccountAndKey(ACCOUNT_NAME, ACCOUNT_KEY);
        assertEquals(ACCOUNT_NAME, credentials2.getAccountName());
        assertEquals(ACCOUNT_KEY, Base64.encode(credentials2.exportKey()));

        byte[] nullKeyValueAsByteArray = null;
        try {
            new StorageCredentialsAccountAndKey(ACCOUNT_NAME, nullKeyValueAsByteArray);
            fail("Did not hit expected exception");
        }
        catch (IllegalArgumentException ex) {
            assertEquals(SR.INVALID_KEY, ex.getMessage());
        }
    }

    private void AccountsAreEqual(CloudStorageAccount a, CloudStorageAccount b) {
        // endpoints are the same
        assertEquals(a.getBlobEndpoint(), b.getBlobEndpoint());
        assertEquals(a.getQueueEndpoint(), b.getQueueEndpoint());
        assertEquals(a.getTableEndpoint(), b.getTableEndpoint());
        assertEquals(a.getFileEndpoint(), b.getFileEndpoint());

        // storage uris are the same
        assertEquals(a.getBlobStorageUri(), b.getBlobStorageUri());
        assertEquals(a.getQueueStorageUri(), b.getQueueStorageUri());
        assertEquals(a.getTableStorageUri(), b.getTableStorageUri());
        assertEquals(a.getFileStorageUri(), b.getFileStorageUri());

        // seralized representatons are the same.
        String aToStringNoSecrets = a.toString();
        String aToStringWithSecrets = a.toString(true);
        String bToStringNoSecrets = b.toString(false);
        String bToStringWithSecrets = b.toString(true);
        assertEquals(aToStringNoSecrets, bToStringNoSecrets);
        assertEquals(aToStringWithSecrets, bToStringWithSecrets);

        // credentials are the same
        if (a.getCredentials() != null && b.getCredentials() != null) {
            assertEquals(a.getCredentials().getClass(), b.getCredentials().getClass());
        }
        else if (a.getCredentials() == null && b.getCredentials() == null) {
            return;
        }
        else {
            fail("credentials mismatch");
        }
    }

    @Test
    public void testCloudStorageAccountDevelopmentStorageAccount() throws InvalidKeyException, URISyntaxException {
        CloudStorageAccount devstoreAccount = CloudStorageAccount.getDevelopmentStorageAccount();
        assertEquals(devstoreAccount.getBlobStorageUri().getPrimaryUri(), new URI(
                "http://127.0.0.1:10000/devstoreaccount1"));
        assertEquals(devstoreAccount.getQueueStorageUri().getPrimaryUri(), new URI(
                "http://127.0.0.1:10001/devstoreaccount1"));
        assertEquals(devstoreAccount.getTableStorageUri().getPrimaryUri(), new URI(
                "http://127.0.0.1:10002/devstoreaccount1"));

        assertEquals(devstoreAccount.getBlobStorageUri().getSecondaryUri(), new URI(
                "http://127.0.0.1:10000/devstoreaccount1-secondary"));
        assertEquals(devstoreAccount.getQueueStorageUri().getSecondaryUri(), new URI(
                "http://127.0.0.1:10001/devstoreaccount1-secondary"));
        assertEquals(devstoreAccount.getTableStorageUri().getSecondaryUri(), new URI(
                "http://127.0.0.1:10002/devstoreaccount1-secondary"));

        String devstoreAccountToStringWithSecrets = devstoreAccount.toString(true);
        CloudStorageAccount testAccount = CloudStorageAccount.parse(devstoreAccountToStringWithSecrets);

        AccountsAreEqual(testAccount, devstoreAccount);
        // Following should not throw exception:
        CloudStorageAccount.parse(devstoreAccountToStringWithSecrets);
    }

    @Test
    public void testCloudStorageAccountDefaultStorageAccountWithHttp() throws URISyntaxException, InvalidKeyException {
        StorageCredentialsAccountAndKey cred = new StorageCredentialsAccountAndKey(ACCOUNT_NAME, ACCOUNT_KEY);
        CloudStorageAccount cloudStorageAccount = new CloudStorageAccount(cred, false);
        assertEquals(cloudStorageAccount.getBlobEndpoint(),
                new URI(String.format("http://%s.blob.core.windows.net", ACCOUNT_NAME)));
        assertEquals(cloudStorageAccount.getQueueEndpoint(),
                new URI(String.format("http://%s.queue.core.windows.net", ACCOUNT_NAME)));
        assertEquals(cloudStorageAccount.getTableEndpoint(),
                new URI(String.format("http://%s.table.core.windows.net", ACCOUNT_NAME)));
        assertEquals(cloudStorageAccount.getFileEndpoint(),
                new URI(String.format("http://%s.file.core.windows.net", ACCOUNT_NAME)));
        assertEquals(cloudStorageAccount.getBlobStorageUri().getSecondaryUri(),
                new URI(String.format("http://%s-secondary.blob.core.windows.net", ACCOUNT_NAME)));
        assertEquals(cloudStorageAccount.getQueueStorageUri().getSecondaryUri(),
                new URI(String.format("http://%s-secondary.queue.core.windows.net", ACCOUNT_NAME)));
        assertEquals(cloudStorageAccount.getTableStorageUri().getSecondaryUri(),
                new URI(String.format("http://%s-secondary.table.core.windows.net", ACCOUNT_NAME)));
        assertEquals(cloudStorageAccount.getFileStorageUri().getSecondaryUri(),
                new URI(String.format("http://%s-secondary.file.core.windows.net", ACCOUNT_NAME)));

        String cloudStorageAccountToStringWithSecrets = cloudStorageAccount.toString(true);
        CloudStorageAccount testAccount = CloudStorageAccount.parse(cloudStorageAccountToStringWithSecrets);

        AccountsAreEqual(testAccount, cloudStorageAccount);
    }

    @Test
    public void testCloudStorageAccountDefaultStorageAccountWithHttps() throws URISyntaxException, InvalidKeyException {
        StorageCredentialsAccountAndKey cred = new StorageCredentialsAccountAndKey(ACCOUNT_NAME, ACCOUNT_KEY);
        CloudStorageAccount cloudStorageAccount = new CloudStorageAccount(cred, true);
        assertEquals(cloudStorageAccount.getBlobEndpoint(),
                new URI(String.format("https://%s.blob.core.windows.net", ACCOUNT_NAME)));
        assertEquals(cloudStorageAccount.getQueueEndpoint(),
                new URI(String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME)));
        assertEquals(cloudStorageAccount.getTableEndpoint(),
                new URI(String.format("https://%s.table.core.windows.net", ACCOUNT_NAME)));
        assertEquals(cloudStorageAccount.getFileEndpoint(),
                new URI(String.format("https://%s.file.core.windows.net", ACCOUNT_NAME)));
        assertEquals(cloudStorageAccount.getBlobStorageUri().getSecondaryUri(),
                new URI(String.format("https://%s-secondary.blob.core.windows.net", ACCOUNT_NAME)));
        assertEquals(cloudStorageAccount.getQueueStorageUri().getSecondaryUri(),
                new URI(String.format("https://%s-secondary.queue.core.windows.net", ACCOUNT_NAME)));
        assertEquals(cloudStorageAccount.getTableStorageUri().getSecondaryUri(),
                new URI(String.format("https://%s-secondary.table.core.windows.net", ACCOUNT_NAME)));
        assertEquals(cloudStorageAccount.getFileStorageUri().getSecondaryUri(),
                new URI(String.format("https://%s-secondary.file.core.windows.net", ACCOUNT_NAME)));

        String cloudStorageAccountToStringWithSecrets = cloudStorageAccount.toString(true);
        CloudStorageAccount testAccount = CloudStorageAccount.parse(cloudStorageAccountToStringWithSecrets);

        AccountsAreEqual(testAccount, cloudStorageAccount);
    }

    @Test
    public void testCloudStorageAccountConnectionStringRoundtrip() throws InvalidKeyException, URISyntaxException {
        Object[] accountKeyParams = new String[]
        {
            ACCOUNT_NAME,
            ACCOUNT_KEY,
            "fake.endpoint.suffix",
            "https://primary.endpoint/",
            "https://secondary.endpoint/"
        };

        Object[] accountSasParams = new String[]
        {
            ACCOUNT_NAME,
            "sasTest",
            "fake.endpoint.suffix",
            "https://primary.endpoint/",
            "https://secondary.endpoint/"
        };

        // account key

        String accountString1 =
                String.format(
                        "DefaultEndpointsProtocol=http;AccountName=%1$s;AccountKey=%2$s;EndpointSuffix=%3$s;",
                        accountKeyParams);

        String accountString2 =
                String.format(
                        "DefaultEndpointsProtocol=https;AccountName=%1$s;AccountKey=%2$s;",
                        accountKeyParams);

        String accountString3 =
                String.format(
                        "DefaultEndpointsProtocol=https;AccountName=%1$s;AccountKey=%2$s;QueueEndpoint=%4$s",
                        accountKeyParams);

        String accountString4 =
                String.format(
                        "DefaultEndpointsProtocol=https;AccountName=%1$s;AccountKey=%2$s;EndpointSuffix=%3$s;QueueEndpoint=%4$s",
                        accountKeyParams);

        connectionStringRoundtripHelper(accountString1);
        connectionStringRoundtripHelper(accountString2);
        connectionStringRoundtripHelper(accountString3);
        connectionStringRoundtripHelper(accountString4);

        String accountString5 =
                String.format(
                        "AccountName=%1$s;AccountKey=%2$s;EndpointSuffix=%3$s;",
                        accountKeyParams);

        String accountString6 =
                String.format(
                        "AccountName=%1$s;AccountKey=%2$s;",
                        accountKeyParams);

        String accountString7 =
                String.format(
                        "AccountName=%1$s;AccountKey=%2$s;QueueEndpoint=%4$s",
                        accountKeyParams);

        String accountString8 =
                String.format(
                        "AccountName=%1$s;AccountKey=%2$s;EndpointSuffix=%3$s;QueueEndpoint=%4$s",
                        accountKeyParams);

        connectionStringRoundtripHelper(accountString5);
        connectionStringRoundtripHelper(accountString6);
        connectionStringRoundtripHelper(accountString7);
        connectionStringRoundtripHelper(accountString8);

        // shared access

        String accountString9 =
                String.format(
                        "DefaultEndpointsProtocol=http;AccountName=%1$s;SharedAccessSignature=%2$s;EndpointSuffix=%3$s;",
                        accountSasParams);

        String accountString10 =
                String.format(
                        "DefaultEndpointsProtocol=https;AccountName=%1$s;SharedAccessSignature=%2$s;",
                        accountSasParams);

        String accountString11 =
                String.format(
                        "DefaultEndpointsProtocol=https;AccountName=%1$s;SharedAccessSignature=%2$s;QueueEndpoint=%4$s",
                        accountSasParams);

        String accountString12 =
                String.format(
                        "DefaultEndpointsProtocol=https;AccountName=%1$s;SharedAccessSignature=%2$s;EndpointSuffix=%3$s;QueueEndpoint=%4$s",
                        accountSasParams);

        connectionStringRoundtripHelper(accountString9);
        connectionStringRoundtripHelper(accountString10);
        connectionStringRoundtripHelper(accountString11);
        connectionStringRoundtripHelper(accountString12);

        String accountString13 =
                String.format(
                        "AccountName=%1$s;SharedAccessSignature=%2$s;EndpointSuffix=%3$s;",
                        accountSasParams);

        String accountString14 =
                String.format(
                        "AccountName=%1$s;SharedAccessSignature=%2$s;",
                        accountSasParams);

        String accountString15 =
                String.format(
                        "AccountName=%1$s;SharedAccessSignature=%2$s;QueueEndpoint=%4$s",
                        accountSasParams);

        String accountString16 =
                String.format(
                        "AccountName=%1$s;SharedAccessSignature=%2$s;EndpointSuffix=%3$s;QueueEndpoint=%4$s",
                        accountSasParams);

        connectionStringRoundtripHelper(accountString13);
        connectionStringRoundtripHelper(accountString14);
        connectionStringRoundtripHelper(accountString15);
        connectionStringRoundtripHelper(accountString16);

        // shared access no account name

        String accountString17 =
                String.format(
                        "SharedAccessSignature=%2$s;QueueEndpoint=%4$s",
                        accountSasParams);

        connectionStringRoundtripHelper(accountString17);
    }

    @Test
    public void CloudStorageAccountConnectionStringExpectedExceptions()
    {
        String[][] endpointCombinations = new String[][]
        {
            new String[] { "BlobEndpoint=%4$s", "BlobSecondaryEndpoint=%5$s", "BlobEndpoint=%4$s;BlobSecondaryEndpoint=%5$s" },
            new String[] { "QueueEndpoint=%4$s", "QueueSecondaryEndpoint=%5$s", "QueueEndpoint=%4$s;QueueSecondaryEndpoint=%5$s" },
            new String[] { "TableEndpoint=%4$s", "TableSecondaryEndpoint=%5$s", "TableEndpoint=%4$s;TableSecondaryEndpoint=%5$s" },
            new String[] { "FileEndpoint=%4$s", "FileSecondaryEndpoint=%5$s", "FileEndpoint=%4$s;FileSecondaryEndpoint=%5$s" }
        };

        Object[] accountKeyParams = new String[]
                {
                        ACCOUNT_NAME,
                        ACCOUNT_KEY,
                        "fake.endpoint.suffix",
                        "https://primary.endpoint/",
                        "https://secondary.endpoint/"
                };

        Object[] accountSasParams = new String[]
                {
                        ACCOUNT_NAME,
                        "sasTest",
                        "fake.endpoint.suffix",
                        "https://primary.endpoint/",
                        "https://secondary.endpoint/"
                };

        for (String[] endpointCombination: endpointCombinations)
        {
            // account key

            String accountStringKeyPrimary =
                    String.format(
                            "DefaultEndpointsProtocol=https;AccountName=%1$s;AccountKey=%2$s;EndpointSuffix=%3$s;" + endpointCombination[0],
                            accountKeyParams
                    );

            String accountStringKeySecondary =
                    String.format(
                            "DefaultEndpointsProtocol=https;AccountName=%1$s;AccountKey=%2$s;EndpointSuffix=%3$s;" + endpointCombination[1],
                            accountKeyParams
                    );

            String accountStringKeyPrimarySecondary =
                    String.format(
                            "DefaultEndpointsProtocol=https;AccountName=%1$s;AccountKey=%2$s;EndpointSuffix=%3$s;" + endpointCombination[2],
                            accountKeyParams
                    );

            try {
                CloudStorageAccount.parse(accountStringKeyPrimary); // no exception expected
            } catch (Exception e) {
                fail("Unexpected exception");
            }

            try {
                CloudStorageAccount.parse(accountStringKeySecondary);
                fail("Expected exception not thrown");
            } catch (IllegalArgumentException e) {
                // pass
            } catch (InvalidKeyException e) {
                fail("Unexpected exception");
            } catch (URISyntaxException e) {
                fail("Unexpected exception");
            }

            try {
                CloudStorageAccount.parse(accountStringKeyPrimarySecondary); // no exception expected
            } catch (Exception e) {
                fail("Unexpected exception");
            }

            // account key, no default protocol

            String accountStringKeyNoDefaultProtocolPrimary =
                    String.format(
                            "AccountName=%1$s;AccountKey=%2$s;EndpointSuffix=%3$s;" + endpointCombination[0],
                            accountKeyParams
                    );

            String accountStringKeyNoDefaultProtocolSecondary =
                    String.format(
                            "AccountName=%1$s;AccountKey=%2$s;EndpointSuffix=%3$s;" + endpointCombination[1],
                            accountKeyParams
                    );

            String accountStringKeyNoDefaultProtocolPrimarySecondary =
                    String.format(
                            "AccountName=%1$s;AccountKey=%2$s;EndpointSuffix=%3$s;" + endpointCombination[2],
                            accountKeyParams
                    );

            try {
                CloudStorageAccount.parse(accountStringKeyNoDefaultProtocolPrimary); // no exception expected
            } catch (Exception e) {
                fail("Unexpected exception");
            }

            try {
                CloudStorageAccount.parse(accountStringKeyNoDefaultProtocolSecondary);
                fail("Expected exception not thrown");
            } catch (IllegalArgumentException e) {
                // pass
            } catch (InvalidKeyException e) {
                fail("Unexpected exception");
            } catch (URISyntaxException e) {
                fail("Unexpected exception");
            }

            try {
                CloudStorageAccount.parse(accountStringKeyNoDefaultProtocolPrimarySecondary); // no exception expected
            } catch (Exception e) {
                fail("Unexpected exception");
            }

            // SAS

            String accountStringSasPrimary =
                    String.format(
                            "DefaultEndpointsProtocol=https;AccountName=%1$s;SharedAccessSignature=%2$s;EndpointSuffix=%3$s;" + endpointCombination[0],
                            accountSasParams
                    );

            String accountStringSasSecondary =
                    String.format(
                            "DefaultEndpointsProtocol=https;AccountName=%1$s;SharedAccessSignature=%2$s;EndpointSuffix=%3$s;" + endpointCombination[1],
                            accountSasParams
                    );

            String accountStringSasPrimarySecondary =
                    String.format(
                            "DefaultEndpointsProtocol=https;AccountName=%1$s;SharedAccessSignature=%2$s;EndpointSuffix=%3$s;" + endpointCombination[2],
                            accountSasParams
                    );

            try {
                CloudStorageAccount.parse(accountStringSasPrimary); // no exception expected
            } catch (Exception e) {
                fail("Unexpected exception");
            }

            try {
                CloudStorageAccount.parse(accountStringSasSecondary);
                fail("Expected exception not thrown");
            } catch (IllegalArgumentException e) {
                // pass
            } catch (InvalidKeyException e) {
                fail("Unexpected exception");
            } catch (URISyntaxException e) {
                fail("Unexpected exception");
            }

            try {
                CloudStorageAccount.parse(accountStringSasPrimarySecondary); // no exception expected
            } catch (Exception e) {
                fail("Unexpected exception");
            }

            // SAS, no default protocol

            String accountStringSasNoDefaultProtocolPrimary =
                    String.format(
                            "AccountName=%1$s;SharedAccessSignature=%2$s;EndpointSuffix=%3$s;" + endpointCombination[0],
                            accountSasParams
                    );

            String accountStringSasNoDefaultProtocolSecondary =
                    String.format(
                            "AccountName=%1$s;SharedAccessSignature=%2$s;EndpointSuffix=%3$s;" + endpointCombination[1],
                            accountSasParams
                    );

            String accountStringSasNoDefaultProtocolPrimarySecondary =
                    String.format(
                            "AccountName=%1$s;SharedAccessSignature=%2$s;EndpointSuffix=%3$s;" + endpointCombination[2],
                            accountSasParams
                    );

            try {
                CloudStorageAccount.parse(accountStringSasNoDefaultProtocolPrimary); // no exception expected
            } catch (Exception e) {
                fail("Unexpected exception");
            }

            try {
                CloudStorageAccount.parse(accountStringSasNoDefaultProtocolSecondary);
                fail("Expected exception not thrown");
            } catch (IllegalArgumentException e) {
                // pass
            } catch (InvalidKeyException e) {
                fail("Unexpected exception");
            } catch (URISyntaxException e) {
                fail("Unexpected exception");
            }

            try {
                CloudStorageAccount.parse(accountStringSasNoDefaultProtocolPrimarySecondary); // no exception expected
            } catch (Exception e) {
                fail("Unexpected exception");
            }

            // SAS without AccountName

            String accountStringSasNoNameNoEndpoint =
                    String.format(
                            "SharedAccessSignature=%2$s",
                            accountSasParams
                    );

            String accountStringSasNoNamePrimary =
                    String.format(
                            "SharedAccessSignature=%2$s;" + endpointCombination[0],
                            accountSasParams
                    );

            String accountStringSasNoNameSecondary =
                    String.format(
                            "SharedAccessSignature=%2$s;" + endpointCombination[1],
                            accountSasParams
                    );

            String accountStringSasNoNamePrimarySecondary =
                    String.format(
                            "SharedAccessSignature=%2$s;" + endpointCombination[2],
                            accountSasParams
                    );

            try {
                CloudStorageAccount.parse(accountStringSasNoNameNoEndpoint);
                fail("Expected exception not thrown");
            } catch (IllegalArgumentException e) {
                // pass
            } catch (InvalidKeyException e) {
                fail("Unexpected exception");
            } catch (URISyntaxException e) {
                fail("Unexpected exception");
            }

            try {
                CloudStorageAccount.parse(accountStringSasNoNamePrimary); // no exception expected
            } catch (Exception e) {
                fail("Unexpected exception");
            }

            try {
                CloudStorageAccount.parse(accountStringSasNoNameSecondary);
                fail("Expected exception not thrown");
            } catch (IllegalArgumentException e) {
                // pass
            } catch (InvalidKeyException e) {
                fail("Unexpected exception");
            } catch (URISyntaxException e) {
                fail("Unexpected exception");
            }

            try {
                CloudStorageAccount.parse(accountStringSasNoNamePrimarySecondary); // no exception expected
            } catch (Exception e) {
                fail("Unexpected exception");
            }
        }
    }
    
    private void connectionStringRoundtripHelper(String accountString) throws InvalidKeyException, URISyntaxException {
        CloudStorageAccount originalAccount = CloudStorageAccount.parse(accountString);
        String copiedAccountString = originalAccount.toString(true);
        CloudStorageAccount copiedAccount = CloudStorageAccount.parse(copiedAccountString);

        // make sure it round trips
        this.AccountsAreEqual(originalAccount, copiedAccount);
    }

    @Test
    public void testCloudStorageAccountClientMethods() throws URISyntaxException {
        StorageCredentialsAccountAndKey cred = new StorageCredentialsAccountAndKey(ACCOUNT_NAME, ACCOUNT_KEY);

        CloudStorageAccount account = new CloudStorageAccount(cred, false);
        CloudBlobClient blob = account.createCloudBlobClient();
        CloudQueueClient queue = account.createCloudQueueClient();
        CloudTableClient table = account.createCloudTableClient();
        CloudFileClient file = account.createCloudFileClient();

        // check endpoints
        assertEquals("Blob endpoint doesn't match account", account.getBlobEndpoint(), blob.getEndpoint());
        assertEquals("Queue endpoint doesn't match account", account.getQueueEndpoint(), queue.getEndpoint());
        assertEquals("Table endpoint doesn't match account", account.getTableEndpoint(), table.getEndpoint());
        assertEquals("File endpoint doesn't match account", account.getFileEndpoint(), file.getEndpoint());

        // check storage uris
        assertEquals("Blob endpoint doesn't match account", account.getBlobStorageUri(), blob.getStorageUri());
        assertEquals("Queue endpoint doesn't match account", account.getQueueStorageUri(), queue.getStorageUri());
        assertEquals("Table endpoint doesn't match account", account.getTableStorageUri(), table.getStorageUri());
        assertEquals("File endpoint doesn't match account", account.getFileStorageUri(), file.getStorageUri());

        // check creds
        assertEquals("Blob creds don't match account", account.getCredentials(), blob.getCredentials());
        assertEquals("Queue creds don't match account", account.getCredentials(), queue.getCredentials());
        assertEquals("Table creds don't match account", account.getCredentials(), table.getCredentials());
        assertEquals("File creds don't match account", account.getCredentials(), file.getCredentials());
    }

    @Test
    public void testCloudStorageAccountClientUriVerify() throws URISyntaxException, StorageException {
        StorageCredentialsAccountAndKey cred = new StorageCredentialsAccountAndKey(ACCOUNT_NAME, ACCOUNT_KEY);
        CloudStorageAccount cloudStorageAccount = new CloudStorageAccount(cred, true);

        CloudBlobClient blobClient = cloudStorageAccount.createCloudBlobClient();
        CloudBlobContainer container = blobClient.getContainerReference("container1");
        assertEquals(cloudStorageAccount.getBlobEndpoint().toString() + "/container1", container.getUri().toString());

        CloudQueueClient queueClient = cloudStorageAccount.createCloudQueueClient();
        CloudQueue queue = queueClient.getQueueReference("queue1");
        assertEquals(cloudStorageAccount.getQueueEndpoint().toString() + "/queue1", queue.getUri().toString());

        CloudTableClient tableClient = cloudStorageAccount.createCloudTableClient();
        CloudTable table = tableClient.getTableReference("table1");
        assertEquals(cloudStorageAccount.getTableEndpoint().toString() + "/table1", table.getUri().toString());

        CloudFileClient fileClient = cloudStorageAccount.createCloudFileClient();
        CloudFileShare share = fileClient.getShareReference("share1");
        assertEquals(cloudStorageAccount.getFileEndpoint().toString() + "/share1", share.getUri().toString());
    }

    @Test
    public void testCloudStorageAccountParseNullEmpty() throws InvalidKeyException, URISyntaxException {
        // parse() should throw exception when passing in null or empty string
        try {
            CloudStorageAccount.parse(null);
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals(SR.INVALID_CONNECTION_STRING, ex.getMessage());
        }

        try {
            CloudStorageAccount.parse("");
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals(SR.INVALID_CONNECTION_STRING, ex.getMessage());
        }
    }

    @Test
    public void testCloudStorageAccountDevStoreFalseFails()
            throws InvalidKeyException, URISyntaxException {
        try {
            CloudStorageAccount.parse("UseDevelopmentStorage=false");
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals(SR.INVALID_CONNECTION_STRING_DEV_STORE_NOT_TRUE, ex.getMessage());
        }
    }

    @Test
    public void testCloudStorageAccountDevStoreFalsePlusAccountFails()
            throws InvalidKeyException, URISyntaxException {
        try {
            CloudStorageAccount.parse("UseDevelopmentStorage=false;AccountName=devstoreaccount1");
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals(SR.INVALID_CONNECTION_STRING, ex.getMessage());
        }
    }

    @Test
    public void testCloudStorageAccountDevStoreFalsePlusEndpointFails()
            throws InvalidKeyException, URISyntaxException {
        try {
            CloudStorageAccount.parse("UseDevelopmentStorage=false;"
                    + "BlobEndpoint=http://127.0.0.1:1000/devstoreaccount1");
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals(SR.INVALID_CONNECTION_STRING, ex.getMessage());
        }
    }

    @Test
    public void testCloudStorageAccountDevStoreFalsePlusEndpointSuffixFails()
            throws InvalidKeyException, URISyntaxException {
        try {
            CloudStorageAccount
                    .parse("UseDevelopmentStorage=false;EndpointSuffix=core.chinacloudapi.cn");
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals(SR.INVALID_CONNECTION_STRING, ex.getMessage());
        }
    }

    @Test
    public void testCloudStorageAccountDefaultEndpointOverride() throws URISyntaxException, InvalidKeyException {
        CloudStorageAccount account = CloudStorageAccount
                .parse("DefaultEndpointsProtocol=http;BlobEndpoint=http://customdomain.com/;AccountName=asdf;AccountKey=123=");

        assertEquals(new URI("http://customdomain.com/"), account.getBlobEndpoint());
        assertNull(account.getBlobStorageUri().getSecondaryUri());
    }

    @Test
    public void testCloudStorageAccountDevStore() throws URISyntaxException {
        // default
        CloudStorageAccount account = CloudStorageAccount.getDevelopmentStorageAccount();
        assertEquals(new URI("http://127.0.0.1:10000/devstoreaccount1"), account.getBlobEndpoint());
        assertEquals(new URI("http://127.0.0.1:10001/devstoreaccount1"), account.getQueueEndpoint());
        assertEquals(new URI("http://127.0.0.1:10002/devstoreaccount1"), account.getTableEndpoint());
        assertEquals(new URI("http://127.0.0.1:10000/devstoreaccount1-secondary"), account.getBlobStorageUri()
                .getSecondaryUri());
        assertEquals(new URI("http://127.0.0.1:10001/devstoreaccount1-secondary"), account.getQueueStorageUri()
                .getSecondaryUri());
        assertEquals(new URI("http://127.0.0.1:10002/devstoreaccount1-secondary"), account.getTableStorageUri()
                .getSecondaryUri());

        // proxy
        account = CloudStorageAccount.getDevelopmentStorageAccount(new URI("http://ipv4.fiddler"));
        assertEquals(new URI("http://ipv4.fiddler:10000/devstoreaccount1"), account.getBlobEndpoint());
        assertEquals(new URI("http://ipv4.fiddler:10001/devstoreaccount1"), account.getQueueEndpoint());
        assertEquals(new URI("http://ipv4.fiddler:10002/devstoreaccount1"), account.getTableEndpoint());
        assertEquals(new URI("http://ipv4.fiddler:10000/devstoreaccount1-secondary"), account.getBlobStorageUri()
                .getSecondaryUri());
        assertEquals(new URI("http://ipv4.fiddler:10001/devstoreaccount1-secondary"), account.getQueueStorageUri()
                .getSecondaryUri());
        assertEquals(new URI("http://ipv4.fiddler:10002/devstoreaccount1-secondary"), account.getTableStorageUri()
                .getSecondaryUri());
    }

    @Test
    public void testCloudStorageAccountDevStoreProxyUri() throws InvalidKeyException, URISyntaxException {
        CloudStorageAccount account = CloudStorageAccount
                .parse("UseDevelopmentStorage=true;DevelopmentStorageProxyUri=http://ipv4.fiddler");

        assertEquals(new URI("http://ipv4.fiddler:10000/devstoreaccount1"), account.getBlobEndpoint());
        assertEquals(new URI("http://ipv4.fiddler:10001/devstoreaccount1"), account.getQueueEndpoint());
        assertEquals(new URI("http://ipv4.fiddler:10002/devstoreaccount1"), account.getTableEndpoint());
        assertEquals(new URI("http://ipv4.fiddler:10000/devstoreaccount1-secondary"), account.getBlobStorageUri()
                .getSecondaryUri());
        assertEquals(new URI("http://ipv4.fiddler:10001/devstoreaccount1-secondary"), account.getQueueStorageUri()
                .getSecondaryUri());
        assertEquals(new URI("http://ipv4.fiddler:10002/devstoreaccount1-secondary"), account.getTableStorageUri()
                .getSecondaryUri());
    }

    @Test
    public void testCloudStorageAccountDevStoreRoundtrip()
            throws InvalidKeyException, URISyntaxException {
        String accountString = "UseDevelopmentStorage=true";

        assertEquals(accountString, CloudStorageAccount.parse(accountString).toString(true));
    }

    @Test
    public void testCloudStorageAccountDevStoreProxyRoundtrip()
            throws InvalidKeyException, URISyntaxException {
        String accountString = "UseDevelopmentStorage=true;DevelopmentStorageProxyUri=http://ipv4.fiddler/";

        assertEquals(accountString, CloudStorageAccount.parse(accountString).toString(true));
    }

    @Test
    public void testCloudStorageAccountDefaultCloudRoundtrip()
            throws InvalidKeyException, URISyntaxException {
        String accountString = "EndpointSuffix=a.b.c;DefaultEndpointsProtocol=http;AccountName=test;"
                + "AccountKey=abc=";

        assertEquals(accountString, CloudStorageAccount.parse(accountString).toString(true));
    }

    @Test
    public void testCloudStorageAccountExplicitCloudRoundtrip()
            throws InvalidKeyException, URISyntaxException {
        String accountString = "EndpointSuffix=a.b.c;BlobEndpoint=https://blobs/;DefaultEndpointsProtocol=https;"
                + "AccountName=test;AccountKey=abc=";

        assertEquals(accountString, CloudStorageAccount.parse(accountString).toString(true));
    }

    @Test
    public void testCloudStorageAccountAnonymousRoundtrip()
            throws InvalidKeyException, URISyntaxException {
        String accountString = "BlobEndpoint=http://blobs/";

        assertEquals(accountString, CloudStorageAccount.parse(accountString).toString(true));

        CloudStorageAccount account = new CloudStorageAccount(
                null, new StorageUri(new URI("http://blobs/")), null, null, null);

        AccountsAreEqual(account, CloudStorageAccount.parse(account.toString(true)));
    }

    @Test
    public void testCloudStorageAccountInvalidAnonymousRoundtrip()
            throws InvalidKeyException, URISyntaxException {
        String accountString = "AccountKey=abc=";
        try {
            assertNull(CloudStorageAccount.parse(accountString));
            fail();
        }
        catch (Exception ex) {
            assertEquals(SR.INVALID_CONNECTION_STRING, ex.getMessage());
        }
    }

    @Test
    public void testCloudStorageAccountEmptyValues() throws InvalidKeyException, URISyntaxException {
        String accountString = ";EndpointSuffix=a.b.c;;BlobEndpoint=http://blobs/;;"
                + "AccountName=test;;AccountKey=abc=;;;";
        String validAccountString = "EndpointSuffix=a.b.c;BlobEndpoint=http://blobs/;"
                + "DefaultEndpointsProtocol=http;AccountName=test;AccountKey=abc=";

        assertEquals(validAccountString, CloudStorageAccount.parse(accountString).toString(true));
    }

    @Test
    public void testCloudStorageAccountEndpointSuffix()
            throws InvalidKeyException, URISyntaxException, StorageException {
        final String mooncake = "core.chinacloudapi.cn";
        final String fairfax = "core.usgovcloudapi.net";

        // Endpoint suffix for mooncake
        CloudStorageAccount accountParse = CloudStorageAccount.parse(
                "DefaultEndpointsProtocol=http;AccountName=test;"
                + "AccountKey=abc=;EndpointSuffix=" + mooncake);
        CloudStorageAccount accountConstruct = new CloudStorageAccount(accountParse.getCredentials(),
                false, accountParse.getEndpointSuffix());
        assertNotNull(accountParse);
        assertNotNull(accountConstruct);
        assertNotNull(accountParse.getBlobEndpoint());
        assertEquals(accountParse.getBlobEndpoint(), accountConstruct.getBlobEndpoint());
        assertTrue(accountParse.getBlobEndpoint().toString().endsWith(mooncake));

        // Endpoint suffix for fairfax
        accountParse = CloudStorageAccount.parse(
                "TableEndpoint=http://tables/;DefaultEndpointsProtocol=http;"
                + "AccountName=test;AccountKey=abc=;EndpointSuffix=" + fairfax);
        accountConstruct = new CloudStorageAccount(accountParse.getCredentials(),
                false, accountParse.getEndpointSuffix());
        assertNotNull(accountParse);
        assertNotNull(accountConstruct);
        assertNotNull(accountParse.getBlobEndpoint());
        assertEquals(accountParse.getBlobEndpoint(), accountConstruct.getBlobEndpoint());
        assertTrue(accountParse.getBlobEndpoint().toString().endsWith(fairfax));

        // Explicit table endpoint should override endpoint suffix for fairfax
        CloudTableClient tableClientParse = accountParse.createCloudTableClient();
        assertNotNull(tableClientParse);
        assertEquals(accountParse.getTableEndpoint(), tableClientParse.getEndpoint());
        assertTrue(tableClientParse.getEndpoint().toString().endsWith("tables/"));
    }

    @Test
    public void testCloudStorageAccountJustBlobToString() throws InvalidKeyException, URISyntaxException {
        String accountString = "BlobEndpoint=http://blobs/;DefaultEndpointsProtocol=http;AccountName=test;AccountKey=abc=";

        assertEquals(accountString, CloudStorageAccount.parse(accountString).toString(true));
    }

    @Test
    public void testCloudStorageAccountJustQueueToString() throws InvalidKeyException, URISyntaxException {
        String accountString = "QueueEndpoint=http://queue/;DefaultEndpointsProtocol=https;AccountName=test;AccountKey=abc=";

        assertEquals(accountString, CloudStorageAccount.parse(accountString).toString(true));
    }

    @Test
    public void testCloudStorageAccountJustTableToString() throws InvalidKeyException, URISyntaxException {
        String accountString = "TableEndpoint=http://table/;DefaultEndpointsProtocol=https;AccountName=test;AccountKey=abc=";

        assertEquals(accountString, CloudStorageAccount.parse(accountString).toString(true));
    }

    @Test
    public void testCloudStorageAccountJustFileToString() throws InvalidKeyException, URISyntaxException {
        String accountString = "FileEndpoint=http://file/;DefaultEndpointsProtocol=https;AccountName=test;AccountKey=abc=";

        assertEquals(accountString, CloudStorageAccount.parse(accountString).toString(true));
    }

    @Test
    public void testCloudStorageAccountExportKey() throws InvalidKeyException, URISyntaxException {
        String accountKeyString = "abc2564=";
        String accountString = "BlobEndpoint=http://blobs/;AccountName=test;AccountKey=" + accountKeyString;
        CloudStorageAccount account = CloudStorageAccount.parse(accountString);
        StorageCredentialsAccountAndKey accountAndKey = (StorageCredentialsAccountAndKey) account.getCredentials();
        String key = accountAndKey.exportBase64EncodedKey();
        assertEquals(accountKeyString, key);

        byte[] keyBytes = accountAndKey.exportKey();
        byte[] expectedKeyBytes = Base64.decode(accountKeyString);
        assertArrayEquals(expectedKeyBytes, keyBytes);
    }
}
