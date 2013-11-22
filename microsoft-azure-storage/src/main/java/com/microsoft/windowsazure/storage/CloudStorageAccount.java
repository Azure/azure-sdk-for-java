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
package com.microsoft.windowsazure.storage;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.microsoft.windowsazure.storage.blob.CloudBlobClient;
import com.microsoft.windowsazure.storage.core.SR;
import com.microsoft.windowsazure.storage.core.Utility;
import com.microsoft.windowsazure.storage.queue.CloudQueueClient;
import com.microsoft.windowsazure.storage.table.CloudTableClient;

/**
 * Represents a Windows Azure storage account.
 */
public final class CloudStorageAccount {
    /**
     * Represents the setting name for the account key.
     */
    protected static final String ACCOUNT_KEY_NAME = "AccountKey";

    /**
     * Represents the setting name for the account name.
     */
    protected static final String ACCOUNT_NAME_NAME = "AccountName";

    /**
     * The root blob storage DNS name.
     */
    private static final String BLOB_BASE_DNS_NAME = "blob.core.windows.net";

    /**
     * The suffix appended to account in order to access secondary location for read only access.
     */
    private static final String SECONDARY_LOCATION_ACCOUNT_SUFFIX = "-secondary";

    /**
     * Represents the setting name for a custom blob storage endpoint.
     */
    protected static final String BLOB_ENDPOINT_NAME = "BlobEndpoint";

    /**
     * The setting name for using the default storage endpoints with the specified protocol.
     */
    private static final String DEFAULT_ENDPOINTS_PROTOCOL_NAME = "DefaultEndpointsProtocol";

    /**
     * The setting name for specifying a development storage proxy Uri.
     */
    private static final String DEVELOPMENT_STORAGE_PROXY_URI_NAME = "DevelopmentStorageProxyUri";

    /**
     * The default account key for the development storage.
     */
    private static final String DEVSTORE_ACCOUNT_KEY = "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==";

    /**
     * The default account name for the development storage.
     */
    private static final String DEVSTORE_ACCOUNT_NAME = "devstoreaccount1";

    /**
     * The format string for the primary endpoint.
     */
    private static final String PRIMARY_ENDPOINT_FORMAT = "%s://%s.%s";

    /**
     * The format string for the secondary endpoint
     */
    private static final String SECONDARY_ENDPOINT_FORMAT = "%s://%s%s.%s";

    /**
     * The credentials string used to test for the development storage credentials.
     */
    private static final String DEVSTORE_CREDENTIALS_IN_STRING = CloudStorageAccount.ACCOUNT_NAME_NAME + "="
            + CloudStorageAccount.DEVSTORE_ACCOUNT_NAME + ";" + CloudStorageAccount.ACCOUNT_KEY_NAME + "="
            + CloudStorageAccount.DEVSTORE_ACCOUNT_KEY;

    /**
     * A CloudStorageAccount that represents the development storage account.
     */
    private static CloudStorageAccount devStoreAccount;

    /**
     * Represents the root queue DNS name.
     */
    protected static final String QUEUE_BASE_DNS_NAME = "queue.core.windows.net";

    /**
     * Represents the setting name for a custom queue endpoint.
     */
    protected static final String QUEUE_ENDPOINT_NAME = "QueueEndpoint";

    /**
     * Represents the setting name for a shared access key.
     */
    protected static final String SHARED_ACCESS_SIGNATURE_NAME = "SharedAccessSignature";

    /**
     * Represents the root table storage DNS name.
     */
    protected static final String TABLE_BASE_DNS_NAME = "table.core.windows.net";

    /**
     * Represents the setting name for a custom table storage endpoint.
     */
    protected static final String TABLE_ENDPOINT_NAME = "TableEndpoint";

    /**
     * The setting name for using the development storage.
     */
    private static final String USE_DEVELOPMENT_STORAGE_NAME = "UseDevelopmentStorage";

    /**
     * Gets the default blob {@link StorageUri} using specified settings.
     * 
     * @param settings
     *            The settings to use
     * @return The default blob {@link StorageUri}.
     * @throws URISyntaxException
     */
    private static StorageUri getDefaultBlobStorageUri(final HashMap<String, String> settings)
            throws URISyntaxException {
        final String scheme = settings.get(CloudStorageAccount.DEFAULT_ENDPOINTS_PROTOCOL_NAME) != null ? settings
                .get(CloudStorageAccount.DEFAULT_ENDPOINTS_PROTOCOL_NAME) : Constants.HTTP;
        final String accountName = settings.get(CloudStorageAccount.ACCOUNT_NAME_NAME);

        return getDefaultBlobStorageUri(scheme, accountName);
    }

