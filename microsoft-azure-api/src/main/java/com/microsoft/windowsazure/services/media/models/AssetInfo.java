/*
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

import java.util.Date;

/**
 * The Class AssetInfo.
 */
public class AssetInfo {

    /** The id. */
    private String id;

    /** The state. */
    private AssetState state;

    /** The created. */
    private Date created;

    /** The last modified. */
    private Date lastModified;

    /** The alternate id. */
    private String alternateId;

    /** The name. */
    private String name;

    /** The options. */
    private EncryptionOption options;

    /** The locator infos. */
    private Iterable<LocatorInfo> locatorInfos;

    /** The content key infos. */
    private Iterable<ContentKeyInfo> contentKeyInfos;

    /** The file infos. */
    private Iterable<FileInfo> fileInfos;

    /** The parent asset infos. */
    private Iterable<AssetInfo> parentAssetInfos;

    /**
     * Gets the id.
     * 
     * @return the id
     */
    public String getId() {
        return this.id;
    }

    /**
     * Sets the id.
     * 
     * @param id
     *            the id
     * @return the asset info
     */
    public AssetInfo setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Gets the state.
     * 
     * @return the state
     */
    public AssetState getState() {
        return this.state;
    }

    /**
     * Sets the state.
     * 
     * @param state
     *            the state
     * @return the asset info
     */
    public AssetInfo setState(AssetState state) {
        this.state = state;
        return this;
    }

    /**
     * Gets the created.
     * 
     * @return the created
     */
    public Date getCreated() {
        return this.created;
    }

    /**
     * Sets the create.
     * 
     * @param created
     *            the created
     * @return the asset info
     */
    public AssetInfo setCreate(Date created) {
        this.created = created;
        return this;
    }

    /**
     * Gets the last modified.
     * 
     * @return the last modified
     */
    public Date getLastModified() {
        return this.lastModified;
    }

    /**
     * Sets the last modified.
     * 
     * @param lastModified
     *            the last modified
     * @return the asset info
     */
    public AssetInfo setLastModified(Date lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    /**
     * Gets the alternate id.
     * 
     * @return the alternate id
     */
    public String getAlternateId() {
        return this.alternateId;
    }

    /**
     * Sets the alternate id.
     * 
     * @param alternateId
     *            the alternate id
     * @return the asset info
     */
    public AssetInfo setAlternateId(String alternateId) {
        this.alternateId = alternateId;
        return this;
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name.
     * 
     * @param name
     *            the name
     * @return the asset info
     */
    public AssetInfo setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets the options.
     * 
     * @return the options
     */
    public EncryptionOption getOptions() {
        return this.options;
    }

    /**
     * Sets the options.
     * 
     * @param options
     *            the options
     * @return the asset info
     */
    public AssetInfo setOptions(EncryptionOption options) {
        this.options = options;
        return this;
    }

    /**
     * Gets the locators.
     * 
     * @return the locators
     */
    public Iterable<LocatorInfo> getLocators() {
        return this.locatorInfos;
    }

    /**
     * Sets the locators.
     * 
     * @param locatorInfos
     *            the locator infos
     * @return the asset info
     */
    public AssetInfo setLocators(Iterable<LocatorInfo> locatorInfos) {
        this.locatorInfos = locatorInfos;
        return this;
    }

    /**
     * Gets the content keys.
     * 
     * @return the content keys
     */
    public Iterable<ContentKeyInfo> getContentKeys() {
        return this.contentKeyInfos;
    }

    /**
     * Sets the content keys.
     * 
     * @param expectedContentKeys
     *            the expected content keys
     * @return the asset info
     */
    public AssetInfo setContentKeys(Iterable<ContentKeyInfo> expectedContentKeys) {
        this.contentKeyInfos = expectedContentKeys;
        return this;
    }

    /**
     * Gets the files.
     * 
     * @return the files
     */
    public Iterable<FileInfo> getFiles() {
        return this.fileInfos;
    }

    /**
     * Sets the files.
     * 
     * @param fileInfos
     *            the file infos
     * @return the asset info
     */
    public AssetInfo setFiles(Iterable<FileInfo> fileInfos) {
        this.fileInfos = fileInfos;
        return this;
    }

    /**
     * Gets the parent assets.
     * 
     * @return the parent assets
     */
    public Iterable<AssetInfo> getParentAssets() {
        return this.parentAssetInfos;
    }

    /**
     * Sets the parent assets.
     * 
     * @param parentAssetInfos
     *            the parent asset infos
     * @return the asset info
     */
    public AssetInfo setParentAssets(Iterable<AssetInfo> parentAssetInfos) {
        this.parentAssetInfos = parentAssetInfos;
        return this;
    }

}
