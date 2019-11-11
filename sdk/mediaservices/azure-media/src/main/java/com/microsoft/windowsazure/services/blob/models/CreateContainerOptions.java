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
package com.microsoft.windowsazure.services.blob.models;

import java.util.HashMap;

/**
 * Represents the options that may be set on a
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#createContainer(String, CreateContainerOptions)
 * createContainer} request. These options include a server response timeout for
 * the request, metadata to set on the container, and the public access level
 * for container and blob data. Options that are not set will not be passed to
 * the server with a request.
 */
public class CreateContainerOptions extends BlobServiceOptions {
    private String publicAccess;
    private HashMap<String, String> metadata = new HashMap<String, String>();

    /**
     * Sets the server request timeout value associated with this
     * {@link CreateContainerOptions} instance.
     * <p>
     * The <em>timeout</em> value only affects calls made on methods where this
     * {@link CreateContainerOptions} instance is passed as a parameter.
     * 
     * @param timeout
     *            The server request timeout value to set in milliseconds.
     * @return A reference to this {@link CreateContainerOptions} instance.
     */
    @Override
    public CreateContainerOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    /**
     * Gets the metadata collection associated with this
     * {@link CreateContainerOptions} instance.
     * 
     * @return A {@link java.util.HashMap} of name-value pairs of {@link String}
     *         containing the names and values of the container metadata to set.
     */
    public HashMap<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata collection associated with this
     * {@link CreateContainerOptions} instance. Metadata is a collection of
     * name-value {@link String} pairs for client use and is opaque to the
     * server. Metadata names must adhere to the naming rules for <a
     * href="http://msdn.microsoft.com/en-us/library/aa664670(VS.71).aspx">C#
     * identifiers</a>.
     * <p>
     * The <em>metadata</em> value only affects calls made on methods where this
     * {@link CreateContainerOptions} instance is passed as a parameter.
     * 
     * @param metadata
     *            A {@link java.util.HashMap} of name-value pairs of
     *            {@link String} containing the names and values of the
     *            container metadata to set.
     * @return A reference to this {@link CreateContainerOptions} instance.
     */
    public CreateContainerOptions setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Adds a metadata name-value pair to the metadata collection associated
     * with this {@link CreateContainerOptions} instance.
     * 
     * @param key
     *            A {@link String} containing the name portion of the name-value
     *            pair to add to the metadata collection.
     * @param value
     *            A {@link String} containing the value portion of the
     *            name-value pair to add to the metadata collection.
     * @return A reference to this {@link CreateContainerOptions} instance.
     */
    public CreateContainerOptions addMetadata(String key, String value) {
        this.getMetadata().put(key, value);
        return this;
    }

    /**
     * Gets the public access level value associated with this
     * {@link CreateContainerOptions} instance. The public access level
     * specifies whether data in the container may be accessed publicly and the
     * level of access. Possible values include:
     * <ul>
     * <li><em>container</em>&nbsp;&nbsp;Specifies full public read access for
     * container and blob data. Clients can enumerate blobs within the container
     * via anonymous request, but cannot enumerate containers within the storage
     * account.</li>
     * <li><em>blob</em>&nbsp;&nbsp;Specifies public read access for blobs. Blob
     * data within this container can be read via anonymous request, but
     * container data is not available. Clients cannot enumerate blobs within
     * the container via anonymous request.</li>
     * </ul>
     * The default value of <code>null</code> sets the container data private to
     * the storage account owner.
     * 
     * @return A {@link String} containing the public access level value to set,
     *         or <code>null</code>.
     */
    public String getPublicAccess() {
        return publicAccess;
    }

    /**
     * Sets the public access level value associated with this
     * {@link CreateContainerOptions} instance. The public access level
     * specifies whether data in the container may be accessed publicly and the
     * level of access. Possible values include:
     * <ul>
     * <li><em>container</em>&nbsp;&nbsp;Specifies full public read access for
     * container and blob data. Clients can enumerate blobs within the container
     * via anonymous request, but cannot enumerate containers within the storage
     * account.</li>
     * <li><em>blob</em>&nbsp;&nbsp;Specifies public read access for blobs. Blob
     * data within this container can be read via anonymous request, but
     * container data is not available. Clients cannot enumerate blobs within
     * the container via anonymous request.</li>
     * </ul>
     * The default value of <code>null</code> sets the container data private to
     * the storage account owner.
     * <p>
     * The <em>publicAccess</em> value only affects calls made on methods where
     * this {@link CreateContainerOptions} instance is passed as a parameter.
     * 
     * @param publicAccess
     *            A {@link String} containing the public access level value to
     *            set, or <code>null</code> to set the container data private to
     *            the storage account owner.
     * @return A reference to this {@link CreateContainerOptions} instance.
     */
    public CreateContainerOptions setPublicAccess(String publicAccess) {
        this.publicAccess = publicAccess;
        return this;
    }
}
