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
 * A wrapper class for the response returned from a Blob Service REST API Get
 * Blob Properties operation. This is returned by calls to implementations of
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#getBlobProperties(String, String)} and
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#getBlobProperties(String, String, GetBlobPropertiesOptions)}
 * .
 * <p>
 * See the <a
 * href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179394.aspx">Get
 * Blob Properties</a> documentation on MSDN for details of the underlying Blob
 * Service REST API operation.
 */
public class GetBlobPropertiesResult {
    private BlobProperties properties;
    private HashMap<String, String> metadata = new HashMap<String, String>();

    /**
     * Gets the standard HTTP properties and system properties of the blob.
     * 
     * @return A {@link BlobProperties} instance containing the properties of
     *         the blob.
     */
    public BlobProperties getProperties() {
        return properties;
    }

    /**
     * Reserved for internal use. Sets the blob properties from the headers
     * returned in the response.
     * <p>
     * This method is invoked by the API to set the value from the Blob Service
     * REST API operation response returned by the server.
     * 
     * @param properties
     *            A {@link BlobProperties} instance containing the properties of
     *            the blob.
     */
    public void setProperties(BlobProperties properties) {
        this.properties = properties;
    }

    /**
     * Gets the user-defined blob metadata as a map of name and value pairs. The
     * metadata is for client use and is opaque to the server.
     * 
     * @return A {@link java.util.HashMap} of key-value pairs of {@link String}
     *         containing the names and values of the blob metadata.
     */
    public HashMap<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Reserved for internal use. Sets the blob metadata from the
     * <code>x-ms-meta-<em>name:value</em></code> headers returned in the
     * response.
     * <p>
     * This method is invoked by the API to set the value from the Blob Service
     * REST API operation response returned by the server.
     * 
     * @param metadata
     *            A {@link java.util.HashMap} of key-value pairs of
     *            {@link String} containing the names and values of the blob
     *            metadata.
     */
    public void setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
    }
}