    /**
     * Gets the default blob {@link StorageUri} using the specified protocol and account name.
     * 
     * @param scheme
     *            The protocol to use.
     * @param accountName
     *            The name of the storage account.
     * @return The default blob {@link StorageUri}.
     */
    private static StorageUri getDefaultBlobStorageUri(final String scheme, final String accountName)
            throws URISyntaxException {
        if (Utility.isNullOrEmpty(scheme)) {
            throw new IllegalArgumentException(SR.SCHEME_NULL_OR_EMPTY);
        }

        if (Utility.isNullOrEmpty(accountName)) {
            throw new IllegalArgumentException(SR.ACCOUNT_NAME_NULL_OR_EMPTY);
        }

        return new StorageUri(new URI(String.format(PRIMARY_ENDPOINT_FORMAT, scheme, accountName, BLOB_BASE_DNS_NAME)),
                new URI(String.format(SECONDARY_ENDPOINT_FORMAT, scheme, accountName,
                        SECONDARY_LOCATION_ACCOUNT_SUFFIX, BLOB_BASE_DNS_NAME)));
    }

    /**
     * Gets the default queue {@link StorageUri} using the specified settings.
     * 
     * @param settings
     *            The settings.
     * @return The default queue {@link StorageUri}.
     * @throws URISyntaxException
     */
    private static StorageUri getDefaultQueueStorageUri(final HashMap<String, String> settings)
            throws URISyntaxException {
        final String scheme = settings.get(CloudStorageAccount.DEFAULT_ENDPOINTS_PROTOCOL_NAME) != null ? settings
                .get(CloudStorageAccount.DEFAULT_ENDPOINTS_PROTOCOL_NAME) : Constants.HTTP;

        final String accountName = settings.get(CloudStorageAccount.ACCOUNT_NAME_NAME);

        return getDefaultQueueStorageUri(scheme, accountName);
    }

    /**
     * Gets the default queue endpoint using the specified settings.
     * 
     * @param settings
     *            The settings.
     * @return The default queue endpoint.
     */
    private static StorageUri getDefaultQueueStorageUri(final String scheme, final String accountName)
            throws URISyntaxException {
        if (Utility.isNullOrEmpty(scheme)) {
            throw new IllegalArgumentException(SR.SCHEME_NULL_OR_EMPTY);
        }

        if (Utility.isNullOrEmpty(accountName)) {
            throw new IllegalArgumentException(SR.ACCOUNT_NAME_NULL_OR_EMPTY);
        }

        return new StorageUri(
                new URI(String.format(PRIMARY_ENDPOINT_FORMAT, scheme, accountName, QUEUE_BASE_DNS_NAME)), new URI(
                        String.format(SECONDARY_ENDPOINT_FORMAT, scheme, accountName,
                                SECONDARY_LOCATION_ACCOUNT_SUFFIX, QUEUE_BASE_DNS_NAME)));
    }

    /**
     * Gets the default table {@link StorageUri} using the specified settings.
     * 
     * @param settings
     *            The settings.
     * @return The default table {@link StorageUri}.
     */
    private static StorageUri getDefaultTableStorageUri(final HashMap<String, String> settings)
            throws URISyntaxException {
        final String scheme = settings.get(CloudStorageAccount.DEFAULT_ENDPOINTS_PROTOCOL_NAME) != null ? settings
                .get(CloudStorageAccount.DEFAULT_ENDPOINTS_PROTOCOL_NAME) : Constants.HTTP;
        final String accountName = settings.get(CloudStorageAccount.ACCOUNT_NAME_NAME);

        return getDefaultTableStorageUri(scheme, accountName);
    }

