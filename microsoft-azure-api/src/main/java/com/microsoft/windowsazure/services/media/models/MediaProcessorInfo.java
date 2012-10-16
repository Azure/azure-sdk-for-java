/**
 * Copyright 2012 Microsoft Corporation
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

package com.microsoft.windowsazure.services.media.models;

import com.microsoft.windowsazure.services.media.implementation.ODataEntity;
import com.microsoft.windowsazure.services.media.implementation.atom.EntryType;
import com.microsoft.windowsazure.services.media.implementation.content.MediaProcessorType;

// TODO: Auto-generated Javadoc
/**
 * Data about a Media Processor entity.
 * 
 */
public class MediaProcessorInfo extends ODataEntity<MediaProcessorType> {

    /**
     * Instantiates a new media processor info.
     * 
     * @param entry
     *            the entry
     * @param content
     *            the content
     */
    public MediaProcessorInfo(EntryType entry, MediaProcessorType content) {
        super(entry, content);
    }

    /**
     * Instantiates a new media processor info.
     */
    public MediaProcessorInfo() {
        super(new MediaProcessorType());
    }

    /**
     * Get the asset id.
     * 
     * @return the id
     */
    public String getId() {
        return getContent().getId();
    }

    /**
     * Set the id.
     * 
     * @param id
     *            the id
     * @return the asset info
     */
    public MediaProcessorInfo setId(String id) {
        getContent().setId(id);
        return this;
    }

    /**
     * Get the asset name.
     * 
     * @return the name
     */
    public String getName() {
        return this.getContent().getName();
    }

    /**
     * set the name.
     * 
     * @param name
     *            the name
     * @return the asset info
     */
    public MediaProcessorInfo setName(String name) {
        this.getContent().setName(name);
        return this;
    }

    /**
     * Gets the description.
     * 
     * @return the description
     */
    public String getDescription() {
        return this.getContent().getDescription();
    }

    /**
     * Sets the description.
     * 
     * @param description
     *            the description
     * @return the media processor info
     */
    public MediaProcessorInfo setDescription(String description) {
        this.getContent().setDescription(description);
        return this;
    }

    /**
     * Gets the sku.
     * 
     * @return the sku
     */
    public String getSku() {
        return this.getContent().getSku();
    }

    /**
     * Sets the sku.
     * 
     * @param sku
     *            the sku
     * @return the media processor info
     */
    public MediaProcessorInfo setSku(String sku) {
        this.getContent().setSku(sku);
        return this;
    }

    /**
     * Gets the vendor.
     * 
     * @return the vendor
     */
    public String getVendor() {
        return this.getContent().getVendor();
    }

    /**
     * Sets the vendor.
     * 
     * @param vendor
     *            the vendor
     * @return the media processor info
     */
    public MediaProcessorInfo setVendor(String vendor) {
        this.getContent().setVendor(vendor);
        return this;
    }

    /**
     * Gets the version.
     * 
     * @return the version
     */
    public String getVersion() {
        return this.getContent().getVersion();
    }

    /**
     * Sets the version.
     * 
     * @param version
     *            the version
     * @return the media processor info
     */
    public MediaProcessorInfo setVersion(String version) {
        this.getContent().setVersion(version);
        return this;
    }
}
