/**
 * 
 */
package com.microsoft.windowsazure.services.core.storage;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.HashMap;

import com.microsoft.windowsazure.services.core.storage.utils.Base64;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;

/**
 * Represents a set of credentials used to authenticate access to a Windows Azure storage account. This is the base
 * class for the {@link StorageCredentialsAccountAndKey} and {@link StorageCredentialsSharedAccessSignature} classes.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 */
public abstract class StorageCredentials {

    /**
     * Tries to determine the storage credentials from a collection of name/value pairs.
     * 
     * @param settings
     *            A <code>HashMap</code> object of the name/value pairs that represent the settings to use to configure
     *            the credentials.
     *            <p>
     *            Either include an account name with an account key (specifying values for
     *            {@link CloudStorageAccount#ACCOUNT_NAME_NAME} and {@link CloudStorageAccount#ACCOUNT_KEY_NAME} ), or a
     *            shared access signature (specifying a value for
     *            {@link CloudStorageAccount#SHARED_ACCESS_SIGNATURE_NAME} ). If you use an account name and account
     *            key, do not include a shared access signature, and vice versa.
     * 
     * @return A {@link StorageCredentials} object representing the storage credentials determined from the name/value
     *         pairs.
     * 
     * @throws InvalidKeyException
     *             If the key value specified for {@link CloudStorageAccount#ACCOUNT_KEY_NAME} is not a valid
     *             Base64-encoded string.
     */
    protected static StorageCredentials tryParseCredentials(final HashMap<String, String> settings)
            throws InvalidKeyException {
        final String accountName = settings.get(CloudStorageAccount.ACCOUNT_NAME_NAME) != null ? settings
                .get(CloudStorageAccount.ACCOUNT_NAME_NAME) : null;

        final String accountKey = settings.get(CloudStorageAccount.ACCOUNT_KEY_NAME) != null ? settings
                .get(CloudStorageAccount.ACCOUNT_KEY_NAME) : null;

        final String sasSignature = settings.get(CloudStorageAccount.SHARED_ACCESS_SIGNATURE_NAME) != null ? settings
                .get(CloudStorageAccount.SHARED_ACCESS_SIGNATURE_NAME) : null;

        if (accountName != null && accountKey != null && sasSignature == null) {
            if (Base64.validateIsBase64String(accountKey)) {
                return new StorageCredentialsAccountAndKey(accountName, accountKey);
            }
            else {
                throw new InvalidKeyException("Storage Key is not a valid base64 encoded string.");
            }
        }
        if (accountName == null && accountKey == null && sasSignature != null) {
            return new StorageCredentialsSharedAccessSignature(sasSignature);
        }

        return null;
    }

    /**
     * Tries to determine the storage credentials from a connection string.
     * 
     * @param connectionString
     *            A <code>String</code> that contains the key/value pairs that represent the storage credentials.
     *            <p>
     *            The format for the connection string is in the pattern "<i>keyname=value</i>". Multiple key/value
     *            pairs can be separated by a semi-colon, for example, "<i>keyname1=value1;keyname2=value2</i>".
     * 
     * @return A {@link StorageCredentials} object representing the storage credentials determined from the connection
     *         string.
     * 
     * @throws InvalidKeyException
     *             If the account key specified in <code>connectionString</code> is not valid.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public static StorageCredentials tryParseCredentials(final String connectionString) throws InvalidKeyException,
            StorageException {
        return tryParseCredentials(Utility.parseAccountString(connectionString));
    }

    //
    // RESERVED, for internal use only. Gets a value indicating whether the
    // <Code>ComputeHmac</Code> method will return a valid HMAC-encoded
    // signature string when called using the specified credentials.
    //
    // @return <Code>True</Code> if these credentials will yield a valid
    // signature string; otherwise, <Code>false</Code>
    //
    /** Reserved. */
    public abstract boolean canCredentialsComputeHmac();

    //
    // RESERVED, for internal use only. Gets a value indicating whether a
    // request can be signed under the Shared Key authentication scheme using
    // the specified credentials.
    //
    // @return <Code>True</Code> if a request can be signed with these
    // credentials; otherwise, <Code>false</Code>
    //
    /** Reserved. */
    public abstract boolean canCredentialsSignRequest();

    //
    // RESERVED, for internal use only. Gets a value indicating whether a
    // request can be signed under the Shared Key Lite authentication scheme
    // using the specified credentials.
    //
    // @return <code>true</code> if a request can be signed with these
    // credentials; otherwise, <code>false</code>
    //
    /** Reserved. */
    public abstract boolean canCredentialsSignRequestLite();

    /**
     * Computes a signature for the specified string using the HMAC-SHA256 algorithm.
     * 
     * @param value
     *            The UTF-8-encoded string to sign.
     * 
     * @return A <code>String</code> that contains the HMAC-SHA256-encoded signature.
     * 
     * @throws InvalidKeyException
     *             If the key is not a valid Base64-encoded string.
     */
    public abstract String computeHmac256(String value) throws InvalidKeyException;