    /**
     * Gets the default table {@link StorageUri} using the specified protocol and account name.
     * 
     * @param scheme
     *            The protocol to use.
     * @param accountName
     *            The name of the storage account.
     * @return The default table {@link StorageUri},
     * @throws URISyntaxException
     */
    private static StorageUri getDefaultTableStorageUri(final String scheme, final String accountName)
            throws URISyntaxException {
        if (Utility.isNullOrEmpty(scheme)) {
            throw new IllegalArgumentException(SR.SCHEME_NULL_OR_EMPTY);
        }

        if (Utility.isNullOrEmpty(accountName)) {
            throw new IllegalArgumentException(SR.ACCOUNT_NAME_NULL_OR_EMPTY);
        }

        return new StorageUri(
                new URI(String.format(PRIMARY_ENDPOINT_FORMAT, scheme, accountName, TABLE_BASE_DNS_NAME)), new URI(
                        String.format(SECONDARY_ENDPOINT_FORMAT, scheme, accountName,
                                SECONDARY_LOCATION_ACCOUNT_SUFFIX, TABLE_BASE_DNS_NAME)));
    }

    /**
     * Returns a {@link CloudStorageAccount} object that represents the development storage credentials.
     * 
     * @return A {@link CloudStorageAccount} object for the development storage credentials.
     */
    public static CloudStorageAccount getDevelopmentStorageAccount() {
        if (devStoreAccount == null) {
            try {
                devStoreAccount = getDevelopmentStorageAccount(new URI("http://127.0.0.1"));
            }
            catch (final URISyntaxException e) {
                // this wont happen since we know the uri above.
            }
        }
        return devStoreAccount;
    }

    /**
     * Returns a {@link CloudStorageAccount} object that represents the development storage credentials, using the
     * specified proxy URI.
     * 
     * @param proxyUri
     *            A <code>java.net.URI</code> object that represents the proxy endpoint to use.
     * 
     * @return A {@link CloudStorageAccount} object for the development storage credentials.
     * 
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public static CloudStorageAccount getDevelopmentStorageAccount(final URI proxyUri) throws URISyntaxException {
        if (proxyUri == null) {
            return getDevelopmentStorageAccount();
        }

        String prefix = proxyUri.getScheme().concat("://");
        prefix = prefix.concat(proxyUri.getHost());

        return new CloudStorageAccount(
                new StorageCredentialsAccountAndKey(DEVSTORE_ACCOUNT_NAME, DEVSTORE_ACCOUNT_KEY), new StorageUri(
                        new URI(prefix.concat(":10000/devstoreaccount1"))), new StorageUri(new URI(
                        prefix.concat(":10001/devstoreaccount1"))), new StorageUri(new URI(
                        prefix.concat(":10002/devstoreaccount1"))));
    }

    /**
     * Parses a connection string and returns a cloud storage account created from the connection string.
     * <p>
     * Note this method is not supported for shared access signature credentials as they do not contain the required
     * endpoint configuration parameters.
     * 
     * @param connectionString
     *            A <code>String</code> that represents the connection string to parse.
     * 
     * @return A {@link CloudStorageAccount} object that represents the cloud storage account constructed from the
     *         values provided in the connection string.
     * 
     * @throws InvalidKeyException
     *             If credentials in the connection string contain an invalid key.
     * @throws URISyntaxException
     *             If the connection string specifies an invalid URI.
     */
    public static CloudStorageAccount parse(final String connectionString) throws URISyntaxException,
            InvalidKeyException {
        if (connectionString == null || connectionString.length() == 0) {
            throw new IllegalArgumentException(SR.INVALID_CONNECTION_STRING);
        }

        // 1. Parse connection string in to key / value pairs
        final HashMap<String, String> settings = Utility.parseAccountString(connectionString);

        // 2 Validate General Settings rules,
        // - only setting value per key
        // - setting must have value.
        //   - One special case to this rule - the account key can be empty.
        for (final Entry<String, String> entry : settings.entrySet()) {
            if (entry.getValue() == null || entry.getValue().equals(Constants.EMPTY_STRING)) {
                if (!entry.getKey().equals(CloudStorageAccount.ACCOUNT_KEY_NAME)) {
                    throw new IllegalArgumentException(SR.INVALID_CONNECTION_STRING);
                }
            }
        }

        // 3. Validate scenario specific constraints
        CloudStorageAccount retVal = tryConfigureDevStore(settings);
        if (retVal != null) {
            return retVal;
        }

        retVal = tryConfigureServiceAccount(settings);
        if (retVal != null) {
            return retVal;
        }

        throw new IllegalArgumentException(SR.INVALID_CONNECTION_STRING);
    }

