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

import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;

/**
 * Holds a list of URIs that represents the storage resource.
 */
public class StorageUri {

    private static boolean AreUrisEqual(URI uri1, URI uri2) {
        return uri1 == null ? uri2 == null : uri1.equals(uri2);
    }

    private static void AssertAbsoluteUri(URI uri) {
        if ((uri != null) && !uri.isAbsolute()) {
            String errorMessage = String.format(Utility.LOCALE_US, SR.RELATIVE_ADDRESS_NOT_PERMITTED, uri.toString());
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private URI primaryUri;

    private URI secondaryUri;

    /**
     * Initializes a new instance of the <code>StorageUri</code> class using the URI specified.
     */
    public StorageUri(URI primaryUri) {
        this(primaryUri, null /* secondaryUri */);
    }

    /**
     * Initializes a new instance of the <code>StorageUri</code> class using the URI specified.
     */
    public StorageUri(URI primaryUri, URI secondaryUri) {
        if (primaryUri != null && secondaryUri != null) {
            // check query component is equivalent
            if ((primaryUri.getQuery() == null && secondaryUri.getQuery() != null)
                    || (primaryUri.getQuery() != null && !(primaryUri.getQuery().equals(secondaryUri.getQuery())))) {
                throw new IllegalArgumentException(SR.STORAGE_URI_MUST_MATCH);
            }

            boolean primaryPathStyle = Utility.determinePathStyleFromUri(primaryUri);
            boolean secondaryPathStyle = Utility.determinePathStyleFromUri(secondaryUri);

            if (!primaryPathStyle && !secondaryPathStyle) {
                if ((primaryUri.getPath() == null && secondaryUri.getPath() != null)
                        || (primaryUri.getPath() != null && !(primaryUri.getPath().equals(secondaryUri.getPath())))) {
                    throw new IllegalArgumentException(SR.STORAGE_URI_MUST_MATCH);
                }
            }
            else {
                final int maxPrimaryPathSegments = primaryPathStyle ? 3 : 2;
                final int maxSecondaryPathSegments = secondaryPathStyle ? 3 : 2;

                // getPath() on a path-style uri returns /devstore1[/path1/path2], split(3) returns ["","devstore","path1/path2"]
                // getPath() on a regular uri returns [/path1/path2], split(2) returns ["","path1/path2"]
                final String[] primaryPathSegments = primaryUri.getPath().split("/", maxPrimaryPathSegments);
                final String[] secondaryPathSegments = secondaryUri.getPath().split("/", maxSecondaryPathSegments);

                String primaryPath = "";
                if (primaryPathSegments.length == maxPrimaryPathSegments) {
                    primaryPath = primaryPathSegments[primaryPathSegments.length - 1];
                }

                String secondaryPath = "";
                if (secondaryPathSegments.length == maxSecondaryPathSegments) {
                    secondaryPath = secondaryPathSegments[secondaryPathSegments.length - 1];
                }

                if (!primaryPath.equals(secondaryPath)) {
                    throw new IllegalArgumentException(SR.STORAGE_URI_MUST_MATCH);
                }
            }
        }

        this.setPrimaryUri(primaryUri);
        this.setSecondaryUri(secondaryUri);
    }

    @Override
    public boolean equals(Object obj) {
        return this.equals((StorageUri) obj);
    }

    /**
     * Indicates whether some other <code>StorageUri</code> object is "equal to" this one.
     * 
     * @param other
     *            the reference <code>StorageUri</code> object with which to compare.
     * @return
     *         <code>true</code> if this object is the same as the other argument; <code>false</code> otherwise.
     */
    public boolean equals(StorageUri other) {
        return (other != null) && StorageUri.AreUrisEqual(this.primaryUri, other.primaryUri)
                && StorageUri.AreUrisEqual(this.secondaryUri, other.secondaryUri);
    }

    /**
     * Gets the endpoint for the primary location for the storage account.
     * 
     * @return the primaryUri
     */
    public URI getPrimaryUri() {
        return this.primaryUri;
    }

    /**
     * Gets the endpoint for the secondary location for the storage account.
     * 
     * @return the secondaryUri
     */
    public URI getSecondaryUri() {
        return this.secondaryUri;
    }

    /**
     * Gets the URI given a Storage Location.
     * 
     * @param location
     *            The <code>StorageLocation</code> object whose corresponding Uri is going to be returned.
     * @return
     *         <code>java.net.URI</code> given the location.
     */
    public URI getUri(StorageLocation location) {
        switch (location) {
            case PRIMARY:
                return this.primaryUri;

            case SECONDARY:
                return this.secondaryUri;

            default:
                throw new IllegalArgumentException(String.format(Utility.LOCALE_US, SR.ARGUMENT_OUT_OF_RANGE_ERROR,
                        "location", location.toString()));
        }
    }

    @Override
    public int hashCode() {
        int hash1 = this.primaryUri != null ? this.primaryUri.hashCode() : 0;
        int hash2 = this.secondaryUri != null ? this.secondaryUri.hashCode() : 0;
        return hash1 ^ hash2;
    }

    /**
     * Sets the endpoint for the primary location for the storage account.
     * 
     * @param primaryUri
     *            the primaryUri to set
     */
    private void setPrimaryUri(URI primaryUri) {
        Utility.assertNotNull("primaryUri", primaryUri);
        StorageUri.AssertAbsoluteUri(primaryUri);
        this.primaryUri = primaryUri;
    }

    /**
     * Sets the endpoint for the secondary location for the storage account.
     * 
     * @param secondaryUri
     *            the secondaryUri to set
     */
    private void setSecondaryUri(URI secondaryUri) {
        StorageUri.AssertAbsoluteUri(secondaryUri);
        this.secondaryUri = secondaryUri;
    }

    @Override
    public String toString() {
        return String.format(Utility.LOCALE_US, "Primary = '%s'; Secondary = '%s'", this.primaryUri, this.secondaryUri);
    }

    /**
     * Validate that we have the URI corresponding to the given <code>LocationMode</code>.
     * 
     * @param mode
     *            The <code>LocationMode</code> object.
     * @return
     *         <code>true</code> if the mode is valid; <code>false</code> otherwise.
     */
    public boolean validateLocationMode(LocationMode mode) {
        switch (mode) {
            case PRIMARY_ONLY:
                return this.primaryUri != null;

            case SECONDARY_ONLY:
                return this.secondaryUri != null;

            default:
                return (this.primaryUri != null) && (this.secondaryUri != null);
        }
    }

    /**
     * Tells whether or not this <code>StorageUri</code> is absolute.
     * 
     * @return
     *         <code>true</code> if, and only if, this <code>StorageUri</code> is absolute.
     */
    public boolean isAbsolute() {
        if (this.secondaryUri == null) {
            return this.primaryUri.isAbsolute();
        }
        else {
            return this.primaryUri.isAbsolute() && this.secondaryUri.isAbsolute();
        }
    }

    /**
     * Returns the decoded query component of this <code>StorageUri</code> object.
     * 
     * @return
     *         The decoded query component of this <code>StorageUri</code>, or <code>null</code> if the query is
     *         undefined.
     */
    public String getQuery() {
        return this.primaryUri.getQuery();
    }
}
