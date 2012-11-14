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

import java.net.URI;
import java.util.Date;

import com.microsoft.windowsazure.services.media.implementation.ODataEntity;
import com.microsoft.windowsazure.services.media.implementation.atom.EntryType;
import com.microsoft.windowsazure.services.media.implementation.content.AssetType;

/**
 * Data about a Media Services Asset entity.
 * 
 */
public class AssetInfo extends ODataEntity<AssetType> {

    private URI uri;

    public AssetInfo(EntryType entry, AssetType content) {
        super(entry, content);
    }

    public AssetInfo() {
        super(new AssetType());
    }

    /**
     * Get the asset id
     * 
     * @return the id
     */
    public String getId() {
        return getContent().getId();
    }

    /**
     * Set the id
     * 
     * @param id
     *            the id
     * @return the asset info
     */
    public AssetInfo setId(String id) {
        getContent().setId(id);
        return this;
    }

    /**
     * Get the asset name
     * 
     * @return the name
     */
    public String getName() {
        return this.getContent().getName();
    }

    /**
     * set the name
     * 
     * @param name
     *            the name
     * @return the asset info
     */
    public AssetInfo setName(String name) {
        this.getContent().setName(name);
        return this;
    }

    /**
     * Get the asset state
     * 
     * @return the state
     */
    public AssetState getState() {
        return AssetState.fromCode(getContent().getState());
    }

    /**
     * Set the state
     * 
     * @param state
     *            the state
     * @return the asset info
     */
    public AssetInfo setState(AssetState state) {
        getContent().setState(state.getCode());
        return this;
    }

    /**
     * Get the creation date
     * 
     * @return the date
     */
    public Date getCreated() {
        return this.getContent().getCreated();
    }

    /**
     * Set creation date
     * 
     * @param created
     *            the date
     * @return the asset info
     */
    public AssetInfo setCreated(Date created) {
        getContent().setCreated(created);
        return this;
    }

    /**
     * Get last modified date
     * 
     * @return the date
     */
    public Date getLastModified() {
        return getContent().getLastModified();
    }

    /**
     * Set last modified date
     * 
     * @param lastModified
     *            the date
     * @return the asset info
     */
    public AssetInfo setLastModified(Date lastModified) {
        getContent().setLastModified(lastModified);
        return this;
    }

    /**
     * Get the alternate id
     * 
     * @return the id
     */
    public String getAlternateId() {
        return getContent().getAlternateId();
    }

    /**
     * Set the alternate id
     * 
     * @param alternateId
     *            the id
     * @return the asset info
     */
    public AssetInfo setAlternateId(String alternateId) {
        getContent().setAlternateId(alternateId);
        return this;
    }

    /**
     * Get the options
     * 
     * @return the options
     */
    public EncryptionOption getOptions() {
        return EncryptionOption.fromCode(getContent().getOptions());
    }

}