    /**
     * Evaluates connection settings and returns a CloudStorageAccount representing Development Storage.
     * 
     * @param settings
     *            A Hashmap of key value pairs representing the connection.
     * @return A CloudStorageAccount object constructed from the values provided in the connection settings, or null if
     *         one cannot be constructed.
     * @throws URISyntaxException
     *             if the connection settings contains an invalid URI
     */
    private static CloudStorageAccount tryConfigureDevStore(final HashMap<String, String> settings)
            throws URISyntaxException {
        if (settings.containsKey(USE_DEVELOPMENT_STORAGE_NAME)) {
            final String useDevStoreSetting = settings.get(USE_DEVELOPMENT_STORAGE_NAME);
            URI devStoreProxyUri = null;

            if (!Boolean.parseBoolean(useDevStoreSetting)) {
                throw new IllegalArgumentException(SR.INVALID_CONNECTION_STRING_DEV_STORE_NOT_TRUE);
            }

            if (settings.containsKey(DEVELOPMENT_STORAGE_PROXY_URI_NAME)) {
                devStoreProxyUri = new URI(settings.get(DEVELOPMENT_STORAGE_PROXY_URI_NAME));
            }

            return getDevelopmentStorageAccount(devStoreProxyUri);

        }
        else {
            return null;
        }
    }

    /**
     * Evaluates connection settings and configures a CloudStorageAccount accordingly.
     * 
     * @param settings
     *            A Hashmap of key value pairs representing the connection.
     * @return A CloudStorageAccount represented by the settings.
     * @throws URISyntaxException
     *             if the connectionString specifies an invalid URI.
     * @throws InvalidKeyException
     *             if credentials in the connection settings contain an invalid key.
     */
    private static CloudStorageAccount tryConfigureServiceAccount(final HashMap<String, String> settings)
            throws URISyntaxException, InvalidKeyException {

        if (settings.containsKey(USE_DEVELOPMENT_STORAGE_NAME)) {
            final String useDevStoreSetting = settings.get(USE_DEVELOPMENT_STORAGE_NAME);

            if (!Boolean.parseBoolean(useDevStoreSetting)) {
                throw new IllegalArgumentException(SR.INVALID_CONNECTION_STRING_DEV_STORE_NOT_TRUE);
            }
            else {
                return null;
            }
        }

        final String defaultEndpointSetting = settings.get(CloudStorageAccount.DEFAULT_ENDPOINTS_PROTOCOL_NAME) != null ? settings
                .get(CloudStorageAccount.DEFAULT_ENDPOINTS_PROTOCOL_NAME).toLowerCase() : null;

        if (defaultEndpointSetting != null && !defaultEndpointSetting.equals(Constants.HTTP)
                && !defaultEndpointSetting.equals(Constants.HTTPS)) {
            return null;
        }

        final StorageCredentials credentials = StorageCredentials.tryParseCredentials(settings);

        boolean isExplicitBlobEndpoint = settings.containsKey(CloudStorageAccount.BLOB_ENDPOINT_NAME);
        boolean isExplicitQueueEndpoint = settings.containsKey(CloudStorageAccount.QUEUE_ENDPOINT_NAME);
        boolean isExplicitTableEndpoint = settings.containsKey(CloudStorageAccount.TABLE_ENDPOINT_NAME);

        final StorageUri blobURI = isExplicitBlobEndpoint ? new StorageUri(new URI(
                settings.get(CloudStorageAccount.BLOB_ENDPOINT_NAME))) : null;
        final StorageUri queueURI = isExplicitQueueEndpoint ? new StorageUri(new URI(
                settings.get(CloudStorageAccount.QUEUE_ENDPOINT_NAME))) : null;
        final StorageUri tableURI = isExplicitTableEndpoint ? new StorageUri(new URI(
                settings.get(CloudStorageAccount.TABLE_ENDPOINT_NAME))) : null;

        CloudStorageAccount retVal = null;
        // Automatic endpoint Case
        if (defaultEndpointSetting != null && settings.containsKey(CloudStorageAccount.ACCOUNT_NAME_NAME)
                && settings.containsKey(CloudStorageAccount.ACCOUNT_KEY_NAME)) {

            retVal = new CloudStorageAccount(credentials, isExplicitBlobEndpoint ? blobURI
                    : getDefaultBlobStorageUri(settings), isExplicitQueueEndpoint ? queueURI
                    : getDefaultQueueStorageUri(settings), isExplicitTableEndpoint ? tableURI
                    : getDefaultTableStorageUri(settings));
            retVal.isBlobEndpointDefault = !isExplicitBlobEndpoint;
            retVal.isQueueEndpointDefault = !isExplicitQueueEndpoint;
            retVal.isTableEndpointDefault = !isExplicitTableEndpoint;
        }

        // Explicit endpoint Case
        else if (isExplicitBlobEndpoint || isExplicitQueueEndpoint || isExplicitTableEndpoint) {
            retVal = new CloudStorageAccount(credentials, blobURI, queueURI, tableURI);
        }

        return retVal;
    }

