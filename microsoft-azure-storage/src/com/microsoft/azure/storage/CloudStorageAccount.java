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

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.microsoft.azure.storage.analytics.CloudAnalyticsClient;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.SharedAccessSignatureHelper;
import com.microsoft.azure.storage.core.StorageCredentialsHelper;
import com.microsoft.azure.storage.core.UriQueryBuilder;
import com.microsoft.azure.storage.core.Utility;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueClient;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;

/**
 * Represents a Microsoft Azure storage account.
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
     * Represents the final terms of each root storage DNS name.
     */
    private static final String DNS_NAME_FORMAT = "%s.%s";
    
    /**
     * Represents the root storage DNS name.
     */
    private static final String DEFAULT_DNS = "core.windows.net";
    
    /**
     * The suffix appended to account in order to access secondary location for read only access.
     */
    private static final String SECONDARY_LOCATION_ACCOUNT_SUFFIX = "-secondary";
    
    /**
     * Represents the setting name for a custom storage endpoint suffix.
     */
    private static final String ENDPOINT_SUFFIX_NAME = "EndpointSuffix";

    /**
     * Represents the setting name for a custom blob storage endpoint.
     */
    protected static final String BLOB_ENDPOINT_NAME = "BlobEndpoint";

    /**
     * The setting name for using the default storage endpoints with the specified protocol.
     */
    private static final String DEFAULT_ENDPOINTS_PROTOCOL_NAME = "DefaultEndpointsProtocol";

    /**
     * The format string for the primary endpoint.
     */
    private static final String DEVELOPMENT_STORAGE_PRIMARY_ENDPOINT_FORMAT = "%s://%s:%s/%s";

    /**
     * The format string for the secondary endpoint.
     */
    private static final String DEVELOPMENT_STORAGE_SECONDARY_ENDPOINT_FORMAT =
            DEVELOPMENT_STORAGE_PRIMARY_ENDPOINT_FORMAT + SECONDARY_LOCATION_ACCOUNT_SUFFIX;

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
     * Represents the setting name for a custom file endpoint.
     */
    private static final String FILE_ENDPOINT_NAME = "FileEndpoint";

    /**
     * The format string for the primary endpoint.
     */
    private static final String PRIMARY_ENDPOINT_FORMAT = "%s://%s.%s";

    /**
     * The format string for the secondary endpoint
     */
    private static final String SECONDARY_ENDPOINT_FORMAT = "%s://%s%s.%s";

    /**
     * Represents the setting name for a custom queue endpoint.
     */
    protected static final String QUEUE_ENDPOINT_NAME = "QueueEndpoint";

    /**
     * Represents the setting name for a shared access key.
     */
    protected static final String SHARED_ACCESS_SIGNATURE_NAME = "SharedAccessSignature";

    /**
     * Represents the setting name for a custom table storage endpoint.
     */
    protected static final String TABLE_ENDPOINT_NAME = "TableEndpoint";

    /**
     * The setting name for using the development storage.
     */
    private static final String USE_DEVELOPMENT_STORAGE_NAME = "UseDevelopmentStorage";
    
    /**
     * Returns a {@link CloudStorageAccount} object that represents the development storage credentials. Secondary
     * endpoints are enabled by default.
     * 
     * @return A {@link CloudStorageAccount} object for the development storage credentials.
     */
    public static CloudStorageAccount getDevelopmentStorageAccount() {
        try {
            return getDevelopmentStorageAccount(null);
        }
        catch (final URISyntaxException e) {
            // this won't happen since we know the standard development stororage uri is valid.
            return null;
        }
    }

    /**
     * Returns a {@link CloudStorageAccount} object that represents the development storage credentials, using the
     * specified proxy URI. Secondary endpoints are enabled by default.
     * 
     * @param proxyUri
     *            A <code>java.net.URI</code> object that represents the proxy endpoint to use. Specifying
     *            <code>null</code> will use the default <code>http://127.0.0.1</code>.
     * 
     * @return A {@link CloudStorageAccount} object for the development storage credentials.
     * 
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public static CloudStorageAccount getDevelopmentStorageAccount(final URI proxyUri) throws URISyntaxException {
        String scheme;
        String host;
        if (proxyUri == null) {
            scheme = "http";
            host = "127.0.0.1";
        }
        else {
            scheme = proxyUri.getScheme();
            host = proxyUri.getHost();
        }

        StorageCredentials credentials = new StorageCredentialsAccountAndKey(DEVSTORE_ACCOUNT_NAME,
                DEVSTORE_ACCOUNT_KEY);

        URI blobPrimaryEndpoint = new URI(String.format(DEVELOPMENT_STORAGE_PRIMARY_ENDPOINT_FORMAT, scheme, host,
                "10000", DEVSTORE_ACCOUNT_NAME));
        URI queuePrimaryEndpoint = new URI(String.format(DEVELOPMENT_STORAGE_PRIMARY_ENDPOINT_FORMAT, scheme, host,
                "10001", DEVSTORE_ACCOUNT_NAME));
        URI tablePrimaryEndpoint = new URI(String.format(DEVELOPMENT_STORAGE_PRIMARY_ENDPOINT_FORMAT, scheme, host,
                "10002", DEVSTORE_ACCOUNT_NAME));

        URI blobSecondaryEndpoint = new URI(String.format(DEVELOPMENT_STORAGE_SECONDARY_ENDPOINT_FORMAT, scheme, host,
                "10000", DEVSTORE_ACCOUNT_NAME));
        URI queueSecondaryEndpoint = new URI(String.format(DEVELOPMENT_STORAGE_SECONDARY_ENDPOINT_FORMAT, scheme, host,
                "10001", DEVSTORE_ACCOUNT_NAME));
        URI tableSecondaryEndpoint = new URI(String.format(DEVELOPMENT_STORAGE_SECONDARY_ENDPOINT_FORMAT, scheme, host,
                "10002", DEVSTORE_ACCOUNT_NAME));

        CloudStorageAccount account = new CloudStorageAccount(credentials, new StorageUri(blobPrimaryEndpoint,
                blobSecondaryEndpoint), new StorageUri(queuePrimaryEndpoint, queueSecondaryEndpoint), new StorageUri(
                tablePrimaryEndpoint, tableSecondaryEndpoint), null /* fileStorageUri */);

        account.isDevStoreAccount = true;

        return account;
    }

    /**
     * Parses a connection string and returns a cloud storage account created from the connection string.
     * <p>
     * The connection string should be in the <a href="http://msdn.microsoft.com/library/azure/ee758697.aspx">Azure
     * connection string</a> format.
     * <p>
     * Note that while a connection string may include a SAS token, it is often easier to use the
     * {@link CloudBlobContainer#CloudBlobContainer(URI)}, {@link CloudQueue#CloudQueue(URI)},
     * {@link CloudTable#CloudTable(URI)} constructors directly. To do this, create a
     * {@link StorageCredentialsSharedAccessSignature#StorageCredentialsSharedAccessSignature(String)} object with your
     * SAS token, use the {@link StorageCredentialsSharedAccessSignature#transformUri(URI)} method on the container,
     * queue, or table URI, and then use that URI to construct the object.
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
        final Map<String, String> settings = Utility.parseAccountString(connectionString);

        // 2 Validate General Settings rules,
        // - only setting value per key
        // - setting must have value.
        for (final Entry<String, String> entry : settings.entrySet()) {
            if (entry.getValue() == null || entry.getValue().equals(Constants.EMPTY_STRING)) {
                throw new IllegalArgumentException(SR.INVALID_CONNECTION_STRING);
            }
        }

        // 3. Validate scenario specific constraints
        CloudStorageAccount account = tryConfigureDevStore(settings);
        if (account != null) {
            return account;
        }

        account = tryConfigureServiceAccount(settings);
        if (account != null) {
            return account;
        }

        throw new IllegalArgumentException(SR.INVALID_CONNECTION_STRING);
    }
    
    /**
     * Gets the {@link StorageUri} using specified service, settings, and endpoint.
     * 
     * @param settings
     *            A <code>java.util.Map</code> of key/value pairs which represents
     *            the connection settings.
     * @param service
     *            A String that represents the service's base DNS name.
     * @param serviceEndpoint
     *            The service endpoint name to check in settings.
     * @return The {@link StorageUri}.
     * @throws URISyntaxException
     */
    private static StorageUri getStorageUri(
            final Map<String, String> settings, final String service, final String serviceEndpoint)
            throws URISyntaxException {
        // Explicit Endpoint Case
        if (settings.containsKey(serviceEndpoint)) {
            return new StorageUri(new URI(settings.get(serviceEndpoint)));
        }
        // Automatic Endpoint Case
        else if (settings.containsKey(DEFAULT_ENDPOINTS_PROTOCOL_NAME) &&
                 settings.containsKey(CloudStorageAccount.ACCOUNT_NAME_NAME) &&
                 settings.containsKey(CloudStorageAccount.ACCOUNT_KEY_NAME)) {
            final String scheme = settings.get(CloudStorageAccount.DEFAULT_ENDPOINTS_PROTOCOL_NAME);
            final String accountName = settings.get(CloudStorageAccount.ACCOUNT_NAME_NAME);
            final String endpointSuffix = settings.get(CloudStorageAccount.ENDPOINT_SUFFIX_NAME);
            return getDefaultStorageUri(scheme, accountName, getDNS(service, endpointSuffix));
        }
        // Otherwise
        else {
            return null;
        }
    }
    
    /**
     * Gets the default {@link StorageUri} using the specified service, protocol and account name.
     * 
     * @param scheme
     *            The protocol to use.
     * @param accountName
     *            The name of the storage account.
     * @param service
     *            A String that represents the service's base DNS name.
     * @return The default {@link StorageUri}.
     */
    private static StorageUri getDefaultStorageUri(final String scheme, final String accountName, final String service)
            throws URISyntaxException {
        if (Utility.isNullOrEmpty(scheme)) {
            throw new IllegalArgumentException(SR.SCHEME_NULL_OR_EMPTY);
        }

        if (Utility.isNullOrEmpty(accountName)) {
            throw new IllegalArgumentException(SR.ACCOUNT_NAME_NULL_OR_EMPTY);
        }

        URI primaryUri = new URI(String.format(PRIMARY_ENDPOINT_FORMAT, scheme, accountName, service));
        URI secondaryUri = new URI(
                String.format(SECONDARY_ENDPOINT_FORMAT, scheme, accountName, SECONDARY_LOCATION_ACCOUNT_SUFFIX, service));
        return new StorageUri(primaryUri, secondaryUri);
    }
    
    /**
     * This generates a domain name for the given service.
     * 
     * @param service
     *              the service to connect to
     * @param base
     *              the suffix to use
     * @return the domain name
     */
    private static String getDNS(String service, String base) {
        if (base == null) {
            base = DEFAULT_DNS;
        }
        
        return String.format(DNS_NAME_FORMAT, service, base);
    }
    
    /**
     * Evaluates connection settings and returns a CloudStorageAccount representing Development Storage.
     * 
     * @param settings
     *            A <code>java.util.Map</code> of key/value pairs which represents the connection settings.
     * @return A {@link CloudStorageAccount} object constructed from the values provided in the connection settings, or
     *         null if
     *         one cannot be constructed.
     * @throws URISyntaxException
     *             if the connection settings contains an invalid URI
     */
    private static CloudStorageAccount tryConfigureDevStore(final Map<String, String> settings)
            throws URISyntaxException {
        if (settings.containsKey(USE_DEVELOPMENT_STORAGE_NAME)) {
            if (!Boolean.parseBoolean(settings.get(USE_DEVELOPMENT_STORAGE_NAME))) {
                throw new IllegalArgumentException(SR.INVALID_CONNECTION_STRING_DEV_STORE_NOT_TRUE);
            }

            URI devStoreProxyUri = null;
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
     *            A <code>java.util.Map</code> of key/value pairs which represents
     *            the connection settings.
     * @return A {@link CloudStorageAccount} represented by the settings.
     * @throws URISyntaxException
     *             if the connectionString specifies an invalid URI.
     * @throws InvalidKeyException
     *             if credentials in the connection settings contain an invalid key.
     */
    private static CloudStorageAccount tryConfigureServiceAccount(final Map<String, String> settings)
            throws URISyntaxException, InvalidKeyException {
        if (settings.containsKey(USE_DEVELOPMENT_STORAGE_NAME)) {
            if (!Boolean.parseBoolean(settings.get(USE_DEVELOPMENT_STORAGE_NAME))) {
                throw new IllegalArgumentException(SR.INVALID_CONNECTION_STRING_DEV_STORE_NOT_TRUE);
            }
            else {
                return null;
            }
        }

        String defaultEndpointSetting = settings.get(DEFAULT_ENDPOINTS_PROTOCOL_NAME);
        if (defaultEndpointSetting != null) {
            defaultEndpointSetting = defaultEndpointSetting.toLowerCase();
            if(!defaultEndpointSetting.equals(Constants.HTTP)
                    && !defaultEndpointSetting.equals(Constants.HTTPS)) {
                return null;
            }
        }

        final StorageCredentials credentials = StorageCredentials.tryParseCredentials(settings);
        final CloudStorageAccount account = new CloudStorageAccount(credentials,
                getStorageUri(settings, SR.BLOB, BLOB_ENDPOINT_NAME),
                getStorageUri(settings, SR.QUEUE, QUEUE_ENDPOINT_NAME),
                getStorageUri(settings, SR.TABLE, TABLE_ENDPOINT_NAME),
                getStorageUri(settings, SR.FILE, FILE_ENDPOINT_NAME));
        
        // Invalid Account String
        if ((account.getBlobEndpoint() == null) && (account.getFileEndpoint() == null) &&
                (account.getQueueEndpoint() == null) && (account.getTableEndpoint() == null)) {
            return null;
        }

        // Endpoint is only default if it is neither null nor explicitly specified
        account.isBlobEndpointDefault = !((account.getBlobEndpoint() == null) ||
                  settings.containsKey(CloudStorageAccount.BLOB_ENDPOINT_NAME));
        account.isFileEndpointDefault = !((account.getFileEndpoint() == null) ||
                  settings.containsKey(CloudStorageAccount.FILE_ENDPOINT_NAME));
        account.isQueueEndpointDefault = !((account.getQueueEndpoint() == null) ||
                  settings.containsKey(CloudStorageAccount.QUEUE_ENDPOINT_NAME));
        account.isTableEndpointDefault = !((account.getTableEndpoint() == null) ||
                  settings.containsKey(CloudStorageAccount.TABLE_ENDPOINT_NAME));
        
        account.endpointSuffix = settings.get(CloudStorageAccount.ENDPOINT_SUFFIX_NAME);
        
        return account;
    }

    
    /**
     * The explicit endpoint suffix if one other than the default is needed.  Null otherwise.
     */
    private String endpointSuffix;
    
    /**
     * The internal Blob StorageUri.
     */
    private final StorageUri blobStorageUri;

    /**
     * The internal file StorageUri.
     */
    private final StorageUri fileStorageUri;

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
     * Internal flag storing true if the blob endpoint was created using default settings.
     * False if the caller specified the blob endpoint explicitly.
     */
    private boolean isBlobEndpointDefault = false;

    /**
     * Internal flag storing true if the file endpoint was created using default settings.
     * False if the caller specified the file endpoint explicitly.
     */
    private boolean isFileEndpointDefault = false;

    /**
     * Internal flag storing true if the queue endpoint was created using default settings.
     * False if the caller specified the queue endpoint explicitly.
     */
    private boolean isQueueEndpointDefault = false;

    /**
     * Internal flag storing true if the table endpoint was created using default settings.
     * False if the caller specified the table endpoint explicitly.
     */
    private boolean isTableEndpointDefault = false;

    /**
     * Internal flag storing true if this is a dev store account created by one of the
     * getDevelopmentStorageAccount methods, either called directly or by parsing a
     * connection string with the UseDevelopmentStorage flag. False otherwise.
     */
    private boolean isDevStoreAccount = false;

    /**
     * Creates an instance of the <code>CloudStorageAccount</code> class using the specified
     * account credentials.
     * <p>
     * With this constructor, the <code>CloudStorageAccount</code> object is constructed using the
     * default HTTP storage service endpoints. The default HTTP storage service endpoints are
     * <code>http://<i>myaccount</i>.blob.core.windows.net</code>,
     * <code>http://<i>myaccount</i>.queue.core.windows.net</code>,
     * <code>http://<i>myaccount</i>.table.core.windows.net</code>, and
     * <code>http://<i>myaccount</i>.file.core.windows.net</code>, where
     * <code><i>myaccount</i></code> is the name of your storage account.
     * <p>
     * The credentials provided when constructing the <code>CloudStorageAccount</code> object
     * are used to authenticate all further requests against resources that are accessed via
     * the <code>CloudStorageAccount</code> object or a client object created from it.
     * A client object may be a {@link CloudBlobClient} object.
     * 
     * @param storageCredentials
     *            A {@link StorageCredentials} object that represents the storage credentials
     *            to use to authenticate this account.
     * 
     * @throws URISyntaxException
     *             If <code>storageCredentials</code> specify an invalid account name.
     */
    public CloudStorageAccount(final StorageCredentials storageCredentials) throws URISyntaxException {
        // Protocol defaults to HTTP unless otherwise specified
        this(storageCredentials, false, null);
    }

    /**
     * Creates an instance of the <code>CloudStorageAccount</code> class using the specified
     * account credentials and the default service endpoints, using HTTP or HTTPS as specified.
     * <p>
     * With this constructor, the <code>CloudStorageAccount</code> object is constructed using
     * the default storage service endpoints. The default storage service endpoints are:
     * <code>[http|https]://<i>myaccount</i>.blob.core.windows.net</code>;
     * <code>[http|https]://<i>myaccount</i>.queue.core.windows.net</code>;
     * <code>[http|https]://<i>myaccount</i>.table.core.windows.net</code>; and
     * <code>[http|https]://<i>myaccount</i>.file.core.windows.net</code>,
     * where <code><i>myaccount</i></code> is the name of your storage account. Access to the cloud
     * storage account may be via HTTP or HTTPS, as specified by the <code>useHttps</code> parameter.
     * <p>
     * The credentials provided when constructing the <code>CloudStorageAccount</code> object
     * are used to authenticate all further requests against resources that are accessed via
     * the <code>CloudStorageAccount</code> object or a client object created from it. A client
     * object may be a {@link CloudBlobClient} object.
     * 
     * @param storageCredentials
     *            A {@link StorageCredentials} object that represents the storage credentials
     *            to use to authenticate this account.
     * @param useHttps
     *            <code>true</code> to use HTTPS to connect to the storage service endpoints;
     *            otherwise, <code>false</code>.
     * 
     * @throws URISyntaxException
     *             If <code>storageCredentials</code> specify an invalid account name.
     */
    public CloudStorageAccount(final StorageCredentials storageCredentials, final boolean useHttps)
            throws URISyntaxException {
        this (storageCredentials, useHttps, null);
    }
    
    /**
     * Creates an instance of the <code>CloudStorageAccount</code> class using the specified
     * account credentials.
     * <p>
     * With this constructor, the <code>CloudStorageAccount</code> object is constructed using the
     * given HTTP storage service endpoint suffix (if any, otherwise the default is used).
     * 
     * The credentials provided when constructing the <code>CloudStorageAccount</code> object
     * are used to authenticate all further requests against resources that are accessed via
     * the <code>CloudStorageAccount</code> object or a client object created from it.
     * A client object may be a {@link CloudBlobClient} object.
     * 
     * @param storageCredentials
     *            A {@link StorageCredentials} object that represents the storage credentials
     *            to use to authenticate this account.
     * @param useHttps
     *            <code>true</code> to use HTTPS to connect to the storage service endpoints;
     *            otherwise, <code>false</code>.
     * @param endpointSuffix
     *            A String that represents the endpointSuffix to use, if any.
     * 
     * @throws URISyntaxException
     *             If <code>storageCredentials</code> specify an invalid account name.
     */
    public CloudStorageAccount(
            final StorageCredentials storageCredentials, final boolean useHttps, final String endpointSuffix)
            throws URISyntaxException {
        this(storageCredentials, useHttps, endpointSuffix, null);
    }
    
    /**
     * Creates an instance of the <code>CloudStorageAccount</code> class using the specified
     * account credentials.
     * <p>
     * With this constructor, the <code>CloudStorageAccount</code> object is constructed using the
     * given HTTP storage service endpoint suffix (if any, otherwise the default is used).
     * 
     * The credentials provided when constructing the <code>CloudStorageAccount</code> object
     * are used to authenticate all further requests against resources that are accessed via
     * the <code>CloudStorageAccount</code> object or a client object created from it.
     * A client object may be a {@link CloudBlobClient} object.
     * 
     * @param storageCredentials
     *            A {@link StorageCredentials} object that represents the storage credentials
     *            to use to authenticate this account.
     * @param useHttps
     *            <code>true</code> to use HTTPS to connect to the storage service endpoints;
     *            otherwise, <code>false</code>.
     * @param endpointSuffix
     *            A String that represents the endpointSuffix to use, if any.
     * @param accountName
     *            A <code>String</code> that contains the account name.  This will be used in place of a
     *            <code>null</code> {@link StorageCredentials#getAccountName()}, but the two must match if
     *            both are not <code>null</code>.
     * 
     * @throws URISyntaxException
     *             If <code>storageCredentials</code> specify an invalid account name.
     */
    public CloudStorageAccount(
            final StorageCredentials storageCredentials, final boolean useHttps, final String endpointSuffix, String accountName)
            throws URISyntaxException {
        Utility.assertNotNull("storageCredentials", storageCredentials);
        if (Utility.isNullOrEmpty(accountName)) {
            accountName = storageCredentials.getAccountName();
        }
        else if (!Utility.isNullOrEmpty(storageCredentials.getAccountName()) &&
                !accountName.equals(storageCredentials.getAccountName())) {
            
            throw new IllegalArgumentException(SR.ACCOUNT_NAME_MISMATCH);
        }
        
        String protocol = useHttps ? Constants.HTTPS : Constants.HTTP;
        
        this.credentials = storageCredentials;
        this.blobStorageUri = getDefaultStorageUri(protocol, accountName, getDNS(SR.BLOB, endpointSuffix));
        this.fileStorageUri = getDefaultStorageUri(protocol, accountName, getDNS(SR.FILE, endpointSuffix));
        this.queueStorageUri = getDefaultStorageUri(protocol, accountName, getDNS(SR.QUEUE, endpointSuffix));
        this.tableStorageUri = getDefaultStorageUri(protocol, accountName, getDNS(SR.TABLE, endpointSuffix));
        this.endpointSuffix = endpointSuffix;
        
        this.isBlobEndpointDefault = true;
        this.isFileEndpointDefault = true;
        this.isQueueEndpointDefault = true;
        this.isTableEndpointDefault = true;
    }
    
    /**
     * Creates an instance of the <code>CloudStorageAccount</code> class using the specified
     * account credentials and service endpoints.
     * <p>
     * Use this constructor to construct a <code>CloudStorageAccount</code> object using custom
     * endpoints, in the case where you've configured a custom domain name for your storage account.
     * <p>
     * The credentials provided when constructing the <code>CloudStorageAccount</code> object
     * are used to authenticate all further requests against resources that are accessed via
     * the <code>CloudStorageAccount</code> object or a client object created from it. A
     * client object may be a {@link CloudBlobClient} object.
     * 
     * @param storageCredentials
     *            A {@link StorageCredentials} object that represents the storage credentials
     *            to use to authenticate this account.
     * @param blobEndpoint
     *            A <code>java.net.URI</code> object that represents the Blob service endpoint.
     * @param queueEndpoint
     *            A <code>java.net.URI</code> object that represents the Queue service endpoint.
     * @param tableEndpoint
     *            A <code>java.net.URI</code> object that represents the Table service endpoint.
     */
    public CloudStorageAccount(final StorageCredentials storageCredentials, final URI blobEndpoint,
            final URI queueEndpoint, final URI tableEndpoint) {
        this(storageCredentials, new StorageUri(blobEndpoint), new StorageUri(queueEndpoint),
                new StorageUri(tableEndpoint), null);
    }

    /**
     * Creates an instance of the <code>CloudStorageAccount</code> class using the specified
     * account credentials and service endpoints.
     * <p>
     * Use this constructor to construct a <code>CloudStorageAccount</code> object using custom
     * endpoints, in the case where you've configured a custom domain name for your storage account.
     * <p>
     * The credentials provided when constructing the <code>CloudStorageAccount</code> object
     * are used to authenticate all further requests against resources that are accessed via
     * the <code>CloudStorageAccount</code> object or a client object created from it. A client
     * object may be a {@link CloudBlobClient} object.
     * 
     * @param storageCredentials
     *            A {@link StorageCredentials} object that represents the storage credentials
     *            to use to authenticate this account.
     * @param blobEndpoint
     *            A <code>java.net.URI</code> object that represents the Blob service endpoint.
     * @param queueEndpoint
     *            A <code>java.net.URI</code> object that represents the Queue service endpoint.
     * @param tableEndpoint
     *            A <code>java.net.URI</code> object that represents the Table service endpoint.
     * @param fileEndpoint
     *            A <code>java.net.URI</code> object that represents the File service endpoint.
     */
    public CloudStorageAccount(final StorageCredentials storageCredentials, final URI blobEndpoint,
            final URI queueEndpoint, final URI tableEndpoint, final URI fileEndpoint) {
        this(storageCredentials, new StorageUri(blobEndpoint), new StorageUri(queueEndpoint),
                new StorageUri(tableEndpoint), new StorageUri(fileEndpoint));
    }

    /**
     * Creates an instance of the <code>CloudStorageAccount</code> class using the specified
     * account credentials and service endpoints.
     * <p>
     * Use this constructor to construct a <code>CloudStorageAccount</code> object using custom
     * endpoints, in the case where you've configured a custom domain name for your storage account.
     * <p>
     * The credentials provided when constructing the <code>CloudStorageAccount</code> object
     * are used to authenticate all further requests against resources that are accessed via
     * the <code>CloudStorageAccount</code> object or a client object created from it. A client
     * object may be a {@link CloudBlobClient} object.
     * 
     * @param storageCredentials
     *            A {@link StorageCredentials} object that represents the storage credentials
     *            to use to authenticate this account.
     * @param blobStorageUri
     *            A {@link StorageUri} object that represents the Blob service endpoint.
     * @param queueStorageUri
     *            A {@link StorageUri} object that represents the Queue service endpoint.
     * @param tableStorageUri
     *            A {@link StorageUri} object that represents the Table service endpoint.
     */
    public CloudStorageAccount(final StorageCredentials storageCredentials,
            final StorageUri blobStorageUri,
            final StorageUri queueStorageUri,
            final StorageUri tableStorageUri) {
        this(storageCredentials, blobStorageUri, queueStorageUri, tableStorageUri, null);
    }

    /**
     * Creates an instance of the <code>CloudStorageAccount</code> class using the specified
     * account credentials and service endpoints.
     * <p>
     * Use this constructor to construct a <code>CloudStorageAccount</code> object using custom
     * endpoints, in the case where you've configured a custom domain name for your storage account.
     * <p>
     * The credentials provided when constructing the <code>CloudStorageAccount</code> object are
     * used to authenticate all further requests against resources that are accessed via the
     * <code>CloudStorageAccount</code> object or a client object created from it.
     * A client object may be a {@link CloudBlobClient} object.
     * 
     * @param storageCredentials
     *            A {@link StorageCredentials} object that represents the storage credentials
     *            to use to authenticate this account.
     * @param blobStorageUri
     *            A {@link StorageUri} object that represents the Blob service endpoint.
     * @param queueStorageUri
     *            A {@link StorageUri} object that represents the Queue service endpoint.
     * @param tableStorageUri
     *            A {@link StorageUri} object that represents the Table service endpoint.
     * @param fileStorageUri
     *            A {@link StorageUri} object that represents the File service endpoint.
     */
    public CloudStorageAccount(
            final StorageCredentials storageCredentials, final StorageUri blobStorageUri,
            final StorageUri queueStorageUri, final StorageUri tableStorageUri,
            final StorageUri fileStorageUri) {
        this.credentials = storageCredentials;
        this.blobStorageUri = blobStorageUri;
        this.fileStorageUri = fileStorageUri;
        this.queueStorageUri = queueStorageUri;
        this.tableStorageUri = tableStorageUri;
        this.endpointSuffix = null;
    }

    /**
     * Creates a new Analytics service client.
     * 
     * @return An analytics client object that uses the Blob and Table service endpoints.
     */
    public CloudAnalyticsClient createCloudAnalyticsClient() {
        if (this.getBlobStorageUri() == null) {
            throw new IllegalArgumentException(SR.BLOB_ENDPOINT_NOT_CONFIGURED);
        }

        if (this.getTableStorageUri() == null) {
            throw new IllegalArgumentException(SR.TABLE_ENDPOINT_NOT_CONFIGURED);
        }

        if (this.credentials == null) {
            throw new IllegalArgumentException(SR.MISSING_CREDENTIALS);
        }

        return new CloudAnalyticsClient(this.getBlobStorageUri(), this.getTableStorageUri(), this.getCredentials());
    }

    /**
     * Creates a new Blob service client.
     * 
     * @return A {@link CloudBlobClient} that represents the cloud Blob client.
     * 
     */
    public CloudBlobClient createCloudBlobClient() {
        if (this.getBlobStorageUri() == null) {
            throw new IllegalArgumentException(SR.BLOB_ENDPOINT_NOT_CONFIGURED);
        }

        if (this.credentials == null) {
            throw new IllegalArgumentException(SR.MISSING_CREDENTIALS);
        }

        return new CloudBlobClient(this.getBlobStorageUri(), this.getCredentials());
    }

    /**
     * Creates a new File service client.
     * 
     * @return A {@link CloudFileClient} that represents the cloud File client.
     * 
     */
    public CloudFileClient createCloudFileClient() {
        if (this.getFileStorageUri() == null) {
            throw new IllegalArgumentException(SR.FILE_ENDPOINT_NOT_CONFIGURED);
        }

        if (this.credentials == null) {
            throw new IllegalArgumentException(SR.MISSING_CREDENTIALS);
        }

        if (!StorageCredentialsHelper.canCredentialsGenerateClient(this.credentials)) {
            
            throw new IllegalArgumentException(SR.CREDENTIALS_CANNOT_SIGN_REQUEST);
        }
        
        return new CloudFileClient(this.getFileStorageUri(), this.getCredentials());
    }

    /**
     * Creates a new Queue service client.
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

        if (!StorageCredentialsHelper.canCredentialsGenerateClient(this.credentials)) {
            
            throw new IllegalArgumentException(SR.CREDENTIALS_CANNOT_SIGN_REQUEST);
        }
        
        return new CloudQueueClient(this.getQueueStorageUri(), this.getCredentials());
    }

    /**
     * Creates a new Table service client.
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

        if (!StorageCredentialsHelper.canCredentialsGenerateClient(this.credentials)) {
            
            throw new IllegalArgumentException(SR.CREDENTIALS_CANNOT_SIGN_REQUEST);
        }
        
        return new CloudTableClient(this.getTableStorageUri(), this.getCredentials());
    }

    /**
     * Returns the endpoint for the Blob service for the storage account. This method is not supported when using shared
     * access signature credentials.
     * 
     * @return A <code>java.net.URI</code> object that represents the Blob endpoint associated with this account.
     */
    public URI getBlobEndpoint() {
        if (this.blobStorageUri == null) {
            return null;
        }

        return this.blobStorageUri.getPrimaryUri();
    }

    /**
     * Returns the endpoint for the Blob service for the storage account. This method is not supported when using shared
     * access signature credentials.
     * 
     * @return A {@link StorageUri} object that represents the Blob endpoint associated with this account.
     */
    public StorageUri getBlobStorageUri() {
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
     * If an endpoint suffix was specified, return it
     * @return the endpoint suffix
     */
    public String getEndpointSuffix() {
        return this.endpointSuffix;
    }

    /**
     * Returns the endpoint for the File service for the storage account. This method is not supported when using shared
     * access signature credentials.
     * 
     * @return A <code>java.net.URI</code> object that represents the File endpoint associated with this account.
     */
    public URI getFileEndpoint() {
        if (this.fileStorageUri == null) {
            return null;
        }

        return this.fileStorageUri.getPrimaryUri();
    }

    /**
     * Returns the endpoint for the File service for the storage account. This method is not supported when using shared
     * access signature credentials.
     * 
     * @return A {@link StorageUri} object that represents the File endpoint associated with this account.
     */
    public StorageUri getFileStorageUri() {
        return this.fileStorageUri;
    }

    /**
     * Returns the endpoint for the Queue service for the storage account.
     * 
     * @return A <code>java.net.URI</code> object that represents the queue endpoint associated with this account.
     */
    public URI getQueueEndpoint() {
        if (this.queueStorageUri == null) {
            return null;
        }

        return this.queueStorageUri.getPrimaryUri();
    }

    /**
     * Returns the endpoint for the Queue service for the storage account.
     * 
     * @return A {@link StorageUri} object that represents the Queue endpoint associated with this account.
     */
    public StorageUri getQueueStorageUri() {
        return this.queueStorageUri;
    }

    /**
     * Returns the endpoint for the Table service for the storage account.
     * 
     * @return A {@link StorageUri} object that represents the Table endpoint associated with this account.
     */
    public URI getTableEndpoint() {
        if (this.tableStorageUri == null) {
            return null;
        }

        return this.tableStorageUri.getPrimaryUri();
    }

    /**
     * Returns the endpoint for the Table service for the storage account.
     * 
     * @return A <code>java.net.URI</code> object that represents the Table endpoint associated with this account.
     */
    public StorageUri getTableStorageUri() {
        return this.tableStorageUri;
    }
    
    /**
     * Returns a shared access signature for the account.
     * 
     * @param policy
     *            A {@link SharedAccessAccountPolicy} specifying the access policy for the shared access signature.
     *            
     * @return The query string returned includes the leading question mark.
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws InvalidKeyException
     *             If the key is invalid.
     */
    public String generateSharedAccessSignature(SharedAccessAccountPolicy policy)
            throws InvalidKeyException, StorageException {
        if (!StorageCredentialsHelper.canCredentialsSignRequest(this.getCredentials())) {
            throw new IllegalArgumentException(SR.CANNOT_CREATE_SAS_WITHOUT_ACCOUNT_KEY);
        }
        
        final String sig = SharedAccessSignatureHelper.generateSharedAccessSignatureHashForAccount(
                this.credentials.getAccountName(), policy, this.getCredentials());
        final UriQueryBuilder sasBuilder =
                SharedAccessSignatureHelper.generateSharedAccessSignatureForAccount(policy, sig);
        return sasBuilder.toString();
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
     * @param exportSecrets
     *            <code>true</code> to include sensitive data in the string;
     *            otherwise, <code>false</code>.
     * @return A <code>String</code> that represents the connection string for this storage account,
     *         optionally with sensitive data.
     */
    public String toString(final boolean exportSecrets) {
        if (this.credentials != null && Utility.isNullOrEmpty(this.credentials.getAccountName())) {
            return this.credentials.toString(exportSecrets);
        }

        final List<String> values = new ArrayList<String>();
        if (this.isDevStoreAccount) {
            values.add(String.format("%s=true", USE_DEVELOPMENT_STORAGE_NAME));
            if (!this.getBlobEndpoint().toString().equals("http://127.0.0.1:10000/devstoreaccount1")) {
                values.add(String.format("%s=%s://%s/", DEVELOPMENT_STORAGE_PROXY_URI_NAME,
                        this.getBlobEndpoint().getScheme(), this.getBlobEndpoint().getHost()));
            }
        }
        else {
            final String attributeFormat = "%s=%s";
            boolean addDefault = false;
            
            if (this.endpointSuffix != null) {
                values.add(String.format(attributeFormat, ENDPOINT_SUFFIX_NAME, this.endpointSuffix));
            }
            
            if (this.getBlobStorageUri() != null) {
                if (this.isBlobEndpointDefault) {
                    addDefault = true;
                }
                else {
                    values.add(String.format(attributeFormat, BLOB_ENDPOINT_NAME, this.getBlobEndpoint()));
                }
            }

            if (this.getQueueStorageUri() != null) {
                if (this.isQueueEndpointDefault) {
                    addDefault = true;
                }
                else {
                    values.add(String.format(attributeFormat, QUEUE_ENDPOINT_NAME, this.getQueueEndpoint()));
                }
            }

            if (this.getTableStorageUri() != null) {
                if (this.isTableEndpointDefault) {
                    addDefault = true;
                }
                else {
                    values.add(String.format(attributeFormat, TABLE_ENDPOINT_NAME, this.getTableEndpoint()));
                }
            }

            if (this.getFileStorageUri() != null) {
                if (this.isFileEndpointDefault) {
                    addDefault = true;
                }
                else {
                    values.add(String.format(attributeFormat, FILE_ENDPOINT_NAME, this.getFileEndpoint()));
                }
            }

            if (addDefault) {
                values.add(String.format(attributeFormat, DEFAULT_ENDPOINTS_PROTOCOL_NAME,
                        this.getBlobEndpoint().getScheme()));
            }

            if (this.getCredentials() != null) {
                values.add(this.getCredentials().toString(exportSecrets));
            }
        }

        final StringBuilder returnString = new StringBuilder();
        for (final String val : values) {
            returnString.append(val);
            returnString.append(';');
        }

        // Remove trailing ';'
        if (values.size() > 0) {
            returnString.deleteCharAt(returnString.length() - 1);
        }

        return returnString.toString();
    }
    
    /**
     *  Sets the StorageCredentials to use with this account. Warning: for internal use only,
     *  as updating the credentials to a new account can invalidate pre-existing objects.
     *
     * @param credentials
     *     the credentials to set
    */
    protected void setCredentials(final StorageCredentials credentials) {
        this.credentials = credentials;
    }
}