    /**
     * Computes a signature for the specified string using the HMAC-SHA256 algorithm with the specified operation
     * context.
     * 
     * @param value
     *            The UTF-8-encoded string to sign.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return A <code>String</code> that contains the HMAC-SHA256-encoded signature.
     * 
     * @throws InvalidKeyException
     *             If the key is not a valid Base64-encoded string.
     */
    public abstract String computeHmac256(String value, OperationContext opContext) throws InvalidKeyException;

    /**
     * Computes a signature for the specified string using the HMAC-SHA512 algorithm.
     * 
     * @param value
     *            The UTF-8-encoded string to sign.
     * 
     * @return A <code>String</code> that contains the HMAC-SHA512-encoded signature.
     * 
     * @throws InvalidKeyException
     *             If the key is not a valid Base64-encoded string.
     */
    public abstract String computeHmac512(String value) throws InvalidKeyException;

    /**
     * Computes a signature for the specified string using the HMAC-SHA512 algorithm with the specified operation
     * context.
     * 
     * @param value
     *            The UTF-8-encoded string to sign.
     * 
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return A <code>String</code> that contains the HMAC-SHA512-encoded signature.
     * 
     * @throws InvalidKeyException
     *             If the key is not a valid Base64-encoded string.
     */
    public abstract String computeHmac512(String value, OperationContext opContext) throws InvalidKeyException;

    //
    // RESERVED, for internal use only. Gets a value indicating whether the
    // <Code>TransformUri</Code> method should be called to transform a resource
    // URI to a URI that includes a token for a shared access signature.
    //
    // @return <Code>True</Code> if the URI must be transformed; otherwise,
    // <Code>false</Code>
    //
    /** Reserved. */
    public abstract boolean doCredentialsNeedTransformUri();

    /**
     * Returns the associated account name for the credentials.
     * 
     * @return A <code>String</code> that represents the associated account name for the credentials
     */
    public abstract String getAccountName();

    /**
     * Signs a request under the Shared Key authentication scheme.
     * 
     * @param request
     *            An <code>HttpURLConnection</code> object that represents the request to sign.
     * @param contentLength
     *            The length of the content written to the output stream. If unknown, specify -1.
     * 
     * @throws InvalidKeyException
     *             If the given key is invalid.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public abstract void signRequest(HttpURLConnection request, long contentLength) throws InvalidKeyException,
            StorageException;

    /**
     * Signs a request using the specified operation context under the Shared Key authentication scheme.
     * 
     * @param request
     *            An <code>HttpURLConnection</code> object that represents the request to sign.
     * @param contentLength
     *            The length of the content written to the output stream. If unknown, specify -1.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws InvalidKeyException
     *             If the given key is invalid.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public abstract void signRequest(HttpURLConnection request, long contentLength, OperationContext opContext)
            throws InvalidKeyException, StorageException;

    /**
     * Signs a request under the Shared Key Lite authentication scheme.
     * 
     * @param request
     *            An <code>HttpURLConnection</code> object that represents the request to sign.
     * @param contentLength
     *            The length of the content written to the output stream. If unknown, specify -1.
     * 
     * @throws InvalidKeyException
     *             If the given key is invalid.
     * @throws StorageException
     *             If an unspecified storage exception occurs.
     */
    public abstract void signRequestLite(HttpURLConnection request, long contentLength) throws StorageException,
            InvalidKeyException;

    /**
     * Signs a request using the specified operation context under the Shared Key Lite authentication scheme.
     * 
     * @param request
     *            An <code>HttpURLConnection</code> object that represents the request to sign.
     * @param contentLength
     *            The length of the content written to the output stream. If unknown, specify -1.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws InvalidKeyException
     *             If the given key is invalid.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public abstract void signRequestLite(HttpURLConnection request, long contentLength, OperationContext opContext)
            throws StorageException, InvalidKeyException;

    /**
     * Returns a <code>String</code> that represents this instance.
     * 
     * @param exportSecrets
     *            <code>true</code> to include sensitive data in the return string; otherwise, <code>false</code>.
     * @return A <code>String</code> that represents this object, optionally including sensitive data.
     */
    public abstract String toString(boolean exportSecrets);

    /**
     * Transforms a resource URI into a shared access signature URI, by appending a shared access token.
     * 
     * @param resourceUri
     *            A <code>java.net.URI</code> object that represents the resource URI to be transformed.
     * 
     * @return A <code>java.net.URI</code> object that represents the signature, including the resource URI and the
     *         shared access token.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is not properly formatted.
     */
    public abstract URI transformUri(URI resourceUri) throws URISyntaxException, StorageException;

    /**
     * Transforms a resource URI into a shared access signature URI, by appending a shared access token and using the
     * specified operation context.
     * 
     * @param resourceUri
     *            A <code>java.net.URI</code> object that represents the resource URI to be transformed.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return A <code>java.net.URI</code> object that represents the signature, including the resource URI and the
     *         shared access token.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is not properly formatted.
     */
    public abstract URI transformUri(URI resourceUri, OperationContext opContext) throws URISyntaxException,
            StorageException;
}