    /**
     * The internal Blob StorageUri.
     */
    private final StorageUri blobStorageUri;

    /**
     * The internal queue StorageUri.
     */
    private final StorageUri queueStorageUri;

    /**
     * The internal table StorageUri.
     */
    private final StorageUri tableStorageUri;

    /**
     * The internal Storage Credentials.
     */
    private StorageCredentials credentials;

    /**
     * Internal flag storing true if the blob endpoint was created using default settings. False if the caller specified
     * the
     * blob endpoint explicitly.
     */
    private boolean isBlobEndpointDefault = false;

    /**
     * Internal flag storing true if the queue endpoint was created using default settings. False if the caller
     * specified the
     * blob endpoint explicitly.
     */
    private boolean isQueueEndpointDefault = false;

    /**
     * Internal flag storing true if the table endpoint was created using default settings. False if the caller
     * specified the
     * blob endpoint explicitly.
     */
    private boolean isTableEndpointDefault = false;

    /**
     * Creates an instance of the <code>CloudStorageAccount</code> class using the specified account credentials.
     * <p>
     * With this constructor, the <code>CloudStorageAccount</code> object is constructed using the default HTTP storage
     * service endpoints. The default HTTP storage service endpoints are
     * <code>http://<i>myaccount</i>.blob.core.windows.net</code>,
     * <code>http://<i>myaccount</i>.queue.core.windows.net</code>, and
     * <code>http://<i>myaccount</i>.table.core.windows.net</code>, where <code><i>myaccount</i></code> is the name of
     * your storage account.
     * <p>
     * The credentials provided when constructing the <code>CloudStorageAccount</code> object are used to authenticate
     * all further requests against resources that are accessed via the <code>CloudStorageAccount</code> object or a
     * client object created from it. A client object may be a {@link CloudBlobClient} object.
     * 
     * @param storageCredentials
     *            A {@link StorageCredentials} object that represents the storage credentials to use to authenticate
     *            this account.
     * 
     * @throws URISyntaxException
     *             If <code>storageCredentials</code> specify an invalid account name.
     */
    public CloudStorageAccount(final StorageCredentials storageCredentials) throws URISyntaxException {
        Utility.assertNotNull("storageCredentials", storageCredentials);
        this.credentials = storageCredentials;

        this.isBlobEndpointDefault = true;
        this.isQueueEndpointDefault = true;
        this.isTableEndpointDefault = true;

        this.blobStorageUri = getDefaultBlobStorageUri(Constants.HTTP, this.credentials.getAccountName());
        this.queueStorageUri = getDefaultQueueStorageUri(Constants.HTTP, this.credentials.getAccountName());
        this.tableStorageUri = getDefaultTableStorageUri(Constants.HTTP, this.credentials.getAccountName());
    }

    /**
     * Creates an instance of the <code>CloudStorageAccount</code> class using the specified account credentials and
     * service endpoints.
     * <p>
     * Use this constructor to construct a <code>CloudStorageAccount</code> object using custom endpoints, in the case
     * where you've configured a custom domain name for your storage account.
     * <p>
     * The credentials provided when constructing the <code>CloudStorageAccount</code> object are used to authenticate
     * all further requests against resources that are accessed via the <code>CloudStorageAccount</code> object or a
     * client object created from it. A client object may be a {@link CloudBlobClient} object.
     * 
     * @param storageCredentials
     *            A {@link StorageCredentials} object that represents the storage credentials to use to authenticate
     *            this account.
     * @param blobEndpoint
     *            A <code>java.net.URI</code> object that represents the Blob service endpoint.
     * @param queueEndpoint
     *            A <code>java.net.URI</code> object that represents the Queue service endpoint.
     * @param tableEndpoint
     *            A <code>java.net.URI</code> object that represents the Table service endpoint.
     */
    public CloudStorageAccount(final StorageCredentials storageCredentials, final URI blobEndpoint,
            final URI queueEndpoint, final URI tableEndpoint) {
        this(storageCredentials, new StorageUri(blobEndpoint), new StorageUri(queueEndpoint), new StorageUri(
                tableEndpoint));
    }

