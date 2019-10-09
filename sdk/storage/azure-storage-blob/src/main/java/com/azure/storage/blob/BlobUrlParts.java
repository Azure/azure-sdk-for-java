// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.implementation.http.UrlBuilder;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.specialized.BlobServiceSasQueryParameters;
import com.azure.storage.common.Constants;
import com.azure.storage.common.Utility;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class represents the components that make up an Azure Storage Container/Blob URL. You may parse an
 * existing URL into its parts with the {@link #parse(URL)} class. You may construct a URL from parts by calling {@link
 * #toURL()}.
 */
public final class BlobUrlParts {
    private String scheme;
    private String host;
    private String containerName;
    private String blobName;
    private String snapshot;
    private String accountName;
    private BlobServiceSasQueryParameters blobServiceSasQueryParameters;
    private Map<String, String[]> unparsedParameters;

    /**
     * Initializes a BlobUrlParts object which helps aid in the construction of a Blob Storage URL.
     */
    public BlobUrlParts() {
        unparsedParameters = new HashMap<>();
    }

    /**
     * Gets the accountname, ex. "myaccountname".
     *
     * @return the account name.
     */
    public String getAccountName() {
        return accountName;
    }

    /**
     * Sets the account name.
     *
     * @param accountName The account name.
     * @return the updated BlobURLParts object.
     */
    public BlobUrlParts setAccountName(String accountName) {
        this.accountName = accountName;
        return this;
    }

    /**
     * Gets the URL scheme, ex. "https://".
     *
     * @return the URL scheme.
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * Sets the URL scheme, ex. "https://".
     *
     * @param scheme The URL scheme.
     * @return the updated BlobUrlParts object.
     */
    public BlobUrlParts setScheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    /**
     * Gets the URL host, ex. "account.blob.core.windows.net".
     *
     * @return the URL host.
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the URL host, ex. "account.blob.core.windows.net".
     *
     * @param host The URL host.
     * @return the updated BlobUrlParts object.
     */
    public BlobUrlParts setHost(String host) {
        this.host = host;
        return this;
    }

    /**
     * Gets the container name that will be used as part of the URL path.
     *
     * @return the container name.
     */
    public String getBlobContainerName() {
        return containerName;
    }

    /**
     * Sets the container name that will be used as part of the URL path.
     *
     * @param containerName The container nme.
     * @return the updated BlobUrlParts object.
     */
    public BlobUrlParts setContainerName(String containerName) {
        this.containerName = containerName;
        return this;
    }

    /**
     * Gets the blob name that will be used as part of the URL path.
     *
     * @return the blob name.
     */
    public String getBlobName() {
        return blobName;
    }

    /**
     * Sets the blob name that will be used as part of the URL path.
     *
     * @param blobName The blob name.
     * @return the updated BlobUrlParts object.
     */
    public BlobUrlParts setBlobName(String blobName) {
        this.blobName = blobName;
        return this;
    }

    /**
     * Gets the snapshot identifier that will be used as part of the query string if set.
     *
     * @return the snapshot identifier.
     */
    public String getSnapshot() {
        return snapshot;
    }

    /**
     * Sets the snapshot identifier that will be used as part of the query string if set.
     *
     * @param snapshot The snapshot identifier.
     * @return the updated BlobUrlParts object.
     */
    public BlobUrlParts setSnapshot(String snapshot) {
        this.snapshot = snapshot;
        return this;
    }

    /**
     * Gets the {@link BlobServiceSasQueryParameters} representing the SAS query parameters that will be used to
     * generate the SAS token for this URL.
     *
     * @return the {@link BlobServiceSasQueryParameters} of the URL
     */
    public BlobServiceSasQueryParameters getSasQueryParameters() {
        return blobServiceSasQueryParameters;
    }

    /**
     * Sets the {@link BlobServiceSasQueryParameters} representing the SAS query parameters that will be used to
     * generate the SAS token for this URL.
     *
     * @param blobServiceSasQueryParameters The SAS query parameters.
     * @return the updated BlobUrlParts object.
     */
    public BlobUrlParts setSasQueryParameters(BlobServiceSasQueryParameters blobServiceSasQueryParameters) {
        this.blobServiceSasQueryParameters = blobServiceSasQueryParameters;
        return this;
    }

    /**
     * Gets the query string parameters that aren't part of the SAS token that will be used by this URL.
     *
     * @return the non-SAS token query string values.
     */
    public Map<String, String[]> getUnparsedParameters() {
        return unparsedParameters;
    }

    /**
     * Sets the query string parameters that aren't part of the SAS token that will be used by this URL.
     *
     * @param unparsedParameters The non-SAS token query string values.
     * @return the updated BlobUrlParts object.
     */
    public BlobUrlParts setUnparsedParameters(Map<String, String[]> unparsedParameters) {
        this.unparsedParameters = unparsedParameters;
        return this;
    }

    /**
     * Converts the blob URL parts to a {@link URL}.
     *
     * @return A {@code URL} to the blob resource composed of all the elements in this object.
     * @throws MalformedURLException The fields present on the BlobUrlParts object were insufficient to construct a
     * valid URL or were ill-formatted.
     */
    public URL toURL() throws MalformedURLException {
        UrlBuilder url = new UrlBuilder().setScheme(this.scheme).setHost(this.host);

        StringBuilder path = new StringBuilder();

        if ((this.containerName == null || this.containerName.isEmpty()) && this.blobName != null) {
            this.containerName = BlobContainerAsyncClient.ROOT_CONTAINER_NAME;
        }

        if (this.containerName != null) {
            path.append(this.containerName);
            if (this.blobName != null) {
                path.append('/');
                path.append(this.blobName);
            }
        }
        url.setPath(path.toString());

        if (this.snapshot != null) {
            url.setQueryParameter(Constants.SNAPSHOT_QUERY_PARAMETER, this.snapshot);
        }
        if (this.blobServiceSasQueryParameters != null) {
            String encodedSAS = this.blobServiceSasQueryParameters.encode();
            if (encodedSAS.length() != 0) {
                url.setQuery(encodedSAS);
            }
        }

        for (Map.Entry<String, String[]> entry : this.unparsedParameters.entrySet()) {
            // The commas are intentionally encoded.
            url.setQueryParameter(entry.getKey(),
                Utility.urlEncode(String.join(",", entry.getValue())));
        }

        return url.toURL();
    }

    /**
     * URLParser parses a string URL initializing BlobUrlParts' fields including any SAS-related and snapshot query
     * parameters. Any other query parameters remain in the UnparsedParams field. This method overwrites all fields
     * in the BlobUrlParts object.
     *
     * @param url The string URL to be parsed.
     * @param logger Logger associated to the calling class to log a {@link MalformedURLException}.
     * @return A {@link BlobUrlParts} object containing all the components of a BlobURL.
     * @throws IllegalArgumentException If the {@code url} is malformed.
     */
    public static BlobUrlParts parse(String url, ClientLogger logger) {
        try {
            return parse(new URL(url));
        } catch (MalformedURLException e) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Please double check the URL format. URL: "
                + url));
        }
    }

    /**
     * Parses an existing URL into a BlobUrlParts.
     *
     * <p>Query parameters will be parsed into two properties, {@link BlobServiceSasQueryParameters} which contains
     * all SAS token related values and {@link #getUnparsedParameters() unparsedParameters} which is all other query
     * parameters.</p>
     *
     * <p>If a URL points to a blob in the root container, and the root container is referenced implicitly, i.e. there
     * is no path element for the container, the name of this blob in the root container will be set as the
     * containerName field in the resulting {@code BlobURLParts}.</p>
     *
     * @param url The {@code URL} to be parsed.
     * @return A {@link BlobUrlParts} object containing all the components of a BlobURL.
     */
    public static BlobUrlParts parse(URL url) {

        final String scheme = url.getProtocol();
        final String host = url.getHost();

        String containerName = null;
        String blobName = null;
        String accountName = null;

        //Parse host to get account name
        // host will look like this : <accountname>.blob.core.windows.net
        if (!ImplUtils.isNullOrEmpty(host)) {
            int accountNameIndex = host.indexOf('.');
            if (accountNameIndex == -1) {
                // host only contains account name
                accountName = host;
            } else {
                // if host is separated by .
                accountName = host.substring(0, accountNameIndex);
            }
        }

        // find the container & blob names (if any)
        String path = url.getPath();
        if (!ImplUtils.isNullOrEmpty(path)) {
            // if the path starts with a slash remove it
            if (path.charAt(0) == '/') {
                path = path.substring(1);
            }

            int containerEndIndex = path.indexOf('/');
            if (containerEndIndex == -1) {
                // path contains only a container name and no blob name
                containerName = path;
            } else {
                // path contains the container name up until the slash and blob name is everything after the slash
                containerName = path.substring(0, containerEndIndex);
                blobName = path.substring(containerEndIndex + 1);
            }
        }
        Map<String, String[]> queryParamsMap = parseQueryString(url.getQuery());

        String snapshot = null;
        String[] snapshotArray = queryParamsMap.get("snapshot");
        if (snapshotArray != null) {
            snapshot = snapshotArray[0];
            queryParamsMap.remove("snapshot");
        }

        BlobServiceSasQueryParameters blobServiceSasQueryParameters =
            new BlobServiceSasQueryParameters(queryParamsMap, true);

        return new BlobUrlParts()
            .setScheme(scheme)
            .setHost(host)
            .setContainerName(containerName)
            .setBlobName(blobName)
            .setSnapshot(snapshot)
            .setAccountName(accountName)
            .setSasQueryParameters(blobServiceSasQueryParameters)
            .setUnparsedParameters(queryParamsMap);
    }

    /**
     * Parses a query string into a one to many hashmap.
     *
     * @param queryParams The string of query params to parse.
     * @return A {@code HashMap<String, String[]>} of the key values.
     */
    private static TreeMap<String, String[]> parseQueryString(String queryParams) {
        final TreeMap<String, String[]> retVals = new TreeMap<>(Comparator.naturalOrder());

        if (ImplUtils.isNullOrEmpty(queryParams)) {
            return retVals;
        }

        // split name value pairs by splitting on the '&' character
        final String[] valuePairs = queryParams.split("&");

        // for each field value pair parse into appropriate map entries
        for (String valuePair : valuePairs) {
            // Getting key and value for a single query parameter
            final int equalDex = valuePair.indexOf("=");
            String key = Utility.urlDecode(valuePair.substring(0, equalDex)).toLowerCase(Locale.ROOT);
            String value = Utility.urlDecode(valuePair.substring(equalDex + 1));

            // add to map
            String[] keyValues = retVals.get(key);

            // check if map already contains key
            if (keyValues == null) {
                // map does not contain this key
                keyValues = new String[]{value};
            } else {
                // map contains this key already so append
                final String[] newValues = new String[keyValues.length + 1];
                System.arraycopy(keyValues, 0, newValues, 0, keyValues.length);

                newValues[newValues.length - 1] = value;
                keyValues = newValues;
            }
            retVals.put(key, keyValues);
        }

        return retVals;
    }
}
