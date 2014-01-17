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
package com.microsoft.windowsazure.services.blob.client;

import java.net.URI;
import java.util.HashMap;

/**
 * RESERVED FOR INTERNAL USE. Represents a container's attributes, including its
 * properties and metadata.
 * 
 */
final class BlobContainerAttributes
{
    /**
     * Holds the Container Metadata
     */
    private HashMap<String, String> metadata;

    /**
     * Holds the Container Properties
     */
    private BlobContainerProperties properties;

    /**
     * Holds the Name of the Container
     */
    private String name;

    /**
     * Holds the URI of the container
     */
    private URI uri;

    /**
     * Initializes a new instance of the BlobContainerAttributes class
     */
    public BlobContainerAttributes()
    {
        this.setMetadata(new HashMap<String, String>());
        this.setProperties(new BlobContainerProperties());
    }

    public HashMap<String, String> getMetadata()
    {
        return this.metadata;
    }

    public String getName()
    {
        return this.name;
    }

    public BlobContainerProperties getProperties()
    {
        return this.properties;
    }

    public URI getUri()
    {
        return this.uri;
    }

    public void setMetadata(final HashMap<String, String> metadata)
    {
        this.metadata = metadata;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public void setProperties(final BlobContainerProperties properties)
    {
        this.properties = properties;
    }

    public void setUri(final URI uri)
    {
        this.uri = uri;
    }
}