    /**
     * Creates an instance of the <code>CloudStorageAccount</code> class using the specified account credentials and
     * service endpoints.
     * <p>
     * Use this constructor to construct a <code>CloudStorageAccount</code> object using custom endpoints, in the case
     * where you've configured a custom domain name for your storage account.
     * <p>
     * The credentials provided when constructing the <code>CloudStorageAccount</code> object are used to authenticate
     * all further requests against resources that are accessed via the <code>CloudStorageAccount</code> object or a
     * client object created from it. A client object may be a {@link CloudBlobClient} object.
     * 
     * @param storageCredentials
     *            A {@link StorageCredentials} object that represents the storage credentials to use to authenticate
     *            this account.
     * @param blobStorageUri
     *            A <code>StorageUri</code> object that represents the Blob service endpoint.
     * @param queueStorageUri
     *            A <code>StorageUri</code> object that represents the Queue service endpoint.
     * @param tableStorageUri
     *            A <code>StorageUri</code> object that represents the Table service endpoint.
     */
    public CloudStorageAccount(final StorageCredentials storageCredentials, final StorageUri blobStorageUri,
            final StorageUri queueStorageUri, final StorageUri tableStorageUri) {
        this.credentials = storageCredentials;
        this.blobStorageUri = blobStorageUri;
        this.queueStorageUri = queueStorageUri;
        this.tableStorageUri = tableStorageUri;
    }

    /**
     * Creates an instance of the <code>CloudStorageAccount</code> class using the specified account credentials and the
     * default service endpoints, using HTTP or HTTPS as specified.
     * <p>
     * With this constructor, the <code>CloudStorageAccount</code> object is constructed using the default storage
     * service endpoints. The default storage service endpoints are
     * <code>[http|https]://<i>myaccount</i>.blob.core.windows.net</code>;
     * <code>[http|https]://<i>myaccount</i>.queue.core.windows.net</code>; and
     * <code>[http|https]://<i>myaccount</i>.table.core.windows.net</code>, where <code><i>myaccount</i></code> is the
     * name of your storage account. Access to the cloud storage account may be via HTTP or HTTPS, as specified by the
     * <code>useHttps</code> parameter.
     * <p>
     * The credentials provided when constructing the <code>CloudStorageAccount</code> object are used to authenticate
     * all further requests against resources that are accessed via the <code>CloudStorageAccount</code> object or a
     * client object created from it. A client object may be a {@link CloudBlobClient} object.
     * 
     * @param storageCredentials
     *            A {@link StorageCredentialsAccountAndKey} object that represents the storage credentials to use to
     *            authenticate this account.
     * @param useHttps
     *            <code>true</code> to use HTTPS to connect to the storage service endpoints; otherwise,
     *            <code>false</code>.
     * 
     * @throws URISyntaxException
     *             If <code>storageCredentials</code> specify an invalid account name.
     */
    public CloudStorageAccount(final StorageCredentialsAccountAndKey storageCredentials, final boolean useHttps)
            throws URISyntaxException {
        Utility.assertNotNull("storageCredentials", storageCredentials);

        this.credentials = storageCredentials;
        this.blobStorageUri = getDefaultBlobStorageUri(useHttps ? Constants.HTTPS : Constants.HTTP,
                storageCredentials.getAccountName());
        this.queueStorageUri = getDefaultQueueStorageUri(useHttps ? Constants.HTTPS : Constants.HTTP,
                storageCredentials.getAccountName());
        this.tableStorageUri = getDefaultTableStorageUri(useHttps ? Constants.HTTPS : Constants.HTTP,
                storageCredentials.getAccountName());
        this.isBlobEndpointDefault = true;
        this.isQueueEndpointDefault = true;
        this.isTableEndpointDefault = true;
    }

    /**
     * Creates a new Blob service client.
     * 
     * @return A {@link CloudBlobClient} that represents the cloud blob client.
     * 
     */
    public CloudBlobClient createCloudBlobClient() {
        if (this.getBlobStorageUri() == null) {
            throw new IllegalArgumentException(SR.BLOB_ENDPOINT_NOT_CONFIGURED);
        }

        if (this.credentials == null) {
            throw new IllegalArgumentException(SR.MISSING_CREDENTIALS);
        }

        if (!this.credentials.canCredentialsSignRequest()) {
            throw new IllegalArgumentException(SR.CREDENTIALS_CANNOT_SIGN_REQUEST);
        }
        return new CloudBlobClient(this.getBlobStorageUri(), this.getCredentials());
    }

    /**
     * Creates a new queue service client.
     * 
     * @return A client object that uses the Queue service endpoint.
     */
    public CloudQueueClient createCloudQueueClient() {
        if (this.getQueueStorageUri() == null) {
            throw new IllegalArgumentException(SR.QUEUE_ENDPOINT_NOT_CONFIGURED);
        }

        if (this.credentials == null) {
            throw new IllegalArgumentException(SR.MISSING_CREDENTIALS);
        }

        if (!this.credentials.canCredentialsSignRequest()) {
            throw new IllegalArgumentException(SR.CREDENTIALS_CANNOT_SIGN_REQUEST);
        }
        return new CloudQueueClient(this.getQueueStorageUri(), this.getCredentials());
    }

    /**
     * Creates a new table service client.
     * 
     * @return A client object that uses the Table service endpoint.
     */
    public CloudTableClient createCloudTableClient() {
        if (this.getTableStorageUri() == null) {
            throw new IllegalArgumentException(SR.TABLE_ENDPOINT_NOT_CONFIGURED);
        }

        if (this.credentials == null) {
            throw new IllegalArgumentException(SR.MISSING_CREDENTIALS);
        }

        if (!this.credentials.canCredentialsSignRequest()) {
            throw new IllegalArgumentException(SR.CREDENTIALS_CANNOT_SIGN_REQUEST);
        }
        return new CloudTableClient(this.getTableStorageUri(), this.getCredentials());
    }

    /**
     * Returns the endpoint for the Blob service for the storage account. This method is not supported when using shared
     * access signature credentials.
     * 
     * @return A <code>java.net.URI</code> object that represents the blob endpoint associated with this account.
     */
    public URI getBlobEndpoint() {
        if (this.getCredentials() instanceof StorageCredentialsSharedAccessSignature) {
            throw new IllegalArgumentException(SR.ENDPOINT_INFORMATION_UNAVAILABLE);
        }

        if (this.blobStorageUri == null) {
            return null;
        }

        return this.blobStorageUri.getPrimaryUri();
    }

    /**
     * Returns the endpoint for the Blob service for the storage account. This method is not supported when using shared
     * access signature credentials.
     * 
     * @return A <code>StorageUri</code> object that represents the blob endpoint associated with this account.
     */
    public StorageUri getBlobStorageUri() {
        if (this.getCredentials() instanceof StorageCredentialsSharedAccessSignature) {
            throw new IllegalArgumentException(SR.ENDPOINT_INFORMATION_UNAVAILABLE);
        }

        return this.blobStorageUri;
    }

    /**
     * Returns the credentials for the storage account.
     * 
     * @return A {@link StorageCredentials} object that represents the credentials for this storage account.
     */
    public StorageCredentials getCredentials() {
        return this.credentials;
    }

    /**
     * Returns the endpoint for the Queue service for the storage account.
     * 
     * @return A <code>java.net.URI</code> object that represents the queue endpoint associated with this account.
     */
    public URI getQueueEndpoint() {
        if (this.getCredentials() instanceof StorageCredentialsSharedAccessSignature) {
            throw new IllegalArgumentException(SR.ENDPOINT_INFORMATION_UNAVAILABLE);
        }

        if (this.queueStorageUri == null) {
            return null;
        }

        return this.queueStorageUri.getPrimaryUri();
    }

    /**
     * Returns the endpoint for the Queue service for the storage account.
     * 
     * @return A <code>StorageUri</code> object that represents the queue endpoint associated with this account.
     */
    public StorageUri getQueueStorageUri() {
        if (this.getCredentials() instanceof StorageCredentialsSharedAccessSignature) {
            throw new IllegalArgumentException(SR.ENDPOINT_INFORMATION_UNAVAILABLE);
        }

        return this.queueStorageUri;
    }

    /**
     * Returns the endpoint for the table service for the storage account.
     * 
     * @return A <code>StorageUri</code> object that represents the table endpoint associated with this account.
     */
    public URI getTableEndpoint() {
        if (this.getCredentials() instanceof StorageCredentialsSharedAccessSignature) {
            throw new IllegalArgumentException(SR.ENDPOINT_INFORMATION_UNAVAILABLE);
        }

        if (this.tableStorageUri == null) {
            return null;
        }

        return this.tableStorageUri.getPrimaryUri();
    }

    /**
     * Returns the endpoint for the table service for the storage account.
     * 
     * @return A <code>java.net.URI</code> object that represents the table endpoint associated with this account.
     */
    public StorageUri getTableStorageUri() {
        if (this.getCredentials() instanceof StorageCredentialsSharedAccessSignature) {
            throw new IllegalArgumentException(SR.ENDPOINT_INFORMATION_UNAVAILABLE);
        }

        return this.tableStorageUri;
    }

    //
    // Sets the StorageCredentials to use with this account. Warning internal
    // use only, updating the credentials to a new account can potentially
    // invalidate a bunch of pre-existingobjects.
    //
    // @param credentials
    // the credentials to set
    //
    /**
     * Reserved for internal use.
     * 
     * @param credentials
     *            Reserved.
     */
    protected void setCredentials(final StorageCredentials credentials) {
        this.credentials = credentials;
    }

    /**
     * Returns a connection string for this storage account, without sensitive data.
     * 
     * @return A <code>String</code> that represents the connection string for this storage account, without sensitive
     *         data.
     */
    @Override
    public String toString() {
        return this.toString(false);
    }

    /**
     * Returns a connection string for this storage account, optionally with sensitive data.
     * 
     * @return A <code>String</code> that represents the connection string for this storage account, optionally with
     *         sensitive data.
     * @param exportSecrets
     *            <code>true</code> to include sensitive data in the string; otherwise, <code>false</code>.
     */
    public String toString(final boolean exportSecrets) {
        if (this.credentials != null && Utility.isNullOrEmpty(this.credentials.getAccountName())) {
            return this.credentials.toString(exportSecrets);
        }

        final ArrayList<String> retVals = new ArrayList<String>();
        if (this == devStoreAccount) {
            retVals.add(String.format("%s=true", USE_DEVELOPMENT_STORAGE_NAME));
        }
        else if (this.credentials != null && DEVSTORE_ACCOUNT_NAME.equals(this.credentials.getAccountName())
                && this.credentials.toString(true).equals(CloudStorageAccount.DEVSTORE_CREDENTIALS_IN_STRING)
                && this.getBlobStorageUri() != null && this.getTableStorageUri() != null
                && this.getQueueStorageUri() != null
                && this.getBlobEndpoint().getHost().equals(this.getQueueEndpoint().getHost())
                && this.getQueueEndpoint().getHost().equals(this.getTableEndpoint().getHost())
                && this.getBlobEndpoint().getScheme().equals(this.getQueueEndpoint().getScheme())
                && this.getQueueEndpoint().getScheme().equals(this.getTableEndpoint().getScheme())) {
            retVals.add(String.format("%s=true", USE_DEVELOPMENT_STORAGE_NAME));
            retVals.add(String.format("%s=%s://%s/", DEVELOPMENT_STORAGE_PROXY_URI_NAME, this.getBlobEndpoint()
                    .getScheme(), this.getBlobEndpoint().getHost()));
        }
        else {
            boolean addDefault = false;
            if (this.getBlobStorageUri() != null) {
                if (this.isBlobEndpointDefault) {
                    addDefault = true;
                }
                else {
                    retVals.add(String.format("%s=%s", BLOB_ENDPOINT_NAME, this.getBlobEndpoint()));
                }
            }

            if (this.getQueueStorageUri() != null) {
                if (this.isQueueEndpointDefault) {
                    addDefault = true;
                }
                else {
                    retVals.add(String.format("%s=%s", QUEUE_ENDPOINT_NAME, this.getQueueEndpoint()));
                }
            }

            if (this.getTableStorageUri() != null) {
                if (this.isTableEndpointDefault) {
                    addDefault = true;
                }
                else {
                    retVals.add(String.format("%s=%s", TABLE_ENDPOINT_NAME, this.getTableEndpoint()));
                }
            }

            if (addDefault) {
                retVals.add(String.format("%s=%s", DEFAULT_ENDPOINTS_PROTOCOL_NAME, this.getBlobEndpoint().getScheme()));
            }

            if (this.getCredentials() != null) {
                retVals.add(this.getCredentials().toString(exportSecrets));
            }
        }

        final StringBuilder returnString = new StringBuilder();
        for (final String val : retVals) {
            returnString.append(val);
            returnString.append(';');
        }

        // Remove trailing ';'
        if (retVals.size() > 0) {
            returnString.deleteCharAt(returnString.length() - 1);
        }

        return returnString.toString();
    }
}
